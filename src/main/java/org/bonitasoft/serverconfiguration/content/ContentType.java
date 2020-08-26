package org.bonitasoft.serverconfiguration.content;

import java.io.File;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public abstract class ContentType {

    
    File file;
    
    public ContentType(File file ) {
        this.file = file;
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Abstract method */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public abstract void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult);

    public abstract String getName();

    public abstract boolean matchFile(File file);
    /**
     * ComparaisonParameter is given, in order to calculate a correct level. For example, change the database.propertie is expected if you compare with a Bundle 
     * @param comparaisonParameter
     * @return
     */
    public abstract DIFFERENCELEVEL getLevel( ComparaisonParameter comparaisonParameter);

    /* ******************************************************************************** */
    /*                                                                                  */
    /* getter/setter                                                                    */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    public String getFileName() {
        return file.getName();
    }
    
    public File getFile( ) {
        return file;
    }
    public String getCompleteFileName() {
        return file.getAbsolutePath();
    }
    
    public String getPrefixFile() {
        String fileName = file.getName();
        int pos=fileName.lastIndexOf(".");
        if (pos!=-1)
            return fileName.substring(pos+1);
        return fileName;
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Register all different contentType */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public static ContentType getContentType(File file) {
        if ((new ContentTypeProperties(file)).matchFile(file))
            return new ContentTypeProperties(file);

        if ((new ContentTypeXml(file)).matchFile(file))
            return new ContentTypeXml(file);

        if ((new ContentTypeText(file)).matchFile(file))
            return new ContentTypeText(file);

        if ((new ContentTypeJar(file)).matchFile(file))
            return new ContentTypeJar(file);

        if ((new ContentTypeBat(file)).matchFile(file))
            return new ContentTypeBat(file);

        if ((new ContentTypeSh(file)).matchFile(file))
            return new ContentTypeSh(file);

        if ((new ContentTypeImage(file)).matchFile(file))
            return new ContentTypeImage(file);
        
        if ((new ContentTypePo(file)).matchFile(file))
            return new ContentTypePo(file);

        if ((new ContentTypeZip(file)).matchFile(file))
            return new ContentTypeZip(file);

        return new ContentTypeOther(file);
    }
}
