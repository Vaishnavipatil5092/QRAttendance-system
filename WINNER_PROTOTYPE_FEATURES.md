# QR Attendance — Prototype Upgrade

## Added
- Command-center dashboard with live KPIs
- Present/absent calculation for today
- Attendance rate and 7-day check-in trend
- Recent activity feed with auto-refresh
- Server-side attendance report endpoint with employee names
- Report filters: date range, type, employee search
- CSV export for attendance
- CSV export for employee directory
- QR view/print actions
- Improved add-employee flow
- Improved QR scanner with camera selection, image scanning and manual QR fallback
- Shared responsive UI

## Existing backend retained
- Spring Boot
- JPA/H2
- Employee CRUD creation flow
- QR generation using ZXing
- QR attendance check-in/check-out logic

Run:
`.\mvnw.cmd spring-boot:run`

Open:
`http://localhost:8080/`
