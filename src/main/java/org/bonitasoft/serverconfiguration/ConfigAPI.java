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
                /* Process process = */ rt.exec(command, null, setupFile);
                
                
                // BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
        public boolean collectAnalysis=true;
        
        public boolean hidePassword=true;
        public boolean collectPlatformCharacteristic=true;
        public boolean useLocalFile=true;
        public File localFile=null;
        public long tenantId =1;
        
        public static CollectParameter getInstanceFromMap(Map<String,Object> parameters) {
            CollectParameter comparaisonParameter = new CollectParameter();
            comparaisonParameter.collectSetup = getBoolean(parameters.get("collectSetup"), false);
            comparaisonParameter.collectServer = getBoolean(parameters.get("collectServer"), true);
            comparaisonParameter.collectAnalysis = getBoolean(parameters.get("collectAnalysis"), true);
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

        ComparaisonOperation comparaisonOperation = new ComparaisonOperation( );
        return comparaisonOperation.compareWithReferentiel(localBonitaConfig, referentiel, comparaisonParameter, logStrategy);
    }


    /* ******************************************************************************** */
    /*                                                                                  */
    /* Tools */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */


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
