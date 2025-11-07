package app.converter;

import app.exception.ConversionException;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Mp4ToMp3Converter implements FileConverter {

    @Override
    public boolean canConvert(String inputType, String outputType) {
        return "video/mp4".equals(inputType) && "audio/mp3".equals(outputType);
    }

    @Override
    public void convert(File source, File dest, Map<String, Object> options) throws ConversionException {
        // Logica di conversione MP4 -> MP3, usando librerie esterne o codice custom
        // Usa opzioni per qualità, bitrate, ecc.
    }

    @Override
    public Collection<String> getSupportedOutputTypes() {
        return List.of();
    }
}
