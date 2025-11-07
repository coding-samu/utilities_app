package app.controller;

import app.converter.ConverterRegistry;
import app.converter.FileConverter;
import app.enums.MimeType;
import app.exception.ConversionErrorException;
import app.exception.ConversionWarningException;
import app.exception.GenericException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConverterController extends GenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    @FXML
    private TextField sourceFilePath;

    @FXML
    private TextField destFolderPath;

    @FXML
    private TextField outputFileName;

    @FXML
    private ChoiceBox<String> outputTypeChoice;

    private ConverterRegistry converterRegistry;

    private File sourceFile;
    private File destFolder;

    @FXML
    public void initialize() {
        LOGGER.info("Inizializzazione del controller di conversione");
        converterRegistry = new ConverterRegistry();

        Set<String> allOutputTypes = new HashSet<>();
        for (FileConverter c : converterRegistry.getAllConverters()) {
            allOutputTypes.add(c.getOutputMimeType().toString());
        }
        outputTypeChoice.setItems(FXCollections.observableArrayList(allOutputTypes));
    }

    @FXML
    public void selectSourceFile(ActionEvent event) throws GenericException {
        LOGGER.info("Apertura del file chooser per selezionare il file sorgente");
        FileChooser fileChooser = new FileChooser();
        if (sourceFile != null) {
            fileChooser.setInitialDirectory(sourceFile.getParentFile());
        }
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            sourceFile = file;
            sourceFilePath.setText(file.getAbsolutePath());

            MimeType inputType = MimeType.fromString(guessMimeType(sourceFile));
            List<String> availableOutputs = new ArrayList<>();
            for (FileConverter c : converterRegistry.getAllConverters()) {
                if (c.getInputMimeType().equals(inputType)) {
                    availableOutputs.add(c.getOutputMimeType().toString());
                }
            }
            outputTypeChoice.setItems(FXCollections.observableArrayList(availableOutputs));
        }
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
    public void performConversion(ActionEvent event) {
        LOGGER.info("Avvio del processo di conversione del file");

        try {
            if (sourceFile == null || destFolder == null || outputFileName.getText().isEmpty() || outputTypeChoice.getValue() == null) {
                throw new ConversionWarningException("Per favore, assicurati di aver selezionato il file sorgente, la cartella di destinazione, il nome del file di output e il tipo di output.");
            }

            MimeType inputType = MimeType.fromString(guessMimeType(sourceFile));
            MimeType outputType = MimeType.fromString(outputTypeChoice.getValue());

            String baseName = outputFileName.getText();
            if (!baseName.toLowerCase().endsWith(outputType.getExtension())) {
                baseName += outputType.getExtension();
            }
            File destFile = new File(destFolder, baseName);

            Optional<FileConverter> converter = converterRegistry.getConverter(inputType, outputType);
            if (converter.isEmpty()) {
                throw new ConversionErrorException("Nessun convertitore trovato per i tipi MIME specificati: " + inputType + " -> " + outputType);
            }

            converter.get().convert(sourceFile, destFile, Collections.emptyMap());

            LOGGER.info("Conversione completata con successo: {} -> {}", sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "Conversione completata", "File convertito con successo!\n\nSalvato in:\n" + destFile.getAbsolutePath());

        } catch (ConversionWarningException e) {
            LOGGER.warn("Attenzione: {}", e.getMessage());
            showAlert(Alert.AlertType.WARNING, "Attenzione: ", e.getMessage());
        } catch (ConversionErrorException e) {
            LOGGER.error("Errore durante la conversione: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore di conversione", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Errore imprevisto: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore imprevisto", "Si è verificato un errore imprevisto:\n" + e.getMessage());
        }
    }


    private String guessMimeType(File file) throws ConversionErrorException {
        Tika tika = new Tika();
        try {
            LOGGER.info("Rilevamento del tipo MIME per il file: {}", file.getAbsolutePath());
            return tika.detect(file);
        } catch (IOException e) {
            LOGGER.error("Errore nel rilevamento del tipo MIME: {}", e.getMessage());
            throw new ConversionErrorException("Errore nel rilevamento del tipo MIME: " + e.getMessage());
        }
    }
}
