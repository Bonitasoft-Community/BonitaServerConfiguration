package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult.ComparaisonCoumpound;
import org.bonitasoft.serverconfiguration.ComparaisonResult.ComparaisonItem;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;

public class ComparaisonResultDecoLog {
    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    private static String logHeader = "BonitaServerConfiguration.DecoLog:";
    
  
    private ComparaisonResult comparaisonResult;
    private String title;
    private File localFolderName; 
    private File referentielFolderName;
    
    public ComparaisonResultDecoLog(ComparaisonResult comparaisonResult, String title, File localFolderName, File referentielFolderName) {
        this.comparaisonResult = comparaisonResult;
        this.title = title;
        this.localFolderName = localFolderName;
        this.referentielFolderName = referentielFolderName;
    }

    public void log( LOGSTRATEGY logStrategy) {
         Map<String, Long> countPerContentType=  comparaisonResult.getCountPerContentType();
         Map<String,ComparaisonItem> listComparaisonsItems = comparaisonResult.getListComparaisonsItems();
         info(logStrategy," ######################################################### "  );
         info(logStrategy," #");
         info(logStrategy," # "+title  );
         info(logStrategy," #");
         info(logStrategy," ######################################################### "  );
         if (comparaisonResult.getErrors().size()>0)
         {
             info(logStrategy," ######### Errors"  );
             for (String info : comparaisonResult.getErrors())
             {
                 info(logStrategy,info);
             }
         }
         
         info(logStrategy," ######### Count per type"  );
            for (String key : countPerContentType.keySet())
                info(logStrategy,"  "+key+": "+countPerContentType.get( key ));
            info(logStrategy," #########"  );
            info(logStrategy,"Number of difference:"+listComparaisonsItems.size()+" found in "+comparaisonResult.getTime()+" ms"  );
            for (ComparaisonItem comparaisonItem : listComparaisonsItems.values()) {
                info(logStrategy,"");
                info(logStrategy,"    ("+comparaisonItem.listCompounds.size()+")  "+getDirectFileName( comparaisonItem.file) );
                for (ComparaisonCoumpound compound : comparaisonItem.listCompounds) {
                    info(logStrategy,"         "+logCompound(compound));
                }
        }

    }
    
    public String logCompound( ComparaisonCoumpound compound) {
        String report =compound.differenceStatus.toString() + " ("+compound.level.toString().toLowerCase()+") " + getLimitedString(compound.explanation);
        if (compound.differenceStatus == DIFFERENCESTATUS.LOCALONLY && compound.withValues) {
            report+=" LocalValue[" + getLimitedString(compound.localValue) + "]";
        }
        if (compound.differenceStatus == DIFFERENCESTATUS.REFERENTIELONLY && compound.withValues) {
            report+=" ReferentielValue[" + getLimitedString(compound.referentielValue) + "]";
        }
        if (compound.differenceStatus == DIFFERENCESTATUS.DIFFERENT && compound.withValues)
            report+=" LocalValue[" + getLimitedString(compound.localValue) + "] ReferentielValue[" + getLimitedString(compound.referentielValue) + "]";
        
        return report;
    }
    
    public String getLimitedString( String value )
    {
        if (value==null || value.length() < 100)
            return value;
        return value.substring(0,100)+"...";
    }
    /**
     * 
     * @param fileName
     * @return
     */
    public String getDirectFileName(File file) {
        String directFileName="";
        if (file.getAbsolutePath().startsWith(localFolderName.getAbsolutePath())) {
            directFileName=file.getAbsolutePath().substring(localFolderName.getAbsolutePath().length());
        }
        else if (file.getAbsolutePath().startsWith(referentielFolderName.getAbsolutePath())) {
             directFileName= file.getAbsolutePath().substring(referentielFolderName.getAbsolutePath().length());
        }
        else {
                 directFileName=file.getAbsolutePath();
        }
       if (directFileName.length()==0)
           return File.separator;
       return directFileName;
    }
    

public void info(LOGSTRATEGY logStrategy, String info) {
    if ((logStrategy== LOGSTRATEGY.OUTALL) || (logStrategy== LOGSTRATEGY.OUTDIFFERENCE))
        System.out.println(info);   
    if ((logStrategy== LOGSTRATEGY.LOGALL) || (logStrategy== LOGSTRATEGY.LOGDIFFERENCE))
        logger.info( logHeader+  info);   
}
}
