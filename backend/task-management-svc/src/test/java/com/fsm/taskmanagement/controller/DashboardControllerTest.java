package com.fsm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.taskmanagement.dto.DashboardStatsResponse;
import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DashboardService dashboardService;

    private DashboardStatsResponse statsResponse;

    @BeforeEach
    void setUp() {
        Map<Priority, Long> tasksByPriority = new HashMap<>();
        tasksByPriority.put(Priority.CRITICAL, 10L);
        tasksByPriority.put(Priority.HIGH, 20L);
        tasksByPriority.put(Priority.MEDIUM, 40L);
        tasksByPriority.put(Priority.LOW, 30L);

        statsResponse = DashboardStatsResponse.builder()
                .totalTasks(100L)
                .openTasks(25L)
                .completedTasks(75L)
                .completionPercentage(75.0)
                .tasksByPriority(tasksByPriority)
                .build();
    }

    @Test
    @DisplayName("Should get statistics without date range")
    void shouldGetStatisticsWithoutDateRange() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(null)).thenReturn(statsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(100))
                .andExpect(jsonPath("$.openTasks").value(25))
                .andExpect(jsonPath("$.completedTasks").value(75))
                .andExpect(jsonPath("$.completionPercentage").value(75.0))
                .andExpect(jsonPath("$.tasksByPriority.CRITICAL").value(10))
                .andExpect(jsonPath("$.tasksByPriority.HIGH").value(20))
                .andExpect(jsonPath("$.tasksByPriority.MEDIUM").value(40))
                .andExpect(jsonPath("$.tasksByPriority.LOW").value(30));

        verify(dashboardService).getStatistics(null);
    }

    @Test
    @DisplayName("Should get statistics with 'today' date range")
    void shouldGetStatisticsWithTodayDateRange() throws Exception {
        // Arrange
        Map<Priority, Long> tasksByPriority = new HashMap<>();
        tasksByPriority.put(Priority.CRITICAL, 5L);
        tasksByPriority.put(Priority.HIGH, 10L);
        tasksByPriority.put(Priority.MEDIUM, 8L);
        tasksByPriority.put(Priority.LOW, 2L);

        DashboardStatsResponse todayStats = DashboardStatsResponse.builder()
                .totalTasks(25L)
                .openTasks(15L)
                .completedTasks(10L)
                .completionPercentage(40.0)
                .tasksByPriority(tasksByPriority)
                .build();

        when(dashboardService.getStatistics("today")).thenReturn(todayStats);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats")
                        .param("dateRange", "today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(25))
                .andExpect(jsonPath("$.openTasks").value(15))
                .andExpect(jsonPath("$.completedTasks").value(10))
                .andExpect(jsonPath("$.completionPercentage").value(40.0))
                .andExpect(jsonPath("$.tasksByPriority.CRITICAL").value(5))
                .andExpect(jsonPath("$.tasksByPriority.HIGH").value(10));

        verify(dashboardService).getStatistics("today");
    }

    @Test
    @DisplayName("Should get statistics with 'this week' date range")
    void shouldGetStatisticsWithThisWeekDateRange() throws Exception {
        // Arrange
        Map<Priority, Long> tasksByPriority = new HashMap<>();
        tasksByPriority.put(Priority.CRITICAL, 12L);
        tasksByPriority.put(Priority.HIGH, 18L);
        tasksByPriority.put(Priority.MEDIUM, 20L);
        tasksByPriority.put(Priority.LOW, 10L);

        DashboardStatsResponse weekStats = DashboardStatsResponse.builder()
                .totalTasks(60L)
                .openTasks(30L)
                .completedTasks(30L)
                .completionPercentage(50.0)
                .tasksByPriority(tasksByPriority)
                .build();

        when(dashboardService.getStatistics("this week")).thenReturn(weekStats);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats")
                        .param("dateRange", "this week")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(60))
                .andExpect(jsonPath("$.openTasks").value(30))
                .andExpect(jsonPath("$.completedTasks").value(30))
                .andExpect(jsonPath("$.completionPercentage").value(50.0));

        verify(dashboardService).getStatistics("this week");
    }

    @Test
    @DisplayName("Should handle empty statistics response")
    void shouldHandleEmptyStatisticsResponse() throws Exception {
        // Arrange
        Map<Priority, Long> emptyTasksByPriority = new HashMap<>();
        emptyTasksByPriority.put(Priority.CRITICAL, 0L);
        emptyTasksByPriority.put(Priority.HIGH, 0L);
        emptyTasksByPriority.put(Priority.MEDIUM, 0L);
        emptyTasksByPriority.put(Priority.LOW, 0L);

        DashboardStatsResponse emptyStats = DashboardStatsResponse.builder()
                .totalTasks(0L)
                .openTasks(0L)
                .completedTasks(0L)
                .completionPercentage(0.0)
                .tasksByPriority(emptyTasksByPriority)
                .build();

        when(dashboardService.getStatistics(null)).thenReturn(emptyStats);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.openTasks").value(0))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.0));

        verify(dashboardService).getStatistics(null);
    }

    @Test
    @DisplayName("Should return 200 OK for any date range value")
    void shouldReturn200ForAnyDateRange() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(any())).thenReturn(statsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats")
                        .param("dateRange", "unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(dashboardService).getStatistics("unknown");
    }

    @Test
    @DisplayName("Should handle service call properly")
    void shouldHandleServiceCallProperly() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(any())).thenReturn(statsResponse);

        // Act
        mockMvc.perform(get("/api/dashboard/stats")
                        .param("dateRange", "today"))
                .andExpect(status().isOk());

        // Assert
        verify(dashboardService, times(1)).getStatistics("today");
        verifyNoMoreInteractions(dashboardService);
    }

    @Test
    @DisplayName("Should return valid JSON structure")
    void shouldReturnValidJsonStructure() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(null)).thenReturn(statsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalTasks").exists())
                .andExpect(jsonPath("$.openTasks").exists())
                .andExpect(jsonPath("$.completedTasks").exists())
                .andExpect(jsonPath("$.completionPercentage").exists())
                .andExpect(jsonPath("$.tasksByPriority").exists())
                .andExpect(jsonPath("$.tasksByPriority").isMap());
    }

    @Test
    @DisplayName("Should accept GET requests only")
    void shouldAcceptGetRequestsOnly() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(any())).thenReturn(statsResponse);

        // Act & Assert - GET should work
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle statistics with 100 percent completion")
    void shouldHandleFullCompletion() throws Exception {
        // Arrange
        Map<Priority, Long> tasksByPriority = new HashMap<>();
        tasksByPriority.put(Priority.CRITICAL, 25L);
        tasksByPriority.put(Priority.HIGH, 25L);
        tasksByPriority.put(Priority.MEDIUM, 25L);
        tasksByPriority.put(Priority.LOW, 25L);

        DashboardStatsResponse fullCompletionStats = DashboardStatsResponse.builder()
                .totalTasks(100L)
                .openTasks(0L)
                .completedTasks(100L)
                .completionPercentage(100.0)
                .tasksByPriority(tasksByPriority)
                .build();

        when(dashboardService.getStatistics(null)).thenReturn(fullCompletionStats);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(100))
                .andExpect(jsonPath("$.openTasks").value(0))
                .andExpect(jsonPath("$.completedTasks").value(100))
                .andExpect(jsonPath("$.completionPercentage").value(100.0));
    }

    @Test
    @DisplayName("Should properly pass null when dateRange parameter is absent")
    void shouldPassNullWhenDateRangeAbsent() throws Exception {
        // Arrange
        when(dashboardService.getStatistics(null)).thenReturn(statsResponse);

        // Act
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk());

        // Assert - verify null was passed
        verify(dashboardService).getStatistics(null);
    }
}
