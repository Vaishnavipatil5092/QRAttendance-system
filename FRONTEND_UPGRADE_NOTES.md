# Frontend upgrade

This version keeps the existing Spring Boot controllers, models, repositories, H2 configuration, QR generation and attendance API endpoints.

Modified:
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/add-employee.html`
- `src/main/resources/templates/employees.html`
- `src/main/resources/templates/attendance-report.html`
- `src/main/resources/templates/scan.html`
- Added `src/main/resources/static/style.css`

The frontend now uses the existing `/api/employees`, `/api/employees/{id}/qrcode`, `/api/attendance`, and `/api/attendance/scan` endpoints with relative URLs, so the UI does not depend on a hard-coded `localhost:8080` address.
