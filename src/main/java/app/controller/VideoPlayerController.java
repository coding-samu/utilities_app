package app.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VideoPlayerController extends GenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoPlayerController.class);

    @FXML
    private BorderPane borderPane;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider speedSlider;

    @FXML
    private Slider timeSlider;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    private MediaPlayer mediaPlayer;
    private boolean atEndOfMedia = false;

    @FXML
    public void initialize() {
        LOGGER.info("Inizializzazione del controller video player");
        
        // Initialize sliders
        volumeSlider.setValue(50);
        speedSlider.setValue(1.0);
        timeSlider.setValue(0);
        
        statusLabel.setText("Nessun video caricato");
        timeLabel.setText("00:00 / 00:00");
    }

    @FXML
    public void selectVideo(ActionEvent event) {
        LOGGER.info("Apertura del file chooser per selezionare un video");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Video");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv", "*.flv", "*.mov"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            loadVideo(file);
        }
    }

    private void loadVideo(File file) {
        LOGGER.info("Caricamento del video: {}", file.getAbsolutePath());
        
        // Clean up previous media player if exists
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            // Bind media view size to scene size
            DoubleProperty width = mediaView.fitWidthProperty();
            DoubleProperty height = mediaView.fitHeightProperty();
            width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
            mediaView.setPreserveRatio(true);
            
            // Configure volume
            mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            
            // Configure playback speed
            mediaPlayer.rateProperty().bind(speedSlider.valueProperty());
            
            // Configure time slider
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newValue.toSeconds());
                }
                updateTimeLabel();
            });
            
            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                timeSlider.setMax(total.toSeconds());
                statusLabel.setText("Video caricato: " + file.getName());
                updateTimeLabel();
            });
            
            mediaPlayer.setOnEndOfMedia(() -> {
                playPauseButton.setText("Play");
                atEndOfMedia = true;
            });
            
            timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (timeSlider.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
            });
            
            playPauseButton.setText("Play");
            LOGGER.info("Video caricato con successo");
            
        } catch (Exception e) {
            LOGGER.error("Errore durante il caricamento del video: {}", e.getMessage());
            statusLabel.setText("Errore nel caricamento del video");
        }
    }

    @FXML
    public void playPause(ActionEvent event) {
        if (mediaPlayer == null) {
            statusLabel.setText("Nessun video caricato");
            return;
        }
        
        MediaPlayer.Status status = mediaPlayer.getStatus();
        
        if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
            statusLabel.setText("Errore nello stato del media player");
            return;
        }
        
        if (atEndOfMedia) {
            mediaPlayer.seek(mediaPlayer.getStartTime());
            atEndOfMedia = false;
        }
        
        if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.READY) {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
            LOGGER.info("Riproduzione video avviata");
        } else {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
            LOGGER.info("Riproduzione video in pausa");
        }
    }

    @FXML
    public void stop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playPauseButton.setText("Play");
            statusLabel.setText("Riproduzione fermata");
            LOGGER.info("Riproduzione video fermata");
        }
    }

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();
            timeLabel.setText(formatTime(currentTime) + " / " + formatTime(totalDuration));
        }
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}
