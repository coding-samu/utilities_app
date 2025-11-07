package app.converter;

import app.enums.MimeType;
import app.exception.ConversionErrorException;

import java.io.File;
import java.util.Map;

public interface FileConverter {

    MimeType getInputMimeType();

    MimeType getOutputMimeType();

    void convert(File source, File dest, Map<String, Object> options) throws ConversionErrorException;
}
