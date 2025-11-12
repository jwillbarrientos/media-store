import { myFetch } from "./myfetch.js";

document.addEventListener("DOMContentLoaded", () => {
    const changePasswordBtn = document.getElementById("changePasswordBtn");

    changePasswordBtn.addEventListener("click", async () => {
        const newPassword = prompt("Enter your new password:");
        if (!newPassword) return;

        try {
            const response = await myFetch(`/api/changepassword?newpassword=${encodeURIComponent(newPassword)}`)
            if (response.ok) {
                alert("Password updated successfully!");
                window.location.href = "/index.html";
            } else {
                const msg = await response.text();
                alert("Error changing password:" + msg);
            }
        } catch (err) {
            console.error(err);
            alert("An error occurred while changing password");
        }
    });
});