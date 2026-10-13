package com.leavemanagement.leave_management.controller;

import com.leavemanagement.leave_management.dto.LeavePartialUpdateRequest;
import com.leavemanagement.leave_management.dto.LeaveRequest;
import com.leavemanagement.leave_management.dto.LeaveResponse;
import com.leavemanagement.leave_management.service.LeaveManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaves")
public class LeaveManagementController {
    private final LeaveManagementService leaveManagementService;

    public LeaveManagementController(LeaveManagementService leaveManagementService) {
        this.leaveManagementService = leaveManagementService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getLeaves() {
        return ResponseEntity.ok(leaveManagementService.getLeaves());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveById(@PathVariable("id") int id) {
        return ResponseEntity.ok(leaveManagementService.getLeaveById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveResponse> createLeave(@Valid @RequestBody LeaveRequest leaveRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveManagementService.createLeave(leaveRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponse> updateLeave(@PathVariable("id") int id, @Valid @RequestBody LeaveRequest leaveRequest) {
        return ResponseEntity.ok(leaveManagementService.updateLeave(id, leaveRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LeaveResponse> partialUpdateLeave(@PathVariable("id") int id, @Valid @RequestBody LeavePartialUpdateRequest leavePartialUpdateRequest) {
        return ResponseEntity.ok(leaveManagementService.partialUpdateLeave(id, leavePartialUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable("id") int id) {
        leaveManagementService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}