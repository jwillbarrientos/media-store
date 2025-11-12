package io.jona.memestore.controller;

import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.http.MimeType;

import java.time.LocalDateTime;

public class TestController {
    public void getDate(HttpRequest request, HttpResponse response) {
        response.setResponseCode(HttpCode.OK_200);
        response.setContentType(MimeType.TEXT_PLAIN);
        response.setBody(LocalDateTime.now().toString().getBytes());
    }

    public void getDateAndCookies(HttpRequest request, HttpResponse response) {
        response.setResponseCode(HttpCode.OK_200);
        response.setContentType(MimeType.TEXT_PLAIN);
        response.setBody(LocalDateTime.now()+ " - " + request.getHeaders().get("Cookie"));
    }

    public void setCookie(HttpRequest request, HttpResponse response) {
        response.setResponseCode(HttpCode.NO_CONTENT_204);
        response.addCookie("hola", "myCookie");
        response.addCookie("hola2", "myCookie2");
    }

    public void deleteCookie(HttpRequest request, HttpResponse response) {
        response.setResponseCode(HttpCode.NO_CONTENT_204);
        response.deleteCookies();
    }
}
