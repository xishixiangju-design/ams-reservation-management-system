package com.trae.ams.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.service.ServiceDTO;
import com.trae.ams.entity.AmsService;
import com.trae.ams.mapper.AmsServiceMapper;
import com.trae.ams.service.AmsServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AmsServiceServiceImpl implements AmsServiceService {

    @Autowired
    private AmsServiceMapper amsServiceMapper;

    @Autowired
    private com.trae.ams.mapper.AmsAppointmentItemMapper appointmentItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createService(ServiceDTO dto) {
        AmsService service = new AmsService();
        BeanUtil.copyProperties(dto, service);
        
        // 设置默认值
        service.setCreateTime(LocalDateTime.now());
        // 从 Context 获取当前登录用户的 storeId
        service.setStoreId(UserContext.getStoreIdOrDefault()); 
        
        if (service.getStatus() == null) {
            service.setStatus(1); // 默认上架
        }

        amsServiceMapper.insert(service);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateService(Long id, ServiceDTO dto) {
        AmsService exist = amsServiceMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("服务不存在");
        }

        AmsService update = new AmsService();
        BeanUtil.copyProperties(dto, update);
        update.setId(id);
        
        amsServiceMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteService(Long id) {
        AmsService exist = amsServiceMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("服务不存在");
        }
        
        // 检查是否有关联的预约订单，如果有则不允许删除
        if (appointmentItemMapper.countByServiceId(id) > 0) {
            throw new BusinessException("该服务已被预约使用，无法删除");
        }
        
        amsServiceMapper.deleteById(id);
    }

    @Override
    public AmsService getService(Long id) {
        return amsServiceMapper.selectById(id);
    }

    @Override
    public List<AmsService> listServices(Long storeId, Integer status) {
        return amsServiceMapper.selectList(storeId, status);
    }
}
