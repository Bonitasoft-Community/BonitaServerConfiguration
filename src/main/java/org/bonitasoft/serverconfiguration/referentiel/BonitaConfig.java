package org.bonitasoft.serverconfiguration.referentiel;

import java.io.File;
import java.util.List;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.content.ContentPath;

public abstract class BonitaConfig {

    public abstract List<BEvent> initialisation();

    public abstract ContentPath getContentLevel(String folderPath);

    /**
     * check if the path finish by the endpath
     * BUT on windows, absolutepath is \\analysis\\7.8.4\\BonitaSubscription-7.8.4_SIT_110119-2
     * on Linux, it's                  /analysis/7.8.4/BonitaSubscription-7.8.4_SIT_110119-2
     * File.pathSep
     * 
     * @param folder
     * @param endPath
     * @return
     */
    public static boolean checkLocalisation(File folder, String endPath) {
        int pos = -1;
        do {
            pos = endPath.indexOf("/", pos);
            if (pos > -1) {
                endPath = endPath.substring(0, pos) + File.separator + endPath.substring(pos + 1);
                pos++;
            }
        } while (pos != -1);
        return folder.getAbsolutePath().endsWith(endPath);
    }
}
