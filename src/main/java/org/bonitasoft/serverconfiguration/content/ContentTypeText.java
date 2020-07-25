package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

public class ContentTypeText extends ContentType {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypeText(File file) {
        super(file);
    }

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".txt")
                || file.getName().endsWith(".md")
                || file.getName().endsWith(".conf")
                || file.getName().endsWith(".cfg")
                || file.getName().endsWith(".policy")
                || file.getName().endsWith(".jsp")
                || file.getName().endsWith(".html")
                || file.getName().endsWith(".json")
                || file.getName().endsWith(".js")
                || file.getName().endsWith(".css");

    }

    @Override
    public DIFFERENCELEVEL getLevel() {
        // change in a txt, or a md is not important
        if (file.getName().endsWith(".txt")
                || file.getName().endsWith(".md"))
            return DIFFERENCELEVEL.LOWER;
        return DIFFERENCELEVEL.MEDIUM;
    }

    /**
     * comparaison with an another contentType
     */
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        List<String> original;
        List<String> revised;

        try {
            original = readFile(fileReferentiel);
            if (original == null) {
                comparaisonResult.report(fileReferentiel, DIFFERENCESTATUS.ERROR, getLevel(), "Can't read file [" + fileReferentiel.toPath());
                return;
            }
            revised = readFile(fileLocal);
            if (revised == null) {
                comparaisonResult.report(fileReferentiel, DIFFERENCESTATUS.ERROR, getLevel(), "Can't read file [" + fileLocal.toPath());
                return;
            }
            //compute the patch: this is the diffutils part
            Patch<String> patch = DiffUtils.diff(original, revised);

            //simple output the computed patch to console

            reportDeltas(getLevel(), fileReferentiel, fileLocal, patch.getDeltas(), comparaisonResult);

        } catch (DiffException e) {

        }
    }

    /**
     * read and return the content of the file
     * 
     * @return
     */
    public String getContent() {
        List<String> listLines = readFile();
        StringBuffer content = new StringBuffer();
        for (String line : listLines) {
            content.append(line + "\n");
        }
        return content.toString();
    }

    /**
     * read this content file
     * 
     * @return
     */
    public List<String> readFile() {
        return readFile(this.file);
    }

    /**
     * @param file
     * @return
     */
    protected List<String> readFile(File file) {
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        for (Charset charset : charsets.values()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath(), charset);
                return lines;
            } catch (IOException e) {

            }
        }
        return null;
    }

    /**
     * @param fileReferentiel
     * @param fileLocal
     * @param deltas
     * @param comparaisonResult
     */
    public void reportDeltas(DIFFERENCELEVEL level, File fileReferentiel, File fileLocal, List<AbstractDelta<String>> deltas, ComparaisonResult comparaisonResult) {
        for (AbstractDelta<String> delta : deltas) {
            comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT, level, delta.toString());
        }

    }

}
