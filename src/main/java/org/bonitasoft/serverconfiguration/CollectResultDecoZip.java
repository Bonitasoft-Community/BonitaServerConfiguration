package org.bonitasoft.serverconfiguration;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CollectResultDecoZip {

    public static BEvent EVENT_ZIP_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 1, BEvent.Level.ERROR, "Zip Error", "An severe error when zipping a file", "This file won't be in the final zip", "Check this file" );
    public static BEvent EVENT_ZIP_CLOSE_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 2, BEvent.Level.ERROR, "Close Zip Entry Error", "An severe error during zipping a file", "This file is probably corrupt", "Check the error" );
    
    public static BEvent EVENT_ZIP_CLOSE = new BEvent( CollectResultDecoZip.class.getName(), 3, BEvent.Level.ERROR, "Close Zip Error", "An severe error during closing zip file", "This file is probably corrupt", "Check the error" );

    private CollectResult collectResult;
    
    



    public CollectResultDecoZip(CollectResult collectResult) {
        this.collectResult = collectResult;

    }

    public static class ResultZip {
        public ByteArrayOutputStream zipContent;
        public List<BEvent> listEvents = new ArrayList<BEvent>();
    }
    
    public ResultZip getZip(List<TYPECOLLECT> listTypeCollect) {
        ResultZip resultZip = new ResultZip();
        resultZip.zipContent = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( resultZip.zipContent );
        
        ResultZip resultZipZos = addToZip( listTypeCollect,zos);
        resultZip.listEvents.addAll( resultZipZos.listEvents);
        
        
        //remember close it
         try {
             zos.close();
         } catch (IOException e) {
             resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE, e, ""));

         }
         
         return resultZip;
        
    }
    /**
     * Complete an existing ZipOutStream, to let the caller to add more file in the ZIP
     *
     * @return
     */
    public ResultZip addToZip(List<TYPECOLLECT> listTypeCollect,ZipOutputStream zos ) {

        ResultZip resultZip = new ResultZip();
        for (TYPECOLLECT typeCollect : listTypeCollect) 
        {
            CollectResult.ClassCollect classCollect = collectResult.getClassCollect(typeCollect.toString());
    
            for (String name : classCollect.listPropertiesReader.keySet()) {
    
                for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.listPropertiesReader.get( name )) {
                    
                    addFileToZip(zos, keyPropertiesReader.getFile(), resultZip);
                }
            }
    
    
            for (String tenantid : classCollect.listTenantsReader.keySet()) {
    
                for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.listTenantsReader.get( tenantid )) {
                    addFileToZip(zos, keyPropertiesReader.getFile(), resultZip);
                }
            }
            // content Text
            for (String contentFileName : classCollect.mapContentText.keySet()) {
                addFileToZip(zos, classCollect.mapContentText.get(contentFileName).getFile(), resultZip);
            }
        }
     
        return resultZip;
    }

  
    
    
    /**
     * add the File in the ZIP in construction
     * @param zos
     * @param fileToZip
     * @param resultZip
     */
    private void addFileToZip(ZipOutputStream zos, File fileToZip, ResultZip resultZip  ) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte bytes[] = new byte[2048];
        if (fileToZip==null)
        {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY,"fileName [no file]"));
            return;
        }
        try {
            fis = new FileInputStream( fileToZip );
            bis = new BufferedInputStream( fis );

            zos.putNextEntry( new ZipEntry( fileToZip.getName() ) );

            int bytesRead;
            while ((bytesRead = bis.read( bytes )) != -1) {
                zos.write( bytes, 0, bytesRead );
            }
        } catch (Exception e) {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "fileName [" + fileToZip.getName()+"]"));
        }
        try {
            if (zos != null) {
                zos.closeEntry();
            }
            if (bis != null) {
                bis.close();
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Exception e) {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE_ENTRY, e, "fileName [" + fileToZip.getName()+"]"));
        }
    }
}
