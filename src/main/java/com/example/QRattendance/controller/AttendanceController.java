package com.example.QRattendance.controller;

import com.example.QRattendance.model.Attendance;
import com.example.QRattendance.model.Employee;
import com.example.QRattendance.repository.AttendanceRepository;
import com.example.QRattendance.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/scan")
    public ScanResponse scan(@RequestBody ScanRequest request) {
        if (request == null || request.getQrCode() == null || request.getQrCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR code is empty");
        }

        Employee employee = employeeRepository.findByQrCode(request.getQrCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        List<Attendance> history = attendanceRepository.findByEmployeeId(employee.getId());
        Attendance lastRecord = history.stream()
                .max(Comparator.comparing(Attendance::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        String type = "CHECK_IN";
        if (lastRecord != null && "CHECK_IN".equalsIgnoreCase(lastRecord.getType())) {
            type = "CHECK_OUT";
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employee.getId());
        attendance.setType(type);
        attendance.setTimestamp(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);
        return new ScanResponse(saved, employee.getName());
    }

    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    /**
     * Frontend-friendly report data: employee name is resolved on the server,
     * avoiding fragile client-side joins.
     */
    @GetMapping("/report")
    public List<ReportRow> getReport() {
        Map<Long, String> employeeNames = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        return attendanceRepository.findAll().stream()
                .sorted(Comparator.comparing(Attendance::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> new ReportRow(
                        a.getId(),
                        a.getEmployeeId(),
                        employeeNames.getOrDefault(a.getEmployeeId(), "Unknown employee"),
                        a.getType(),
                        a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Dashboard metrics used by the prototype.
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Employee> employees = employeeRepository.findAll();
        List<Attendance> records = attendanceRepository.findAll();
        LocalDate today = LocalDate.now();

        List<Attendance> todayRecords = records.stream()
                .filter(a -> a.getTimestamp() != null && a.getTimestamp().toLocalDate().equals(today))
                .collect(Collectors.toList());

        long checkInsToday = todayRecords.stream()
                .filter(a -> "CHECK_IN".equalsIgnoreCase(a.getType())).count();

        long checkOutsToday = todayRecords.stream()
                .filter(a -> "CHECK_OUT".equalsIgnoreCase(a.getType())).count();

        long presentToday = todayRecords.stream()
                .filter(a -> "CHECK_IN".equalsIgnoreCase(a.getType()))
                .map(Attendance::getEmployeeId)
                .distinct()
                .count();

        double attendanceRate = employees.isEmpty()
                ? 0
                : Math.round((presentToday * 10000.0 / employees.size())) / 100.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEmployees", employees.size());
        stats.put("presentToday", presentToday);
        stats.put("absentToday", Math.max(0, employees.size() - presentToday));
        stats.put("checkInsToday", checkInsToday);
        stats.put("checkOutsToday", checkOutsToday);
        stats.put("totalRecords", records.size());
        stats.put("attendanceRate", attendanceRate);
        return stats;
    }

    public static class ScanRequest {
        private String qrCode;
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    }

    public static class ScanResponse {
        private Long id;
        private Long employeeId;
        private LocalDateTime timestamp;
        private String type;
        private String employeeName;

        public ScanResponse(Attendance attendance, String employeeName) {
            this.id = attendance.getId();
            this.employeeId = attendance.getEmployeeId();
            this.timestamp = attendance.getTimestamp();
            this.type = attendance.getType();
            this.employeeName = employeeName;
        }

        public Long getId() { return id; }
        public Long getEmployeeId() { return employeeId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getEmployeeName() { return employeeName; }
    }

    public static class ReportRow {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String type;
        private LocalDateTime timestamp;

        public ReportRow(Long id, Long employeeId, String employeeName, String type, LocalDateTime timestamp) {
            this.id = id;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.type = type;
            this.timestamp = timestamp;
        }

        public Long getId() { return id; }
        public Long getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
