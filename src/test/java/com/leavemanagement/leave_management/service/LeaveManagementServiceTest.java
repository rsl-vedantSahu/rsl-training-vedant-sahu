package com.leavemanagement.leave_management.service;

import com.leavemanagement.leave_management.dto.LeavePartialUpdateRequest;
import com.leavemanagement.leave_management.dto.LeaveRequest;
import com.leavemanagement.leave_management.dto.LeaveResponse;
import com.leavemanagement.leave_management.exception.LeaveNotFoundException;
import com.leavemanagement.leave_management.mapper.LeaveMapper;
import com.leavemanagement.leave_management.model.Leave;
import com.leavemanagement.leave_management.repository.LeaveManagementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveManagementServiceTest {

    @Mock
    private LeaveManagementRepository leaveManagementRepository;

    @Mock
    private LeaveMapper leaveMapper;

    @InjectMocks
    private LeaveManagementService leaveManagementService;

    private Leave sampleLeave;
    private LeaveResponse sampleResponse;
    private LeaveRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleLeave = new Leave(1, 101, "Sick Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Fever", "Pending");

        sampleResponse = new LeaveResponse(1, 101, "Sick Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Fever", "Pending");

        sampleRequest = new LeaveRequest();
        sampleRequest.setEmployeeId(101);
        sampleRequest.setLeaveType("Sick Leave");
        sampleRequest.setStartDate(LocalDate.of(2024, 10, 1));
        sampleRequest.setEndDate(LocalDate.of(2024, 10, 5));
        sampleRequest.setReason("Fever");
        sampleRequest.setStatus("Pending");
    }

    @Test
    @DisplayName("getLeaves should return a list of leave responses")
    void testGetLeaves_Success() {
        when(leaveManagementRepository.findAll()).thenReturn(List.of(sampleLeave));
        when(leaveMapper.toLeaveResponseDto(sampleLeave)).thenReturn(sampleResponse);

        List<LeaveResponse> result = leaveManagementService.getLeaves();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(leaveManagementRepository, times(1)).findAll();
        verify(leaveMapper, times(1)).toLeaveResponseDto(sampleLeave);
    }

    @Test
    @DisplayName("getLeaves should return empty list when no leaves exist")
    void testGetLeaves_EmptyList() {
        when(leaveManagementRepository.findAll()).thenReturn(Collections.emptyList());

        List<LeaveResponse> result = leaveManagementService.getLeaves();

        assertThat(result).isEmpty();
        verify(leaveManagementRepository, times(1)).findAll();
        verifyNoInteractions(leaveMapper);
    }

    @Test
    @DisplayName("getLeaveById should return LeaveResponse when leave exists")
    void testGetLeaveById_Success() {
        when(leaveManagementRepository.findById(1)).thenReturn(Optional.of(sampleLeave));
        when(leaveMapper.toLeaveResponseDto(sampleLeave)).thenReturn(sampleResponse);

        LeaveResponse result = leaveManagementService.getLeaveById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getLeaveType()).isEqualTo("Sick Leave");
        verify(leaveManagementRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getLeaveById should throw LeaveNotFoundException when leave does not exist")
    void testGetLeaveById_NotFound() {
        when(leaveManagementRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(LeaveNotFoundException.class, () -> leaveManagementService.getLeaveById(999));
        verify(leaveManagementRepository, times(1)).findById(999);
        verifyNoInteractions(leaveMapper);
    }

    @Test
    @DisplayName("createLeave should map, save and return created LeaveResponse")
    void testCreateLeave_Success() {
        when(leaveMapper.toLeave(sampleRequest)).thenReturn(sampleLeave);
        when(leaveManagementRepository.save(sampleLeave)).thenReturn(sampleLeave);
        when(leaveMapper.toLeaveResponseDto(sampleLeave)).thenReturn(sampleResponse);

        LeaveResponse result = leaveManagementService.createLeave(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(leaveMapper, times(1)).toLeave(sampleRequest);
        verify(leaveManagementRepository, times(1)).save(sampleLeave);
        verify(leaveMapper, times(1)).toLeaveResponseDto(sampleLeave);
    }

    @Test
    @DisplayName("updateLeave should update and return LeaveResponse when leave exists")
    void testUpdateLeave_Success() {
        when(leaveManagementRepository.existsById(1)).thenReturn(true);
        when(leaveMapper.toLeave(sampleRequest)).thenReturn(sampleLeave);
        when(leaveManagementRepository.save(sampleLeave)).thenReturn(sampleLeave);
        when(leaveMapper.toLeaveResponseDto(sampleLeave)).thenReturn(sampleResponse);

        LeaveResponse result = leaveManagementService.updateLeave(1, sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(leaveManagementRepository, times(1)).existsById(1);
        verify(leaveManagementRepository, times(1)).save(sampleLeave);
    }

    @Test
    @DisplayName("updateLeave should throw LeaveNotFoundException when leave does not exist")
    void testUpdateLeave_NotFound() {
        when(leaveManagementRepository.existsById(999)).thenReturn(false);

        assertThrows(LeaveNotFoundException.class, () -> leaveManagementService.updateLeave(999, sampleRequest));
        verify(leaveManagementRepository, times(1)).existsById(999);
        verify(leaveManagementRepository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdateLeave should update fields in-place and return updated LeaveResponse")
    void testPartialUpdateLeave_Success() {
        LeavePartialUpdateRequest patchRequest = new LeavePartialUpdateRequest(
                "Casual Leave", null, null, "Family function", null);

        Leave updatedLeave = new Leave(1, 101, "Casual Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Family function", "Pending");

        LeaveResponse updatedResponse = new LeaveResponse(1, 101, "Casual Leave",
                LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5),
                "Family function", "Pending");

        when(leaveManagementRepository.findById(1)).thenReturn(Optional.of(sampleLeave));
        doNothing().when(leaveMapper).updateLeaveFromDto(patchRequest, sampleLeave);
        when(leaveManagementRepository.save(sampleLeave)).thenReturn(updatedLeave);
        when(leaveMapper.toLeaveResponseDto(sampleLeave)).thenReturn(updatedResponse);

        LeaveResponse result = leaveManagementService.partialUpdateLeave(1, patchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getLeaveType()).isEqualTo("Casual Leave");
        verify(leaveManagementRepository, times(1)).findById(1);
        verify(leaveMapper, times(1)).updateLeaveFromDto(patchRequest, sampleLeave);
        verify(leaveManagementRepository, times(1)).save(sampleLeave);
    }

    @Test
    @DisplayName("partialUpdateLeave should throw LeaveNotFoundException when leave does not exist")
    void testPartialUpdateLeave_NotFound() {
        LeavePartialUpdateRequest patchRequest = new LeavePartialUpdateRequest();
        when(leaveManagementRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(LeaveNotFoundException.class, () -> leaveManagementService.partialUpdateLeave(999, patchRequest));
        verify(leaveManagementRepository, times(1)).findById(999);
        verify(leaveManagementRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteLeave should delete when leave exists")
    void testDeleteLeave_Success() {
        when(leaveManagementRepository.existsById(1)).thenReturn(true);
        doNothing().when(leaveManagementRepository).deleteById(1);

        leaveManagementService.deleteLeave(1);

        verify(leaveManagementRepository, times(1)).existsById(1);
        verify(leaveManagementRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("deleteLeave should throw LeaveNotFoundException when leave does not exist")
    void testDeleteLeave_NotFound() {
        when(leaveManagementRepository.existsById(999)).thenReturn(false);

        assertThrows(LeaveNotFoundException.class, () -> leaveManagementService.deleteLeave(999));
        verify(leaveManagementRepository, times(1)).existsById(999);
        verify(leaveManagementRepository, never()).deleteById(anyInt());
    }
}
