package app.downloader;

import app.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class VideoDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDownloader.class);
    private static final int BUFFER_SIZE = 8192;
    private static final int TIMEOUT_SECONDS = 300;

    public interface ProgressCallback {
        void onProgress(long bytesDownloaded, long totalBytes);
    }

    public void downloadVideo(String url, File outputFile, ProgressCallback progressCallback) throws GenericException {
        LOGGER.info("Inizio download del video da: {}", url);
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new GenericException("Errore nel download: HTTP " + response.statusCode());
            }

            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            LOGGER.info("Dimensione del file: {} bytes", contentLength);

            try (InputStream inputStream = response.body();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                long totalBytesRead = 0;
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (progressCallback != null) {
                        progressCallback.onProgress(totalBytesRead, contentLength);
                    }
                }

                LOGGER.info("Download completato: {} bytes scaricati", totalBytesRead);
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Errore durante il download: {}", e.getMessage());
            throw new GenericException("Errore durante il download del video: " + e.getMessage());
        }
    }
}
