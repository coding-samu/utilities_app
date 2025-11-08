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

public class Mp4ToWavConverter implements FileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4ToWavConverter.class);
    private static final int DEFAULT_AUDIO_BITRATE = 192_000;
    private static final int DEFAULT_SAMPLE_RATE = 44100;

    @Override
    public MimeType getInputMimeType() {
        return MimeType.VIDEO_MP4;
    }

    @Override
    public MimeType getOutputMimeType() {
        return MimeType.AUDIO_WAV;
    }

    @Override
    public void convert(File source, File dest, Map<String, Object> options) throws ConversionErrorException {
        LOGGER.info("Avvio conversione da MP4 a WAV: {} -> {}", source.getAbsolutePath(), dest.getAbsolutePath());
        if (!source.exists()) {
            LOGGER.error("File sorgente non trovato: {}", source.getAbsolutePath());
            throw new ConversionErrorException("File sorgente non trovato: " + source.getAbsolutePath());
        }

        int bitrate = (int) options.getOrDefault("audioBitrate", DEFAULT_AUDIO_BITRATE);
        int sampleRate = (int) options.getOrDefault("sampleRate", DEFAULT_SAMPLE_RATE);
        String codec = (String) options.getOrDefault("audioCodec", "pcm_s16le");

        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            LOGGER.info("Impostazioni di conversione - Bitrate: {}, Sample Rate: {}, Codec: {}", bitrate, sampleRate, codec);
            grabber = new FFmpegFrameGrabber(source);
            grabber.start();

            int audioChannels = grabber.getAudioChannels();
            if (audioChannels == 0) {
                throw new ConversionErrorException("Il file sorgente non contiene tracce audio.");
            }

            recorder = new FFmpegFrameRecorder(dest, audioChannels);
            recorder.setFormat("wav");
            recorder.setAudioCodecName(codec);
            recorder.setAudioBitrate(bitrate);
            recorder.setSampleRate(sampleRate);
            recorder.setAudioChannels(audioChannels);

            recorder.start();

            Frame frame;
            while ((frame = grabber.grabSamples()) != null) {
                recorder.recordSamples(frame.samples);
            }
            LOGGER.info("Conversione completata con successo: {}", dest.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Errore durante la conversione: {}", e.getMessage());
            throw new ConversionErrorException("Errore durante la conversione: " + e.getMessage());
        } finally {
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
}
