(function () {
    window.EMS_CONFIG = Object.assign(
        {
            // Set this to your Railway backend URL when the frontend is deployed on Vercel.
            // Example: "https://your-backend.up.railway.app"
            apiBaseUrl: ""
        },
        window.EMS_CONFIG || {}
    );
}());
