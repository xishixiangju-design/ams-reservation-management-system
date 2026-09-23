package com.trae.ams.service;

import com.trae.ams.dto.service.ServiceDTO;
import com.trae.ams.entity.AmsService;

import java.util.List;

public interface AmsServiceService {
    /**
     * 创建服务
     */
    void createService(ServiceDTO dto);

    /**
     * 更新服务
     */
    void updateService(Long id, ServiceDTO dto);

    /**
     * 删除服务
     */
    void deleteService(Long id);

    /**
     * 获取服务详情
     */
    AmsService getService(Long id);

    /**
     * 获取服务列表
     */
    List<AmsService> listServices(Long storeId, Integer status);
}
