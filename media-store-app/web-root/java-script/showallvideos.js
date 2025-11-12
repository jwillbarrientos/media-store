import { myFetch } from "./myfetch.js";

document.addEventListener('DOMContentLoaded', () => {
    const showAllBtn = document.getElementById('showAllBtn');
    if (!showAllBtn) return;

    showAllBtn.addEventListener('click', async() => {
        try {
            const response = await myFetch('/api/getvideosforreel?tag=all');
            if (!response.ok) {
                console.error(`Server returned ${response.status}`);
                return;
            }

            const videos = await response.json();
            console.log(`Fetched ${videos.length} videos (all)`);

            localStorage.setItem('selectTag', 'all');
            localStorage.setItem('videoList', JSON.stringify(videos));

            window.location.href = '/app/reels.html';
        } catch (err) {
            console.error('Error fetching all videos: ', err)
        }
    });
});