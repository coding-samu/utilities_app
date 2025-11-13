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

public class MkvToMp4ConverterUtils extends ConverterUtils implements FileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MkvToMp4ConverterUtils.class);
    private static final int DEFAULT_VIDEO_BITRATE = 2_000_000;
    private static final int DEFAULT_AUDIO_BITRATE = 192_000;
    private static final int DEFAULT_FRAME_RATE = 30;

    @Override
    public MimeType getInputMimeType() {
        return MimeType.VIDEO_MKV;
    }

    @Override
    public MimeType getOutputMimeType() {
        return MimeType.VIDEO_MP4;
    }

    @Override
    public void convert(File source, File dest, Map<String, Object> options) throws ConversionErrorException {
        LOGGER.info("Avvio conversione da MKV a MP4: {} -> {}", source.getAbsolutePath(), dest.getAbsolutePath());
        if (!source.exists()) {
            LOGGER.error("File sorgente non trovato: {}", source.getAbsolutePath());
            throw new ConversionErrorException("File sorgente non trovato: " + source.getAbsolutePath());
        }

        int videoBitrate = (int) options.getOrDefault("videoBitrate", DEFAULT_VIDEO_BITRATE);
        int audioBitrate = (int) options.getOrDefault("audioBitrate", DEFAULT_AUDIO_BITRATE);
        int frameRate = (int) options.getOrDefault("frameRate", DEFAULT_FRAME_RATE);
        String videoCodec = (String) options.getOrDefault("videoCodec", "libx264");
        String audioCodec = (String) options.getOrDefault("audioCodec", "aac");

        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            LOGGER.info("Impostazioni di conversione - Video Bitrate: {}, Audio Bitrate: {}, Frame Rate: {}, Video Codec: {}, Audio Codec: {}",
                    videoBitrate, audioBitrate, frameRate, videoCodec, audioCodec);
            grabber = new FFmpegFrameGrabber(source);
            grabber.start();

            int imageWidth = grabber.getImageWidth();
            int imageHeight = grabber.getImageHeight();
            int audioChannels = grabber.getAudioChannels();
            int sampleRate = grabber.getSampleRate();

            if (imageWidth <= 0 || imageHeight <= 0) {
                LOGGER.error("Il file sorgente non contiene tracce video valide.");
                throw new ConversionErrorException("Il file sorgente non contiene tracce video valide.");
            }

            recorder = new FFmpegFrameRecorder(dest, imageWidth, imageHeight, audioChannels);
            recorder.setFormat("mp4");
            recorder.setVideoCodecName(videoCodec);
            recorder.setVideoBitrate(videoBitrate);
            recorder.setFrameRate(frameRate);

            if (audioChannels > 0 && sampleRate > 0) {
                recorder.setAudioCodecName(audioCodec);
                recorder.setAudioBitrate(audioBitrate);
                recorder.setSampleRate(sampleRate);
                recorder.setAudioChannels(audioChannels);
            }

            recorder.start();

            Frame frame;
            while ((frame = grabber.grab()) != null) {
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
