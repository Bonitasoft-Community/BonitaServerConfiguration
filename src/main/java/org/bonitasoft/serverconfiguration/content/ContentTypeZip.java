package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public class ContentTypeZip extends ContentTypeBinary {

    Logger logger = Logger.getLogger(ContentTypeZip.class.getName());

    public ContentTypeZip(File file ) {
        super( file );
    }
    @Override
    public String getName() {
        return "zip";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".zip") ||  file.getName().endsWith(".gz") || file.getName().endsWith(".war");

    }
    @Override
    public DIFFERENCELEVEL getLevel(ComparaisonParameter comparaisonParameter) {
        return DIFFERENCELEVEL.IMPORTANT;       
    }
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 signatures files
        String signatureReferentiel = getSignature(fileReferentiel);
        String signatureLocal = getSignature(fileLocal);

        comparaisonResult.info("  [" + fileLocal.getName() + "] (ZIP) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        if (!signatureLocal.equals(signatureReferentiel)) {
            comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT,  getLevel(comparaisonParameter), "Signature are different Referentiel[" + signatureReferentiel + "] Local[" + signatureLocal + "]",comparaisonParameter);
        }
    }
   
}