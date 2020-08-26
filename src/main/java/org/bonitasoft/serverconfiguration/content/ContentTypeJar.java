package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public class ContentTypeJar extends ContentTypeBinary {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypeJar(File file ) {
        super(file );
    }
    @Override
    public String getName() {
        return "jar";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".jar");

    }
    @Override
    public DIFFERENCELEVEL getLevel(ComparaisonParameter comparaisonParameter) {
        if (comparaisonParameter.referentielIsABundle && file.getName().contains("ojdbc"))
            return DIFFERENCELEVEL.EXPECTED;
        return DIFFERENCELEVEL.CRITICAL;
    }
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 signatures files
        String signatureReferentiel = getSignature(fileReferentiel);
        String signatureLocal = getSignature(fileLocal);

        comparaisonResult.info("  [" + fileLocal.getName() + "] (JAR) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        if (!signatureLocal.equals(signatureReferentiel)) {
            comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT,  getLevel( comparaisonParameter ), "Signature are different Referentiel[" + signatureReferentiel + "] Local[" + signatureLocal + "]",comparaisonParameter);
        }
    }

}
