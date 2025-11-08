package app.converter;

import app.enums.MimeType;
import app.exception.ConversionErrorException;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class PngToJpgConverterUtils extends ConverterUtils implements FileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PngToJpgConverterUtils.class);

    @Override
    public MimeType getInputMimeType() {
        return MimeType.IMAGE_PNG;
    }

    @Override
    public MimeType getOutputMimeType() {
        return MimeType.IMAGE_JPEG;
    }

    @Override
    public void convert(File source, File dest, Map<String, Object> options) throws ConversionErrorException {
        LOGGER.info("Avvio conversione da PNG a JPG: {} -> {}", source.getAbsolutePath(), dest.getAbsolutePath());
        if (!source.exists()) {
            LOGGER.error("File sorgente non trovato: {}", source.getAbsolutePath());
            throw new ConversionErrorException("File sorgente non trovato: " + source.getAbsolutePath());
        }

        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            grabber = new FFmpegFrameGrabber(source);
            grabber.start();

            int imageWidth = grabber.getImageWidth();
            int imageHeight = grabber.getImageHeight();
            if (imageWidth <= 0 || imageHeight <= 0) {
                LOGGER.error("Il file sorgente non contiene dati immagine validi: {}", source.getAbsolutePath());
                throw new ConversionErrorException("Il file sorgente non contiene dati immagine validi: " + source.getAbsolutePath());
            }

            recorder = new FFmpegFrameRecorder(dest, imageWidth, imageHeight);
            recorder.setFormat("jpeg");
            recorder.setVideoCodecName("mjpeg");
            recorder.start();

            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                recorder.record(frame);
            }

            LOGGER.info("Conversione completata con successo: {}", dest.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Errore durante la conversione: {}", e.getMessage());
            throw new ConversionErrorException("Errore durante la conversione: " + e.getMessage());
        } finally {
            releaseRecorderAndGrabber(recorder, grabber);
        }
    }
}
