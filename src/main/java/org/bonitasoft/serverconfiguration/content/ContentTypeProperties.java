package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public class ContentTypeProperties extends ContentType {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypeProperties(File file ) {
        super(file );
    }
    
    @Override
    public String getName() {
        return "properties";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".properties");
    }
    
    @Override
    public DIFFERENCELEVEL getLevel() {
        if (file.getName().contains("logging.properties") )
            return DIFFERENCELEVEL.MEDIUM;
        if (file.getName().contains("custom-permissions-mapping.properties"))
            return DIFFERENCELEVEL.MEDIUM;
        return DIFFERENCELEVEL.IMPORTANT;
    }
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 properties files
        comparaisonResult.info("    [" + fileLocal.getName() + "] (Properties) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        
        // change in logging.properties ? It's a MEDIUM level
        DIFFERENCELEVEL level = getLevel();
        // change in a txt, or a md is not important
       

        try {
            InputStream inputReferentiel = new FileInputStream(fileReferentiel);
            InputStream inputLocal = new FileInputStream(fileLocal);

            Properties propReferentiel = new Properties();
            propReferentiel.load(inputReferentiel);

            Properties propLocal = new Properties();
            propLocal.load(inputLocal);

            // first pass: all value in REFERENTIEL and not in LOCAL
            for (Object keyReferentiel : propReferentiel.keySet()) {
                String propertyReferentiel = propReferentiel.getProperty(keyReferentiel.toString());
                String propertyLocal = propLocal.getProperty(keyReferentiel.toString());
                if (propertyLocal == null) {
                    comparaisonResult.reportReferentielOnly(fileLocal, level, propertyReferentiel, "Key="+keyReferentiel , true);
                } else if (!propertyLocal.equals(propertyReferentiel)) {
                    comparaisonResult.reportDifference(fileLocal,  level,  propertyLocal, propertyReferentiel, "Key="+keyReferentiel,true);

                }
            }
            // second pass : all new objects in LOCAL
            for (Object keyLocal : propLocal.keySet()) {
                String propertyLocal = propLocal.getProperty(keyLocal.toString());
                String propertyReferentiel = propReferentiel.getProperty(keyLocal.toString());
                if (propertyReferentiel == null) {
                    comparaisonResult.reportLocalOnly(fileLocal, level, propertyLocal, "Key="+keyLocal,true );
                }
            }
        } catch (Exception e) {
            comparaisonResult.reportError(fileLocal, e, "Read properties");
        }

    }

  

}
