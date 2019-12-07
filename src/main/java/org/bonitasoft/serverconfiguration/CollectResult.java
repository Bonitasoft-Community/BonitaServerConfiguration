package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;


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
    /* Class Collect                                                                    */ 
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    
    public class ClassCollect {
        public String name;
        public Map<String,List<KeyPropertiesReader>> listPropertiesReader = new HashMap<String,List<KeyPropertiesReader>>();
        public Map<String,List<KeyPropertiesReader>> listTenantsReader = new HashMap<String,List<KeyPropertiesReader>>();
        public Map<String, String> mapContentReader = new HashMap<String,String>();
        public Map<String, Object> mapCharacteristic = new HashMap<String,Object>();
        public List<Analyse> listAnalyses = new ArrayList<Analyse>();
        
        /**
         * mapContent key is the complete file name
         * @param fileName
         * @return
         */
        public String getContentByFileName(String fileName ) {
            for (String completeName : mapContentReader.keySet())
                if (completeName.endsWith(fileName))
                    return mapContentReader.get( completeName);
            return null;
        }
        
    }
    public Map<String, ClassCollect> mapClassCollect = new HashMap<String, ClassCollect >();
   
    public ClassCollect currentClassCollect;
    public List<KeyPropertiesReader> currentCollector;
    
    public void setCurrentClassCollect( String classCollectName )
    {
        if (! mapClassCollect.containsKey(classCollectName)) 
        {
            ClassCollect classCollect = new ClassCollect();
            classCollect.name = classCollectName;
            mapClassCollect.put(classCollectName,  classCollect);
        }
        currentClassCollect= mapClassCollect.get(classCollectName );
    }
    
    public ClassCollect getClassCollect(String classCollectName )
    {
        return mapClassCollect.get(classCollectName );
        
    }
    
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Collect report */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public void setCollectorTenant(String tenantId ) 
    {
        currentCollector = currentClassCollect.listTenantsReader.get( tenantId );
        if (currentCollector==null)
        {
            currentCollector = new ArrayList<KeyPropertiesReader>();
            currentClassCollect.listTenantsReader.put( tenantId, currentCollector);
        }
    }
    
    public void setCollector(String collectorName ) 
    {
        currentCollector = currentClassCollect.listPropertiesReader.get( collectorName );
        if (currentCollector==null)
        {
            currentCollector = new ArrayList<KeyPropertiesReader>();
            currentClassCollect.listPropertiesReader.put( collectorName, currentCollector);
        }
    }
   
    /**
     * report a set of properties.
     * nota : the report will be grouped base on the fileName
     * @param localFolderPath
     * @param status
     * @param Explanation
     */
    public void reportProperties(ContentTypeProperties contentType, KeyPropertiesReader keyPropertiesReaders) {
        listEvents.addAll( keyPropertiesReaders.listEvents);
        if (currentCollector!=null)
            currentCollector.add( keyPropertiesReaders);
    }
    public void reportContent(ContentType contentType, String content) {
        currentClassCollect.mapContentReader.put(contentType.getCompleteFileName(), content);
    }
    
    /**
     * report a new characteristic on the platform
     * @param name
     * @param value
     */
    public void reportCharacteristics( String name, Object value) {
        currentClassCollect.mapCharacteristic.put( name, value);
        
    }

    
    public void reportAnalysis( Analyse analyse ) {
        currentClassCollect.listAnalyses.add(analyse);
    }
  

    
}
