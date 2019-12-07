package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.content.ContentPath;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.bonitasoft.serverconfiguration.content.ContentTypeText;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfig;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class CollectOperation {
    
    public static BEvent EVENT_DIRECTORYNOTEXIST = new BEvent(CollectOperation.class.getName(), 1, Level.APPLICATIONERROR, "Directory not exist", "A directory is expected, not found", "Collect will not be complete", "Check error" );

    private BonitaConfigPath localBonitaConfig;
    CollectParameter collectParameter;
    
    public enum TYPECOLLECT { SETUP, TOMCAT,ANALYSIS };
    
     
    public CollectOperation() {
       
    }
    /**
     * 
     * @param localConfig
     * @param collectParameter
     * @param logStrategy
     * @return
     */
     public CollectResult collectParameters(BonitaConfigPath bonitaConfig,  CollectParameter collectParameter, COLLECTLOGSTRATEGY logStrategy) {

         
         this.localBonitaConfig = bonitaConfig;
         this.collectParameter = collectParameter;
         

         CollectResult collectResult = new CollectResult(localBonitaConfig.getRootPath(), logStrategy);
        
         if (collectParameter.collectSetup) {
             collectResult.setCurrentClassCollect(TYPECOLLECT.SETUP.toString());
             exploreLevel("/setup/platform_conf/current/", TYPECOLLECT.SETUP, collectResult );
         }
         
         if (collectParameter.collectServer) {
             collectResult.setCurrentClassCollect(TYPECOLLECT.TOMCAT.toString());
             exploreLevel("/server/",  TYPECOLLECT.TOMCAT, collectResult );
             }
         if (collectParameter.collectAnalysis) {
             collectResult.setCurrentClassCollect(TYPECOLLECT.ANALYSIS.toString());
             exploreLevel("/",  TYPECOLLECT.ANALYSIS, collectResult );
             List<Analyse> listAnalyses = Analyse.instanciateAllAnalyses();
             for (Analyse analyse : listAnalyses) {
                 analyse.analyse(collectResult);
                 collectResult.reportAnalysis(analyse);
             }
         }
             
         if (collectParameter.collectPlatformCharacteristic)  {
             collectResult.reportCharacteristics("javaruntimeversion", System.getProperty("java.runtime.version"));
         }
         return collectResult;
     }
     
     
     
     /* ******************************************************************************** */
     /*                                                                                  */
     /* private method */
     /*                                                                                  */
     /*                                                                                  */
     /* ******************************************************************************** */
     private List<String> listFilesLoad = Arrays.asList("jaas-standard.cfg", "login.conf", "web.xml", "context.xml", "bonita.xml", "server.xml");

     
     private void exploreLevel(String relativeLocalFolderPath, TYPECOLLECT typeCollect, CollectResult collectResult ) {

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
                 if (typeCollect == TYPECOLLECT.SETUP || typeCollect == TYPECOLLECT.ANALYSIS)
                 {
                     // do not explore the tenant_template_engine, tenant_template_portal, tenant_template_security_scripts
                     if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_engine") )
                         continue;
                     if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_portal") )
                         continue;
                     if (BonitaConfig.checkLocalisation(exploreFile, "/tenant_template_security_scripts") )
                         continue;
                     // if exploreFile is a tenant ID (so the PARENT is the tenants folder)
                     if (BonitaConfig.checkLocalisation(localFolderPath, "/current/tenants") )
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
                 }
                 if (typeCollect == TYPECOLLECT.TOMCAT)
                 {
                     // if exploreFile is a tenant ID (so the PARENT is the tenants folder)
                     if (BonitaConfig.checkLocalisation(localFolderPath, "/tomcat/conf") )
                     {
                         // folder is the tenant Id
                         collectResult.setCollector("conf" );
                     }
                     if (BonitaConfig.checkLocalisation(localFolderPath, "/tomcat/webapps/bonita") )
                     {
                         // folder is the tenant Id
                         collectResult.setCollector("bonita" );
                     }
                     if (BonitaConfig.checkLocalisation(localFolderPath, "/tomcat/webapps") && ! exploreFile.getName().equals("bonita") )
                         continue; // only on Bonita
                 }
             
                 // are we in the tenant directory? Then we have to split per tenant
                 exploreLevel( relativeLocalFolderPath + exploreFile.getName() + File.separator,typeCollect, collectResult);
             }
             else {
                 ContentType contentType = ContentType.getContentType(exploreFile);
                 if (typeCollect == TYPECOLLECT.SETUP || typeCollect ==  TYPECOLLECT.ANALYSIS)
                 {
                     // collect only properties files
                     if (contentType instanceof ContentTypeProperties) 
                     {
                         // we can collect now
                         KeyPropertiesReader keyReader = ((ContentTypeProperties)contentType).readKeys( collectParameter.hidePassword );
                         collectResult.reportProperties( ((ContentTypeProperties)contentType), keyReader );
                         
                     }
                 }
                 if (typeCollect == TYPECOLLECT.TOMCAT || typeCollect ==  TYPECOLLECT.ANALYSIS)
                 {
                     if (exploreFile.getName().equals("pom.properties"))
                         continue;
                     // collect XML, INI, CFG files
                     if (contentType instanceof ContentTypeProperties) 
                     {
                         // we can collect now
                         KeyPropertiesReader keyReader = ((ContentTypeProperties)contentType).readKeys( collectParameter.hidePassword );
                         collectResult.setCollector("conf" );
                         collectResult.reportProperties( ((ContentTypeProperties)contentType), keyReader );                         
                     }                     
                     if (contentType instanceof ContentTypeText && (listFilesLoad.contains(contentType.getFileName()))) 
                     {
                         // we can collect now
                         ContentTypeText contentTypetext = (ContentTypeText) contentType;
                         List<String> listLines = contentTypetext.readFile();
                         StringBuffer content = new StringBuffer();
                         for( String line : listLines )
                             content.append(line+"\n");
                         
                         collectResult.reportContent( contentTypetext, content.toString() );
                         
                     }
                    
                 }
             }
         }
        return;     
     }
     
}