import { myFetch } from "./myfetch.js";

document.addEventListener("DOMContentLoaded", () => {
    const uploadBtn = document.getElementById("uploadBtn");
    const linkInput = document.getElementById("videoUrl");

    uploadBtn.addEventListener("click", async () => {
        const videoLink = linkInput.value.trim();
        if (!videoLink) {
            alert("Please enter a video link.");
            return;
        }

        try {
            const response = await myFetch(`/api/addvideobylink?link=${encodeURIComponent(videoLink)}`);
            const result = await response.text(); // read server response message

            if (!response.ok) {
                console.error(result);
                alert("Error: " + result);
                return;
            }

            alert("Video submitted!");
            linkInput.value = "";
        } catch (err) {
            console.error("Submit error:", err);
            alert("Something went wrong!");
        }
    });
});