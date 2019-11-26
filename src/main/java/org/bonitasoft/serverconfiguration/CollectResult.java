package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.ComparaisonResult.ComparaisonItem;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;


public class CollectResult {
    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    private static String logHeader = "BonitaServerConfiguration:";
    
    public static BEvent EVENT_ERROR = new BEvent(CollectResult.class.getName(), 1, Level.APPLICATIONERROR, "Error reported ", "An severe error is reported", "Analysis can continue, even if one error is reported", "Check parameters" );
            
    public List<BEvent> listEvents = new ArrayList<BEvent>();

    public enum COLLECTLOGSTRATEGY {
        OUTALL, OUTSET, LOGALL, NOLOG
    }
    private COLLECTLOGSTRATEGY logStrategy;
    private List<BEvent> listErrors= new ArrayList<BEvent>();
    
    private File localRootPath;
    
    /**
     * different characteristics on the platform is collected, and save in this map
     */
    private Map<String,Object> platformCharacteristic = new HashMap<String,Object>();
 
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Construtor                                                                       */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    
    public CollectResult(File localRootPath, COLLECTLOGSTRATEGY logStrategy) {
        this.localRootPath = localRootPath;
        this.logStrategy = logStrategy;
    }
    
    public void severe(BEvent errorEvent) {
        listErrors.add( errorEvent );
        if (this.logStrategy== COLLECTLOGSTRATEGY.OUTALL)
            System.out.println( errorEvent.toString() );
        if (this.logStrategy== COLLECTLOGSTRATEGY.LOGALL)
            logger.info( logHeader+  errorEvent.toString());   
    }
    
    public List<BEvent> getErrors() {
        return listErrors;
    };

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Keep track for statistics */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public long startTime;
    public long comparaisonTime;
    
    public void startComparaison ()
    {
        startTime = System.currentTimeMillis();
    }
    public void endComparaison ()
    {
        comparaisonTime = System.currentTimeMillis() - startTime;
    }
    
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Collect report */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * report a difference.
     * nota : the report will be grouped base on the fileName
     * @param localFolderPath
     * @param status
     * @param Explanation
     */
    public void reportProperties(File localFolderPath, String Key, String value, String defaultValue, String explanation) {
    }
    
    /**
     * report a new characteristic on the platform
     * @param name
     * @param value
     */
    public void reportCharacteristics( String name, Object value) {
        platformCharacteristic.put( name, value);
        
    }

    public Map<String, Object> getPlatformCharacteristic() {
        return platformCharacteristic;
    }

    
}
