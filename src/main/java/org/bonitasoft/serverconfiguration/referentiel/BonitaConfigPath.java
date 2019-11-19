package org.bonitasoft.serverconfiguration.referentiel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.content.ContentPath;

public class BonitaConfigPath extends BonitaConfig {

    public static BonitaConfigPath getInstance(File rootPath) {
        return new BonitaConfigPath(rootPath);

    }

    public static BonitaConfigPath getInstance(String rootPath) {
        return new BonitaConfigPath(new File(rootPath));

    }

    protected File rootPath;

    protected BonitaConfigPath(File rootPath) {
        this.rootPath = rootPath;
    }

    public List<BEvent> initialisation() {
        List<BEvent> listEvents = new ArrayList<BEvent>();
        return listEvents;

    }

    @Override
    public ContentPath getContentLevel(String relativePath) {
        ContentPath contentLevel = new ContentPath();
        File folderPath = new File(rootPath.getAbsolutePath() + relativePath);
        /** a directory may exist in one config, and not in the referentel */
        if (!folderPath.isDirectory()) {
            contentLevel.setContentExist(false);
            return contentLevel;
        }

        // load each file in this level
        for (String fileName : folderPath.list()) {

            contentLevel.addFile(new File(folderPath.getAbsolutePath() + "/" + fileName));
        }

        return contentLevel;
    }

}
