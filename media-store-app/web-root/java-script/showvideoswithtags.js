import { myFetch } from "./myfetch.js";

document.addEventListener('DOMContentLoaded', () => {
    const videosWithTagsBtn = document.getElementById('videosWithTagsBtn');
    if (!videosWithTagsBtn) return;

    videosWithTagsBtn.addEventListener('click', async() => {
        try {
            const response = await myFetch('/api/getvideosforreel?tag=with');

            if (!response.ok) {
                console.error(`Server returned ${response.status}`);
                return;
            }

            const videos = await response.json();
            console.log(`Fetched ${videos.length} videos (with)`);

            localStorage.setItem('selectTag', 'with');
            localStorage.setItem('videoList', JSON.stringify(videos));

            window.location.href = '/app/reels.html';
        } catch (err) {
            console.error('Error fetching videos with tags: ', err)
        }
    });
});