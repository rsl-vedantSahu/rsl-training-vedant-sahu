package com.leavemanagement.leave_management.repository;

import com.leavemanagement.leave_management.model.Leave;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LeaveManagementRepository {

    private final AtomicInteger idCounter = new AtomicInteger(10); // starts after your 10 initial leaves

    ConcurrentHashMap<String, Leave> data = new ConcurrentHashMap<>();

    public LeaveManagementRepository() {
        Leave leave1 = new Leave(1, 1, "Sick Leave", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5), "Sick",
                "Pending");
        Leave leave2 = new Leave(2, 2, "Casual Leave", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5), "Casual",
                "Pending");
        Leave leave3 = new Leave(3, 3, "Sick Leave", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5), "Sick",
                "Pending");
        Leave leave4 = new Leave(4, 4, "Casual Leave", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 5), "Casual",
                "Pending");
        Leave leave5 = new Leave(5, 5, "Sick Leave", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5), "Sick",
                "Pending");
        Leave leave6 = new Leave(6, 6, "Casual Leave", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 5), "Casual",
                "Pending");
        Leave leave7 = new Leave(7, 7, "Sick Leave", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5), "Sick",
                "Pending");
        Leave leave8 = new Leave(8, 8, "Casual Leave", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 5), "Casual",
                "Pending");
        Leave leave9 = new Leave(9, 9, "Sick Leave", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 5), "Sick",
                "Pending");
        Leave leave10 = new Leave(10, 10, "Casual Leave", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5), "Casual",
                "Pending");
        data.put("1", leave1);
        data.put("2", leave2);
        data.put("3", leave3);
        data.put("4", leave4);
        data.put("5", leave5);
        data.put("6", leave6);
        data.put("7", leave7);
        data.put("8", leave8);
        data.put("9", leave9);
        data.put("10", leave10);
    }

    public Optional<Leave> findById(int id) {
        return Optional.ofNullable(data.get(String.valueOf(id)));
    }

    public Boolean existsById(int id) {
        return data.containsKey(String.valueOf(id));
    }

    public List<Leave> findAll() {
        return new ArrayList<>(data.values());
    }

    public Leave save(Leave leave) {
        if (leave.getId() <= 0) {
            leave.setId(idCounter.incrementAndGet());
        }
        data.put(String.valueOf(leave.getId()), leave);
        return leave;
    }

    public void deleteById(int id) {
        data.remove(String.valueOf(id));
    }
}
