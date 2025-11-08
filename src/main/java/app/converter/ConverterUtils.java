package app.converter;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConverterUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterUtils.class);

    protected static void releaseRecorderAndGrabber(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber) {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                LOGGER.warn("Errore durante la chiusura del recorder: {}", e.getMessage());
            }
            try {
                recorder.release();
            } catch (Exception e) {
                LOGGER.warn("Errore durante il rilascio del recorder: {}", e.getMessage());
            }
        }
        if (grabber != null) {
            try {
                grabber.stop();
            } catch (Exception e) {
                LOGGER.warn("Errore durante la chiusura del grabber: {}", e.getMessage());
            }
            try {
                grabber.release();
            } catch (Exception e) {
                LOGGER.warn("Errore durante il rilascio del grabber: {}", e.getMessage());
            }
        }
    }
}
