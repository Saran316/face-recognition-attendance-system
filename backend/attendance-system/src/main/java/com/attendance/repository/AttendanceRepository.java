
package com.attendance.repository;

import com.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findByAttendanceDate(LocalDate date);

    List<Attendance> findByUserId(Long userId);

    boolean existsByUserIdAndAttendanceDate(
        Long userId,
        LocalDate date
    );
}
