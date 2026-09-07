# Greater Jasol Complaint Portal - Android (Java)

This repository now contains a Java-based Android app (traditional XML layouts) that lets citizens file complaints to wards (1..20), pick photos, upload via presigned URLs, and submit complaints.

How to run
1. Open the project in Android Studio.
2. Update the backend base URL in `app/src/main/java/com/greaterjasol/complaints/network/NetworkModule.java`.
3. Build and run on a device or emulator.

Notes
- The app expects backend endpoints:
  - POST /auth/request-otp
  - POST /auth/verify-otp
  - GET /uploads/presign?filename=...&filetype=...
  - POST /complaints

- This app performs synchronous network calls on a background thread via an ExecutorService for simplicity. For production, consider more robust async handling and progress reporting.
