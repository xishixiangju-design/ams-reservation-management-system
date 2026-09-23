package com.trae.ams.service;

import com.trae.ams.dto.service.ServiceDTO;
import com.trae.ams.entity.AmsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Transactional // 测试完回滚
public class AmsServiceTest {

    @Autowired
    private AmsServiceService amsServiceService;

    @Test
    public void testCrud() {
        // 1. Create
        ServiceDTO dto = new ServiceDTO();
        dto.setName("全身按摩");
        dto.setDuration(60);
        dto.setPrice(new BigDecimal("298.00"));
        dto.setDescription("放松身心");
        dto.setStatus(1);

        amsServiceService.createService(dto);

        // 2. List
        List<AmsService> list = amsServiceService.listServices(1L, 1);
        Assertions.assertFalse(list.isEmpty());
        AmsService created = list.get(0);
        Assertions.assertEquals("全身按摩", created.getName());

        // 3. Update
        ServiceDTO updateDto = new ServiceDTO();
        updateDto.setName("全身按摩(升级版)");
        amsServiceService.updateService(created.getId(), updateDto);

        AmsService updated = amsServiceService.getService(created.getId());
        Assertions.assertEquals("全身按摩(升级版)", updated.getName());
        Assertions.assertEquals(new BigDecimal("298.00"), updated.getPrice()); // 应该保持原样吗？BeanUtil copyProperties 会覆盖 null 吗？
        // BeanUtil.copyProperties 默认会覆盖，所以如果 updateDto 中 price 为 null，updated 中也会是 null 吗？
        // 这是一个潜在 bug，需要检查 BeanUtil 行为。Hutool 默认 copyProperties 是覆盖的。
        // 如果 DTO 字段为 null，也会覆盖。通常 Update DTO 应该只包含需要更新的字段，或者 Service 层要做非空判断。
        // 让我们看看 Service 实现。
    }
}
