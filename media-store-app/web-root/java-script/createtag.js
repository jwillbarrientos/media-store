import { myFetch } from "./myfetch.js";
import { loadTags } from "./loadtags.js";

document.addEventListener("DOMContentLoaded", () => {
    const createTagBtn = document.getElementById("createTag");
    const tagList = document.getElementById("tagList");
    createTagBtn.addEventListener("click", async () => {
        // Ask for the tag name
        const tagName = prompt("Enter a name for the new tag:");
        if (!tagName) return;
        try {
            const response = await myFetch(`/api/addtag?tagName=${encodeURIComponent(tagName)}`);
            if (response.status === 200) {
                await loadTags();
                return;
            }

            if (response.status === 401) {
                return;
            }

            alert("Tag already exists");
            console.error("Tag already exists");
        } catch (err) {
            console.error("Error creating tag:", err);
        }
    });
});