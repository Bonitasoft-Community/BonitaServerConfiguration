package org.bonitasoft.serverconfiguration.referentiel;

import java.io.File;

import org.bonitasoft.serverconfiguration.content.ContentPath;

public class BonitaConfigBundle extends BonitaConfigPath {

    public static BonitaConfigBundle getInstance(File rootPath) {
        return new BonitaConfigBundle( rootPath);
    }

    private BonitaConfigBundle(File rootPath) {
        super(rootPath);
    }

    // get 
    public ContentPath getContentLevel(String relativePath) {

        File folderRelative = new File(relativePath);

        if (checkLocalisation(folderRelative, "/server/webapps/bonita")) {
            // maybe there is the bonita.war...

            // then, unzip it
            // for the moment
            return super.getContentLevel(relativePath);
        } else
            return super.getContentLevel(relativePath);

    }
}
