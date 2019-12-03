package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;

public class CollectResultDecoMap {
    private CollectResult collectResult;
    private String title;
    private File localFolderName; 
    
    public CollectResultDecoMap(CollectResult collectResult, String title, File localFolderName) {
        this.collectResult = collectResult;
        this.title = title;
        this.localFolderName = localFolderName;

    }
    /**
     * get map of the collect operation
     * @return
     */
    public Map<String,Object> getMap() {
        Map<String, Object> result= new HashMap<String,Object>();
        
        Map<String,Object> listPropertiesMap = new HashMap<String,Object>();
        result.put( "properties", listPropertiesMap);

        for (String name : collectResult.listPropertiesReader.keySet())
        {
            List<Map<String,Object>> listEngineKeys = new ArrayList<Map<String,Object>>();
            listPropertiesMap.put( name, listEngineKeys);
            for (KeyPropertiesReader keyPropertiesReader :  collectResult.listPropertiesReader.get( name ))
            {
                listEngineKeys.add( keyPropertiesReader.getMap());
            }
        }
        // tenants now
        Map<String,Object> listTenantMap = new HashMap<String,Object>();
        result.put( "tenants", listTenantMap);

        for (String tenantid : collectResult.listTenantsReader.keySet())
        {
            List<Map<String,Object>> listEngineKeys = new ArrayList<Map<String,Object>>();
            listTenantMap.put( tenantid, listEngineKeys);
            for (KeyPropertiesReader keyPropertiesReader :  collectResult.listTenantsReader.get( tenantid ))
            {
                listEngineKeys.add( keyPropertiesReader.getMap());
            }
        }
        return result;
    }
}
