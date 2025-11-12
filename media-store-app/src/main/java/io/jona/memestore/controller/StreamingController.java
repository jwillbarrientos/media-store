package io.jona.memestore.controller;

import com.google.gson.Gson;
import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.http.MimeType;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.Video;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StreamingController {
    private final Map<String, Client> sessionCookies;

    public StreamingController(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void loadVideos(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        List<Video> listVideos = Video.getVideosDownloaded(client.getId());
        Gson gson = new Gson();
        String json = gson.toJson(listVideos);
        response.setContentType(MimeType.APPLICATION_JSON, "UTF-8");
        response.setResponseCode(HttpCode.OK_200);
        response.setBody(json.getBytes());
    }

    public void streamVideos(HttpRequest request, HttpResponse response) {
        String id = request.getQueryParams().get("id");
        Video video = Video.findById(Long.parseLong(id));
        Path path = Path.of(video.getPath());
        try {
            response.setResponseCode(HttpCode.PARTIAL_CONTENT_206);
            String extension = video.getPath().substring(video.getPath().lastIndexOf("."));
            MimeType mimeType = MimeType.getMimeForExtension(extension);
            response.setContentType(mimeType);
            long end = Math.min(request.getRangeStart() + HttpResponse.CHUNK_SIZE_BYTES - 1, path.toFile().length() - 1);
            response.setBodyWithRange(path, request.getRangeStart(), end, path.toFile().length());
        } catch (IOException e) {
            response.setResponseCode(HttpCode.CONFLICT_409);
            log.error("Error streaming the video: ", e);
        }
    }

    public void getVideosForReel(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        String tag = request.getQueryParams().get("tag");
        List<Video> listVideos;
        if (tag.equals("all"))
            listVideos = Video.getAllVideosByClient(client);
        else if (tag.equals("lte60"))
            listVideos = Video.getShortVideosByClient(client);
        else if (tag.equals("bt60"))
            listVideos = Video.getLongVideosByClient(client);
        else if (tag.equals("with"))
            listVideos = Video.getVideosWithTags(client);
        else if (tag.equals("without"))
            listVideos = Video.getVideosWithoutTags(client);
        else
            listVideos = Video.getVideosWithSpecificTag(tag);
        Gson gson = new Gson();
        String json = gson.toJson(listVideos);
        response.setContentType(MimeType.APPLICATION_JSON, "UTF-8");
        response.setResponseCode(HttpCode.OK_200);
        response.setBody(json.getBytes());
    }
}
