package com.leavemanagement.leave_management.service;

import com.leavemanagement.leave_management.dto.LeaveRequest;
import com.leavemanagement.leave_management.dto.LeaveResponse;
import com.leavemanagement.leave_management.dto.LeavePartialUpdateRequest;
import com.leavemanagement.leave_management.exception.LeaveNotFoundException;
import com.leavemanagement.leave_management.mapper.LeaveMapper;
import com.leavemanagement.leave_management.model.Leave;
import com.leavemanagement.leave_management.repository.LeaveManagementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveManagementService {
    private final LeaveManagementRepository leaveManagementRepository;
    private final LeaveMapper leaveMapper;

    public LeaveManagementService(LeaveManagementRepository leaveManagementRepository, LeaveMapper leaveMapper) {
        this.leaveManagementRepository = leaveManagementRepository;
        this.leaveMapper = leaveMapper;
    }

    public List<LeaveResponse> getLeaves() {
        List<Leave> leaves = leaveManagementRepository.findAll();
        return leaves.stream().map(leaveMapper::toLeaveResponseDto).toList();
    }

    public LeaveResponse getLeaveById(int id) {
        Leave leave = leaveManagementRepository
                .findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave with id :" + id + " not found"));
        return leaveMapper.toLeaveResponseDto(leave);
    }

    public LeaveResponse createLeave(LeaveRequest leaveRequest) {
        Leave leave = leaveMapper.toLeave(leaveRequest);
        return leaveMapper.toLeaveResponseDto(leaveManagementRepository.save(leave));
    }

    public LeaveResponse updateLeave(int id, LeaveRequest leaveRequest) {
        if (!leaveManagementRepository.existsById(id)) {
            throw new LeaveNotFoundException("Leave with id :" + id + " not found");
        }
        Leave leave = leaveMapper.toLeave(leaveRequest);
        leave.setId(id);
        return leaveMapper.toLeaveResponseDto(leaveManagementRepository.save(leave));
    }

    public LeaveResponse partialUpdateLeave(int id, LeavePartialUpdateRequest leavePartialUpdateRequest) {
        Leave existingLeave = leaveManagementRepository
                .findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave with id :" + id + " not found"));
        leaveMapper.updateLeaveFromDto(leavePartialUpdateRequest, existingLeave);
        leaveManagementRepository.save(existingLeave);
        return leaveMapper.toLeaveResponseDto(existingLeave);
    }

    public void deleteLeave(int id) {
        if (!leaveManagementRepository.existsById(id)) {
            throw new LeaveNotFoundException("Leave with id :" + id + " not found");
        }
        leaveManagementRepository.deleteById(id);
    }
}
