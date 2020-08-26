package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;

public class ContentTypeBat extends ContentTypeText {

    Logger logger = Logger.getLogger(ContentTypeBat.class.getName());

    public ContentTypeBat(File file ) {
        super(file );
    }
    @Override
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        super.compareFile(fileLocal, fileReferentiel, comparaisonParameter, comparaisonResult);
        // compare 2 Ascii file files
        // comparaisonResult.info("   ["+fileLocal.getName()+"] (BAT-SH)<-> ["+fileReferentiel.getName()+"] ("+fileLocal.getAbsolutePath()+") <-> ("+fileReferentiel.getAbsolutePath()+")");
    }

    @Override
    public String getName() {
        return "bat";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".bat");

    }
    @Override
    public DIFFERENCELEVEL getLevel( ComparaisonParameter comparaisonParameter) {
        if (comparaisonParameter.referentielIsABundle && file.getName().contains("setenv.bat"))
            return DIFFERENCELEVEL.EXPECTED;

        return DIFFERENCELEVEL.MEDIUM;       
    }
 
    /**
     * special report: report on
     */
    @Override
    public void reportDeltas(DIFFERENCELEVEL level,File fileReferentiel, File fileLocal, List<AbstractDelta<String>> deltas, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {

        /**
         * not good, should report all delta in one call
         */
        for (AbstractDelta<String> delta : deltas) {
            boolean isOnlyRem = true;
            @SuppressWarnings("rawtypes")
            Chunk chunk = delta.getSource();
            for (Object lineOb : chunk.getLines()) {

                String line = lineOb.toString();
                if (!(line.isEmpty() || line.toLowerCase().startsWith("rem")))
                    isOnlyRem = false;
            }
            chunk = delta.getTarget();
            for (Object lineOb : chunk.getLines()) {
                String line = lineOb.toString();
                if (!(line.isEmpty() || line.toLowerCase().startsWith("rem")))
                    isOnlyRem = false;
            }
            if (!isOnlyRem)
                comparaisonResult.reportDifference(fileLocal, level, delta.getTarget().toString(), delta.getSource().toString(), "Difference in file", true);
        }

    }
}
