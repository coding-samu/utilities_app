package app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller for the dashboard.
 */
public class DashboardController extends GenericController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DashboardController.class);

    /**
     * Open the file converter utility window.
     *
     * @throws IOException if FXML loading fails
     */
    public void openConverter() throws IOException {
        LOGGER.info("Apertura della finestra di conversione");
        openUtilityWindow("ConverterUtility.fxml", "Converti File");
    }

    /**
     * Open the video downloader utility window.
     *
     * @throws IOException if FXML loading fails
     */
    public void openVideoDownloader() throws IOException {
        LOGGER.info("Apertura della finestra di download video");
        openUtilityWindow("VideoDownloader.fxml", "Scarica Video");
    }

    private void openUtilityWindow(final String fxmlFile,
                                    final String title) throws IOException {
        LOGGER.info("Caricamento della finestra: {}", title);
        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/" + fxmlFile));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
