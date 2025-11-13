package app.converter;

import app.enums.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConverterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterRegistry.class);
    private final List<FileConverter> converters = new ArrayList<>();

    public ConverterRegistry() {
        LOGGER.info("Inizializzazione del registro dei convertitori di file");
        converters.add(new Mp4ToMp3ConverterUtils());
        converters.add(new Mp4ToWavConverterUtils());
        converters.add(new Mp4ToMkvConverterUtils());
        converters.add(new MkvToMp4ConverterUtils());
        converters.add(new PngToJpgConverterUtils());
        converters.add(new JpgToPngConverterUtils());
        converters.add(new JasperToJrxmlConverterUtils());
        LOGGER.info("Registro dei convertitori inizializzato con {} convertitori", converters.size());
    }

    public Optional<FileConverter> getConverter(MimeType input, MimeType output) {
        LOGGER.info("Ricerca del convertitore per tipi MIME di input: {} e output: {}", input, output);
        return converters.stream()
                .filter(c -> c.getInputMimeType() == input && c.getOutputMimeType() == output)
                .findFirst();
    }

    public List<FileConverter> getAllConverters() {
        return converters;
    }
}
