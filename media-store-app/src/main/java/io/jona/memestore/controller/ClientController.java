package io.jona.memestore.controller;

import io.jona.framework.JonaDb;
import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.memestore.dto.Client;
import io.jona.memestore.dto.Tag;
import io.jona.memestore.dto.Video;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ClientController {
    private final Map<String, Client> sessionCookies;

    public ClientController(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void deleteAccount(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        JonaDb.updateGeneric(Video.getDeleteFromClient(), client.getId());
        JonaDb.updateGeneric(Tag.getDeleteFromClient(), client.getId());
        boolean deleteSuccess = JonaDb.deleteSingle(client);
        log.info("Video deletion was successful: {}", deleteSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }

    public void changePassword(HttpRequest request, HttpResponse response) {
        String sessionCookie = request.getCookies().get("sessionCookie");
        Client client = sessionCookies.get(sessionCookie);
        String newPassword = request.getQueryParams().get("newpassword");
        boolean updatePasswordSuccess = JonaDb.updateGeneric(client.getUpdate(), newPassword);
        log.info("Password update was successful: {}", updatePasswordSuccess);
        response.setResponseCode(HttpCode.OK_200);
    }
}
