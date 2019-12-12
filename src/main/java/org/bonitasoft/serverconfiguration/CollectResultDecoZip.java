package org.bonitasoft.serverconfiguration;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CollectResultDecoZip {

    public static BEvent EVENT_ZIP_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 1, BEvent.Level.ERROR, "Zip Error", "An severe error when zipping a file", "This file won't be in the final zip", "Check this file" );
    public static BEvent EVENT_ZIP_CLOSE_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 2, BEvent.Level.ERROR, "Close Zip Entry Error", "An severe error during zipping a file", "This file is probably corrupt", "Check the error" );
    private CollectResult collectResult;



    public CollectResultDecoZip(CollectResult collectResult) {
        this.collectResult = collectResult;

    }

    /**
     * get map of the collect operation
     *
     * @return
     */
    public ResultZip getZip(TYPECOLLECT typeCollect) {

        ResultZip resultZip = new ResultZip();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( baos );
        byte bytes[] = new byte[2048];

        CollectResult.ClassCollect classCollect = collectResult.getClassCollect(typeCollect.toString());

        for (String name : classCollect.listPropertiesReader.keySet()) {

            for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.listPropertiesReader.get( name )) {
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream( keyPropertiesReader.getFile() );
                    bis = new BufferedInputStream( fis );

                    zos.putNextEntry( new ZipEntry( keyPropertiesReader.getFile().getName() ) );

                    int bytesRead;
                    while ((bytesRead = bis.read( bytes )) != -1) {
                        zos.write( bytes, 0, bytesRead );
                    }
                } catch (Exception e) {
                    resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "fileName [" + (keyPropertiesReader.getFile() == null ? "no file" : keyPropertiesReader.getFile().getName())));
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
                    resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE_ENTRY, e, "fileName [" + (keyPropertiesReader.getFile() == null ? "no file" : keyPropertiesReader.getFile().getName())));
                }

            }
        }


        for (String tenantid : classCollect.listTenantsReader.keySet()) {

            for (ContentTypeProperties.KeyPropertiesReader keyPropertiesReader : classCollect.listTenantsReader.get( tenantid )) {
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream( keyPropertiesReader.getFile() );
                    bis = new BufferedInputStream( fis );

                    zos.putNextEntry( new ZipEntry( tenantid + "-" + keyPropertiesReader.getFile().getName() ) );

                    int bytesRead;
                    while ((bytesRead = bis.read( bytes )) != -1) {
                        zos.write( bytes, 0, bytesRead );
                    }
                } catch (Exception e) {
                    resultZip.listEvents.add( new BEvent( EVENT_ZIP_ENTRY, e, "fileName [" + (keyPropertiesReader.getFile() == null ? "no file" : keyPropertiesReader.getFile().getName())));
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
                    resultZip.listEvents.add( new BEvent( EVENT_ZIP_CLOSE_ENTRY, e, "fileName [" + (keyPropertiesReader.getFile() == null ? "no file" : keyPropertiesReader.getFile().getName())));
                }
            }
        }
        resultZip.zipContent = baos.toByteArray();
        return resultZip;
    }

    public static class ResultZip {
        public byte[] zipContent;
        public List<BEvent> listEvents = new ArrayList<BEvent>();
    }
}
