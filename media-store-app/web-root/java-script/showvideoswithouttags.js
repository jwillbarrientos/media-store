import { myFetch } from "./myfetch.js";

document.addEventListener('DOMContentLoaded', () => {
    const videosWithoutTagsBtn = document.getElementById('videosWithoutTagsBtn');
    if (!videosWithoutTagsBtn) return;

    videosWithoutTagsBtn.addEventListener('click', async() => {
        try {
            const response = await myFetch('/api/getvideosforreel?tag=without');
            if (!response.ok) {
                console.error(`Server returned ${response.status}`);
                return;
            }

            const videos = await response.json();
            console.log(`Fetched ${videos.length} videos (videosWithoutTagsBtn)`);

            localStorage.setItem('selectTag', 'videosWithoutTagsBtn');
            localStorage.setItem('videoList', JSON.stringify(videos));

            window.location.href = '/app/reels.html';
        } catch (err) {
            console.error('Error fetching videos without tags: ', err)
        }
    });
});