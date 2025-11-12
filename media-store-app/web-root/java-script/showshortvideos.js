import { myFetch } from "./myfetch.js";

document.addEventListener('DOMContentLoaded', () => {
    const videosShortBtn = document.getElementById('videosShortBtn');
    if (!videosShortBtn) return;

    videosShortBtn.addEventListener('click', async() => {
        try {
            const response = await myFetch('/api/getvideosforreel?tag=lte60');
            if (!response.ok) {
                console.error(`Server returned ${response.status}`);
                return;
            }

            const videos = await response.json();
            console.log(`Fetched ${videos.length} short videos (≤60s)`);

            localStorage.setItem('selectedTag', 'lte60');
            localStorage.setItem('videoList', JSON.stringify(videos));

            window.location.href = '/app/reels.html';
        } catch (err) {
            console.error('Error fetching short videos: ', err);
        }
    });
});