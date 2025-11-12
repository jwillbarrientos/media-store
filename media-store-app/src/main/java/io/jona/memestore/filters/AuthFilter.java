package io.jona.memestore.filters;

import io.jona.framework.http.HttpCode;
import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.memestore.dto.Client;
import java.util.HashMap;
import java.util.Map;

public class AuthFilter {
    private final Map<String, Client> sessionCookies;

    public AuthFilter(HashMap<String, Client> sessionCookies) {
        this.sessionCookies = sessionCookies;
    }

    public void onlyAuthenticated(HttpRequest request, HttpResponse response) {
        boolean sessionCookiePresent = sessionCookies.containsKey(request.getCookies().get("sessionCookie"));
        if (sessionCookiePresent) {
            return;
        }

        response.truncateProcessing();
        if (request.canonicalPath().startsWith("/app/")) {
            response.setResponseCode(HttpCode.FOUND_302);
            response.redirect("/");
            return;
        }

        response.setResponseCode(HttpCode.UNAUTHORIZED_401);
        response.setBody("Request blocked by AuthFilter");
    }
}
