package app.enums;

import app.exception.ConversionErrorException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum MimeType {
    VIDEO_MP4("video/mp4", ".mp4"),
    VIDEO_MKV("video/x-matroska", ".mkv"),
    AUDIO_MP3("audio/mpeg", ".mp3"),
    AUDIO_WAV("audio/wav", ".wav"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_JPEG("image/jpeg", ".jpg"),
    JASPER_JRXML("application/jasper-jrxml", ".jrxml"),
    JASPER_COMPILED("application/x-jasper", ".jasper");

    private final String value;
    private final String extension;

    private static final Map<String, MimeType> MIME_LOOKUP = new HashMap<>();

    static {
        // Primary MIME types
        for (MimeType mimeType : values()) {
            MIME_LOOKUP.put(mimeType.value.toLowerCase(Locale.ROOT), mimeType);
        }
        
        // WAV file aliases - Apache Tika and different systems may return various MIME types for WAV
        MIME_LOOKUP.put("audio/vnd.wave", AUDIO_WAV);
        MIME_LOOKUP.put("audio/x-wav", AUDIO_WAV);
        MIME_LOOKUP.put("audio/wave", AUDIO_WAV);
        MIME_LOOKUP.put("audio/x-pn-wav", AUDIO_WAV);
    }

    MimeType(String value, String extension) {
        this.value = value;
        this.extension = extension;
    }

    public String getValue() {
        return value;
    }

    public String getExtension() {
        return extension;
    }

    public static MimeType fromString(String mime) throws ConversionErrorException {
        if (mime == null || mime.trim().isEmpty()) {
            throw new ConversionErrorException("Tipo MIME null o vuoto");
        }
        
        String normalizedMime = mime.trim().toLowerCase(Locale.ROOT);
        MimeType mimeType = MIME_LOOKUP.get(normalizedMime);
        
        if (mimeType == null) {
            throw new ConversionErrorException("Tipo MIME non supportato: " + mime);
        }
        
        return mimeType;
    }

    @Override
    public String toString() {
        return value;
    }
}
