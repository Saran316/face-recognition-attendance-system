
package com.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attendance_user_date",
            columnNames = {"user_id", "attendance_date"}
        )
    }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceMethod method;

    @Column(nullable = false, length = 20)
    private String status = "PRESENT";

    protected Attendance() {
    }

    public Attendance(User user, AttendanceMethod method) {
        this.user = user;
        this.method = method;
        this.attendanceDate = LocalDate.now();
        this.checkInTime = LocalDateTime.now();
        this.status = "PRESENT";
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public AttendanceMethod getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }
}
