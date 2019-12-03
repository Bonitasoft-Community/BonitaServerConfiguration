package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.content.ContentPath;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfig;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class CollectOperation {
    
    public static BEvent EVENT_DIRECTORYNOTEXIST = new BEvent(CollectOperation.class.getName(), 1, Level.APPLICATIONERROR, "Directory not exist", "A directory is expected, not found", "Collect will not be complete", "Check error" );

    private BonitaConfigPath localBonitaConfig;
    /**
     * 
     * @param localConfig
     * @param collectParameter
     * @param logStrategy
     * @return
     */
     public CollectResult collectParameters(BonitaConfigPath bonitaConfig,  CollectParameter collectParameter, COLLECTLOGSTRATEGY logStrategy) {

         this.localBonitaConfig = bonitaConfig;
         CollectResult collectResult = new CollectResult(localBonitaConfig.getRootPath(), logStrategy);
        
         if (collectParameter.collectSetup) {
             exploreLevel("/setup/platform_conf/current/", collectResult );
         }
         
         if (collectParameter.collectServer) {
             }
         
         if (collectParameter.collectPlatformCharacteristic)  {
             collectResult.reportCharacteristics("javamachine", "");
         }
         return collectResult;
     }
     
     
     
     /* ******************************************************************************** */
     /*                                                                                  */
     /* private method */
     /*                                                                                  */
     /*                                                                                  */
     /* ******************************************************************************** */

     private void exploreLevel(String relativeLocalFolderPath, CollectResult collectResult ) {

         File localFolderPath = ConfigAPI.getFolder(localBonitaConfig.getRootPath(), relativeLocalFolderPath);

         ContentPath localContent = localBonitaConfig.getContentLevel(relativeLocalFolderPath);
         // this content does not exist in the referentiel, no need to go under
         if (!localContent.isContentExist()) {
             collectResult.severe(new BEvent( EVENT_DIRECTORYNOTEXIST, " Directory [" + relativeLocalFolderPath + "] does not exist"));
             return;
         }
         for (File exploreFile : localContent.getListFiles()) {
             if (exploreFile.isDirectory())
             {
                 
                 
                 // do not explore the tenant_template_engine, tenant_template_portal, tenant_template_security_scripts
                 if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_engine") )
                     continue;
                 if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_portal") )
                     continue;
                 if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_security_scripts") )
                     continue;
                 if (BonitaConfig.checkLocalisation(exploreFile, "/current/tenants") )
                 {
                     // folder is the tenant Id
                     collectResult.setCollectorTenant(exploreFile.getName() );
                 }
                 if (BonitaConfig.checkLocalisation(exploreFile, "/current/platform_engine") )
                 {
                     collectResult.setCollector("platform_engine" );
                 }
                 if (BonitaConfig.checkLocalisation(exploreFile, "/current/platform_portal") )
                 {
                     collectResult.setCollector("platform_portal" );
                 }
                     
             
                 // are we in the tenant directory? Then we have to split per tenant
                     exploreLevel( relativeLocalFolderPath + exploreFile.getName() + File.separator, collectResult);
             }
             else {
                 ContentType contentType = ContentType.getContentType(exploreFile);
                 if (contentType instanceof ContentTypeProperties) 
                 {
                     // we can collect now
                     KeyPropertiesReader keyReader = ((ContentTypeProperties)contentType).readKeys();
                     collectResult.reportProperties( ((ContentTypeProperties)contentType), keyReader );
                     
                 }
             }
         }
        return;     
     }
     
}
