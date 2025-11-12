package io.jona.memestore.controller;

import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.JonaDb;
import io.jona.framework.http.MimeType;
import io.jona.memestore.AppProps;
import io.jona.memestore.Platform;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.Video;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class VideoController {
    private final Map<String, Client> sessionCookies;

    public VideoController(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void addVideoByLink(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        Video video = new Video(request.getQueryParams().get("link"), Video.State.SUBMITTED, client.getId());
        if (Video.notYetInserted(video)) {
            JonaDb.insertSingle(video);
        }
        response.setResponseCode(HttpCode.OK_200);
    }

    public void addVideoByFile(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        String body = parseBody(request.getBody());
        String[] links = body.split("\n");
        for (String link : links) {
            Video video = new Video(link, Video.State.SUBMITTED, client.getId());
            if (Video.notYetInserted(video)) {
                JonaDb.insertSingle(video);
            }
        }
        response.setResponseCode(HttpCode.OK_200);
    }

    public void deleteVideo(HttpRequest request, HttpResponse response) {
        Video video = Video.findById(Long.parseLong(request.getQueryParams().get("id")));
        if (video.existClientWithThisVideo()) {
            Path path = Paths.get(video.getPath()).toAbsolutePath();
            try {
                Files.delete(path);
                log.info("Video deleted inside the server, any client is using it");
            } catch (IOException e) {
                log.error("Error deleting video inside the server: ", e);
            }
        }
        boolean deleteSuccess = JonaDb.deleteSingle(video);
        log.info("Video deletion was successful: {}", deleteSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }

    public void downloadVideo(HttpRequest request, HttpResponse response) {
        long id = Long.parseLong(request.getQueryParams().get("id"));
        Video video = Video.findById(id);
        File file = new File(video.getPath());
        if (!file.exists()) {
            response.setResponseCode(HttpCode.NOT_FOUND_404);
            return;
        }
        try {
            String extension = "." + video.getName().split("\\.")[1];
            MimeType mimeType = MimeType.getMimeForExtension(extension);
            response.setContentType(mimeType);
            response.setBody(Files.readAllBytes(Paths.get(video.getPath())));
            response.setContentDisposition("attachment; filename=\"" + file.getName() + "\"");
            response.setResponseCode(HttpCode.OK_200);
        } catch (IOException e) {
            log.error("Error trying to download the video: ", e);
        }
    }

    private String parseBody(String body) {
        Pattern urlPattern = Pattern.compile("(https?://[\\w./?=&%-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(body);
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group(1);
            Platform platform = Platform.whatPlatformIs(url);
            if (platform.equals(Platform.YOU_TUBE)) {
                int qIndex = url.indexOf('?');
                if (qIndex != -1) {
                    String base = url.substring(0, qIndex);
                    String query = url.substring(qIndex + 1);
                    int ampIndex = query.indexOf('&');
                    if (ampIndex != -1) {
                        query = query.substring(0, ampIndex);
                    }
                    links.add(base + "?" + query);
                }
            } else if (!platform.equals(Platform.INVALID_PLATFORM)){
                int qIndex = url.indexOf('?');
                if (qIndex != -1) {
                    links.add(url.substring(0, qIndex));
                }
            }
        }
        return String.join("\n", links);
    }
}
