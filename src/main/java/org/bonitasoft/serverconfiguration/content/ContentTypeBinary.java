package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.logging.Logger;

public abstract class ContentTypeBinary extends ContentType {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());

    public ContentTypeBinary(File file ) {
        super(file );
    }
    protected String getSignature(File fileToGetSignature) {
        String checksum = "";
        try {
            //Use MD5 algorithm
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");

            //Get the checksum
            checksum = getFileChecksum(md5Digest, fileToGetSignature);

        } catch (Exception e) {
            checksum = "Date_" + String.valueOf(fileToGetSignature.lastModified());
        } finally {
            /* logger.info( " CheckSum [" + fileToGetSignature.getName() + "] is [" + checksum + "] in "+ (System.currentTimeMillis() - timeStart) + " ms"); */
        }
        //see checksum
        return checksum;

    }

    /**
     * calulate the checksum
     * 
     * @param digest
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        } ;

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

}
