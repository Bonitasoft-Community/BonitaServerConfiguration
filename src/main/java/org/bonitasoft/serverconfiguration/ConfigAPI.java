package org.bonitasoft.serverconfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.content.ContentPath;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfig;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class ConfigAPI {

    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    public static BEvent EVENT_PULLERROR = new BEvent(ConfigAPI.class.getName(), 1, Level.APPLICATIONERROR, "Pull Error", "An error arrived during a setup pull", "Analysis is performed on the last setup pull done", "Check error" );

    /**
     *  create a ConfigAPI, using a local config. So, the BonitaConfigPath is the local Config API
     * @param rootPath : ROOT of the bundle/Application. under, we have server and config.
     * @return
     */
    public static ConfigAPI getInstance(BonitaConfigPath localBonitaConfig) {
        return new ConfigAPI(localBonitaConfig);
    }

    private BonitaConfigPath localBonitaConfig = null;
    
    private ConfigAPI(BonitaConfigPath localBonitaConfig) {
        this.localBonitaConfig =localBonitaConfig;

    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Operation on configuration */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    
    
    /**
     * execute a setupPull, to ensure the local configuration is up to date
     * 
     * @return
     */
    public List<BEvent> setupPull() {
        List<BEvent> listEvents = new ArrayList<BEvent>();
        List<BEvent> listErrors = new ArrayList<BEvent>();

        // change the current directory to this.rootPath/setup
        //System.out.println(System.getProperty("user.dir"));
        System.setProperty("user.dir", getFolder(this.localBonitaConfig.getRootPath(), "/setup").getAbsolutePath());
        //System.out.println(System.getProperty("user.dir"));
        // Java 7
        // System.out.println(Paths.get("").toAbsolutePath().toString());

        // String[] args = new String[] { "pull" };
        File setupFile = new File( this.localBonitaConfig.getRootPath()+"/setup");
        String[] listCommands = new String[]  {"cmd / c setup.bat pull", "setup.sh pull"};
        boolean success=false;
        for (String command: listCommands)
        {
            try
            {
                // PlatformSetupApplication.main(args);
                Runtime rt = Runtime.getRuntime();
                Process process = rt.exec(command, null, setupFile);
                
                
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String resultCommand="";
                /*String s = null;
                while ((s = stdInput.readLine()) != null) {
                    resultCommand+=s;
                }
                */
                
                logger.info("Result setup pull "+resultCommand);
                success=true;
                break;
            } catch(Exception e ) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionDetails = sw.toString();
                listErrors.add( new BEvent( EVENT_PULLERROR, e,  "Command["+command+"] User dir["+setupFile.getAbsolutePath()+"] " + e.getMessage() + " at " + exceptionDetails));
            }
        }
        if (! success)
        {
            listEvents.addAll(listErrors);
        }
        return listEvents;
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Collect */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public static class CollectParameter {

        public boolean collectSetup = true;
        public boolean collectServer=true;
        public boolean hidePassword=true;
        public boolean collectPlatformCharacteristic=true;
        public boolean useLocalFile=true;
        public File localFile=null;
        
        public static CollectParameter getInstanceFromMap(Map<String,Object> parameters) {
            CollectParameter comparaisonParameter = new CollectParameter();
            comparaisonParameter.collectSetup = getBoolean(parameters.get("collectSetup"), false);
            comparaisonParameter.collectServer = getBoolean(parameters.get("collectServer"), true);
            comparaisonParameter.useLocalFile = getBoolean(parameters.get("useLocalFile"), true);
            
            if (parameters.containsKey("localFile"))
                comparaisonParameter.localFile = new File( (String) parameters.get("localFile"));
            
            return comparaisonParameter;
        }
       
    }

   /**
    * 
    * @param localConfig
    * @param collectParameter
    * @param logStrategy
    * @return
    */
    public CollectResult collectParameters( CollectParameter collectParameter, COLLECTLOGSTRATEGY logStrategy) {

        CollectOperation collectOperation = new CollectOperation();
        return collectOperation.collectParameters(localBonitaConfig, collectParameter, logStrategy);
    }
    

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Comparaison */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
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

    public static class ComparaisonParameter {

        public boolean compareContextXml = false;
        public boolean ignoreImage=false;
        public boolean ignoreBonitaTranslationFile = false;
        public boolean ignoreTemp=false;
        public boolean ignoreLog=false;
        public boolean ignoreLicence=true;
        public boolean ignoreSetup=false;
        public boolean ignoreDeferedJs=true;
        public boolean referentielIsABundle = true;
        
        public boolean useLocalFile=true;
        public File localFile;
        public File referenceFile;
        
        public static ComparaisonParameter getInstanceFromMap(Map<String,Object> parameters) {
            ComparaisonParameter comparaisonParameter = new ComparaisonParameter();
            comparaisonParameter.compareContextXml = getBoolean(parameters.get("compareContextXml"), false);
            comparaisonParameter.ignoreImage = getBoolean(parameters.get("ignoreImage"), true);
            comparaisonParameter.ignoreBonitaTranslationFile = getBoolean(parameters.get("ignoreBonitaTranslationFile"), true);
            comparaisonParameter.ignoreTemp = getBoolean( parameters.get("ignoreTemp"), true);
            comparaisonParameter.ignoreLicence = getBoolean( parameters.get("ignoreLicence"), true);
            comparaisonParameter.ignoreSetup = getBoolean( parameters.get("ignoreSetup"), true);
            comparaisonParameter.ignoreDeferedJs = getBoolean( parameters.get("ignoreDeferedJs"), true);
            comparaisonParameter.useLocalFile = getBoolean( parameters.get("useLocalFile"), true);
            comparaisonParameter.referentielIsABundle = getBoolean( parameters.get("referentielIsABundle"), true);

            comparaisonParameter.localFile = new File( (String) parameters.get("comparaisonFile"));
            comparaisonParameter.referenceFile = new File( (String) parameters.get("referenceFile"));
            
            return comparaisonParameter;
        }
        public static boolean getBoolean(Object value, boolean defaultValue ) { 
            if (value==null)
                return defaultValue;
            try {
                return Boolean.valueOf( value.toString() );
            }
            catch(Exception e)
            {
                return defaultValue;
            }
            
        }
    }

    // local information - attention, then this method is not threadsafe. 
    ComparaisonParameter comparaisonParameter;
    ComparaisonResult comparaisonResult;
    BonitaConfig referentiel;

    public ComparaisonResult compareWithReferentiel(BonitaConfig referentiel, ComparaisonParameter comparaisonParameter, LOGSTRATEGY logStrategy) {

        comparaisonResult = new ComparaisonResult(this.localBonitaConfig.getRootPath(), logStrategy);
        this.comparaisonParameter = comparaisonParameter;
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
            File localFolderPath = getFolder(this.localBonitaConfig.getRootPath(), relativeLocalFolderPath);

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
                File listTenants = getFolder(localFolderPath, "/current/tenants");
                if (!listTenants.isDirectory()) {
                    comparaisonResult.info("No active tenants");
                } else {
                    for (String tenantId : listTenants.list()) {
                        File folderTenant = getFolder(listTenants, "/" + tenantId);

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

                    File folderKid = getFolder(localFolderPath, File.separator + kid);

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

    protected static File getFolder(File folder, String subPath) {
        return new File(folder.getAbsolutePath() + subPath);
    }
    private static boolean getBoolean(Object value, boolean defaultValue ) { 
        if (value==null)
            return defaultValue;
        try {
            return Boolean.valueOf( value.toString() );
        }
        catch(Exception e)
        {
            return defaultValue;
        }
        
    }
}
