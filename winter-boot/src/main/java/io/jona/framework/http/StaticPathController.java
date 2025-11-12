package io.jona.framework.http;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class StaticPathController {

    private static Path getAbsoluteFilePathForRequest(HttpRequest request, String staticResourcesPath) {
        String relativePath = staticResourcesPath + (
                request.welcomeRequested() ?
                        "/index.html" : "/" + request.getPath()
        );
        relativePath = relativePath.replaceAll("/{2,}", "/");
        return Paths.get(relativePath).toAbsolutePath();
    }

    public static void processStaticPath(HttpRequest request, HttpResponse response, String staticResourcesPath) throws IOException {
        try {
            Path absPath = getAbsoluteFilePathForRequest(request, staticResourcesPath);

            boolean requestedResourceExistsAsFile = absPath.toFile().exists();
            if (!requestedResourceExistsAsFile) {
                response.setResponseCode(HttpCode.NOT_FOUND_404);
                response.setContentType(MimeType.TEXT_PLAIN);
                response.setBody("This resource doesnt exist");
                log.info("Served File: NOT FOUND");
                return;
            }

            if (request.welcomeRequested()) {
                response.setResponseCode(HttpCode.OK_200);
                response.setContentType(MimeType.TEXT_HTML, "UTF-8");
                response.setBody(Files.readAllBytes(absPath));
                log.info("Served File: public/index.html (MIME: text/html, Size: {} bytes", response.getBody().length);

                return;
            }

            String extension = request.getPath().substring(request.getPath().lastIndexOf("."));
            MimeType mimeType = MimeType.getMimeForExtension(extension);
            if (mimeType.requiresEncoding) {
                response.setContentType(mimeType, "UTF-8");
            } else {
                response.setContentType(mimeType);
            }

            if (mimeType.isVideo()) {
                response.setResponseCode(HttpCode.PARTIAL_CONTENT_206);
                long end = Math.min(request.getRangeStart() + HttpResponse.CHUNK_SIZE_BYTES - 1, absPath.toFile().length() - 1);
                response.setBodyWithRange(absPath, request.getRangeStart(), end, absPath.toFile().length());
            } else {
                response.setResponseCode(HttpCode.OK_200);
                response.setBody(Files.readAllBytes(absPath));
            }

        } catch (Exception e) {
            log.error("Error processing static request", e);
            response.setResponseCode(HttpCode.SERVER_ERROR_500);
            response.setContentType(MimeType.TEXT_HTML, "UTF-8");
            response.setBody("Error processing static request");
        }
    }
}
