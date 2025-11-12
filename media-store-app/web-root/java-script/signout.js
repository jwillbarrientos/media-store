import { myFetch } from "./myfetch.js";

document.addEventListener("DOMContentLoaded", () => {
    const signOutBtn = document.getElementById("signOutBtn");
    signOutBtn.addEventListener("click", async () => {
        try {
            const response = await myFetch("/api/signout");
            if (!response.ok) {
                throw new Error("Failed to log out");
            } else {
                if (response.status === 200) {
                    window.location.href = '/index.html';
                } else {
                    alert("Log out failed");
                    console.log("Log out failed");
                }
            }
            // alert("Log out successful");
            console.log("Log out successful");
        } catch (err) {
            console.error("Error log out:", err);
        }
    });
});