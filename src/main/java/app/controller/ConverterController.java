package app.controller;

import app.converter.ConverterRegistry;
import app.converter.FileConverter;
import app.exception.ConversionException;
import app.converter.Mp4ToMp3Converter;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ConverterController {

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
        // TODO: Inizializza registry e registra converter noti
        converterRegistry = new ConverterRegistry();
        converterRegistry.registerConverter(new Mp4ToMp3Converter());

        // TODO: Per iniziare si può popolare outputTypeChoice con i tipi supported da tutti i converter
        Set<String> allOutputTypes = new HashSet<>();
        for (FileConverter c : converterRegistry.getAllConverters()) {
            allOutputTypes.addAll(c.getSupportedOutputTypes());
        }
        outputTypeChoice.setItems(FXCollections.observableArrayList(allOutputTypes));
    }

    @FXML
    public void selectSourceFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        if (sourceFile != null) {
            fileChooser.setInitialDirectory(sourceFile.getParentFile());
        }
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            sourceFile = file;
            sourceFilePath.setText(file.getAbsolutePath());

            // TODO: Aggiorna outputTypeChoice in base al tipo input conosciuto (es tramite estensione/mime)
            String inputType = guessMimeType(sourceFile);
            List<String> availableOutputs = new ArrayList<>();
            for (FileConverter c : converterRegistry.getAllConverters()) {
                if (c.canConvert(inputType, null)) { // TODO: modifica canConvert per gestire null outputType
                    availableOutputs.addAll(c.getSupportedOutputTypes());
                }
            }
            outputTypeChoice.setItems(FXCollections.observableArrayList(availableOutputs));
        }
    }

    @FXML
    public void selectDestFolder(ActionEvent event) {
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
        if (sourceFile == null || destFolder == null || outputFileName.getText().isEmpty() || outputTypeChoice.getValue() == null) {
            System.out.println("Completare tutti i campi");
            return;
        }

        String inputType = guessMimeType(sourceFile);
        String outputType = outputTypeChoice.getValue();
        File destFile = new File(destFolder, outputFileName.getText());

        Optional<FileConverter> converter = converterRegistry.getConverter(inputType, outputType);
        if (converter.isPresent()) {
            try {
                converter.get().convert(sourceFile, destFile, Collections.emptyMap());
                System.out.println("Conversione completata con successo!");
            } catch (ConversionException e) {
                System.err.println("Errore durante conversione: " + e.getMessage());
            }
        } else {
            System.out.println("Conversione non supportata: " + inputType + " -> " + outputType);
        }
    }

    // TODO: idealmente si usa una libreria o map da estensioni a mime type
    private String guessMimeType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".mp4")) return "video/mp4";
        else if (fileName.endsWith(".mp3")) return "audio/mp3";
        else if (fileName.endsWith(".txt")) return "text/plain";
        // TODO: aggiungi altre estensioni e tipi
        return "application/octet-stream";
    }
}
