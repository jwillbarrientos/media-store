package io.jona.memestore.test;

import io.jona.framework.JonaServer;
import io.jona.framework.http.Method;
import io.jona.memestore.controller.TestController;

public class MemeStoreTest {

    public static void main(String[] args) {
        TestController testController = new TestController();

        JonaServer jonaServer = new JonaServer(8080);
        jonaServer.registerEndPoint(Method.GET, "/testPath/getdate", testController::getDate);

        jonaServer.registerEndPoint(Method.GET, "/getdate", testController::getDateAndCookies);
        jonaServer.registerEndPoint(Method.GET, "/setcookie", testController::setCookie);
        jonaServer.registerEndPoint(Method.GET, "/deletecookies", testController::deleteCookie);
    }
}
