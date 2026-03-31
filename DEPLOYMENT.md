# Deployment Guide

## Frontend on Vercel

Deploy the folder `src/main/resources/static` as the Vercel project root.

Before deploying, open `src/main/resources/static/assets/config.js` and set:

```js
apiBaseUrl: "https://your-railway-service.up.railway.app"
```

Files included in the Vercel deployment:

- `src/main/resources/static/login.html`
- `src/main/resources/static/index.html`
- `src/main/resources/static/employee-list.html`
- `src/main/resources/static/profile.html`
- `src/main/resources/static/update-employee.html`
- `src/main/resources/static/assets`
- `src/main/resources/static/vercel.json`

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
- `EMS_ALLOWED_ORIGIN_PATTERNS=https://your-frontend.vercel.app`

The application now also reads Railway's `PORT` variable automatically in production.

## Database

Import `database/employee_enrollment.sql` into your MySQL database before starting the Railway service.

Production uses schema validation, so the tables must already exist.
