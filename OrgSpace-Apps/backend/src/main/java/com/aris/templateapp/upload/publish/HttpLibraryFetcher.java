package com.aris.templateapp.upload.publish;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Pengunduh library memakai HttpClient bawaan Java (tanpa library tambahan). */
@Component
public class HttpLibraryFetcher implements LibraryFetcher {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public byte[] fetch(String url, long maxBytes) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode() + " untuk " + url);
                }
                byte[] bytes = body.readNBytes((int) Math.min(Integer.MAX_VALUE - 1, maxBytes + 1));
                if (bytes.length > maxBytes) {
                    throw new IOException("File library terlalu besar: " + url);
                }
                return bytes;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Unduhan dibatalkan", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("Alamat library tidak valid: " + url, e);
        }
    }
}
