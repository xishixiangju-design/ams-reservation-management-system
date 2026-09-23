package com.trae.ams.dto.technician;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TechnicianConflictDTO {
    private Long apptId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String customerName;
    private String serviceName;
}
