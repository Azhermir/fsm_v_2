package com.fsm.taskmanagement.controller;

import com.fsm.taskmanagement.dto.DashboardStatsResponse;
import com.fsm.taskmanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "API for dashboard statistics and analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics", description = "Retrieve task statistics including total, open, completed counts and priority distribution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStatsResponse.class)))
    })
    public ResponseEntity<DashboardStatsResponse> getStatistics(
            @Parameter(description = "Optional date range filter: 'today' or 'this week'")
            @RequestParam(required = false) String dateRange) {
        log.info("Received request for dashboard statistics with date range: {}", dateRange);
        
        DashboardStatsResponse stats = dashboardService.getStatistics(dateRange);
        
        log.info("Successfully retrieved dashboard statistics");
        return ResponseEntity.ok(stats);
    }
}
