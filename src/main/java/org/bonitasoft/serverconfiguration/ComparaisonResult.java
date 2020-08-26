package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.bonitasoft.serverconfiguration.content.ContentType;

public class ComparaisonResult {

    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    private static String logHeader = "BonitaServerConfiguration:";
    
    public static BEvent EVENT_ERROR = new BEvent(ComparaisonResult.class.getName(), 1, Level.APPLICATIONERROR, "Error reported ", "An severe error is reported", "Analysis can continue, even if one error is reported", "Check parameters" );
            
    public List<BEvent> listEvents = new ArrayList<BEvent>();

    public enum LOGSTRATEGY {
        OUTALL, OUTDIFFERENCE, LOGALL, LOGDIFFERENCE, NOLOG
    }
    private LOGSTRATEGY logStrategy;

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Sub class                                                                        */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * Keep the comparaisonItem at the File Level. So, when a file has a list of different, it's a list of Counpound
     * @author Firstname Lastname
     *
     */
    public static class ComparaisonCoumpound {
        public DIFFERENCESTATUS differenceStatus;
        public DIFFERENCELEVEL level;
        public String explanation;
        public String localValue;
        public String referentielValue;
        public boolean withValues=false;
        
       
    }
    /**
     * reference one change. Maybe one properties, a jar with a different version.
     * 
     * @author Firstname Lastname
     */
    public static class ComparaisonItem {

        public File file;
        public List<ComparaisonCoumpound> listCompounds = new ArrayList<>();
        public void addCompound( DIFFERENCESTATUS status, DIFFERENCELEVEL level, String localValue, String referentielValue, String explanation, boolean withValues ) {
            ComparaisonCoumpound compound = new ComparaisonCoumpound();
            compound.differenceStatus = status;
            compound.level = level;
            compound.explanation = explanation;
            compound.localValue = localValue;
            compound.referentielValue= referentielValue;
            compound.withValues = withValues;
            listCompounds.add( compound );
        }
    }

    private Map<String,ComparaisonItem> listComparaisonsItems = new HashMap<>();

    private List<BEvent> listErrors= new ArrayList<>();
    /**
     * 
     */
    private File localRootPath;

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Construtor                                                                       */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public ComparaisonResult(File localRootPath, LOGSTRATEGY logStrategy) {
        this.localRootPath = localRootPath;
        this.logStrategy = logStrategy;
    }

    /**
     * report information / avancement
     * 
     * @param info
     */
    public void info(String info) {
        if (this.logStrategy== LOGSTRATEGY.OUTALL)
            System.out.println(info);   
        if (this.logStrategy== LOGSTRATEGY.LOGALL)
            logger.info( logHeader+  info);   
    }
    public void infoDifference(String info) {
        if (this.logStrategy== LOGSTRATEGY.OUTDIFFERENCE)
            System.out.println(info);   
        if (this.logStrategy== LOGSTRATEGY.LOGDIFFERENCE)
            logger.info( logHeader+  info);   
    }

    public void infoMain(String info) {
        if (this.logStrategy== LOGSTRATEGY.OUTDIFFERENCE)
            System.out.println(info);   
        if (this.logStrategy== LOGSTRATEGY.LOGDIFFERENCE)
            logger.info( logHeader+  info);   
    }

    public void severe(BEvent errorEvent) {
        listErrors.add( errorEvent );
        if (this.logStrategy== LOGSTRATEGY.OUTDIFFERENCE)
            System.out.println( errorEvent.toString() );
        if (this.logStrategy== LOGSTRATEGY.LOGDIFFERENCE)
            logger.info( logHeader+  errorEvent.toString());   
    }

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
    /**
     * Count the value of item per type
     */
    private Map<String, Long> countPerContentType = new HashMap<String, Long>();

    public void countFile(File file, ContentType contentType) {
        Long counter = countPerContentType.get(contentType.getName());
        if (counter == null)
            counter = 0L;
        counter = Long.valueOf(counter + 1);
        countPerContentType.put(contentType.getName(), counter);
    }

   
    public Map<String, Long>  getCountPerContentType() { 
        return countPerContentType;
    }
    public Map<String,ComparaisonItem> getListComparaisonsItems() { 
        return listComparaisonsItems;
    }
    public List<BEvent> getErrors() {
        return listErrors;
    };
    public long getTime() {
        return comparaisonTime;
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Report */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public enum DIFFERENCESTATUS {
        REFERENTIELONLY, LOCALONLY, DIFFERENT, ERROR
    }
    /**
     * the level of change are qualified
     * For example, a difference in a JAR is more important than a difference in the logging.properties. 
     * CRITICAL : this change must not be present (jar file is different for example)
     * IMPORTANT : part of the configuration : a properties change is different for example
     * MEDIUM : this is noticable, but not so important (example, memory site is changed in the setup.sh). Changes are exepected here
     * LOWER : not very important : a image is different for exemple.
     *
     */
    public enum DIFFERENCELEVEL {
        CRITICAL(0), IMPORTANT(1), MEDIUM(2), CONFIGURATION(3), LOWER(4), EXPECTED(5), WORKFILE(6);
        private int severity;
        DIFFERENCELEVEL( int severity ) {
            this.severity = severity;
        }
        public boolean isUpperThan( DIFFERENCELEVEL diff) {
            return diff.severity > severity;
        }
        public int getSeverity() {
            return severity;
        }
    }

    /**
     * report a difference.
     * nota : the report will be grouped base on the fileName
     * @param localFolderPath
     * @param status
     * @param Explanation
     */
    public void report(File localFolderPath, DIFFERENCESTATUS status, DIFFERENCELEVEL level, String explanation, ComparaisonParameter comparaisonParameter) {
        ComparaisonItem item = getComparaisonItem(localFolderPath);
        
        DIFFERENCELEVEL requalifiedLevel = requalifyLevel( localFolderPath,status,level);
        item.addCompound( status, requalifiedLevel, null, null, explanation, false );
        infoDifference("   ** Difference ** " + getRelativePath(localFolderPath) + " : " + status.toString() + " " + explanation);
    }
    /**
     * 
     * @param localFolderPath
     * @param localValue
     * @param explanation
     * @param withValues : may have a sense or not. Detected that a binary is different? values does not have a sense. A properties files? values has a sense
     */
    public void reportLocalOnly(File localFolderPath,  DIFFERENCELEVEL level, String localValue, String explanation, boolean withValues) {
        ComparaisonItem item = getComparaisonItem(localFolderPath);
       
        DIFFERENCELEVEL requalifiedLevel = requalifyLevel( localFolderPath, DIFFERENCESTATUS.LOCALONLY, level);
        item.addCompound( DIFFERENCESTATUS.LOCALONLY, requalifiedLevel, localValue, null, explanation,withValues );
        infoDifference("   ** Difference ** " + getRelativePath(localFolderPath) + " : " + DIFFERENCESTATUS.LOCALONLY.toString() + " " + explanation);
    }
    /**
     * 
     * @param localFolderPath
     * @param referentielValue
     * @param explanation
     * @param withValues : may have a sense or not. Detected that a binary is different? values does not have a sense. A properties files? values has a sense
     */
    public void reportReferentielOnly(File localFolderPath,  DIFFERENCELEVEL level, String referentielValue, String explanation, boolean withValues) {
        ComparaisonItem item = getComparaisonItem(localFolderPath);
        
        DIFFERENCELEVEL requalifiedLevel = requalifyLevel( localFolderPath, DIFFERENCESTATUS.REFERENTIELONLY, level);
        item.addCompound( DIFFERENCESTATUS.REFERENTIELONLY, requalifiedLevel, null, referentielValue, explanation, withValues );
        infoDifference("   ** Difference ** " + getRelativePath(localFolderPath) + " : " + DIFFERENCESTATUS.REFERENTIELONLY.toString() + " " + explanation);
    }
    /**
     * Report a difference
     * 
     * @param localFolderPath
     * @param status
     * @param Explanation
     * @param withValues : may have a sense or not. Detected that a binary is different? values does not have a sense. A properties files? values has a sense
     */
    public void reportDifference(File localFolderPath,  DIFFERENCELEVEL level, String localValue, String referentielValue, String explanation, boolean withValues) {
        ComparaisonItem item = getComparaisonItem(localFolderPath);
        
        DIFFERENCELEVEL requalifiedLevel = requalifyLevel( localFolderPath, DIFFERENCESTATUS.DIFFERENT, level);
        item.addCompound(  DIFFERENCESTATUS.DIFFERENT, requalifiedLevel, localValue, referentielValue, explanation, withValues );
        infoDifference("   ** Difference ** " + getRelativePath(localFolderPath) + " : " + DIFFERENCESTATUS.DIFFERENT.toString() + " " + explanation + " LocalValue[" + localValue + "] ReferentielValue[" + referentielValue + "]");
    }

    /**
     * @param localFolderPath
     * @param e
     */
    public void reportError(File localFolderPath, Exception e, String explanation) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionDetails = sw.toString();
        ComparaisonItem item = getComparaisonItem(localFolderPath);
        
        DIFFERENCELEVEL requalifiedLevel = requalifyLevel( localFolderPath, DIFFERENCESTATUS.ERROR, DIFFERENCELEVEL.IMPORTANT);
        item.addCompound(  DIFFERENCESTATUS.ERROR, requalifiedLevel, null, null, " Error :" + e.getMessage() + " at " + exceptionDetails, false );
        
        severe( new BEvent( EVENT_ERROR,e, "   File:" + getRelativePath(localFolderPath) + " " + explanation+ " Error:" + e.getMessage() + " at " + exceptionDetails));
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Tools */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public String getRelativePath(File file) {
        int pos = localRootPath.getAbsolutePath().length();
        return file.getAbsolutePath().substring(pos);
    }
    
    private ComparaisonItem getComparaisonItem( File file )
    {
        ComparaisonItem item = listComparaisonsItems.get(file.getAbsolutePath() );
        if (item!=null)
            return item;
        item = new ComparaisonItem();
        item.file = file;
        listComparaisonsItems.put(file.getAbsolutePath(),  item);
        return item;
    }
    
    /**
     * Requalify some file. Attention, the default level should be fixed item per item, in the ContentType<xxx>.java
     * @param localFolderPath
     * @param status
     * @param level
     * @return
     */
    private DIFFERENCELEVEL requalifyLevel( File localFolderPath,  DIFFERENCESTATUS status, DIFFERENCELEVEL level) {
        DIFFERENCELEVEL requalify = level;
        
            
        return requalify;
    }

}
