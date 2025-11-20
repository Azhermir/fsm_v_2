package com.fsm.user.dto;

import com.fsm.user.domain.Location;
import com.fsm.user.domain.TechnicianStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Technician response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private TechnicianStatus status;
    private Location currentLocation;
}
