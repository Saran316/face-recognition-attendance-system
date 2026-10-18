
package com.attendance.controller;

import com.attendance.entity.Attendance;
import com.attendance.entity.AttendanceMethod;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.UserRepository;
import com.attendance.service.FaceRecognitionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final FaceRecognitionService recognitionService;

    public AttendanceController(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            FaceRecognitionService recognitionService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.recognitionService = recognitionService;
    }

    // Existing manual check-in endpoint
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(Authentication authentication) {
        User user = getActiveUser(authentication);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found or inactive"));
        }

        return saveAttendance(user, AttendanceMethod.MANUAL);
    }

    // New face-verified check-in endpoint
    @PostMapping(
            value = "/face-check-in",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> faceCheckIn(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Please log in first"));
        }

        if (file == null || file.isEmpty()) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "Please upload a face image."));
}

if (file.getSize() > 5L * 1024 * 1024) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "Image size must not exceed 5 MB."));
}

String contentType = file.getContentType();

if (contentType == null
        || !contentType.startsWith("image/")) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "Please upload an image file."));
}

        User user = getActiveUser(authentication);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found or inactive"));
        }

        Map<?, ?> result;

try {
    result = recognitionService.recognize(user.getId(), file);
} catch (IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage()));
} catch (IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                    "error", "Face recognition service is unavailable."
            ));
}
        if (!Boolean.TRUE.equals(result.get("verified"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "Face verification failed. Attendance not marked."
                    ));
        }

        return saveAttendance(user, AttendanceMethod.FACE_RECOGNITION);
    }

    // Shared attendance-saving logic
    private ResponseEntity<?> saveAttendance(
            User user, AttendanceMethod method) {

        LocalDate today = LocalDate.now();

        if (attendanceRepository
                .existsByUserIdAndAttendanceDate(user.getId(), today)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Attendance already marked for today"
                    ));
        }

        Attendance attendance = new Attendance(user, method);
        Attendance saved = attendanceRepository.save(attendance);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "attendanceId", saved.getId(),
                        "userId", user.getId(),
                        "date", saved.getAttendanceDate().toString(),
                        "checkInTime", saved.getCheckInTime().toString(),
                        "method", saved.getMethod().name(),
                        "status", saved.getStatus()
                ));
    }

    private User getActiveUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .filter(User::isActive)
                .orElse(null);
    }

    // Existing attendance-history endpoint
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyAttendance(
            Authentication authentication) {

        User user = getActiveUser(authentication);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found or inactive"));
        }

        var records = attendanceRepository.findByUserId(user.getId());

        var history = records.stream()
                .map(record -> Map.of(
                        "attendanceId", record.getId(),
                        "date", record.getAttendanceDate().toString(),
                        "checkInTime", record.getCheckInTime().toString(),
                        "method", record.getMethod().name(),
                        "status", record.getStatus()
                ))
                .toList();

        return ResponseEntity.ok(history);
    }
}
