package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

/**
 * Bonita Translation File
 *
 */
public class ContentTypePo extends ContentTypeText {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypePo(File file ) {
        super( file );
    }
    @Override
    public String getName() {
        return "po";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".po");

    }
    @Override
    public DIFFERENCELEVEL getLevel(ComparaisonParameter comparaisonParameter) {
        return DIFFERENCELEVEL.LOWER;
    }
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        if (comparaisonParameter.ignoreBonitaTranslationFile)
            return;
        super.compareFile(fileLocal, fileReferentiel, comparaisonParameter, comparaisonResult);
    }

}