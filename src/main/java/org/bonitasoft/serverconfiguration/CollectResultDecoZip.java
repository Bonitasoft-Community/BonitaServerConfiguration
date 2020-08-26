package org.bonitasoft.serverconfiguration;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CollectResultDecoZip {

    private final static BEvent EVENT_ZIP_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 1, BEvent.Level.ERROR, "Zip Error", "An severe error when zipping a file", "This file won't be in the final zip", "Check this file" );
    private final static BEvent EVENT_ZIP_CLOSE_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 2, BEvent.Level.ERROR, "Close Zip Entry Error", "An severe error during zipping a file", "This file is probably corrupt", "Check the error" );

    private final static BEvent EVENT_ZIP_CLOSE = new BEvent( CollectResultDecoZip.class.getName(), 3, BEvent.Level.ERROR, "Close Zip Error", "An severe error during closing zip file", "This file is probably corrupt", "Check the error" );

    private CollectResult collectResult;
    private FileInputStream fis = null;
    private BufferedInputStream bis = null;


    public CollectResultDecoZip(CollectResult collectResult) {
        this.collectResult = collectResult;

    }

    public ResultZip getZip(List<TYPECOLLECT> listTypeCollect) {
        ResultZip resultZip = new ResultZip();
        resultZip.zipContent = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( resultZip.zipContent );

        ResultZip resultZipZos = addToZip( listTypeCollect, zos );
        resultZip.listEvents.addAll( resultZipZos.listEvents );


        //remember close it
        try {
            zos.close();
        } catch (IOException e) {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE, e, "" ) );

        }

        return resultZip;

    }

    /**
     * Complete an existing ZipOutStream, to let the caller to add more file in the ZIP
     *
     * @return
     */
    public ResultZip addToZip(List<TYPECOLLECT> listTypeCollect, ZipOutputStream zos) {

        ResultZip resultZip = new ResultZip();
        for (TYPECOLLECT typeCollect : listTypeCollect) {
            CollectResult.ClassCollect classCollect = collectResult.getClassCollect( typeCollect.toString() );

            for (CollectResult.TYPECOLLECTOR name : classCollect.mapKeyPropertiesReader.keySet()) {

                for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.mapKeyPropertiesReader.get( name )) {

                    addFileToZip( zos, keyPropertiesReader.getFile(), resultZip );
                }
            }


            for (Long tenantid : classCollect.listTenantsReader.keySet()) {

                for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.listTenantsReader.get( tenantid )) {
                    addFileToZip( zos, keyPropertiesReader.getFile(), resultZip );
                }
            }
            // content Text
            for (String contentFileName : classCollect.mapContentText.keySet()) {
                addFileToZip( zos, classCollect.mapContentText.get( contentFileName ).getFile(), resultZip );
            }
        }

        return resultZip;
    }

    /**
     * Complete an existing ZipOutStream, to let the caller to add more file in the ZIP
     *
     * @return
     */
    public ResultZip addDirectoryToZip(ZipOutputStream zos, File fileToZip, ResultZip resultZip, String parentDirectoryName, FileInputStream fis, BufferedInputStream bis) {

        // Build the full path of files in the result zip file
        String completeFilePath = fileToZip.getName();
        if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
            completeFilePath = parentDirectoryName + "/" + fileToZip.getName();
        }


        // Use a buffer to speed the zip process
        byte bytes[] = new byte[10240];
        if (fileToZip == null) {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, "fileName [no file]" ) );
            return resultZip;
        }

        if (fileToZip.isDirectory()) {

            try {
                // Create a directory in the zip

                for (File file : fileToZip.listFiles()) {
                    addDirectoryToZip( zos, file, resultZip, completeFilePath, fis, bis );
                }
            } catch (Exception e) {
                resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "DirectoryName [" + completeFilePath + "] - fileName [" + fileToZip.getName() + "]" ) );
            }
        } else {

            try {
                // Create a file in the zip
                fis = new FileInputStream( fileToZip );
                bis = new BufferedInputStream( fis );

                zos.putNextEntry( new ZipEntry( completeFilePath ) );

                int bytesRead;
                while ((bytesRead = bis.read( bytes )) != -1) {
                    zos.write( bytes, 0, bytesRead );
                }

            } catch (Exception e) {
                resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "fileName [" + completeFilePath + "] - fileName [" + fileToZip.getName() + "]" ) );
            }

        }
        return resultZip;
    }

    public ResultZip addDirectoriesToZip(ZipOutputStream zos, File fileToZip, ResultZip resultZip, String parentDirectoryName) {
        ResultZip resultZipFinal = addDirectoryToZip( zos, fileToZip, resultZip, parentDirectoryName, fis, bis );
        resultZipFinal = closeZipThread( zos, fis, bis, resultZipFinal, fileToZip );
        return resultZipFinal;
    }


    public ResultZip closeZipThread(ZipOutputStream zos, FileInputStream fis, BufferedInputStream bis, ResultZip resultZip, File fileToZip) {
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
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE_ENTRY, e, "fileName [" + fileToZip.getName() + "]" ) );
        }
        return resultZip;
    }

    /**
     * add the File in the ZIP in construction
     *
     * @param zos
     * @param fileToZip
     * @param resultZip
     */
    private void addFileToZip(ZipOutputStream zos, File fileToZip, ResultZip resultZip) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte bytes[] = new byte[2048];
        if (fileToZip == null) {
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, "fileName [no file]" ) );
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
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "fileName [" + fileToZip.getName() + "]" ) );
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
            resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE_ENTRY, e, "fileName [" + fileToZip.getName() + "]" ) );
        }
    }

    public static class ResultZip {
        public ByteArrayOutputStream zipContent;
        public List<BEvent> listEvents = new ArrayList<BEvent>();
    }
}