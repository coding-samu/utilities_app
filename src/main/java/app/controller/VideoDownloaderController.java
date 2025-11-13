package app.controller;

import app.downloader.VideoDownloader;
import app.exception.GenericException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VideoDownloaderController extends GenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDownloaderController.class);

    @FXML
    private TextField videoUrl;

    @FXML
    private TextField destFolderPath;

    @FXML
    private TextField outputFileName;

    @FXML
    private ProgressBar downloadProgress;

    @FXML
    private Button downloadButton;

    private File destFolder;

    @FXML
    public void initialize() {
        LOGGER.info("Inizializzazione del controller di download video");
        downloadProgress.setProgress(0);
    }

    @FXML
    public void selectDestFolder(ActionEvent event) {
        LOGGER.info("Apertura del directory chooser per selezionare la cartella di destinazione");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (destFolder != null) {
            directoryChooser.setInitialDirectory(destFolder);
        }
        File folder = directoryChooser.showDialog(new Stage());
        if (folder != null) {
            destFolder = folder;
            destFolderPath.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    public void performDownload(ActionEvent event) {
        LOGGER.info("Avvio del processo di download del video");

        String url = videoUrl.getText();
        String fileName = outputFileName.getText();

        if (url == null || url.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Per favore, inserisci l'URL del video.");
            return;
        }

        if (destFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Per favore, seleziona la cartella di destinazione.");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Per favore, inserisci il nome del file di output.");
            return;
        }

        // Ensure the filename has a video extension
        if (!fileName.toLowerCase().matches(".*\\.(mp4|avi|mkv|mov|wmv|flv|webm)$")) {
            fileName += ".mp4";
        }

        File outputFile = new File(destFolder, fileName);

        if (outputFile.exists()) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Il file esiste già. Scegli un nome diverso.");
            return;
        }

        // Disable the download button to prevent multiple downloads
        downloadButton.setDisable(true);
        downloadProgress.setProgress(0);

        // Perform download in a separate thread
        final String finalFileName = fileName;
        Thread downloadThread = new Thread(() -> {
            try {
                VideoDownloader downloader = new VideoDownloader();
                downloader.downloadVideo(url, outputFile, (bytesDownloaded, totalBytes) -> {
                    double progress = totalBytes > 0 ? (double) bytesDownloaded / totalBytes : -1;
                    Platform.runLater(() -> {
                        if (progress >= 0) {
                            downloadProgress.setProgress(progress);
                        } else {
                            downloadProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                        }
                    });
                });

                Platform.runLater(() -> {
                    downloadProgress.setProgress(1.0);
                    downloadButton.setDisable(false);
                    LOGGER.info("Download completato con successo: {}", outputFile.getAbsolutePath());
                    showAlert(Alert.AlertType.INFORMATION, "Download completato",
                            "Video scaricato con successo!\n\nSalvato in:\n" + outputFile.getAbsolutePath());
                });

            } catch (GenericException e) {
                Platform.runLater(() -> {
                    downloadButton.setDisable(false);
                    downloadProgress.setProgress(0);
                    LOGGER.error("Errore durante il download: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Errore di download", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    downloadButton.setDisable(false);
                    downloadProgress.setProgress(0);
                    LOGGER.error("Errore imprevisto: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Errore imprevisto",
                            "Si è verificato un errore imprevisto:\n" + e.getMessage());
                });
            }
        });

        downloadThread.setDaemon(true);
        downloadThread.start();
    }
}
