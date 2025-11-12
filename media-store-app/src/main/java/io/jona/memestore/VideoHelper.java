package io.jona.memestore;

import io.jona.framework.JonaDb;
import io.jona.memestore.dto.Video;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

@Slf4j
public class VideoHelper {
    public static Runnable backgroundDownloader() {
        return () -> {
            while(true) {
                Video video = Video.nextVideoToDownload();
                if (video != null) {
                    String videoPath = DownloadVideo.downloadVideo(video.getLink());
                    int durationSecondsVideo = getSeconds(video.getLink());
                    if (videoPath == null) {
                        video.setVideoState(Video.State.ERROR_DOWNLOADING);
                    } else {
                        video.setName(Paths.get(videoPath).getFileName().toString());
                        video.setPath(videoPath);
                        video.setDurationSeconds(durationSecondsVideo);
                        video.setFileSize((int) (new File(videoPath).length()));
                        video.setVideoState(Video.State.DOWNLOADED);
                    }
                    JonaDb.updateSingle(video);
                    try {
                        log.info("Waiting {} time to download next video", durationSecondsVideo);
                        Thread.sleep(durationSecondsVideo);
                    } catch (InterruptedException e) {
                        log.error("Error waiting the next video to download: ", e);
                    }
                    //break;
                } else {
                    try {
                        log.info("There are no videos to download, sleeping 5 seconds");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error("Error trying to sleep: ", e);
                    }
                }
            }
        };
    }

    public static int getSeconds(String link) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.equals("win")) {
                process = Runtime.getRuntime().exec(new String[]{"./downloaders/yt-dlp.exe", "-j", link, "--skip-download", "--print", "%(duration)s"});
            } else {
                process = Runtime.getRuntime().exec(new String[]{"downloaders/yt-dlp", "-j", link, "--skip-download", "--print", "%(duration)s"});
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null && line.matches("^[0-9]+([0-9]\\.[0-9]+)?$")) {
                process.destroyForcibly();
                line = line.trim();
                double d = Double.parseDouble(line);
                return (int) Math.round(d);
            }
        } catch (IOException e) {
            log.error("Failed to parse duration: ", e);
        }
        return 0;
    }
}
