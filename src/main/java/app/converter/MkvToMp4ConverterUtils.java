package app.converter;

import app.enums.MimeType;
import app.exception.ConversionErrorException;
import org.bytedeco.ffmpeg.global.avutil;
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
    private static final int DEFAULT_CRF = 23;
    private static final String DEFAULT_PRESET = "medium";

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

        int videoBitrate = getIntOption(options, "videoBitrate", DEFAULT_VIDEO_BITRATE);
        int audioBitrate = getIntOption(options, "audioBitrate", DEFAULT_AUDIO_BITRATE);
        int frameRateOption = getIntOption(options, "frameRate", DEFAULT_FRAME_RATE);
        int crf = getIntOption(options, "crf", DEFAULT_CRF);
        String preset = getStringOption(options, "preset", DEFAULT_PRESET);
        String videoCodec = getStringOption(options, "videoCodec", "libx264");
        String audioCodec = getStringOption(options, "audioCodec", "aac");

        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            LOGGER.info("Impostazioni richieste - Video Bitrate: {}, Audio Bitrate: {}, Frame Rate (opt): {}, CRF: {}, Preset: {}, Video Codec: {}, Audio Codec: {}",
                    videoBitrate, audioBitrate, frameRateOption, crf, preset, videoCodec, audioCodec);

            grabber = new FFmpegFrameGrabber(source);
            grabber.start();

            int imageWidth = grabber.getImageWidth();
            int imageHeight = grabber.getImageHeight();
            int audioChannels = grabber.getAudioChannels();
            int sampleRate = grabber.getSampleRate();
            double sourceFrameRate = grabber.getFrameRate();

            if (imageWidth <= 0 || imageHeight <= 0) {
                LOGGER.error("Il file sorgente non contiene tracce video valide.");
                throw new ConversionErrorException("Il file sorgente non contiene tracce video valide.");
            }

            int usedFrameRate = frameRateOption;
            if (sourceFrameRate > 0) {
                usedFrameRate = (int) Math.round(sourceFrameRate);
                LOGGER.info("Utilizzo frame rate sorgente: {}", usedFrameRate);
            }

            recorder = new FFmpegFrameRecorder(dest, imageWidth, imageHeight, audioChannels);
            recorder.setFormat("mp4");
            recorder.setVideoCodecName(videoCodec);
            recorder.setVideoBitrate(videoBitrate);
            recorder.setFrameRate(usedFrameRate);
            recorder.setGopSize(Math.max(1, usedFrameRate * 2));
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setVideoOption("preset", preset);
            recorder.setVideoOption("crf", String.valueOf(crf));
            recorder.setVideoOption("movflags", "faststart");

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
            LOGGER.error("Errore durante la conversione", e);
            throw new ConversionErrorException("Errore durante la conversione: " + e.getMessage());
        } finally {
            releaseRecorderAndGrabber(recorder, grabber);
        }
    }

    private int getIntOption(Map<String, Object> options, String key, int defaultValue) {
        Object val = options != null ? options.get(key) : null;
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private String getStringOption(Map<String, Object> options, String key, String defaultValue) {
        Object val = options != null ? options.get(key) : null;
        return val != null ? val.toString() : defaultValue;
    }
}
