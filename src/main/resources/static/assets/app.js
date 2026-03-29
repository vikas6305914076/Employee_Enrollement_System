(function () {
    const STORAGE_KEY = "ems.session";
    const LOGIN_WARNING_KEY = "ems.pendingLoginWarning";
    const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;
    const PHONE_PATTERN = /^\d{10}$/;
    const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).+$/;
    const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    function parseJson(value) {
        try {
            return JSON.parse(value);
        } catch (error) {
            return null;
        }
    }

    function trimValue(value) {
        return String(value ?? "").trim();
    }

    function getSession() {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) {
            return null;
        }

        const session = parseJson(raw);
        if (!session || !session.accessToken || !session.user) {
            localStorage.removeItem(STORAGE_KEY);
            return null;
        }

        if (session.expiresAt && Date.parse(session.expiresAt) <= Date.now()) {
            localStorage.removeItem(STORAGE_KEY);
            return null;
        }

        return session;
    }

    function storeSession(loginResponse) {
        const session = {
            accessToken: loginResponse.accessToken,
            tokenType: loginResponse.tokenType,
            expiresAt: loginResponse.expiresAt,
            user: loginResponse.user
        };

        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));

        if (loginResponse.loginWarningMessage) {
            sessionStorage.setItem(LOGIN_WARNING_KEY, loginResponse.loginWarningMessage);
        } else {
            sessionStorage.removeItem(LOGIN_WARNING_KEY);
        }

        return session;
    }

    function clearSession() {
        localStorage.removeItem(STORAGE_KEY);
        sessionStorage.removeItem(LOGIN_WARNING_KEY);
    }

    function redirectToLogin() {
        clearSession();
        window.location.href = "/login.html";
    }

    function redirectToDefaultPage() {
        const session = getSession();
        if (!session) {
            window.location.href = "/login.html";
            return;
        }

        if (session.user.userRole === "ADMIN") {
            window.location.href = "/employee-list.html";
            return;
        }

        window.location.href = "/profile.html";
    }

    function requireAuth(roles) {
        const session = getSession();
        if (!session) {
            window.location.href = "/login.html";
            return null;
        }

        if (roles && roles.length && !roles.includes(session.user.userRole)) {
            if (session.user.userRole === "ADMIN") {
                window.location.href = "/employee-list.html";
            } else {
                window.location.href = "/profile.html";
            }
            return null;
        }

        return session;
    }

    function buildHeaders(options) {
        const headers = Object.assign({}, options && options.headers ? options.headers : {});

        if (!headers["Content-Type"] && options && options.body !== undefined) {
            headers["Content-Type"] = "application/json";
        }

        if (!options || options.auth !== false) {
            const session = getSession();
            if (session && session.accessToken) {
                headers.Authorization = `Bearer ${session.accessToken}`;
            }
        }

        return headers;
    }

    function createUnauthorizedResult() {
        const message = "Authentication is required to access this resource";
        return {
            response: new Response("", {
                status: 401,
                statusText: "Unauthorized"
            }),
            payload: {
                message,
                errors: [{ field: null, message }]
            }
        };
    }

    async function request(url, options) {
        const requestOptions = options || {};

        if (requestOptions.auth !== false) {
            const session = getSession();
            if (!session || !session.accessToken) {
                redirectToLogin();
                return createUnauthorizedResult();
            }
        }

        const response = await fetch(url, {
            method: requestOptions.method || "GET",
            headers: buildHeaders(requestOptions),
            body: requestOptions.body !== undefined ? JSON.stringify(requestOptions.body) : undefined
        });

        const text = await response.text();
        const payload = text ? parseJson(text) || { message: text } : null;

        if (response.status === 401 && requestOptions.auth !== false) {
            redirectToLogin();
        }

        return { response, payload };
    }

    function extractError(payload, fallbackMessage) {
        if (payload && Array.isArray(payload.errors) && payload.errors.length) {
            return payload.errors
                .map((error) => error.field ? `${error.field}: ${error.message}` : error.message)
                .join(", ");
        }

        return (payload && payload.message) || fallbackMessage;
    }

    function extractFieldErrors(payload) {
        if (!payload || !Array.isArray(payload.errors)) {
            return [];
        }

        return payload.errors
            .filter((error) => error && error.field && error.message)
            .map((error) => ({
                field: error.field,
                message: error.message
            }));
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    function getInitials(value) {
        const normalized = String(value ?? "").trim();
        if (!normalized) {
            return "--";
        }

        const parts = normalized.split(/\s+/).filter(Boolean);
        if (parts.length === 1) {
            return parts[0].slice(0, 2).toUpperCase();
        }

        return `${parts[0][0] || ""}${parts[1][0] || ""}`.toUpperCase();
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2
        }).format(value || 0);
    }

    function formatDate(value) {
        if (!value) {
            return "-";
        }

        return new Intl.DateTimeFormat("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }).format(new Date(value));
    }

    function formatDateTime(value) {
        if (!value) {
            return "-";
        }

        return new Intl.DateTimeFormat("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(new Date(value));
    }

    function padNumber(value) {
        return String(value).padStart(2, "0");
    }

    function toLocalDateInputValue(date) {
        const source = date instanceof Date ? date : new Date(date);
        return `${source.getFullYear()}-${padNumber(source.getMonth() + 1)}-${padNumber(source.getDate())}`;
    }

    function parseDateInput(value) {
        const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(String(value || ""));
        if (!match) {
            return null;
        }

        const year = Number(match[1]);
        const month = Number(match[2]);
        const day = Number(match[3]);
        const date = new Date(year, month - 1, day);

        if (
            date.getFullYear() !== year
            || date.getMonth() !== month - 1
            || date.getDate() !== day
        ) {
            return null;
        }

        date.setHours(0, 0, 0, 0);
        return date;
    }

    function isFutureDate(value) {
        const parsedDate = parseDateInput(value);
        if (!parsedDate) {
            return false;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return parsedDate.getTime() > today.getTime();
    }

    function clearFormErrors(form) {
        if (!form) {
            return;
        }

        form.querySelectorAll("[data-field-error='true']").forEach((node) => node.remove());
        form.querySelectorAll(".field.invalid").forEach((field) => field.classList.remove("invalid"));

        Array.from(form.elements || []).forEach((field) => {
            if (field && typeof field.setCustomValidity === "function") {
                field.setCustomValidity("");
            }
        });
    }

    function getOrCreateFieldErrorNode(fieldWrapper) {
        let errorNode = fieldWrapper.querySelector("[data-field-error='true']");
        if (errorNode) {
            return errorNode;
        }

        errorNode = document.createElement("span");
        errorNode.className = "field-error";
        errorNode.setAttribute("data-field-error", "true");
        fieldWrapper.appendChild(errorNode);
        return errorNode;
    }

    function applyFieldErrors(form, errors) {
        if (!form || !Array.isArray(errors) || !errors.length) {
            return;
        }

        const fieldsByName = {};
        Array.from(form.elements || []).forEach((field) => {
            if (field && field.name) {
                fieldsByName[field.name] = field;
            }
        });

        errors.forEach((error) => {
            const field = fieldsByName[error.field];
            if (!field) {
                return;
            }

            const message = error.message || "Invalid value";
            field.setCustomValidity(message);

            const wrapper = field.closest(".field");
            if (!wrapper) {
                return;
            }

            wrapper.classList.add("invalid");
            getOrCreateFieldErrorNode(wrapper).textContent = message;
        });
    }

    function focusFirstInvalidField(form) {
        if (!form) {
            return;
        }

        const firstInvalidField = Array.from(form.elements || []).find((field) => (
            field
            && typeof field.reportValidity === "function"
            && !field.checkValidity()
        ));

        if (firstInvalidField) {
            firstInvalidField.reportValidity();
        }
    }

    function validateRequiredText(errors, field, label, value, maxLength, minLength) {
        if (!value) {
            errors.push({ field, message: `${label} is required` });
            return;
        }

        if (minLength && value.length < minLength) {
            errors.push({ field, message: `${label} must be at least ${minLength} characters` });
            return;
        }

        if (maxLength && value.length > maxLength) {
            errors.push({ field, message: `${label} must not exceed ${maxLength} characters` });
        }
    }

    function validateEmployeePayload(payload, options) {
        const mode = options && options.mode ? options.mode : "create";
        const errors = [];

        if (mode === "create") {
            validateRequiredText(errors, "firstName", "First name", trimValue(payload.firstName), 50);
            validateRequiredText(errors, "lastName", "Last name", trimValue(payload.lastName), 50);

            const username = trimValue(payload.username);
            validateRequiredText(errors, "username", "Username", username, 30, 4);
            if (username && !USERNAME_PATTERN.test(username)) {
                errors.push({
                    field: "username",
                    message: "Username may contain only letters, numbers, dots, underscores, and hyphens"
                });
            }

            const email = trimValue(payload.email);
            validateRequiredText(errors, "email", "Email", email, 100);
            if (email && !EMAIL_PATTERN.test(email)) {
                errors.push({ field: "email", message: "Email format is invalid" });
            }
        }

        const phone = trimValue(payload.phone);
        validateRequiredText(errors, "phone", "Phone number", phone);
        if (phone && !PHONE_PATTERN.test(phone)) {
            errors.push({ field: "phone", message: "Phone number must be exactly 10 digits" });
        }

        validateRequiredText(errors, "department", "Department", trimValue(payload.department), 100);
        validateRequiredText(errors, "role", "Role", trimValue(payload.role), 100);
        validateRequiredText(errors, "address", "Address", trimValue(payload.address), 255);

        const salary = Number(payload.salary);
        if (payload.salary === "" || payload.salary === null || payload.salary === undefined || Number.isNaN(salary)) {
            errors.push({ field: "salary", message: "Salary is required" });
        } else if (salary <= 0) {
            errors.push({ field: "salary", message: "Salary must be greater than zero" });
        }

        if (mode === "create") {
            const joiningDate = trimValue(payload.joiningDate);
            if (!joiningDate) {
                errors.push({ field: "joiningDate", message: "Joining date is required" });
            } else if (!parseDateInput(joiningDate)) {
                errors.push({ field: "joiningDate", message: "Joining date is invalid" });
            } else if (isFutureDate(joiningDate)) {
                errors.push({ field: "joiningDate", message: "Joining date cannot be in the future" });
            }

            const password = String(payload.password ?? "");
            if (!password) {
                errors.push({ field: "password", message: "Password is required" });
            } else if (password.length < 8 || password.length > 64) {
                errors.push({ field: "password", message: "Password must be between 8 and 64 characters" });
            } else if (!PASSWORD_PATTERN.test(password)) {
                errors.push({
                    field: "password",
                    message: "Password must contain upper, lower, number, and special character"
                });
            }

            const userRole = trimValue(payload.userRole).toUpperCase();
            if (!userRole) {
                errors.push({ field: "userRole", message: "User role is required" });
            } else if (!["ADMIN", "USER"].includes(userRole)) {
                errors.push({ field: "userRole", message: "User role must be ADMIN or USER" });
            }
        }

        return errors.filter((error, index, list) => list.findIndex((item) => item.field === error.field) === index);
    }

    function validateEmployeeFilters(payload) {
        const errors = [];
        const minSalary = trimValue(payload.minSalary);
        const maxSalary = trimValue(payload.maxSalary);

        if (minSalary && (Number.isNaN(Number(minSalary)) || Number(minSalary) < 0)) {
            errors.push({ field: "minSalary", message: "Minimum salary must be zero or greater" });
        }

        if (maxSalary && (Number.isNaN(Number(maxSalary)) || Number(maxSalary) < 0)) {
            errors.push({ field: "maxSalary", message: "Maximum salary must be zero or greater" });
        }

        if (
            minSalary
            && maxSalary
            && !Number.isNaN(Number(minSalary))
            && !Number.isNaN(Number(maxSalary))
            && Number(minSalary) > Number(maxSalary)
        ) {
            errors.push({ field: "minSalary", message: "Minimum salary cannot be greater than maximum salary" });
            errors.push({ field: "maxSalary", message: "Maximum salary cannot be less than minimum salary" });
        }

        return errors;
    }

    function downloadTextFile(filename, content, contentType) {
        const blob = new Blob([content], { type: contentType || "text/plain;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");

        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    }

    function getPopupHost() {
        let host = document.getElementById("alertPopupHost");
        if (host) {
            return host;
        }

        host = document.createElement("div");
        host.id = "alertPopupHost";
        host.className = "alert-popup-host";
        document.body.appendChild(host);
        return host;
    }

    function showPopupAlert(message, options) {
        if (!message) {
            return;
        }

        const config = options || {};
        const popup = document.createElement("div");
        const title = document.createElement("strong");
        const body = document.createElement("span");

        popup.className = `alert-popup ${config.tone || "danger"}${config.blink ? " blink" : ""}`;
        title.textContent = config.title || "Login inactivity warning";
        body.textContent = message;
        popup.appendChild(title);
        popup.appendChild(body);
        getPopupHost().appendChild(popup);

        window.requestAnimationFrame(() => {
            popup.classList.add("show");
        });

        window.setTimeout(() => {
            popup.classList.remove("show");
            window.setTimeout(() => {
                popup.remove();
            }, 240);
        }, config.duration || 9000);
    }

    function showPendingLoginWarning() {
        const message = sessionStorage.getItem(LOGIN_WARNING_KEY);
        if (!message) {
            return;
        }

        sessionStorage.removeItem(LOGIN_WARNING_KEY);
        showPopupAlert(message, {
            title: "Login inactivity warning",
            tone: "danger",
            blink: true
        });
    }

    window.EmsApp = {
        applyFieldErrors,
        clearFormErrors,
        clearSession,
        downloadTextFile,
        escapeHtml,
        extractError,
        extractFieldErrors,
        focusFirstInvalidField,
        formatCurrency,
        formatDate,
        formatDateTime,
        getInitials,
        getSession,
        isFutureDate,
        redirectToDefaultPage,
        redirectToLogin,
        request,
        requireAuth,
        showPendingLoginWarning,
        showPopupAlert,
        storeSession,
        toLocalDateInputValue,
        validateEmployeeFilters,
        validateEmployeePayload
    };
}());
