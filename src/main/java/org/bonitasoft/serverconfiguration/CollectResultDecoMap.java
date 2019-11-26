package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CollectResultDecoMap {
    private CollectResult collectResult;
    private String title;
    private File localFolderName; 
    
    public CollectResultDecoMap(CollectResult collectResult, String title, File localFolderName) {
        this.collectResult = collectResult;
        this.title = title;
        this.localFolderName = localFolderName;

    }
    
    public Map<String,Object> getMap() {
        Map<String, Object> result= new HashMap<String,Object>();
        return result;
    }
}
