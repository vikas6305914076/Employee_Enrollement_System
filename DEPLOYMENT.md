# Deployment Guide

## Frontend on Vercel

You can now deploy the repository root on Vercel using the new top-level `vercel.json`.

What the root Vercel config does:

- redirects `/` to `/login.html`
- serves frontend files from `src/main/resources/static`
- proxies `/auth/*` and `/employees*` requests to Railway:
  - `https://employeeenrollementsystem-production.up.railway.app`

Files included in the Vercel deployment:

- `vercel.json`
- `src/main/resources/static/login.html`
- `src/main/resources/static/index.html`
- `src/main/resources/static/employee-list.html`
- `src/main/resources/static/profile.html`
- `src/main/resources/static/update-employee.html`
- `src/main/resources/static/assets`
- `src/main/resources/static/vercel.json`

If you prefer deploying only `src/main/resources/static` as the Vercel project root, that folder's local `vercel.json` has also been updated with the same Railway proxy routes.

## Backend on Railway

Deploy the repository root on Railway. Railway can build the Spring Boot app directly from `pom.xml`.

Important files used by Railway:

- `pom.xml`
- `src/main/java`
- `src/main/resources`

Do not upload `target/`.

Set these Railway environment variables:

- `SPRING_PROFILES_ACTIVE=prod`
- `EMS_DB_URL=jdbc:mysql://...`
- `EMS_DB_USERNAME=...`
- `EMS_DB_PASSWORD=...`
- `EMS_JWT_SECRET=...`
- `EMS_ALLOWED_ORIGIN_PATTERNS=https://your-project.vercel.app,https://*.vercel.app`

The application now also reads Railway's `PORT` variable automatically in production.

If you see `Invalid CORS request` after deploying the frontend, it usually means Railway is still running without the correct `EMS_ALLOWED_ORIGIN_PATTERNS` value. Update that variable, redeploy Railway, and then retry from Vercel.

## Database

Import `database/employee_enrollment.sql` into your MySQL database before starting the Railway service.

Production uses schema validation, so the tables must already exist.
