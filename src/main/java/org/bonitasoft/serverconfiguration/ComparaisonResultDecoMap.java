package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.serverconfiguration.ComparaisonResult.ComparaisonCoumpound;
import org.bonitasoft.serverconfiguration.ComparaisonResult.ComparaisonItem;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;

public class ComparaisonResultDecoMap {
    Logger logger = Logger.getLogger(ConfigAPI.class.getName());

    // private static String logHeader = "BonitaServerConfiguration.DecoMap:";
    
  
    private ComparaisonResult comparaisonResult;
    private String title;
    private File localFolderName; 
    private File referentielFolderName;
    
    public ComparaisonResultDecoMap(ComparaisonResult comparaisonResult, String title, File localFolderName, File referentielFolderName) {
        this.comparaisonResult = comparaisonResult;
        this.title = title;
        this.localFolderName = localFolderName;
        this.referentielFolderName = referentielFolderName;
    }
    
    public Map<String,Object> getMap() {
        Map<String, Object> result= new HashMap<String,Object>();
        result.put("countpercontent", comparaisonResult.getCountPerContentType());
        result.put("errors", comparaisonResult.getErrors());
        result.put("title", title);
        
        Map<String,ComparaisonItem> listComparaisonsItems = comparaisonResult.getListComparaisonsItems();
        
        result.put("diffNb", listComparaisonsItems.size());
        result.put("diffTimeInMs", comparaisonResult.getTime() );
        List<Map<String,Object>> listComparaisons = new ArrayList<Map<String,Object>> ();
        result.put("comparaisons", listComparaisons );
        
            for (ComparaisonItem comparaisonItem : listComparaisonsItems.values()) {
                Map<String,Object> comparaisonItemMap = new HashMap<String,Object> ();
                listComparaisons.add( comparaisonItemMap );
                comparaisonItemMap.put("compoundSize", comparaisonItem.listCompounds.size());
                comparaisonItemMap.put("compoundName", getDirectFileName( comparaisonItem.file) );
                List<Map<String,Object>> listCompounds = new ArrayList<Map<String,Object>> ();
                comparaisonItemMap.put("listCompound", listCompounds );
               
                DIFFERENCELEVEL levelComparaison=DIFFERENCELEVEL.LOWER;
                
                for (ComparaisonCoumpound compound : comparaisonItem.listCompounds) {
                    Map<String,Object> compoundMap = new HashMap<String,Object> ();
                    listCompounds.add( compoundMap );
                    compoundMap.put("status", compound.differenceStatus.toString());
                    if (compound.level.isUpperThan( levelComparaison))
                        levelComparaison =compound.level; 
                    compoundMap.put("level", compound.level.toString());
                    compoundMap.put("explanation", compound.explanation);
                    compoundMap.put("withValues", compound.withValues);
                    if ((compound.differenceStatus == DIFFERENCESTATUS.LOCALONLY ||compound.differenceStatus == DIFFERENCESTATUS.DIFFERENT) && compound.withValues) {
                        compoundMap.put("localValue", compound.localValue);
                    }
                    if ((compound.differenceStatus == DIFFERENCESTATUS.REFERENTIELONLY ||compound.differenceStatus == DIFFERENCESTATUS.DIFFERENT) && compound.withValues) {
                        compoundMap.put("referentielValue", compound.referentielValue);
                    }
                }
                comparaisonItemMap.put("level", levelComparaison.toString() );
                comparaisonItemMap.put("levelnum", levelComparaison.getSeverity() );

            }
            

            Collections.sort(listComparaisons, new Comparator<Map<String,Object>>()
               {
                 public int compare(Map<String,Object> s1,
                         Map<String,Object> s2)
                 {
                     String compound1 = (String) s1.get("compoundName");
                     String compound2 = (String) s2.get("compoundName");
                     if (compound1==null)
                         return 1;
                     return compound1.compareTo(compound2);

                 }
               });

            return result;
   } // end getMap
    
    
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

}
