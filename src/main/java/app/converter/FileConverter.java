package app.converter;

import app.exception.ConversionException;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public interface FileConverter {
    boolean canConvert(String inputType, String outputType);
    void convert(File source, File dest, Map<String, Object> options) throws ConversionException;
    Collection<String> getSupportedOutputTypes();
}