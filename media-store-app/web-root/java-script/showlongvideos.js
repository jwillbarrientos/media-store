import { myFetch } from "./myfetch.js";

document.addEventListener('DOMContentLoaded', () => {
    const videosLongBtn = document.getElementById('videosLongBtn');
    if (!videosLongBtn) return;

    videosLongBtn.addEventListener('click', async () => {
        try {
            const response = await myFetch('/api/getvideosforreel?tag=bt60');
            if (!response.ok) {
                console.error(`Server returned ${response.status}`);
                return;
            }

            const videos = await response.json();
            console.log(`Fetched ${videos.length} long videos (>60s)`);

            localStorage.setItem('selectedTag', 'bt60');
            localStorage.setItem('videoList', JSON.stringify(videos));

            window.location.href = '/app/reels.html';
        } catch (err) {
            console.error('Error fetching long videos: ', err);
        }
    });
});