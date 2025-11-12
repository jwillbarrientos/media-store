package io.jona.memestore.controller;

import com.google.gson.Gson;
import io.jona.framework.JonaDb;
import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.http.MimeType;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.VideoTag;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class VideoTagController {
    private final Map<String, Client> sessionCookies;

    public VideoTagController(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void getTagsForVideo(HttpRequest request, HttpResponse response) {
        Long id = Long.parseLong(request.getQueryParams().get("id"));
        List<VideoTag> listTagsForVideo = VideoTag.getVideoTagsByVideoId(id);
        Gson gson = new Gson();
        String json = gson.toJson(listTagsForVideo);
        response.setContentType(MimeType.APPLICATION_JSON, "UTF-8");
        response.setResponseCode(HttpCode.OK_200);
        response.setBody(json.getBytes());
    }

    public void addTagToVideo(HttpRequest request, HttpResponse response) {
        long videoId = Long.parseLong(request.getQueryParams().get("videoId"));
        long tagId = Long.parseLong(request.getQueryParams().get("tagId"));
        VideoTag videoTag = new VideoTag(videoId, tagId);
        boolean insertSuccess = JonaDb.insertSingle(videoTag);
        log.info("Tag checking was made correctly: {}", insertSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }

    public void deleteTagVideo(HttpRequest request, HttpResponse response) {
        long videoId = Long.parseLong(request.getQueryParams().get("videoId"));
        long tagId = Long.parseLong(request.getQueryParams().get("tagId"));
        VideoTag videoTag = new VideoTag(videoId, tagId);
        boolean deleteSuccess = JonaDb.deleteSingle(videoTag);
        log.info("Tag unchecking was made correctly: {}", deleteSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }
}
