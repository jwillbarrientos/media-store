package io.jona.framework;

import io.jona.framework.http.HttpRequest;
import io.jona.framework.http.HttpResponse;
import io.jona.framework.http.Method;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import static io.jona.framework.http.StaticPathController.processStaticPath;

@Slf4j
public class JonaServer {
    private final int port;
    private final ExecutorService requestExecutor;
    private final Map<String, BiConsumer<HttpRequest, HttpResponse>> endPoints = new LinkedHashMap<>();
    private final Map<String, BiConsumer<HttpRequest, HttpResponse>> inboundFilters = new LinkedHashMap<>();
    private final Map<String, BiConsumer<HttpRequest, HttpResponse>> outboundFilters = new LinkedHashMap<>();
    @Getter
    private volatile String staticContentLocation;


    public JonaServer() {
        this(8080);
    }

    public JonaServer(int port) {
        this.port = port;
        requestExecutor = new ThreadPoolExecutor(
                3,
                3,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10)
        );
    }

    public void registerInboundFilter(Method method, String path, BiConsumer<HttpRequest, HttpResponse> biConsumer) {
        inboundFilters.put(path, biConsumer);
    }

    public void registerEndPoint(Method method, String path, BiConsumer<HttpRequest, HttpResponse> biConsumer) {
        endPoints.put(path, biConsumer);
    }

    public void addStaticContent(String location) {
        this.staticContentLocation = location;
    }

    public void registerOutboundFilter(Method method, String path, BiConsumer<HttpRequest, HttpResponse> biConsumer) {
        outboundFilters.put(path, biConsumer);
    }
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, 100, InetAddress.getByName("0.0.0.0"))) {
            log.info("Server started");
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    requestExecutor.execute(() -> processRequest(client));
                } catch (Exception e) {
                    log.error("While accepting client: ", e);
                    break;
                }
            }
        }
    }

    private void processRequest(Socket client) {
        HttpRequest request = null;
        try {
            request = new HttpRequest(client);
            HttpResponse response = new HttpResponse();

            for(String regex : inboundFilters.keySet()) {
                if (request.canonicalPath().matches(regex)) {
                    BiConsumer<HttpRequest, HttpResponse> biConsumer = inboundFilters.get(regex);
                    biConsumer.accept(request, response);
                    if (response.blockedByInboundFilter()) {
                        break;
                    }
                }
            }

            if (response.blockedByInboundFilter()) {
                byte[] responseBytes = response.buildResponse();
                client.getOutputStream().write(responseBytes);
                client.getOutputStream().flush();
                return;
            }

            boolean isDynamic = endPoints.containsKey(request.canonicalPath());
            if (isDynamic) {
                BiConsumer<HttpRequest, HttpResponse> biConsumer = endPoints.get(request.canonicalPath());
                biConsumer.accept(request, response);
            } else {
                processStaticPath(request, response, staticContentLocation);
            }

            for(String regex : outboundFilters.keySet()) {
                if (request.canonicalPath().matches(regex)) {
                    BiConsumer<HttpRequest, HttpResponse> biConsumer = outboundFilters.get(regex);
                    biConsumer.accept(request, response);
                }
            }

            client.getOutputStream().write(response.buildResponse());
            client.getOutputStream().flush();
        } catch (IOException e) {
            log.error("Exception while processing request: {}", request, e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Exception while closing client: ", e);
            }
        }
    }

    public void shutdown() {
        requestExecutor.shutdownNow();
    }
}
