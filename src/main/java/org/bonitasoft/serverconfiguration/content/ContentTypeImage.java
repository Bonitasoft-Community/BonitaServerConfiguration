package org.bonitasoft.serverconfiguration.content;

import java.io.File;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;


public class ContentTypeImage extends ContentTypeBinary {

    public ContentTypeImage(File file ) {
        super(file );
    }
    @Override
    public String getName() {
       return "image";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".gif") 
                || file.getName().endsWith(".png") 
                || file.getName().endsWith(".jpg")
                || file.getName().endsWith(".svg")
                || file.getName().endsWith(".ttf")
                || file.getName().endsWith(".otf")
                || file.getName().endsWith(".woff2")
                || file.getName().endsWith(".woff")
                || file.getName().endsWith(".eot")
                || file.getName().endsWith(".htc")
                || file.getName().endsWith(".ico");

    }

    @Override
    public DIFFERENCELEVEL getLevel(ComparaisonParameter comparaisonParameter) {
       return DIFFERENCELEVEL.LOWER;
    }
    
    @Override
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
       if (comparaisonParameter.ignoreImage)
           return;
       // compare 2 signatures files
       String signatureReferentiel = getSignature(fileReferentiel);
       String signatureLocal = getSignature(fileLocal);

       comparaisonResult.info("  [" + fileLocal.getName() + "] (IMAGE) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
       if (!signatureLocal.equals(signatureReferentiel)) {
           comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT, DIFFERENCELEVEL.LOWER,  "Signature are different Referentiel[" + signatureReferentiel + "] Local[" + signatureLocal + "]", comparaisonParameter);
       }

    }


  
}
