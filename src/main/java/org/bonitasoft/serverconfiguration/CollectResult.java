package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.analyse.Analyse;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.bonitasoft.serverconfiguration.content.ContentTypeText;

public class CollectResult {

    Logger logger = Logger.getLogger(CollectResult.class.getName());

    private static String logHeader = "BonitaServerConfiguration:";

    public static BEvent EVENT_ERROR = new BEvent(CollectResult.class.getName(), 1, Level.APPLICATIONERROR, "Error reported ", "An severe error is reported", "Analysis can continue, even if one error is reported", "Check parameters");

    public List<BEvent> listEvents = new ArrayList<BEvent>();

    public enum COLLECTLOGSTRATEGY {
        OUTALL, OUTSET, LOGALL, NOLOG
    }

    private COLLECTLOGSTRATEGY logStrategy;
    private List<BEvent> listErrors = new ArrayList<BEvent>();

    private File localRootPath;

    /**
     * different characteristics on the platform is collected, and save in this map
     */
    private Map<String, Object> platformCharacteristic = new HashMap<String, Object>();

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Construtor */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public CollectResult(File localRootPath, COLLECTLOGSTRATEGY logStrategy) {
        this.localRootPath = localRootPath;
        this.logStrategy = logStrategy;
    }

    public void severe(BEvent errorEvent) {
        listErrors.add(errorEvent);
        if (this.logStrategy == COLLECTLOGSTRATEGY.OUTALL)
            System.out.println(errorEvent.toString());
        if (this.logStrategy == COLLECTLOGSTRATEGY.LOGALL)
            logger.info(logHeader + errorEvent.toString());
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

    public void startComparaison() {
        startTime = System.currentTimeMillis();
    }

    public void endComparaison() {
        comparaisonTime = System.currentTimeMillis() - startTime;
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Class Collect */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public enum TYPECOLLECTOR { platform_engine, platform_portal, conf, bonita, defaultCollector };

    
    public class ClassCollect {

        public String name;
        /**
         * Key is the collector Name (platform_engine, platform_portal)
         * contents are KeyPropertiesReader
         */
        public Map<TYPECOLLECTOR, List<KeyPropertiesReader>> mapKeyPropertiesReader = new EnumMap(TYPECOLLECTOR.class);
        /**
         * KeyProperties attached to a tenant
         * - key is the TenantId
         * - contents are KeyPropertiesReader attached to the tenant.
         * Each tenant contains a collection of KeyProperties Reader
         */
        public Map<Long, List<KeyPropertiesReader>> listTenantsReader = new HashMap<>();
        /**
         * List of ContentTypeText. There are no content text per tenant
         */
        public Map<String, ContentTypeText> mapContentText = new HashMap<>();
        public Map<String, Object> mapCharacteristic = new HashMap<>();
        public List<Analyse> listAnalyses = new ArrayList<>();

        /**
         * mapContent key is the complete file name
         * 
         * @param fileName
         * @return
         */
        public ContentTypeText getContentTextByFileName(String fileName) {
            for (String completeName : mapContentText.keySet())
                if (completeName.endsWith(fileName))
                    return mapContentText.get(completeName);
            return null;
        }

        /**
         * @param fileName
         * @return
         */
        public KeyPropertiesReader getKeyPropertiesInCollector(TYPECOLLECTOR collector, String fileName) {

            List<KeyPropertiesReader> listProperties =  mapKeyPropertiesReader.get(collector);
            if (listProperties == null)
                return null;
            return getKeyPropertiesInList(listProperties, fileName);
        }
        /**
         * @param fileName
         * @return
         */
        public KeyPropertiesReader getKeyPropertiesInTenantByFileName(long tenantId, String fileName) {

            List<KeyPropertiesReader> listProperties = listTenantsReader.get(tenantId);
            if (listProperties == null)
                return null;
            return getKeyPropertiesInList(listProperties, fileName);
        }

        /**
         * return in the list
         * @param listProperties
         * @param fileName
         * @return
         */
        public KeyPropertiesReader getKeyPropertiesInList(Collection<KeyPropertiesReader> listProperties, String fileName) {

            for (KeyPropertiesReader keyPropertiesReader : listProperties) {
                if (keyPropertiesReader.getFileName().equals(fileName))
                    return keyPropertiesReader;
            }
            return null;
        }

        /**
         * @param fileName
         * @return
         */
        public KeyPropertiesReader getKeyPropertiesByFileName(String fileName) {

            for (List<KeyPropertiesReader> listKeys : mapKeyPropertiesReader.values()) {
                KeyPropertiesReader key = getKeyPropertiesInList(listKeys, fileName);
                if (key != null)
                    return key;
            }
            return null;

        }

        public Map<Long, List<KeyPropertiesReader>> getListTenantsReader() {
           return listTenantsReader;
        }

    }

    /**
     * Contains all items collected.
     * Key is the TYPECOLLECT enum
     */
    public Map<String, ClassCollect> mapClassCollect = new HashMap<>();

    /**
     * Only available to load values
     */
    private ClassCollect currentClassCollect;
    private List<KeyPropertiesReader> currentCollector;

    public void setCurrentClassCollect(String classCollectName) {
        if (!mapClassCollect.containsKey(classCollectName)) {
            ClassCollect classCollect = new ClassCollect();
            classCollect.name = classCollectName;
            mapClassCollect.put(classCollectName, classCollect);
        }
        currentClassCollect = mapClassCollect.get(classCollectName);
    }

    public ClassCollect getClassCollect(String classCollectName) {
        return mapClassCollect.get(classCollectName);

    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Collect report */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public void setCollectorTenant(long tenantId) {
        currentCollector = currentClassCollect.listTenantsReader.get(tenantId);
        if (currentCollector == null) {
            currentCollector = new ArrayList<>();
            currentClassCollect.listTenantsReader.put(tenantId, currentCollector);
        }
    }

    public void setCollector(TYPECOLLECTOR collectorName) {
        currentCollector = currentClassCollect.mapKeyPropertiesReader.get(collectorName);
        if (currentCollector == null) {
            currentCollector = new ArrayList<>();
            currentClassCollect.mapKeyPropertiesReader.put(collectorName, currentCollector);
        }
    }

    /**
     * report a set of properties.
     * nota : the report will be grouped base on the fileName
     * 
     * @param localFolderPath
     * @param status
     * @param Explanation
     */
    public void reportProperties(ContentTypeProperties contentType, KeyPropertiesReader keyPropertiesReaders) {
        listEvents.addAll(keyPropertiesReaders.listEvents);
        if (currentCollector != null)
            currentCollector.add(keyPropertiesReaders);
        else {
            List<KeyPropertiesReader> defaultCollector = currentClassCollect.mapKeyPropertiesReader.get( TYPECOLLECTOR.defaultCollector );
            if (defaultCollector==null) {
                defaultCollector = new ArrayList<>();            
                currentClassCollect.mapKeyPropertiesReader.put( TYPECOLLECTOR.defaultCollector, defaultCollector );
            }
            defaultCollector.add(keyPropertiesReaders);
        }
    }

    public void reportContentText(ContentTypeText contentText) {
        currentClassCollect.mapContentText.put(contentText.getCompleteFileName(), contentText);
    }

    /**
     * report a new characteristic on the platform
     * 
     * @param name
     * @param value
     */
    public void reportCharacteristics(String name, Object value) {
        currentClassCollect.mapCharacteristic.put(name, value);

    }

    public void reportAnalysis(Analyse analyse) {
        currentClassCollect.listAnalyses.add(analyse);
    }

}
