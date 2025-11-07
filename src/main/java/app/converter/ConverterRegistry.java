package app.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConverterRegistry {
    private final List<FileConverter> converters = new ArrayList<>();

    public ConverterRegistry() {
        registerConverter(new Mp4ToMp3Converter());
    }

    public void registerConverter(FileConverter converter) {
        converters.add(converter);
    }

    public Optional<FileConverter> getConverter(String inputType, String outputType) {
        return converters.stream()
                .filter(c -> c.canConvert(inputType, outputType))
                .findFirst();
    }

    public List<FileConverter> getAllConverters() {
        return converters;
    }
}
