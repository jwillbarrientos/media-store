import { myFetch } from "./myfetch.js";

export function createEditButton(tagId, currentName, reloadCallback) {
    const editBtn = document.createElement("button");
    editBtn.textContent = "Edit";

    editBtn.classList.add("tag-action-btn", "edit-btn");

    editBtn.addEventListener("click", async () => {
        const newName = prompt("Enter new tag name:", currentName);
        if (!newName) return;
        try {
            const response = await myFetch(`/api/edittag?id=${tagId}&name=${encodeURIComponent(newName)}`);

            const serverText = await response.text();
            console.log("Edit response:", response.status, serverText);
            if (response.ok) {
                reloadCallback();
            } else {
                alert("Error updating tag: " + serverText);
            }
        } catch (err) {
            console.error("Network error while editing:", err);
        }
    });
    return editBtn;
}