package io.jona.memestore.controller;

import com.google.gson.Gson;
import io.jona.framework.*;
import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.http.MimeType;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TagController {
    private final Map<String, Client> sessionCookies;

    public TagController(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void addTag(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        response.setContentType(MimeType.APPLICATION_JSON, "UTF-8");
        Tag tag = new Tag(request.getQueryParams().get("tagName"), client.getId());
        Gson gson = new Gson();
        String json = gson.toJson(tag);
        boolean alreadyExists = Tag.alreadyExists(tag);
        if (alreadyExists) {
            log.info("Tag already exists");
            response.setResponseCode(HttpCode.CONFLICT_409);
            return;
        }
        boolean insertSuccess = JonaDb.insertSingle(tag);
        log.info("Tag creation was successful: {}", insertSuccess);
        response.setResponseCode(HttpCode.OK_200);
        response.setBody(json.getBytes());

    }

    public void editTag(HttpRequest request, HttpResponse response) {
        Tag tag = Tag.findById(Long.parseLong(request.getQueryParams().get("id")));;
        tag.setName(request.getQueryParams().get("name"));
        boolean updateSuccess = JonaDb.updateSingle(tag);
        log.info("Tag update was successful: {}", updateSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }

    public void deleteTag(HttpRequest request, HttpResponse response) {
        Tag tag = Tag.findById(Long.parseLong(request.getQueryParams().get("id")));
        boolean deleteSuccess = JonaDb.deleteSingle(tag);
        log.info("Tag deletion was successful: {}", deleteSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }

    public void listTags(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        List<Tag> tags = Tag.getTagsByClient(client);
        Gson gson = new Gson();
        String json = gson.toJson(tags);
        response.setContentType(MimeType.APPLICATION_JSON, "UTF-8");
        response.setResponseCode(HttpCode.OK_200);
        response.setBody(json.getBytes());
    }
}
