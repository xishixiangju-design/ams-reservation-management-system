package com.trae.ams.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.appointment.BatchBookingRequest;
import com.trae.ams.dto.appointment.BookingItemDTO;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.dto.appointment.SlotQueryRequest;
import com.trae.ams.dto.appointment.TimeSlotDTO;
import com.trae.ams.entity.*;
import com.trae.ams.mapper.*;
import com.trae.ams.service.AppointmentService;
import com.trae.ams.service.MemberService;
import com.trae.ams.service.NotificationService;
import com.trae.ams.service.PaymentService;
import com.trae.ams.service.PricingService;
import com.trae.ams.service.TechnicianShiftService;
import com.trae.ams.service.WaitingListService;
import com.trae.ams.entity.AmsTechnicianShift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AmsAppointmentMapper appointmentMapper;
    @Autowired
    private AmsAppointmentItemMapper appointmentItemMapper;
    @Autowired
    private AmsServiceMapper serviceMapper;
    @Autowired
    private AmsTechnicianInfoMapper technicianMapper;
    @Autowired
    private AmsRoomMapper roomMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private AmsViolationRecordMapper violationMapper;
    @Autowired
    private AmsComboRuleMapper comboRuleMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    @Lazy
    private WaitingListService waitingListService;
    
    @Autowired
    private TechnicianShiftService shiftService;
    
    @Autowired
    private AmsTransactionMapper transactionMapper;

    // 店铺营业时间 (硬编码，后续可配置)
    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);
    private static final int SLOT_STEP = 30;
    // 锁对象，用于保证预约创建的原子性
    private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();

    @Override
    public List<TimeSlotDTO> getAvailableSlots(SlotQueryRequest request) {
        log.info("getAvailableSlots called with request: {}", JSONUtil.toJsonStr(request));
        // 1. 获取服务时长 (支持多选)
        int durationMinutes = 0;
        List<Long> ids = new ArrayList<>();
        if (CollUtil.isNotEmpty(request.getServiceIds())) {
            ids.addAll(request.getServiceIds());
        } else if (request.getServiceId() != null) {
            ids.add(request.getServiceId());
        }
        
        if (CollUtil.isEmpty(ids)) {
            throw new BusinessException("请选择服务");
        }

        List<AmsService> services = serviceMapper.selectByIds(ids);
        if (CollUtil.isEmpty(services)) {
             throw new BusinessException("服务不存在");
        }
        durationMinutes = services.stream().mapToInt(AmsService::getDuration).sum();

        // 2. 确定查询日期范围
        LocalDate date = request.getDate();
        LocalDateTime dayStart = LocalDateTime.of(date, OPEN_TIME);
        LocalDateTime dayEnd = LocalDateTime.of(date, CLOSE_TIME);

        // 3. 查询当日所有已有预约 (冲突源)
        List<AmsAppointment> conflicts = appointmentMapper.selectConflictAppointments(
                UserContext.getStoreIdOrDefault(),
                dayStart,
                dayEnd
        );

        // Optimization: Batch fetch items to avoid N+1 in loop
        Map<Long, List<AmsAppointmentItem>> conflictItemsMap = new HashMap<>();
        if (CollUtil.isNotEmpty(conflicts)) {
            List<Long> conflictIds = conflicts.stream().map(AmsAppointment::getId).collect(Collectors.toList());
            List<AmsAppointmentItem> allItems = appointmentItemMapper.selectByApptIds(conflictIds);
            conflictItemsMap = allItems.stream().collect(Collectors.groupingBy(AmsAppointmentItem::getApptId));
        }

        // 4. 获取所有可用技师和房间 (排除请假)
        List<AmsTechnicianInfo> allTechs = technicianMapper.selectList(UserContext.getStoreIdOrDefault(), null, null).stream()
                .filter(t -> !"LEAVE".equals(t.getStatus()))
                .collect(Collectors.toList());
        
        List<AmsRoom> activeRooms = roomMapper.selectList(UserContext.getStoreIdOrDefault(), 1);

        // 4.1 获取当日排班信息 (批量)
        List<AmsTechnicianShift> dailyShifts = shiftService.getAllShiftsByDate(date);

        // 5. 生成时间槽
        List<TimeSlotDTO> slots = new ArrayList<>();
        LocalTime cursor = OPEN_TIME;
        int peopleCount = request.getPeopleCount() != null ? request.getPeopleCount() : 1;
        
        while (cursor.plusMinutes(durationMinutes).isBefore(CLOSE_TIME) || cursor.plusMinutes(durationMinutes).equals(CLOSE_TIME)) {
            LocalDateTime slotStart = LocalDateTime.of(date, cursor);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            
            // 6. 检查该时间段是否有资源 (技师 + 房间)
            boolean available = checkResourceAvailability(slotStart, slotEnd, request.getTechId(), peopleCount, conflicts, conflictItemsMap, allTechs, activeRooms, dailyShifts);
            
            slots.add(new TimeSlotDTO(cursor, cursor.plusMinutes(durationMinutes), available));
            cursor = cursor.plusMinutes(SLOT_STEP);
        }

        return slots;
    }

    /**
     * 核心资源检查逻辑
     */
    private boolean checkResourceAvailability(
            LocalDateTime start, LocalDateTime end, Long requestedTechId, int peopleCount,
            List<AmsAppointment> conflicts, 
            Map<Long, List<AmsAppointmentItem>> conflictItemsMap,
            List<AmsTechnicianInfo> allTechs, 
            List<AmsRoom> allRooms,
            List<AmsTechnicianShift> dailyShifts) {
        
        // 1. 找出该时段已被占用的技师ID (技师不可分身，只要重叠即占用)
        List<Long> occupiedTechIds = new ArrayList<>();
        
        for (AmsAppointment appt : conflicts) {
            // Conflict: (StartA < EndB) and (EndA > StartB)
            if (appt.getStartTime().isBefore(end) && appt.getEndTime().isAfter(start)) {
                List<AmsAppointmentItem> items = conflictItemsMap.getOrDefault(appt.getId(), Collections.emptyList());
                for (AmsAppointmentItem item : items) {
                    if (item.getTechId() != null) occupiedTechIds.add(item.getTechId());
                }
            }
        }

        // 2. 检查技师可用性
        // 逻辑更新: 必须未被占用 AND 排班允许
        boolean techAvailable = false;
        
        if (requestedTechId != null) {
            // ... (rest same) ...
            boolean isTargetOccupied = occupiedTechIds.contains(requestedTechId);
            boolean isTargetShiftOk = allTechs.stream().anyMatch(t -> t.getUserId().equals(requestedTechId) 
                                && isTechAvailableInMemory(t, start, end, dailyShifts));
            
            if (!isTargetOccupied && isTargetShiftOk) {
                if (peopleCount > 1) {
                    long otherAvailableCount = allTechs.stream()
                        .filter(t -> !t.getUserId().equals(requestedTechId)) // 排除主技师
                        .filter(t -> !occupiedTechIds.contains(t.getUserId())) // 未被占用
                        .filter(t -> isTechAvailableInMemory(t, start, end, dailyShifts)) // 排班OK
                        .count();
                    techAvailable = otherAvailableCount >= (peopleCount - 1);
                } else {
                    techAvailable = true;
                }
            }
        } else {
            // 任意技师：检查可用技师总数 >= peopleCount
            long availableCount = allTechs.stream()
                .filter(t -> !occupiedTechIds.contains(t.getUserId()))
                .filter(t -> isTechAvailableInMemory(t, start, end, dailyShifts))
                .count();
            
            techAvailable = availableCount >= peopleCount;
        }

        // 3. 检查房间可用性
        // 逻辑修正：不能简单累加所有冲突预约的占用，必须计算时间段内的【最大并发占用】
        int totalRemainingCapacity = 0;
        
        List<AmsRoom> roomsToCheck = allRooms;
        if (CollUtil.isEmpty(roomsToCheck)) {
             // ...
        }

        for (AmsRoom room : roomsToCheck) {
            int capacity = room.getCapacity() != null ? room.getCapacity() : 1;
            
            // 计算该房间在 [start, end] 期间的最大并发占用数
            int maxUsage = getMaxRoomUsage(room.getId(), start, end, conflicts, conflictItemsMap);
            
            if (capacity > maxUsage) {
                totalRemainingCapacity += (capacity - maxUsage);
            }
        }
        
        boolean roomAvailable = totalRemainingCapacity >= peopleCount;

        return techAvailable && roomAvailable;
    }

    /**
     * 计算指定房间在查询时间段内的最大并发占用数
     */
    private int getMaxRoomUsage(Long roomId, LocalDateTime start, LocalDateTime end, List<AmsAppointment> conflicts, Map<Long, List<AmsAppointmentItem>> conflictItemsMap) {
        // 1. 筛选出使用该房间的冲突预约
        List<AmsAppointment> roomAppts = new ArrayList<>();
        // 缓存每个预约在该房间占用的坑位数 (ApptId -> Count)
        Map<Long, Integer> apptOccupancy = new HashMap<>();
        
        for (AmsAppointment appt : conflicts) {
            // Conflict 已经是时间重叠的了，只需检查 RoomId
            List<AmsAppointmentItem> items = conflictItemsMap.getOrDefault(appt.getId(), Collections.emptyList());
            int count = 0;
            for (AmsAppointmentItem item : items) {
                if (roomId.equals(item.getRoomId())) {
                    count++;
                }
            }
            if (count > 0) {
                roomAppts.add(appt);
                apptOccupancy.put(appt.getId(), count);
            }
        }
        
        if (roomAppts.isEmpty()) return 0;
        
        // 2. 扫描线算法或关键点检查
        // 关键点：查询开始时间，以及每个预约的开始时间（如果落在查询范围内）
        Set<LocalDateTime> checkPoints = new HashSet<>();
        checkPoints.add(start);
        for (AmsAppointment appt : roomAppts) {
            if (appt.getStartTime().isAfter(start) && appt.getStartTime().isBefore(end)) {
                checkPoints.add(appt.getStartTime());
            }
        }
        
        int maxUsage = 0;
        for (LocalDateTime pt : checkPoints) {
            // 计算在时刻 pt 的并发数
            // 定义：Active if Start <= pt < End
            int currentUsage = 0;
            for (AmsAppointment appt : roomAppts) {
                // 注意：LocalTime/DateTime 比较
                // 预约 [10:00, 11:00)，查询点 10:00 -> Active
                // 预约 [10:00, 11:00)，查询点 10:59 -> Active
                // 预约 [10:00, 11:00)，查询点 11:00 -> Inactive (已结束)
                if (!appt.getStartTime().isAfter(pt) && appt.getEndTime().isAfter(pt)) {
                    currentUsage += apptOccupancy.getOrDefault(appt.getId(), 0);
                }
            }
            maxUsage = Math.max(maxUsage, currentUsage);
        }
        
        return maxUsage;
    }

    /**
     * 内存中检查技师排班可用性
     */
    private boolean isTechAvailableInMemory(AmsTechnicianInfo tech, LocalDateTime start, LocalDateTime end, List<AmsTechnicianShift> dailyShifts) {
        // Filter shifts for this tech
        List<AmsTechnicianShift> myShifts = dailyShifts.stream()
                .filter(s -> s.getTechId().equals(tech.getUserId()))
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(myShifts)) {
            // Fallback: Global status check (Legacy)
            return !"LEAVE".equals(tech.getStatus());
        }
        
        LocalTime timeStart = start.toLocalTime();
        LocalTime timeEnd = end.toLocalTime();
        
        boolean covered = false;
        boolean blocked = false;
        
        for (AmsTechnicianShift shift : myShifts) {
            if (!"WORK".equals(shift.getType())) {
                // LEAVE or BREAK: Check overlap
                if (shift.getStartTime().isBefore(timeEnd) && shift.getEndTime().isAfter(timeStart)) {
                    blocked = true;
                    break;
                }
            } else {
                // WORK: Check coverage
                if (shift.getStartTime().compareTo(timeStart) <= 0 && shift.getEndTime().compareTo(timeEnd) >= 0) {
                    covered = true;
                }
            }
        }
        
        return covered && !blocked;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // TODO: Use Distributed Lock (e.g. Redisson) instead of synchronized for better performance
    // Lock Key: "appointment:create:" + request.getStoreId() + ":" + request.getTechId()
    public Long createAppointment(BookingRequest request) {
        lock.lock(); // 加锁防止并发超卖
        try {
            // 0. 违约检查 (D-03)
            if (request.getCustomerId() != null) {
                SysUser user = sysUserMapper.selectById(request.getCustomerId());
                // 假设3次为阈值
                if (user != null && user.getViolationCount() != null && user.getViolationCount() >= 3) {
                    throw new BusinessException("因您的违约次数过多（>=3次），系统暂时限制您的预约权限，请联系门店解除。");
                }
            }

            // 1. 基础校验
            if (CollUtil.isEmpty(request.getItems())) {
                throw new BusinessException("请选择服务项目");
            }
            
            // D-02: 支持多人预约拆分逻辑
            int peopleCount = request.getPeopleCount() != null ? request.getPeopleCount() : 1;
            
            // 计算最大时长 (所有项目串行)
            int maxDuration = request.getItems().stream()
                    .mapToInt(i -> {
                        AmsService s = serviceMapper.selectById(i.getServiceId());
                        return s != null ? s.getDuration() : 0;
                    }).sum();
            
            LocalDateTime startTime = request.getStartTime();
            LocalDateTime endTime = startTime.plusMinutes(maxDuration);
            Long storeId = request.getStoreId() != null ? request.getStoreId() : UserContext.getStoreIdOrDefault();

            // 2. 统一获取资源占用情况
            List<AmsAppointment> conflicts = appointmentMapper.selectConflictAppointments(storeId, startTime, endTime);
            
            // Optimization: Batch fetch items for conflict checking
            Map<Long, List<AmsAppointmentItem>> conflictItemsMap = new HashMap<>();
            if (CollUtil.isNotEmpty(conflicts)) {
                List<Long> conflictIds = conflicts.stream().map(AmsAppointment::getId).collect(Collectors.toList());
                List<AmsAppointmentItem> allItems = appointmentItemMapper.selectByApptIds(conflictIds);
                conflictItemsMap = allItems.stream().collect(Collectors.groupingBy(AmsAppointmentItem::getApptId));
            }

            Set<Long> occupiedTechIds = new HashSet<>();
            Map<Long, Integer> roomCurrentUsage = new HashMap<>(); // RoomID -> Current People Count
            
            // 初始化房间信息
            List<AmsRoom> allRooms = roomMapper.selectList(storeId, 1);
            Map<Long, AmsRoom> roomMap = allRooms.stream().collect(Collectors.toMap(AmsRoom::getId, r -> r));

            // 填充已有的占用
            for (AmsAppointment appt : conflicts) {
                List<AmsAppointmentItem> items = conflictItemsMap.getOrDefault(appt.getId(), Collections.emptyList());
                for (AmsAppointmentItem item : items) {
                    if (item.getTechId() != null) occupiedTechIds.add(item.getTechId());
                    if (item.getRoomId() != null) {
                        roomCurrentUsage.put(item.getRoomId(), roomCurrentUsage.getOrDefault(item.getRoomId(), 0) + 1);
                    }
                }
            }
            
            // 获取所有可用技师 (状态OK & 排班OK)
            List<AmsTechnicianInfo> allTechs = technicianMapper.selectList(storeId, null, null).stream()
                    .filter(t -> !"LEAVE".equals(t.getStatus()))
                    .filter(t -> shiftService.isTechWorking(t.getUserId(), startTime, endTime))
                    .collect(Collectors.toList());
            
            List<AmsAppointmentItem> itemsToSave = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // 3. 核心分配逻辑 (按人头分配资源)
            for (int p = 1; p <= peopleCount; p++) {
                final int personIndex = p;
                // 假设每个客人都享受 request.getItems() 里的所有服务
                for (BookingItemDTO itemDto : request.getItems()) {
                    AmsService service = serviceMapper.selectById(itemDto.getServiceId());
                    if (service == null) throw new BusinessException("服务不存在");
                    
                    AmsAppointmentItem item = new AmsAppointmentItem();
                    item.setServiceId(service.getId());
                    
                    // 价格计算 (单人单项)
                    BigDecimal memberPrice = memberService.calculatePrice(request.getCustomerId(), service.getPrice());
                    // 传入1人，因为我们在外层循环处理人数
                    BigDecimal itemTotal = pricingService.calculateServicePrice(service.getId(), 1, memberPrice);
                    
                    item.setPrice(itemTotal);
                    totalAmount = totalAmount.add(itemTotal);
                    
                    // --- 技师分配 ---
                    Long techId = itemDto.getTechId();
                    boolean isAutoAssigned = false;
                    
                    if (techId == null) {
                        // 自动分配: 轮牌策略 (LRU)
                        techId = allTechs.stream()
                                .filter(t -> !occupiedTechIds.contains(t.getUserId()))
                                .sorted(Comparator.comparing(AmsTechnicianInfo::getLastJobEndTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                                .map(AmsTechnicianInfo::getUserId)
                                .findFirst()
                                .orElseThrow(() -> new BusinessException("资源不足：第" + personIndex + "位客人的服务无可用技师"));
                        isAutoAssigned = true;
                    } else {
                        // 指定技师 (如果是多人，这会导致所有人抢一个技师，除非前端传了不同 techId。这里假设前端只传了一个，则只分配给第一个人，其他人必须自动分配或报错)
                        // 优化：如果指定技师已被占 (可能是被同订单的前一个人占了)，则尝试自动分配? 还是报错?
                        // 既然是"指定"，报错更合理。
                        if (occupiedTechIds.contains(techId)) {
                             throw new BusinessException("指定技师 " + techId + " 在该时段已忙碌 (或被同行人占用)");
                        }
                        Long targetTechId = techId;
                        boolean exists = allTechs.stream().anyMatch(t -> t.getUserId().equals(targetTechId));
                        if (!exists) throw new BusinessException("指定技师不可用");
                    }
                    
                    occupiedTechIds.add(techId);
                    item.setTechId(techId);
                    
                    // D-05: 仅自动分配时更新排序
                    if (isAutoAssigned) {
                        AmsTechnicianInfo techUpdate = new AmsTechnicianInfo();
                        techUpdate.setUserId(techId);
                        techUpdate.setLastJobEndTime(endTime);
                        technicianMapper.update(techUpdate);
                    }
                    
                    // --- 房间分配 (D-02: 自动拆分) ---
                    Long roomId = itemDto.getRoomId();
                    if (roomId == null) {
                        // 自动分配
                        roomId = allRooms.stream()
                                .filter(r -> {
                                    int current = roomCurrentUsage.getOrDefault(r.getId(), 0);
                                    int capacity = r.getCapacity() != null ? r.getCapacity() : 1;
                                    return current < capacity;
                                })
                                .sorted(Comparator.comparing(r -> r.getCapacity() != null ? r.getCapacity() : 1)) // 优先填满小房间? 或大房间? 
                                // 策略: 优先使用容量小的合适房间，保留大房间给大组?
                                // 还是优先填满已有人的房间?
                                // 简单起见: 只要有空位就行。
                                .map(AmsRoom::getId)
                                .findFirst()
                                .orElseThrow(() -> new BusinessException("资源不足：无可用房间容纳第" + personIndex + "位客人"));
                    } else {
                        // 指定房间
                        int current = roomCurrentUsage.getOrDefault(roomId, 0);
                        AmsRoom r = roomMap.get(roomId);
                        int capacity = (r != null && r.getCapacity() != null) ? r.getCapacity() : 1;
                        if (r == null || current >= capacity) {
                            throw new BusinessException("指定房间已满");
                        }
                    }
                    
                    roomCurrentUsage.put(roomId, roomCurrentUsage.getOrDefault(roomId, 0) + 1);
                    item.setRoomId(roomId);
                    
                    itemsToSave.add(item);
                }
            }

            // Apply Combo Discounts (Refined)
            List<Long> selectedServiceIds = request.getItems().stream()
                    .map(BookingItemDTO::getServiceId)
                    .collect(Collectors.toList());
            
            List<AmsComboRule> rules = comboRuleMapper.selectActiveRules(storeId);
            for (AmsComboRule rule : rules) {
                 try {
                     if (rule.getConditionJson() != null) {
                         JSONObject condition = JSONUtil.parseObj(rule.getConditionJson());
                         JSONArray requiredIds = condition.getJSONArray("requiredServiceIds");
                         if (requiredIds != null) {
                             Set<Long> reqSet = new HashSet<>();
                             for(Object id : requiredIds) reqSet.add(Long.valueOf(id.toString()));
                             
                             if (new HashSet<>(selectedServiceIds).containsAll(reqSet)) {
                                 if (rule.getDiscountType() == 1) { // Rate (e.g. 0.9)
                                     totalAmount = totalAmount.multiply(rule.getDiscountValue());
                                 } else if (rule.getDiscountType() == 2) { // Fixed Amount
                                     totalAmount = totalAmount.subtract(rule.getDiscountValue());
                                 }
                             }
                         }
                     }
                 } catch (Exception e) {
                     log.error("Failed to apply combo rule: " + rule.getId(), e);
                 }
            }
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

            // 4. 保存主订单
            AmsAppointment appt = new AmsAppointment();
            appt.setStoreId(storeId);
            appt.setCustomerId(request.getCustomerId());
            // Set creator: prefer explicit creatorId from request, fallback to current user
            Long creatorId = request.getCreatorId() != null ? request.getCreatorId() : UserContext.getUserId();
            appt.setCreatorId(creatorId);
            
            // 设置支付方式和来源
            appt.setPaymentMethod(request.getPaymentMethod());
            appt.setSource(request.getSource() != null ? request.getSource() : "CLIENT");
            
            appt.setStartTime(startTime);
            appt.setEndTime(endTime);
            appt.setPeopleCount(peopleCount);
            // Default to 1 (Pending Payment) if not specified
            appt.setStatus(request.getStatus() != null ? request.getStatus() : AmsAppointment.STATUS_PENDING_PAYMENT);
            appt.setTotalAmount(totalAmount);
            appt.setRemark(request.getRemark());
            appt.setContactName(request.getContactName());
            appt.setContactPhone(request.getContactPhone());
            appt.setCreateTime(LocalDateTime.now());
            
            appointmentMapper.insert(appt);

            // 5. 保存明细
            for (AmsAppointmentItem item : itemsToSave) {
                item.setApptId(appt.getId());
            }
            appointmentItemMapper.batchInsert(itemsToSave);

            // 发送预约成功通知
            try {
                String title = "预约成功";
                String content = String.format("您的预约已成功创建，时间：%s，请准时到店。", 
                        DateUtil.format(startTime, "yyyy-MM-dd HH:mm"));
                notificationService.sendNotification(request.getCustomerId(), title, content);
            } catch (Exception e) {
                log.error("Failed to send appointment success notification", e);
            }
            
            return appt.getId();
        } finally {
            lock.unlock();
        }
    }



    /**
     * 权限校验：
     * 1. 超管：无限制
     * 2. 店长/技师：只能操作本店铺数据
     * 3. 客户：只能操作本人数据
     */
    private void checkAccess(AmsAppointment appt) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) return; // Should be handled by Interceptor
        
        if (UserContext.isSuperAdmin()) {
            return; // Super Admin has full access
        }
        
        // Manager or Technician
        if (UserContext.isAdmin()) {
            Long userStoreId = UserContext.getStoreId();
            if (userStoreId != null && !userStoreId.equals(appt.getStoreId())) {
                throw new BusinessException("无权操作其他店铺的数据");
            }
            return;
        }
        
        // Customer
        if (!appt.getCustomerId().equals(currentUserId)) {
            throw new BusinessException("无权操作此预约");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long appointmentId) {
        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) {
            throw new BusinessException("预约不存在");
        }
        
        checkAccess(appt);
        Long currentUserId = UserContext.getUserId();
        
        // 1. 确定取消/违约状态 (保持原有逻辑: 12h)
        LocalDateTime now = LocalDateTime.now();
        long hours = Duration.between(now, appt.getStartTime()).toHours();
        
        if (hours < 12) {
            // 记录违约
            appt.setStatus(AmsAppointment.STATUS_VIOLATION); // 4
            
            AmsViolationRecord record = new AmsViolationRecord();
            record.setStoreId(appt.getStoreId());
            record.setUserId(appt.getCustomerId());
            record.setApptId(appt.getId());
            record.setType("LATE_CANCEL");
            record.setViolationTime(now);
            record.setCreateTime(now);
            violationMapper.insert(record);
            
            // 同步更新会员违约次数
            SysUser user = sysUserMapper.selectById(appt.getCustomerId());
            if (user != null) {
                int count = user.getViolationCount() == null ? 0 : user.getViolationCount();
                user.setViolationCount(count + 1);
                sysUserMapper.update(user);
            }
        } else {
            appt.setStatus(AmsAppointment.STATUS_CANCELLED); // 3
        }
        
        // 2. 处理退款 (新增增强逻辑)
        if ("PAID".equals(appt.getPaymentStatus())) {
            BigDecimal refundAmount = BigDecimal.ZERO;
            BigDecimal totalAmount = appt.getTotalAmount();
            
            // 修正退款规则 (严格执行 12h 违约判定):
            // >= 12h: 全额退款 (正常取消)
            // < 12h 且 >= 2h: 违约取消，全额退款，记录违约一次
            // < 2h: 严重违约，退款 50%，记录违约一次
            if (hours >= 2) {
                refundAmount = totalAmount;
            } else {
                refundAmount = totalAmount.multiply(new BigDecimal("0.5"));
            }
            
            // 精度处理: 保留2位小数，四舍五入
            refundAmount = refundAmount.setScale(2, RoundingMode.HALF_UP);
            
            // 校验: 不得大于原金额 (防御性编程)
            if (refundAmount.compareTo(totalAmount) > 0) {
                refundAmount = totalAmount;
            }
            
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    // 更新退款状态: 处理中
                    appt.setRefundStatus(AmsAppointment.REFUND_STATUS_PROCESSING);
                    appointmentMapper.updateRefundStatus(appt.getId(), AmsAppointment.REFUND_STATUS_PROCESSING);
                    
                    String reason;
                    if (hours < 2) {
                        reason = "违约取消(扣除50%)";
                    } else if (hours < 12) {
                        reason = "违约取消(全额退款，记录违约)";
                    } else {
                        reason = "正常取消(全额退款)";
                    }
                    String operator = (currentUserId != null) ? currentUserId.toString() : "SYSTEM";
                    paymentService.refund(appt.getId(), refundAmount, reason, operator);
                    
                    // 更新退款状态: 完成
                    appt.setRefundStatus(AmsAppointment.REFUND_STATUS_COMPLETED);
                    
                    // 发送通知
                    String content;
                    if (hours < 2) {
                        content = String.format("距离服务时间不足2小时，本次取消将记录一次违约；系统扣除50%%预约款，实际退款 %s 元，预计1-3个工作日到账。", refundAmount);
                    } else if (hours < 12) {
                        content = String.format("距离服务时间不足12小时，本次取消将记录一次违约，已为您全额退款 %s 元，预计1-3个工作日到账。", refundAmount);
                    } else {
                        content = String.format("您的预约已取消，已为您全额退款 %s 元，预计1-3个工作日到账。", refundAmount);
                    }
                    notificationService.sendNotification(appt.getCustomerId(), "退款通知", content);
                    
                } catch (Exception e) {
                    log.error("Refund processing failed for appt " + appt.getId(), e);
                    appt.setRefundStatus(AmsAppointment.REFUND_STATUS_FAILED);
                    // 不阻断取消流程，但记录错误状态，后续转人工
                    // 实际生产中应触发告警或工单
                }
            } else {
                // 金额为0，不退款
                appt.setRefundStatus(AmsAppointment.REFUND_STATUS_NONE);
            }
        }
        
        // 更新状态 (包含 status 和 potential refundStatus changes in memory)
        appointmentMapper.updateStatus(appt.getId(), appt.getStatus());
        if (appt.getRefundStatus() != null) {
            appointmentMapper.updateRefundStatus(appt.getId(), appt.getRefundStatus());
        }

        // 3. 触发候补检查 (异步或同步，这里用同步但捕获异常以免影响取消)
        if (appt.getStatus() == 3 || appt.getStatus() == 4) {
            try {
                waitingListService.checkWaitlistOnCancel(appt.getStoreId(), appt.getStartTime(), appt.getEndTime());
            } catch (Exception e) {
                log.error("Failed to check waiting list after cancellation", e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reschedule(Long appointmentId, LocalDateTime newStartTime, Long newTechId) {
        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException("预约不存在");
        checkAccess(appt);
        // Only Pending Consumption (5) allows rescheduling
        if (appt.getStatus() != AmsAppointment.STATUS_PENDING_CONSUMPTION) throw new BusinessException("当前状态无法改期");

        // 1. 计算新结束时间
        long durationMinutes = Duration.between(appt.getStartTime(), appt.getEndTime()).toMinutes();
        LocalDateTime newEndTime = newStartTime.plusMinutes(durationMinutes);

        // 2. 检查冲突 (排除自身)
        Long storeId = appt.getStoreId();
        List<AmsAppointment> conflicts = appointmentMapper.selectConflictAppointments(storeId, newStartTime, newEndTime);
        conflicts.removeIf(a -> a.getId().equals(appointmentId));

        // 3. 获取现有明细
        List<AmsAppointmentItem> items = appointmentItemMapper.selectByApptId(appointmentId);
        
        // 4. 检查资源并更新
        // 这里简化处理：如果是多项目，假设都尝试更新到 newTechId (如果提供)，否则保持原样
        // 同时检查房间是否冲突
        List<Long> occupiedTechIds = new ArrayList<>();
        List<Long> occupiedRoomIds = new ArrayList<>();
        
        // 收集冲突资源
        for (AmsAppointment conflict : conflicts) {
            List<AmsAppointmentItem> cItems = appointmentItemMapper.selectByApptId(conflict.getId());
            for (AmsAppointmentItem ci : cItems) {
                if (ci.getTechId() != null) occupiedTechIds.add(ci.getTechId());
                if (ci.getRoomId() != null) occupiedRoomIds.add(ci.getRoomId());
            }
        }
        
        // 验证当前 items 在新时间是否可用
        for (AmsAppointmentItem item : items) {
            Long targetTechId = (newTechId != null) ? newTechId : item.getTechId();
            
            // 检查技师
            if (targetTechId != null) {
                 if (occupiedTechIds.contains(targetTechId)) {
                     throw new BusinessException("技师在新时段已忙碌");
                 }
                 // 还需要检查技师是否请假 (这里省略，假设 reschedule 时技师是 active 的，或者依赖前端选择)
                 occupiedTechIds.add(targetTechId);
                 item.setTechId(targetTechId);
            }
            
            // 检查房间
            if (item.getRoomId() != null) {
                if (occupiedRoomIds.contains(item.getRoomId())) {
                    // 房间冲突 -> 尝试重新分配一个空闲房间?
                    // 简单起见，如果冲突则报错，或者自动换房。
                    // 这里实现自动换房逻辑
                    List<AmsRoom> allRooms = roomMapper.selectList(storeId, 1);
                    Long newRoomId = allRooms.stream()
                        .filter(r -> !occupiedRoomIds.contains(r.getId())) // 且满足容量? 假设都满足
                        .map(AmsRoom::getId)
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("新时段无可用房间"));
                    item.setRoomId(newRoomId);
                    occupiedRoomIds.add(newRoomId);
                } else {
                    occupiedRoomIds.add(item.getRoomId());
                }
            }
        }
        
        // 5. 执行更新
        // 更新主表
        appt.setStartTime(newStartTime);
        appt.setEndTime(newEndTime);
        appointmentMapper.update(appt);
        
        // 更新 Items
        // 删除旧的 items
        appointmentItemMapper.deleteByApptId(appt.getId());
        
        // 重新插入 items (with updated tech/room/price)
        // 注意：price 应该保持不变，除非 reschedule 导致价格变化 (此处假设不变)
        // items 中的 apptId 已经有了
        appointmentItemMapper.batchInsert(items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAppointment(Long appointmentId) {
        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) {
            throw new BusinessException("预约不存在");
        }
        checkAccess(appt);
        // 校验状态：只有“待消费”状态允许完成订单
        if (appt.getStatus() != AmsAppointment.STATUS_PENDING_CONSUMPTION) { 
            throw new BusinessException("操作失败：订单当前状态不允许完成，必须为“待消费”状态");
        }

        // 1. 更新预约状态
        appt.setStatus(AmsAppointment.STATUS_COMPLETED); // 2: COMPLETED
        appointmentMapper.updateStatus(appt.getId(), appt.getStatus());

        // 2. 累积会员消费
        memberService.accumulateConsumption(appt.getCustomerId(), appt.getTotalAmount());

        // 3. 更新技师上次服务结束时间
        List<AmsAppointmentItem> items = appointmentItemMapper.selectByApptId(appt.getId());
        for (AmsAppointmentItem item : items) {
            if (item.getTechId() != null) {
                AmsTechnicianInfo tech = technicianMapper.selectByUserId(item.getTechId());
                if (tech != null) {
                    tech.setLastJobEndTime(appt.getEndTime());
                    technicianMapper.update(tech);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPaid(Long appointmentId, String operatorId) {
        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException("预约不存在");
        checkAccess(appt);
        
        // Check current status
        if (!"UNPAID".equals(appt.getPaymentStatus()) && appt.getStatus() != AmsAppointment.STATUS_PENDING_PAYMENT) {
             // 如果已经是PAID，则不处理，或抛异常
             if ("PAID".equals(appt.getPaymentStatus())) {
                 throw new BusinessException("订单已支付");
             }
        }
        
        // 1. Create Transaction Record
        AmsTransaction trans = new AmsTransaction();
        trans.setStoreId(appt.getStoreId());
        trans.setAppointmentId(appt.getId());
        trans.setType("PAYMENT");
        trans.setAmount(appt.getTotalAmount());
        trans.setPaymentMethod("OFFLINE");
        trans.setCreateTime(LocalDateTime.now());
        trans.setOperator(operatorId);
        trans.setStatus("SUCCESS");
        trans.setOutTradeNo("OFFLINE-" + System.currentTimeMillis());
        transactionMapper.insert(trans);
        
        // 2. Update Appointment Status
        appt.setPaymentStatus("PAID");
        // 只有在待支付状态下才流转到待消费，避免覆盖其他状态
        if (appt.getStatus() == AmsAppointment.STATUS_PENDING_PAYMENT) {
            appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        }
        
        appointmentMapper.update(appt); 
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokePayment(Long appointmentId, String reason, String operatorId) {
        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException("预约不存在");
        checkAccess(appt);
        
        if (!"PAID".equals(appt.getPaymentStatus())) {
             throw new BusinessException("订单未支付，无法撤销");
        }
        
        // Handle Completed Order Rollback
        if (appt.getStatus() == AmsAppointment.STATUS_COMPLETED) {
             // Rollback member points/consumption (negative amount)
             if (appt.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                 memberService.accumulateConsumption(appt.getCustomerId(), appt.getTotalAmount().negate());
             }
             
             // Revert status to Pending Payment
             appt.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
        } else if (appt.getStatus() == AmsAppointment.STATUS_PENDING_CONSUMPTION) {
             appt.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
        }
        
        // 1. Create Refund Transaction (Virtual)
        AmsTransaction trans = new AmsTransaction();
        trans.setStoreId(appt.getStoreId());
        trans.setAppointmentId(appt.getId());
        trans.setType("REFUND");
        trans.setAmount(appt.getTotalAmount());
        trans.setPaymentMethod("OFFLINE");
        trans.setCreateTime(LocalDateTime.now());
        trans.setReason(reason); 
        trans.setOperator(operatorId);
        trans.setStatus("REFUNDED");
        trans.setOutTradeNo("REVOKE-" + System.currentTimeMillis());
        transactionMapper.insert(trans);

        // 发送退款通知
        try {
            String title = "退款通知";
            String content = String.format("您的预约(ID:%s)已操作退款，金额：%s，原因：%s。", 
                    appointmentId, NumberUtil.toStr(trans.getAmount()), reason);
            notificationService.sendNotification(appt.getCustomerId(), title, content);
        } catch (Exception e) {
            log.error("Failed to send refund notification for appt " + appointmentId, e);
        }
        
        // 2. Update Status
        appt.setPaymentStatus("UNPAID");
        appointmentMapper.update(appt);
    }

    @Override
    public AmsAppointment getAppointment(Long id) {
        AmsAppointment appt = appointmentMapper.selectById(id);
        if (appt != null) {
            checkAccess(appt);

            List<AmsAppointmentItem> items = appointmentItemMapper.selectByApptId(id);
            // Enrich items with Service/Tech/Room info
            for (AmsAppointmentItem item : items) {
                 if (item.getServiceId() != null) {
                     item.setService(serviceMapper.selectById(item.getServiceId()));
                 }
                 if (item.getTechId() != null) {
                     item.setTechnician(technicianMapper.selectByUserId(item.getTechId()));
                 }
                 if (item.getRoomId() != null) {
                     item.setRoom(roomMapper.selectById(item.getRoomId()));
                 }
            }
            appt.setItems(items);
        }
        return appt;
    }

    @Override
    public PageInfo<AmsAppointment> listMyAppointments(Long userId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<AmsAppointment> list = appointmentMapper.selectByCustomer(userId, startDate, endDate, status);
        
        if (CollUtil.isEmpty(list)) {
            return new PageInfo<>(list);
        }

        enrichAppointments(list);
        
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AmsAppointment> listTechnicianAppointments(Long techId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<AmsAppointment> list = appointmentMapper.selectByTechnician(techId, startDate, endDate, status);

        if (CollUtil.isEmpty(list)) {
            return new PageInfo<>(list);
        }

        enrichAppointments(list);

        return new PageInfo<>(list);
    }

    private void enrichAppointments(List<AmsAppointment> list) {
        // 1. 批量查询 Items
        List<Long> apptIds = list.stream().map(AmsAppointment::getId).collect(Collectors.toList());
        List<AmsAppointmentItem> allItems = appointmentItemMapper.selectByApptIds(apptIds);
        
        // 2. 收集关联ID
        List<Long> serviceIds = allItems.stream().map(AmsAppointmentItem::getServiceId).distinct().collect(Collectors.toList());
        List<Long> techIds = allItems.stream().map(AmsAppointmentItem::getTechId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> roomIds = allItems.stream().map(AmsAppointmentItem::getRoomId).filter(id -> id != null).distinct().collect(Collectors.toList());
        
        // 3. 批量查询关联信息
        List<AmsService> services = CollUtil.isNotEmpty(serviceIds) ? serviceMapper.selectByIds(serviceIds) : Collections.emptyList();
        List<AmsTechnicianInfo> techs = CollUtil.isNotEmpty(techIds) ? technicianMapper.selectByUserIds(techIds) : Collections.emptyList();
        List<AmsRoom> rooms = CollUtil.isNotEmpty(roomIds) ? roomMapper.selectByIds(roomIds) : Collections.emptyList();
        
        // 4. 转为 Map
        Map<Long, AmsService> serviceMap = services.stream().collect(Collectors.toMap(AmsService::getId, s -> s));
        Map<Long, AmsTechnicianInfo> techMap = techs.stream().collect(Collectors.toMap(AmsTechnicianInfo::getUserId, t -> t));
        Map<Long, AmsRoom> roomMap = rooms.stream().collect(Collectors.toMap(AmsRoom::getId, r -> r));
        
        // 5. 组装数据
        Map<Long, List<AmsAppointmentItem>> itemMap = allItems.stream().collect(Collectors.groupingBy(AmsAppointmentItem::getApptId));
        
        for (AmsAppointment appt : list) {
            List<AmsAppointmentItem> items = itemMap.getOrDefault(appt.getId(), Collections.emptyList());
            for (AmsAppointmentItem item : items) {
                if (item.getServiceId() != null) item.setService(serviceMap.get(item.getServiceId()));
                if (item.getTechId() != null) item.setTechnician(techMap.get(item.getTechId()));
                if (item.getRoomId() != null) item.setRoom(roomMap.get(item.getRoomId()));
            }
            appt.setItems(items);
        }
    }
    
    // 新增：Admin查询接口
    @Override
    public PageInfo<AmsAppointment> listAppointments(Long storeId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<AmsAppointment> list = appointmentMapper.selectList(storeId, startDate, endDate, status, keyword);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateAppointment(BatchBookingRequest request) {
        if (CollUtil.isEmpty(request.getServiceIds())) {
            throw new BusinessException("请选择服务项目");
        }
        if (CollUtil.isEmpty(request.getStartTimes())) {
            throw new BusinessException("请选择预约时间");
        }

        List<Long> createdIds = new ArrayList<>();
        
        // 笛卡尔积：所有选中的服务，应用到所有选中的时间
        for (Long serviceId : request.getServiceIds()) {
            for (LocalDateTime startTime : request.getStartTimes()) {
                BookingRequest singleReq = new BookingRequest();
                singleReq.setCustomerId(request.getCustomerId());
                singleReq.setContactName(request.getContactName());
                singleReq.setContactPhone(request.getContactPhone());
                singleReq.setRemark(request.getRemark());
                singleReq.setStartTime(startTime);
                // 强制状态为 PENDING_PAYMENT (1)
                singleReq.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
                singleReq.setStoreId(request.getStoreId());
                singleReq.setCreatorId(request.getCreatorId());
                singleReq.setPeopleCount(request.getPeopleCount() != null ? request.getPeopleCount() : 1);
                
                // Propagate payment method and source
                singleReq.setPaymentMethod(request.getPaymentMethod());
                singleReq.setSource(request.getSource());

                BookingItemDTO item = new BookingItemDTO();
                item.setServiceId(serviceId);
                item.setTechId(request.getTechId());
                
                singleReq.setItems(Collections.singletonList(item));

                // 调用现有的创建逻辑
                Long apptId = createAppointment(singleReq);
                createdIds.add(apptId);
            }
        }
        return createdIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPay(List<Long> appointmentIds) {
        if (CollUtil.isEmpty(appointmentIds)) return;
        Long operatorId = UserContext.getUserId();
        String operator = operatorId != null ? String.valueOf(operatorId) : "ADMIN";
        
        for (Long id : appointmentIds) {
            markAsPaid(id, operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCancel(List<Long> appointmentIds) {
        if (CollUtil.isEmpty(appointmentIds)) return;
        for (Long id : appointmentIds) {
            try {
                cancelAppointment(id);
            } catch (Exception e) {
                log.error("Batch cancel failed for id " + id, e);
                // Continue with others? Or rollback all?
                // Usually batch operations might want to succeed partially or fail all.
                // Given the requirement "Distributed transaction", maybe fail all?
                // But cancelAppointment is transactional itself.
                // If I put @Transactional on batchCancel, one failure rolls back all.
                // Let's assume rollback all for consistency.
                throw new BusinessException("Batch cancel failed for appointment " + id + ": " + e.getMessage());
            }
        }
    }
}
