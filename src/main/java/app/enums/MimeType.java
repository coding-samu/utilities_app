package app.enums;

import app.exception.ConversionErrorException;

public enum MimeType {
    VIDEO_MP4("video/mp4", ".mp4"),
    AUDIO_MP3("audio/mpeg", ".mp3"),
    AUDIO_WAV("audio/wav", ".wav"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_JPEG("image/jpeg", ".jpg"),
    JASPER_JRXML("application/jasper-jrxml", ".jrxml"),
    JASPER_COMPILED("application/x-jasper", ".jasper");

    private final String value;
    private final String extension;

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
        for (MimeType m : values()) {
            if (m.value.equalsIgnoreCase(mime)) {
                return m;
            }
        }
        throw new ConversionErrorException("Tipo MIME non supportato: " + mime);
    }

    @Override
    public String toString() {
        return value;
    }
}
