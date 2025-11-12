import { myFetch } from "./myfetch.js";

export async function loadVideos() {
    try {
        const response = await myFetch("/api/loadvideos");
        if (!response.ok) {
            throw new Error("Failed to load videos");
        }

        const videos = await response.json();
        const videoList = document.getElementById("videoContainer");
        videoList.innerHTML = "";

        videos.forEach(video => {
            if (!video) return;

            const videoItem = document.createElement("div");
            videoItem.classList.add("video-item");

            const nameSpan = document.createElement("span");
            nameSpan.textContent = video.name + " ";

            const videoPlayer = document.createElement("video");
            videoPlayer.controls = true;

            // The browser will request the video from this URL
            videoPlayer.src = "/api/streamingvideos?id=" + encodeURIComponent(video.id);

            // ---- Buttons container ----
            const btnContainer = document.createElement("div");
            btnContainer.style.display = "flex";
            btnContainer.style.gap = "10px";
            btnContainer.style.marginTop = "10px";
            btnContainer.style.justifyContent = "center";

            const buttonStyle = {
                background: "rgba(255,255,255,0.2)",
                color: "#fff",
                border: "none",
                borderRadius: "12px",
                padding: "8px 12px",
                fontSize: "18px",
                cursor: "pointer",
                transition: "background 0.3s",
            };

            function styleButton(btn) {
                btn.style.background = "rgba(255,255,255,0.2)";
                btn.style.color = "#fff";
                btn.style.border = "none";
                btn.style.borderRadius = "12px";
                btn.style.padding = "8px 12px";
                btn.style.fontSize = "18px";
                btn.style.cursor = "pointer";
                btn.style.transition = "background 0.3s";
                btn.addEventListener("mouseover", () => btn.style.background = "rgba(255,255,255,0.5)");
                btn.addEventListener("mouseout", () => btn.style.background = "rgba(255,255,255,0.2)");
            }

            const deleteBtn = document.createElement("button");
            deleteBtn.textContent = "🗑️";
            styleButton(deleteBtn);
            deleteBtn.addEventListener("click", async () => {
                const videoId = video.id; // ✅ capturar ID antes de remover el elemento
                if (!videoId) return;
                if (!confirm("Are you sure you want to delete this video?")) return;
                try {
                    const res = await myFetch(`/api/deletevideo?id=${encodeURIComponent(video.id)}`, { method: "GET" });
                    if (!res.ok) throw new Error (`Failed to delete (${res.status}`);
                    videoItem.remove();
                } catch (err) {
                    console.error(err);
                    alert("Error deleting video");
                }
            });

            const downloadBtn = document.createElement("button");
            downloadBtn.textContent = "⬇️";
            styleButton(downloadBtn);
            downloadBtn.addEventListener("click", async () => {
                try {
                    const res = await fetch(`/api/downloadvideo?id=${encodeURIComponent(video.id)}`);
                    if (!res.ok) { throw new Error("Error downloading the video") }

                    const blob = await res.blob();

                    const url = URL.createObjectURL(blob);
                    const a = document.createElement("a");

                    const cd = res.headers.get("Content-Disposition");
                    let fileName = "video.mp4";
                    if (cd && cd.includes("filename=")) {
                        fileName = cd.split("filename=")[1].replace(/"/g, "");
                    }

                    a.href = url;
                    a.download = fileName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    URL.revokeObjectURL(url);
                } catch (err) {
                    console.error("Error downloading the video: ", err);
                    alert("The video can't be downloaded");
                }
            });

            const actionBtn = document.createElement("button");
            actionBtn.textContent = "⭐";
            styleButton(actionBtn)
            actionBtn.addEventListener("click", async () => {
                const videoId = video.id;
                try {
                    // 1. Cargar todos los tags disponibles
                    const allTagsRes = await myFetch("/api/loadtags"); // endpoint general, no "inreelspage"
                    if (!allTagsRes.ok) throw new Error("Failed to load all tags");
                    const allTags = await allTagsRes.json();

                    // 2. Cargar tags asignados a este video
                    const videoTagsRes = await myFetch(`/api/gettagsforvideo?id=${videoId}`);
                    if (!videoTagsRes.ok) throw new Error("Failed to load video tags");
                    const videoTags = await videoTagsRes.json();
                    const assignedTagIds = videoTags.map(t => t.tagId);

                    // 3. Llenar el modal
                    const tagList = document.getElementById("tag-list");
                    tagList.innerHTML = "";

                    allTags.forEach(tag => {
                        const wrapper = document.createElement("div");
                        const checkbox = document.createElement("input");
                        checkbox.type = "checkbox";
                        checkbox.id = `tag-${tag.id}`;
                        checkbox.checked = assignedTagIds.includes(tag.id);

                        checkbox.addEventListener("change", async () => {
                            try {
                                if (checkbox.checked) {
                                    const res = await myFetch(`/api/addtagtovideo?videoId=${videoId}&tagId=${tag.id}`);
                                    if (!res.ok) throw new Error("Failed to add tag");
                                } else {
                                    const res = await myFetch(`/api/deletetagvideo?videoId=${videoId}&tagId=${tag.id}`);
                                    if (!res.ok) throw new Error("Failed to remove tag");
                                }
                            } catch (err) {
                                console.error(err);
                                alert("Error updating tag");
                                checkbox.checked = !checkbox.checked;
                            }
                        });

                        const label = document.createElement("label");
                        label.htmlFor = checkbox.id;
                        label.textContent = " " + tag.name;

                        wrapper.appendChild(checkbox);
                        wrapper.appendChild(label);
                        tagList.appendChild(wrapper);
                    });

                    // 4. Mostrar modal
                    const tagModal = document.getElementById("tag-modal");
                    tagModal.style.display = "flex";

                    // Cerrar modal
                    document.getElementById("close-modal").onclick = () => {
                        tagModal.style.display = "none";
                    };

                } catch (err) {
                    console.error(err);
                    alert("Failed to load tags");
                }
            });

            btnContainer.appendChild(deleteBtn);
            btnContainer.appendChild(downloadBtn);
            btnContainer.appendChild(actionBtn);

            videoItem.appendChild(nameSpan);
            videoItem.appendChild(videoPlayer);
            videoItem.appendChild(btnContainer);

            videoList.appendChild(videoItem);
        });

    } catch (err) {
        console.error("Error loading videos:", err);
    }
}

document.addEventListener("DOMContentLoaded", loadVideos);