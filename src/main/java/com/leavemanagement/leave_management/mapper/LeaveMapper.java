package com.leavemanagement.leave_management.mapper;

import com.leavemanagement.leave_management.dto.LeavePartialUpdateRequest;
import com.leavemanagement.leave_management.dto.LeaveRequest;
import com.leavemanagement.leave_management.dto.LeaveResponse;
import com.leavemanagement.leave_management.model.Leave;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LeaveMapper {

    @Mapping(target = "id", ignore = true)
    Leave toLeave(LeaveRequest leaveRequest);

    LeaveResponse toLeaveResponseDto(Leave leave);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    void updateLeaveFromDto(LeavePartialUpdateRequest dto, @MappingTarget Leave leave);
}
