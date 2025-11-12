import { myFetch } from "./myfetch.js";
import { createDeleteButton } from "./deletetag.js";
import { createEditButton } from "./edittag.js";

export async function loadTags() {
    try {
        const response = await myFetch("/api/loadtags");
        if (!response.ok) {
            throw new Error("Failed to load tags");
        }

        const tags = await response.json();
        const tagList = document.getElementById("tagList");
        tagList.innerHTML = "";

        tags.forEach(tag => {
            if (!tag) return;
            const tagItem = document.createElement("div");

            // ✅ Create the tag name as a simple clickable text
            const nameSpan = document.createElement("span");
            nameSpan.textContent = tag.name;
            nameSpan.style.cursor = "pointer";

            nameSpan.addEventListener("click", async () => {
                try {
                    const response = await myFetch(`/api/getvideosforreel?tag=${encodeURIComponent(tag.id)}`);
                    if (!response.ok) {
                        console.error(`Failed to fetch videos for tag: ${tag.name}`);
                        return;
                    }

                    const videos = await response.json();
                    console.log(`Fetched ${videos.length} videos for tag "${tag.name}"`);

                    localStorage.setItem("selectedTag", tag.id);
                    localStorage.setItem("videoList", JSON.stringify(videos));

                    window.location.href = `/app/reels.html`;
                } catch (err) {
                    console.error("Error fetching videos by tag: ", err);
                }
            });

            const editBtn = createEditButton(tag.id, tag.name, loadTags);
            const deleteBtn = createDeleteButton(tag.id, loadTags);

            tagItem.appendChild(nameSpan);
            tagItem.appendChild(editBtn);
            tagItem.appendChild(deleteBtn);

            tagList.appendChild(tagItem);
        });
    } catch (err) {
        console.error("Error getting tags:", err);
    }
}

document.addEventListener("DOMContentLoaded", loadTags);
