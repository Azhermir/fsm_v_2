package com.fsm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.taskmanagement.dto.CreateServiceTaskRequest;
import com.fsm.taskmanagement.dto.ServiceTaskResponse;
import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.Status;
import com.fsm.taskmanagement.service.ServiceTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceTaskController.class)
@DisplayName("ServiceTaskController Tests")
class ServiceTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceTaskService service;

    private CreateServiceTaskRequest validRequest;
    private ServiceTaskResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = CreateServiceTaskRequest.builder()
                .title("Fix HVAC System")
                .description("Repair broken air conditioning unit")
                .clientAddress("123 Main St, City, State")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .build();

        validResponse = ServiceTaskResponse.builder()
                .id(UUID.randomUUID())
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .clientAddress(validRequest.getClientAddress())
                .priority(validRequest.getPriority())
                .estimatedDuration(validRequest.getEstimatedDuration())
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task and return 201 Created")
    void shouldCreateTaskAndReturn201() throws Exception {
        // Given
        when(service.createTask(any(CreateServiceTaskRequest.class))).thenReturn(validResponse);

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(validResponse.getId().toString()))
                .andExpect(jsonPath("$.title").value(validRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(validRequest.getDescription()))
                .andExpect(jsonPath("$.clientAddress").value(validRequest.getClientAddress()))
                .andExpect(jsonPath("$.priority").value(validRequest.getPriority().toString()))
                .andExpect(jsonPath("$.estimatedDuration").value(validRequest.getEstimatedDuration()))
                .andExpect(jsonPath("$.status").value(Status.UNASSIGNED.toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(service, times(1)).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when title is missing")
    void shouldReturn400WhenTitleIsMissing() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when title is blank")
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("   ")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when description is missing")
    void shouldReturn400WhenDescriptionIsMissing() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when description is blank")
    void shouldReturn400WhenDescriptionIsBlank() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when clientAddress is missing")
    void shouldReturn400WhenClientAddressIsMissing() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when clientAddress is blank")
    void shouldReturn400WhenClientAddressIsBlank() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress("  ")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when priority is missing")
    void shouldReturn400WhenPriorityIsMissing() throws Exception {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress("Address")
                .build();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should accept task without estimatedDuration")
    void shouldAcceptTaskWithoutEstimatedDuration() throws Exception {
        // Given
        CreateServiceTaskRequest requestWithoutDuration = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.LOW)
                .build();

        ServiceTaskResponse responseWithoutDuration = ServiceTaskResponse.builder()
                .id(UUID.randomUUID())
                .title(requestWithoutDuration.getTitle())
                .description(requestWithoutDuration.getDescription())
                .clientAddress(requestWithoutDuration.getClientAddress())
                .priority(requestWithoutDuration.getPriority())
                .estimatedDuration(null)
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(service.createTask(any(CreateServiceTaskRequest.class))).thenReturn(responseWithoutDuration);

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutDuration)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimatedDuration").doesNotExist());

        verify(service, times(1)).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should create task with all priority levels")
    void shouldCreateTaskWithAllPriorityLevels() throws Exception {
        for (Priority priority : Priority.values()) {
            // Given
            CreateServiceTaskRequest request = CreateServiceTaskRequest.builder()
                    .title("Task " + priority)
                    .description("Description")
                    .clientAddress("Address")
                    .priority(priority)
                    .build();

            ServiceTaskResponse response = ServiceTaskResponse.builder()
                    .id(UUID.randomUUID())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .clientAddress(request.getClientAddress())
                    .priority(priority)
                    .status(Status.UNASSIGNED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(service.createTask(any(CreateServiceTaskRequest.class))).thenReturn(response);

            // When/Then
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.priority").value(priority.toString()));
        }

        verify(service, times(Priority.values().length)).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when service throws IllegalArgumentException")
    void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
        // Given
        when(service.createTask(any(CreateServiceTaskRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(service, times(1)).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON request")
    void shouldHandleMalformedJsonRequest() throws Exception {
        // Given
        String malformedJson = "{invalid json}";

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should validate all required fields together")
    void shouldValidateAllRequiredFieldsTogether() throws Exception {
        // Given - Request missing all required fields
        CreateServiceTaskRequest emptyRequest = new CreateServiceTaskRequest();

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(CreateServiceTaskRequest.class));
    }

    @Test
    @DisplayName("Should return proper content type in response")
    void shouldReturnProperContentType() throws Exception {
        // Given
        when(service.createTask(any(CreateServiceTaskRequest.class))).thenReturn(validResponse);

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should create task with minimum required fields")
    void shouldCreateTaskWithMinimumRequiredFields() throws Exception {
        // Given
        CreateServiceTaskRequest minimalRequest = CreateServiceTaskRequest.builder()
                .title("Minimal Task")
                .description("Minimal Description")
                .clientAddress("Minimal Address")
                .priority(Priority.LOW)
                .build();

        ServiceTaskResponse minimalResponse = ServiceTaskResponse.builder()
                .id(UUID.randomUUID())
                .title(minimalRequest.getTitle())
                .description(minimalRequest.getDescription())
                .clientAddress(minimalRequest.getClientAddress())
                .priority(minimalRequest.getPriority())
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(service.createTask(any(CreateServiceTaskRequest.class))).thenReturn(minimalResponse);

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(minimalRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(minimalRequest.getDescription()))
                .andExpect(jsonPath("$.clientAddress").value(minimalRequest.getClientAddress()))
                .andExpect(jsonPath("$.priority").value(minimalRequest.getPriority().toString()))
                .andExpect(jsonPath("$.status").value(Status.UNASSIGNED.toString()));

        verify(service, times(1)).createTask(any(CreateServiceTaskRequest.class));
    }
}
