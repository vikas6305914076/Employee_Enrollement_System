(function () {
    window.EMS_CONFIG = Object.assign(
        {
            // Leave this empty when using the included Vercel proxy configuration.
            // If you deploy the frontend without the provided vercel.json, set this to:
            // "https://employeeenrollementsystem-production.up.railway.app"
            apiBaseUrl: ""
        },
        window.EMS_CONFIG || {}
    );
}());
