package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

// Read more: https://javarevisited.blogspot.com/2017/04/how-to-compare-two-xml-files-in-java.html#ixzz64wEIRa7y
public class ContentTypeXml extends ContentType {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypeXml(File file ) {
        super( file );
    }
    @Override
    public String getName() {
        return "xml";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".xml") || file.getName().endsWith(".xsd");

    }
    @Override
    public DIFFERENCELEVEL getLevel() {
        return DIFFERENCELEVEL.IMPORTANT;       
    }
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 properties files
        comparaisonResult.info("   [" + fileLocal.getName() + "] (XML) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        try {
            FileInputStream fsReferentiel = new FileInputStream(fileReferentiel);
            FileInputStream fsLocal = new FileInputStream(fileLocal);

            DiffBuilder diffBuilder = DiffBuilder.compare(fsReferentiel);
            diffBuilder.withTest(fsLocal);
            diffBuilder.ignoreComments();
            diffBuilder.ignoreWhitespace();
            diffBuilder.ignoreElementContentWhitespace();
            Diff diff = diffBuilder.build();

            for (Difference difference : diff.getDifferences()) {
                comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT, getLevel(), difference.toString());
            }
        } catch (Exception e) {
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionDetails = sw.toString();

                comparaisonResult.info("Error " + e.getMessage() + " at " + exceptionDetails);
            }
        }
    }

}
