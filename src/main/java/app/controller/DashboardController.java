package app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DashboardController extends GenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    public void openConverter() throws IOException {
        LOGGER.info("Apertura della finestra di conversione");
        openUtilityWindow("ConverterUtility.fxml", "Converti File");
    }

    public void openVideoPlayer() throws IOException {
        LOGGER.info("Apertura della finestra video player");
        openUtilityWindow("VideoPlayerUtility.fxml", "Video Player");
    }

    private void openUtilityWindow(String fxmlFile, String title) throws IOException {
        LOGGER.info("Caricamento della finestra: {}", title);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
