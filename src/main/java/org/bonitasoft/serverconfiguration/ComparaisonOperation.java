package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.bonitasoft.serverconfiguration.content.ContentPath;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfig;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class ComparaisonOperation {
    
    ComparaisonParameter comparaisonParameter;
    BonitaConfigPath localBonitaConfig;
    BonitaConfig referentiel;
    ComparaisonResult comparaisonResult;
    
    /**
     * IDENTICAL : referentiel and LOCAL must be identical. Example, setup must have same files
     * LOCALFIRST : what may exist in the referencel is not so important, but LOCAL must be point.
     * Example, setup\platform_conf\initial\tenant_template_security_scripts : the referenciel has only example, if they don't exist in Local, no problem, but
     * any file in LOCAL must be visible
     * 
     * @author Firstname Lastname
     */
    private enum COMPARAISONPOLICY {
        IDENTICAL, LOCALFIRST
    }


    public ComparaisonResult compareWithReferentiel(BonitaConfigPath localBonitaConfig, BonitaConfig referentiel, ComparaisonParameter comparaisonParameter, LOGSTRATEGY logStrategy) {

            comparaisonResult = new ComparaisonResult(localBonitaConfig.getRootPath(), logStrategy);
        this.comparaisonParameter = comparaisonParameter;
        this.localBonitaConfig = localBonitaConfig;
        this.referentiel = referentiel;
        comparaisonResult.listEvents.addAll( referentiel.initialisation());


        if (!this.localBonitaConfig.getRootPath().exists()) {
            comparaisonResult.report(this.localBonitaConfig.getRootPath(), DIFFERENCESTATUS.ERROR, DIFFERENCELEVEL.CRITICAL, " DirectoryLocal [" + this.localBonitaConfig.getRootPath().getAbsolutePath() + "] does not exist");
            return comparaisonResult;
        }
        if (BEventFactory.isError( comparaisonResult.listEvents)) {
            return comparaisonResult;
        }
        // initialise the local repository now
        this.localBonitaConfig.initialisation();
        
        comparaisonResult.startComparaison();
        compareLevel(File.separator, null, COMPARAISONPOLICY.IDENTICAL);
        comparaisonResult.endComparaison();

        
        return comparaisonResult;
    }

    private List<String> listFilesIgnore = Arrays.asList("catalina.pid");

 
    /**
     * compare a level
     * 
     * @param localFolderPath : the current local file. end with a / (init : = /, then /server/)
     * @param referentielFolderPath : null if identitical, else may be different (specially in the tenants analysis)
     */
    private void compareLevel(String relativeLocalFolderPath, String relativeReferenceFolderPath, COMPARAISONPOLICY comparaisonPolicy) {
        try {
            
            
            comparaisonResult.info("----- Check [" + relativeLocalFolderPath + "] " + (relativeReferenceFolderPath != null ? " <->[" + relativeReferenceFolderPath + "]" : ""));
            // calculate the Local Complete Folder Path
            File localFolderPath = ConfigAPI.getFolder(this.localBonitaConfig.getRootPath(), relativeLocalFolderPath);

            // this may arrive when we expect some directory, like  "current/platform_engine"
            if (! localFolderPath.exists())
            {
                comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.REFERENTIELONLY,  DIFFERENCELEVEL.IMPORTANT, "Directory [" + localFolderPath.getAbsolutePath() + "]");
                return;
            }
            
            // ignore a temporaty folder
            if (localFolderPath.isDirectory() 
                    && comparaisonParameter.ignoreTemp 
                    && ( localFolderPath.getAbsolutePath().endsWith("temp") || localFolderPath.getAbsolutePath().endsWith("tmp")))
                return;
            // ignore the deferedJs
            if (BonitaConfig.checkLocalisation(localFolderPath, "server/webapps/bonita/portal/deferredjs") && comparaisonParameter.ignoreDeferedJs) {
                return;
            }
            
            
            ContentPath refContent = referentiel.getContentLevel(relativeReferenceFolderPath == null ? relativeLocalFolderPath : relativeReferenceFolderPath);
            if (! refContent.isContentExist())
            {
                comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.LOCALONLY,  DIFFERENCELEVEL.IMPORTANT, "Directory [" + (relativeReferenceFolderPath == null ? relativeLocalFolderPath : relativeReferenceFolderPath) + "]");
                return;
            }
          
            ContentPath localContent = this.localBonitaConfig.getContentLevel(relativeLocalFolderPath);
            // this content does not exist in the referentiel, no need to go under
            if (!refContent.isContentExist()) {
                comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.LOCALONLY, DIFFERENCELEVEL.IMPORTANT, " Directory [" + relativeLocalFolderPath + "] does not exist in referentiel");
                return;
            }
            // First, all references files which are not in the local are suspect
            if (comparaisonPolicy != COMPARAISONPOLICY.LOCALFIRST) {
                for (File refFile : refContent.getListFiles()) {
                    File localFile = localContent.getFile(refFile);
                    if (localFile == null) {
                        if (refFile.getAbsolutePath().endsWith(".log") && comparaisonParameter.ignoreLog)
                            continue;
                        if (refFile.getAbsolutePath().endsWith(".lic") && comparaisonParameter.ignoreLicence)
                            continue;
                        
                        // get the level of the missing file
                        DIFFERENCELEVEL level = DIFFERENCELEVEL.IMPORTANT;
                        ContentType contentType = ContentType.getContentType(refFile);
                        level= (contentType == null? DIFFERENCELEVEL.IMPORTANT : contentType.getLevel());
                    
                        comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.REFERENTIELONLY,  level,  (refFile.isDirectory()? "Directory":"File")+" [" + refFile.getName() + "]");
                        continue;
                    }
                    if (refFile.isDirectory() && localFile.isDirectory())
                        continue;
                    else if (refFile.isDirectory() || localFile.isDirectory())
                        comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.DIFFERENT,  DIFFERENCELEVEL.IMPORTANT,  "Referentiel isFolder? " + refFile.isDirectory() + " localIsFolder?" + localFile.isDirectory());
                    else {
                        // compare the file itself now                    
                        refContent.compareFile(refFile, localFile, comparaisonParameter, comparaisonResult);
                    }
                }
            }
            // second, detect all local file, not in the referentiel
            for (File localFile : localContent.getListFiles()) {
                File refFile = refContent.getFile(localFile);
                if (refFile == null) {
                    if (listFilesIgnore.contains(localFile.getName()))
                        continue;
                    if (localFile.getAbsolutePath().endsWith(".log") && comparaisonParameter.ignoreLog)
                        continue;
                    
                    // get the level of the missing file
                    DIFFERENCELEVEL level = DIFFERENCELEVEL.IMPORTANT;
                    ContentType contentType = ContentType.getContentType(localFile);
                    level= (contentType == null? DIFFERENCELEVEL.IMPORTANT : contentType.getLevel());
                    
                    comparaisonResult.report(localFolderPath, DIFFERENCESTATUS.LOCALONLY,  level, (localFile.isDirectory()? "Directory":"File")+ " [" + localFile.getName() + "]");
                    continue;
                }
            }

            // now, check all folders
            // Special management for the tenants : the comparaison must be 
            if (BonitaConfig.checkLocalisation(localFolderPath, "/setup/platform_conf") && comparaisonParameter.ignoreSetup) {
                return;
            }
            if (BonitaConfig.checkLocalisation(localFolderPath, "/setup/platform_conf") && comparaisonParameter.referentielIsABundle) {
                    // explore the setup
                    
                    
                // only the path current is necessary to check
                compareLevel(relativeLocalFolderPath + "current/platform_engine", relativeLocalFolderPath + "initial/platform_engine", comparaisonPolicy);
                compareLevel(relativeLocalFolderPath + "current/platform_portal", relativeLocalFolderPath + "initial/platform_portal", comparaisonPolicy);

                // then, now compare tenants per tenants
                File listTenants = ConfigAPI.getFolder(localFolderPath, "/current/tenants");
                if (!listTenants.isDirectory()) {
                    comparaisonResult.info("No active tenants");
                } else {
                    for (String tenantId : listTenants.list()) {
                        File folderTenant =  ConfigAPI.getFolder(listTenants, "/" + tenantId);

                        comparaisonResult.info("------- check tenant " + folderTenant.getName());
                        compareLevel(relativeLocalFolderPath + "current/tenants/" + tenantId + "/tenant_engine", relativeLocalFolderPath + "initial/tenant_template_engine", comparaisonPolicy);
                        compareLevel(relativeLocalFolderPath + "current/tenants/" + tenantId + "/tenant_portal", relativeLocalFolderPath + "initial/tenant_template_portal", comparaisonPolicy);
                        compareLevel(relativeLocalFolderPath + "current/tenants/" + tenantId + "/tenant_security_scripts", relativeLocalFolderPath + "initial/tenant_template_security_scripts", COMPARAISONPOLICY.LOCALFIRST);
                    }
                }
            
                
             } else if (BonitaConfig.checkLocalisation(localFolderPath, "webapps")) {
                // check only the bonita application
                compareLevel(relativeLocalFolderPath + "bonita" + File.separator, null, comparaisonPolicy);
            } else {
                for (String kid : localFolderPath.list()) {

                    File folderKid =  ConfigAPI.getFolder(localFolderPath, File.separator + kid);

                    if (folderKid.isDirectory()) {
                        if (kid.equals("logs") || kid.endsWith("request_key_utils"))
                            continue;
                        compareLevel(relativeLocalFolderPath + kid + File.separator, null, comparaisonPolicy);
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();

            comparaisonResult.severe( new BEvent( ComparaisonResult.EVENT_ERROR, e,  "Error " + e.getMessage() + " at " + exceptionDetails));
        }
    }
}
