package org.bonitasoft.serverconfiguration.content;

import java.io.File;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public class ContentTypeOther extends ContentTypeBinary {

    public ContentTypeOther(File file ) {
        super(file );
    }
    @Override
    public String getName() {
        return "other";
    }

    @Override
    public boolean matchFile(File file) {
        return true;
    }

    @Override
    public DIFFERENCELEVEL getLevel() {
        return DIFFERENCELEVEL.IMPORTANT;
    }
    @Override
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {

        String signatureReferentiel = getSignature(fileReferentiel);
        String signatureLocal = getSignature(fileLocal);

        comparaisonResult.info("  [" + fileLocal.getName() + "] (OTHER) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        if (!signatureLocal.equals(signatureReferentiel)) {
            comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT, getLevel(),  "Signature are different Referentiel[" + signatureReferentiel + "] Local[" + signatureLocal + "]");
        }
    }

}
