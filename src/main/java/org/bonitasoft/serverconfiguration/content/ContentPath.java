package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

/**
 * describe the content at a path level
 * Example : all files at setup/platform_conf
 * Content files AND subfolder
 */
public class ContentPath {

    public Map<String, File> contentFilefolder = new HashMap<String, File>();

    public ContentPath() {
    }

    public String toString() {
        return contentFilefolder.keySet().toString();
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Manage content */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    private boolean contentExist = true;

    public boolean isContentExist() {
        return contentExist;
    }

    public void setContentExist(boolean contentExist) {
        this.contentExist = contentExist;
    }

    public void addFile(File file) {
        // Let's don't take care for some name
        if (file.isDirectory() && file.getName().equals("temp"))
            return;
        if (file.isDirectory() && file.getName().equals("logs"))
            return;

        contentFilefolder.put(file.getName(), file);

    }

    public Collection<File> getListFiles() {
        return contentFilefolder.values();
    }

    public File getFile(File searchFile) {
        return contentFilefolder.get(searchFile.getName());
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Comparaison tool */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * compare tool between two Files
     * 
     * @param fileReferentiel
     * @param fileLocal
     * @param comparaisonResult
     */
    public void compareFile(File fileReferentiel, File fileLocal, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        ContentType contentType = ContentType.getContentType(fileReferentiel);
        if (contentType == null)
            return;
        comparaisonResult.countFile(fileLocal, contentType);
        
        contentType.compareFile(fileLocal, fileReferentiel,  comparaisonParameter, comparaisonResult);
    }

}
