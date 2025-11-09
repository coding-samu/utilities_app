package app.converter;

import app.enums.MimeType;
import app.exception.ConversionErrorException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class JasperToJrxmlConverterUtils extends ConverterUtils implements FileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JasperToJrxmlConverterUtils.class);

    @Override
    public MimeType getInputMimeType() {
        return MimeType.JASPER_COMPILED;
    }

    @Override
    public MimeType getOutputMimeType() {
        return MimeType.JASPER_JRXML;
    }

    @Override
    public void convert(File source, File dest, Map<String, Object> options) throws ConversionErrorException {
        LOGGER.info("Avvio conversione da JASPER a JRXML: {} -> {}", source.getAbsolutePath(), dest.getAbsolutePath());
        if (!source.exists()) {
            LOGGER.error("File sorgente non trovato: {}", source.getAbsolutePath());
            throw new ConversionErrorException("File sorgente non trovato: " + source.getAbsolutePath());
        }
        try {
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    LOGGER.warn("Impossibile creare la directory di destinazione: {}", parent.getAbsolutePath());
                }
            }

            JasperReport report = (JasperReport)
                    JRLoader.loadObject(source);

            String jrxmlPath = dest.getAbsolutePath();
            JRXmlWriter.writeReport(report, jrxmlPath, "UTF-8");

            LOGGER.info("Conversione completata: {}", jrxmlPath);
        } catch (JRException ex) {
            LOGGER.error("Errore durante la conversione", ex);
            throw new ConversionErrorException("Errore durante la conversione: " + ex.getMessage());
        }
    }
}
