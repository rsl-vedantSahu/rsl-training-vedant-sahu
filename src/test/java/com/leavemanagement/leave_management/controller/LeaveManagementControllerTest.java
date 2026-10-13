package com.leavemanagement.leave_management.controller;

import tools.jackson.databind.ObjectMapper;
import com.leavemanagement.leave_management.dto.LeavePartialUpdateRequest;
import com.leavemanagement.leave_management.dto.LeaveRequest;
import com.leavemanagement.leave_management.dto.LeaveResponse;
import com.leavemanagement.leave_management.exception.LeaveNotFoundException;
import com.leavemanagement.leave_management.service.LeaveManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveManagementController.class)
class LeaveManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveManagementService leaveManagementService;

    private LeaveResponse sampleResponse;
    private LeaveRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = new LeaveResponse(1, 101, "Sick Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Fever", "Pending");

        validRequest = new LeaveRequest();
        validRequest.setEmployeeId(101);
        validRequest.setLeaveType("Sick Leave");
        validRequest.setStartDate(LocalDate.of(2024, 10, 1));
        validRequest.setEndDate(LocalDate.of(2024, 10, 5));
        validRequest.setReason("Fever");
        validRequest.setStatus("Pending");
    }

    @Test
    @DisplayName("GET /leaves - Success should return 200 OK and list of leaves")
    void testGetLeaves_Success() throws Exception {
        when(leaveManagementService.getLeaves()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/leaves"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].leaveType").value("Sick Leave"));

        verify(leaveManagementService, times(1)).getLeaves();
    }

    @Test
    @DisplayName("GET /leaves/{id} - Success should return 200 OK and leave details")
    void testGetLeaveById_Success() throws Exception {
        when(leaveManagementService.getLeaveById(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(101))
                .andExpect(jsonPath("$.leaveType").value("Sick Leave"));

        verify(leaveManagementService, times(1)).getLeaveById(1);
    }

    @Test
    @DisplayName("GET /leaves/{id} - Not Found should return 404 NOT FOUND")
    void testGetLeaveById_NotFound() throws Exception {
        when(leaveManagementService.getLeaveById(999))
                .thenThrow(new LeaveNotFoundException("Leave with id :999 not found"));

        mockMvc.perform(get("/leaves/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Leave Not Found"))
                .andExpect(jsonPath("$.message").value("Leave with id :999 not found"));

        verify(leaveManagementService, times(1)).getLeaveById(999);
    }

    @Test
    @DisplayName("POST /leaves - Valid payload should return 201 CREATED")
    void testCreateLeave_Success() throws Exception {
        when(leaveManagementService.createLeave(any(LeaveRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(101));

        verify(leaveManagementService, times(1)).createLeave(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("POST /leaves - Invalid payload should return 400 BAD REQUEST")
    void testCreateLeave_ValidationFailure() throws Exception {
        LeaveRequest invalidRequest = new LeaveRequest();
        invalidRequest.setEmployeeId(-1); // invalid, must be >= 1
        invalidRequest.setLeaveType("");  // invalid, cannot be blank

        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(leaveManagementService, never()).createLeave(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("PUT /leaves/{id} - Valid payload should return 200 OK")
    void testUpdateLeave_Success() throws Exception {
        when(leaveManagementService.updateLeave(eq(1), any(LeaveRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(leaveManagementService, times(1)).updateLeave(eq(1), any(LeaveRequest.class));
    }

    @Test
    @DisplayName("PUT /leaves/{id} - Not Found should return 404 NOT FOUND")
    void testUpdateLeave_NotFound() throws Exception {
        when(leaveManagementService.updateLeave(eq(999), any(LeaveRequest.class)))
                .thenThrow(new LeaveNotFoundException("Leave with id :999 not found"));

        mockMvc.perform(put("/leaves/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(leaveManagementService, times(1)).updateLeave(eq(999), any(LeaveRequest.class));
    }

    @Test
    @DisplayName("PATCH /leaves/{id} - Valid patch should return 200 OK")
    void testPartialUpdateLeave_Success() throws Exception {
        LeavePartialUpdateRequest patchRequest = new LeavePartialUpdateRequest();
        patchRequest.setStatus("Approved");

        LeaveResponse patchedResponse = new LeaveResponse(1, 101, "Sick Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Fever", "Approved");

        when(leaveManagementService.partialUpdateLeave(eq(1), any(LeavePartialUpdateRequest.class)))
                .thenReturn(patchedResponse);

        mockMvc.perform(patch("/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"));

        verify(leaveManagementService, times(1))
                .partialUpdateLeave(eq(1), any(LeavePartialUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /leaves/{id} - Not Found should return 404 NOT FOUND")
    void testPartialUpdateLeave_NotFound() throws Exception {
        LeavePartialUpdateRequest patchRequest = new LeavePartialUpdateRequest();
        patchRequest.setStatus("Approved");

        when(leaveManagementService.partialUpdateLeave(eq(999), any(LeavePartialUpdateRequest.class)))
                .thenThrow(new LeaveNotFoundException("Leave with id :999 not found"));

        mockMvc.perform(patch("/leaves/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(leaveManagementService, times(1))
                .partialUpdateLeave(eq(999), any(LeavePartialUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /leaves/{id} - Success should return 204 NO CONTENT")
    void testDeleteLeave_Success() throws Exception {
        doNothing().when(leaveManagementService).deleteLeave(1);

        mockMvc.perform(delete("/leaves/1"))
                .andExpect(status().isNoContent());

        verify(leaveManagementService, times(1)).deleteLeave(1);
    }

    @Test
    @DisplayName("DELETE /leaves/{id} - Not Found should return 404 NOT FOUND")
    void testDeleteLeave_NotFound() throws Exception {
        doThrow(new LeaveNotFoundException("Leave with id :999 not found"))
                .when(leaveManagementService).deleteLeave(999);

        mockMvc.perform(delete("/leaves/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(leaveManagementService, times(1)).deleteLeave(999);
    }
}
