import { myFetch } from "./myfetch.js";

document.addEventListener("DOMContentLoaded", () => {
    const processBtn = document.getElementById("processChatBtn");
    const fileInput = document.getElementById("whatsappFile");

    processBtn.addEventListener("click", async () => {
        const file = fileInput.files[0];

        // Just send the raw file
        const formData = new FormData();
        formData.append("chatFile", file);

        try {
            const response = await myFetch("/api/processwhatsappchat", {
                method: "POST",
                body: formData
            });

            if (!response.ok) {
                alert("Error uploading whatsapp file");
                return;
            }

            alert(`✅ Uploaded correctly`);
        } catch (err) {
            console.error("Upload failed:", err);
            alert("Something went wrong!");
        }
    });
});