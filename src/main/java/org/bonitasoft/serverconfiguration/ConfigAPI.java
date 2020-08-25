package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfig;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

public class ConfigAPI {

    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    public static BEvent eventPullError = new BEvent(ConfigAPI.class.getName(), 1, Level.APPLICATIONERROR, "Pull Error", "An error arrived during a setup pull", "Analysis is performed on the last setup pull done", "Check error");
    public static BEvent eventPullTimeout = new BEvent(ConfigAPI.class.getName(), 2, Level.APPLICATIONERROR, "Pull Timeout", "setup pull does not return in the waiting time", "Setup can't be checjout from the server", "Check the setup command");

    /**
     * create a ConfigAPI, using a local config. So, the BonitaConfigPath is the local Config API
     * 
     * @param rootPath : ROOT of the bundle/Application. under, we have server and config.
     * @return
     */
    public static ConfigAPI getInstance(BonitaConfigPath localBonitaConfig) {
        return new ConfigAPI(localBonitaConfig);
    }

    private BonitaConfigPath localBonitaConfig = null;

    private ConfigAPI(BonitaConfigPath localBonitaConfig) {
        this.localBonitaConfig = localBonitaConfig;

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
        List<BEvent> listEvents = new ArrayList<>();
        List<BEvent> listErrors = new ArrayList<>();

        // change the current directory to this.rootPath/setup
        //System.out.println(System.getProperty("user.dir"));
        System.setProperty("user.dir", getFolder(this.localBonitaConfig.getRootPath(), "/setup").getAbsolutePath());
        //System.out.println(System.getProperty("user.dir"));
        // Java 7
        // System.out.println(Paths.get("").toAbsolutePath().toString());

        // String[] args = new String[] { "pull" };
        File setupFile = new File(this.localBonitaConfig.getRootPath() + "/setup/");
        String[] listCommands = new String[] { "cmd /c setup.bat pull", "./setup.sh pull" };
        boolean success = false;
        for (String command : listCommands) {
            try {
                // PlatformSetupApplication.main(args);
                Runtime rt = Runtime.getRuntime();
                
            
            
                Process process = rt.exec(command, null, setupFile);
                Worker worker = new Worker(process);
                worker.start();
                try {
                  worker.join(20000);
                  if (worker.exit != null)
                  {
                      logger.info("Result setup pull " + process.exitValue());
                      success = true;
                      break;
                  }
                  else {
                      listEvents.add(new BEvent(eventPullTimeout, "Timeout: 20 s"));
                      throw new TimeoutException();
                  }
                } catch(InterruptedException ex) {
                  worker.interrupt();
                  Thread.currentThread().interrupt();
                } finally {
                  process.destroyForcibly();
                }
              

                // BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));


                /*
                 * String resultCommand = "";
                 * String s = null;
                 * while ((s = stdInput.readLine()) != null) {
                 * resultCommand+=s;
                 * }
                 */

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionDetails = sw.toString();
                listErrors.add(new BEvent(eventPullError, e, "Command[" + command + "] User dir[" + setupFile.getAbsolutePath() + "] " + e.getMessage() + " at " + exceptionDetails));
            }
        }
        if (!success) {
            listEvents.addAll(listErrors);
        }
        return listEvents;
    }

    
/**
 * Monitor Thread 
 * @author Firstname Lastname
 *
 */
private static class Worker extends Thread {
  private final Process process;
  private Integer exit;
  private Worker(Process process) {
    this.process = process;
  }
  public void run() {
    try { 
      exit = process.waitFor();
    } catch (InterruptedException ignore) {
      return;
    }
  }  
}

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Collect */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public static class CollectParameter {

        public Set<TYPECOLLECT> listTypeCollect = new HashSet<TYPECOLLECT>();

        public boolean hidePassword = true;
        public boolean useLocalFile = true;
        public boolean doSetupPull = true;
        public File localFile = null;
        public long tenantId = 1;

        public static CollectParameter getInstanceFromMap(Map<String, Object> parameters) {
            CollectParameter comparaisonParameter = new CollectParameter();
            comparaisonParameter.listTypeCollect = new HashSet<>();

            boolean collectOnlyForAnalisys=true;
            if (getBoolean(parameters.get("collectSetup"), false)) {
                comparaisonParameter.listTypeCollect.add(TYPECOLLECT.SETUP);
                collectOnlyForAnalisys=false;
            }
            if (getBoolean(parameters.get("collectServer"), false)) {
                comparaisonParameter.listTypeCollect.add(TYPECOLLECT.SERVER);
                collectOnlyForAnalisys=false;
            }
            if (getBoolean(parameters.get("collectAnalysis"), false)) 
                comparaisonParameter.listTypeCollect.add(TYPECOLLECT.ANALYSIS);

            // analysis need to check if password is compliance
            if (collectOnlyForAnalisys)
                comparaisonParameter.hidePassword=false;
            
            comparaisonParameter.useLocalFile = getBoolean(parameters.get("useLocalFile"), true);
            comparaisonParameter.doSetupPull = getBoolean(parameters.get("doSetupPull"), true);

            if (parameters.containsKey("localFile"))
                comparaisonParameter.localFile = new File((String) parameters.get("localFile"));

            return comparaisonParameter;
        }

    }

    /**
     * @param localConfig
     * @param collectParameter
     * @param logStrategy
     * @return
     */
    public CollectResult collectParameters(CollectParameter collectParameter, COLLECTLOGSTRATEGY logStrategy, BonitaAccessor apiAccessor) {

        CollectOperation collectOperation = new CollectOperation();
        return collectOperation.collectParameters(localBonitaConfig, collectParameter, logStrategy, apiAccessor);
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Comparaison */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public static class ComparaisonParameter {

        public boolean compareContextXml = false;
        public boolean ignoreImage = false;
        public boolean ignoreBonitaTranslationFile = false;
        public boolean ignoreTemp = false;
        public boolean ignoreLog = false;
        public boolean ignoreLicence = true;
        public boolean ignoreSetup = false;
        public boolean ignoreDeferedJs = true;
        
        // Referentiel
        public boolean referentielIsABundle = true;
        public File referenceFile;

        // application to compare
        public boolean useLocalServer = true;
        public File applicationFile;
        public boolean doSetupPull = true;
        
        
        
        public static ComparaisonParameter getInstanceFromMap(Map<String, Object> parameters) {
            ComparaisonParameter comparaisonParameter = new ComparaisonParameter();
            comparaisonParameter.compareContextXml = getBoolean(parameters.get("compareContextXml"), false);
            comparaisonParameter.ignoreImage = getBoolean(parameters.get("ignoreImage"), true);
            comparaisonParameter.ignoreBonitaTranslationFile = getBoolean(parameters.get("ignoreBonitaTranslationFile"), true);
            comparaisonParameter.ignoreTemp = getBoolean(parameters.get("ignoreTemp"), true);
            comparaisonParameter.ignoreLicence = getBoolean(parameters.get("ignoreLicence"), true);
            comparaisonParameter.ignoreSetup = getBoolean(parameters.get("ignoreSetup"), true);
            comparaisonParameter.ignoreDeferedJs = getBoolean(parameters.get("ignoreDeferedJs"), true);
            
            // Reference
            comparaisonParameter.referentielIsABundle = getBoolean(parameters.get("referentielIsABundle"), true);
            if (parameters.get("referenceFile")!=null)
                comparaisonParameter.referenceFile = new File((String) parameters.get("referenceFile"));

            // application
            comparaisonParameter.useLocalServer = getBoolean(parameters.get("useLocalServer"), true);
            if (parameters.get("applicationFile") !=null)
                comparaisonParameter.applicationFile = new File((String) parameters.get("applicationFile"));
            comparaisonParameter.doSetupPull = getBoolean(parameters.get("doSetupPull"), true);

            return comparaisonParameter;
        }

        public static boolean getBoolean(Object value, boolean defaultValue) {
            if (value == null)
                return defaultValue;
            try {
                return Boolean.valueOf(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }

        }
    }

    // local information - attention, then this method is not threadsafe. 
    ComparaisonParameter comparaisonParameter;
    ComparaisonResult comparaisonResult;
    BonitaConfig referentiel;

    public ComparaisonResult compareWithReferentiel(BonitaConfig referentiel, ComparaisonParameter comparaisonParameter, LOGSTRATEGY logStrategy) {

        ComparaisonOperation comparaisonOperation = new ComparaisonOperation();
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

    private static boolean getBoolean(Object value, boolean defaultValue) {
        if (value == null)
            return defaultValue;
        try {
            return Boolean.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }

    }
}
