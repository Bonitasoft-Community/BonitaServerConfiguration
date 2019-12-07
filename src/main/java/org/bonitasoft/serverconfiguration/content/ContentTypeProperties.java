package org.bonitasoft.serverconfiguration.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.serverconfiguration.CollectOperation;
import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;

public class ContentTypeProperties extends ContentType {

    Logger logger = Logger.getLogger(ContentTypeProperties.class.getName());
    public static BEvent EVENT_DECODEPROPERTIESERROR = new BEvent(ContentTypeProperties.class.getName(), 1, Level.APPLICATIONERROR, "Decode properties file", "A error arrives during the decoding of an Properties files", "This propertie file is not completely decoded", "Check error");

    public ContentTypeProperties(File file) {
        super(file);
    }

    @Override
    public String getName() {
        return "properties";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".properties");
    }

    @Override
    public DIFFERENCELEVEL getLevel() {
        if (file.getName().contains("logging.properties"))
            return DIFFERENCELEVEL.MEDIUM;
        if (file.getName().contains("custom-permissions-mapping.properties"))
            return DIFFERENCELEVEL.MEDIUM;
        return DIFFERENCELEVEL.IMPORTANT;
    }

    /**
     * Compare file
     */
    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 properties files
        comparaisonResult.info("    [" + fileLocal.getName() + "] (Properties) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");

        // change in logging.properties ? It's a MEDIUM level
        DIFFERENCELEVEL level = getLevel();
        // change in a txt, or a md is not important

        try {
            InputStream inputReferentiel = new FileInputStream(fileReferentiel);
            InputStream inputLocal = new FileInputStream(fileLocal);

            Properties propReferentiel = new Properties();
            propReferentiel.load(inputReferentiel);

            Properties propLocal = new Properties();
            propLocal.load(inputLocal);

            // first pass: all value in REFERENTIEL and not in LOCAL
            for (Object keyReferentiel : propReferentiel.keySet()) {
                String propertyReferentiel = propReferentiel.getProperty(keyReferentiel.toString());
                String propertyLocal = propLocal.getProperty(keyReferentiel.toString());
                if (propertyLocal == null) {
                    comparaisonResult.reportReferentielOnly(fileLocal, level, propertyReferentiel, "Key=" + keyReferentiel, true);
                } else if (!propertyLocal.equals(propertyReferentiel)) {
                    comparaisonResult.reportDifference(fileLocal, level, propertyLocal, propertyReferentiel, "Key=" + keyReferentiel, true);

                }
            }
            // second pass : all new objects in LOCAL
            for (Object keyLocal : propLocal.keySet()) {
                String propertyLocal = propLocal.getProperty(keyLocal.toString());
                String propertyReferentiel = propReferentiel.getProperty(keyLocal.toString());
                if (propertyReferentiel == null) {
                    comparaisonResult.reportLocalOnly(fileLocal, level, propertyLocal, "Key=" + keyLocal, true);
                }
            }
        } catch (Exception e) {
            comparaisonResult.reportError(fileLocal, e, "Read properties");
        }

    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* get content */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * Get list of Keys
     */
    public class KeyProperties {

        public String name;
        public String value;
        public boolean isEnable;
        public String comments;
    }

    public class KeyPropertiesReader {

        File file;
        public List<KeyProperties> listKeys = new ArrayList<KeyProperties>();
        public List<BEvent> listEvents = new ArrayList<BEvent>();

        public String getFileName() {
            return file.getName();
        }

        /**
         * is Initial: if this properties is part of the initial repository
         * 
         * @return
         */
        public boolean isInitial() {
            List<String> decompose = getDecomposePath(false);
            if (decompose.size() > 3 && decompose.get(3).equals("initial"))
                return true;
            if (decompose.size() > 2 && decompose.get(2).equals("initial"))
                return true;
            
            return false;
        }

        public Long getTenantId() {
            List<String> decompose = getDecomposePath(false);
            if (decompose.size() > 2) {
                try {
                    return Long.valueOf( decompose.get(2));
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        private List<String> getDecomposePath(boolean topDown) {
            StringTokenizer st = new StringTokenizer(getCompleteFileName(), File.separator);
            List<String> list = new ArrayList<String>();
            while (st.hasMoreElements()) {
                if (topDown)
                    list.add(st.nextToken());
                else
                    list.add(0, st.nextToken());
            }
            return list;

        }

        public String getCompleteFileName() {
            return file.getAbsolutePath();
        }

        /**
         * return a value in the key
         * 
         * @param name
         * @param defaultValue
         * @return
         */
        public Long getLongValue(String name, Long defaultValue) {
            try {
                for (KeyProperties keyProperties : listKeys) {
                    if (keyProperties.name.equals(name))
                        return Long.valueOf(keyProperties.value);
                }
            } catch (Exception e) {
                return defaultValue;
            }
            return defaultValue;
        }

        public Map<String, Object> getMap(boolean lineFeedToHtml) {
            Map<String, Object> record = new HashMap<String, Object>();

            record.put("name", file.getName());
            record.put("filename", file.getAbsolutePath());
            record.put("listevents", BEventFactory.getSyntheticHtml(listEvents));
            List<Map<String, Object>> listMapKeys = new ArrayList<Map<String, Object>>();
            record.put("keys", listMapKeys);

            for (KeyProperties keyProperties : listKeys) {
                Map<String, Object> recordKey = new HashMap<String, Object>();
                recordKey.put("name", keyProperties.name);
                recordKey.put("value", keyProperties.value);
                recordKey.put("isEnable", keyProperties.isEnable);
                String comments = keyProperties.comments;
                if (lineFeedToHtml)
                    comments = comments.replace("\n", "<br>");
                recordKey.put("comments", comments);
                listMapKeys.add(recordKey);
            }

            return record;
        }
    }

    public KeyPropertiesReader readKeys(boolean hidePassword) {
        KeyPropertiesReader keyPropertiesReader = new KeyPropertiesReader();
        keyPropertiesReader.file = file;

        // search for sothing like 
        // #bdm.hibernate.transaction.jta_platform=${sysprop.bonita.hibernate.transaction.jta_platform:org.bonitasoft.engine.persistence.Narayana5HibernateJtaPlatform}
        //
        // bdm.hibernate.transaction.jta_platform=${sysprop.bonita.hibernate.transaction.jta_platform:org.bonitasoft.engine.persistence.Narayana5HibernateJtaPlatform}
        // if the line is one the first form, this is a non enable key. Else, this is a valid key
        // comment is BEFORE this line
        try {
            BufferedReader b = new BufferedReader(new FileReader(file));

            String readLine = "";
            String comments = "";

            while ((readLine = b.readLine()) != null) {
                // is that a key line?
                boolean isKeyLine = readLine.length() > 0;
                KeyProperties keyProperties = new KeyProperties();

                if (readLine.indexOf("=") == -1)
                    isKeyLine = false;
                StringTokenizer st = new StringTokenizer(readLine, "=");
                if (st.countTokens() != 2)
                    isKeyLine = false;
                else {
                    keyProperties.name = st.nextToken().trim();
                    keyProperties.value = st.nextToken().trim();

                    if (keyProperties.name.indexOf(" ") != -1 || keyProperties.value.indexOf(" ") != -1)
                        isKeyLine = false;
                }

                if (isKeyLine) {
                    keyProperties.isEnable = !readLine.startsWith("#");
                    if (hidePassword && keyProperties.name.toLowerCase().contains("password"))
                        keyProperties.value = "*******";

                    keyProperties.comments = comments;
                    comments = "";
                    if (keyProperties.name.startsWith("#"))
                        keyProperties.name = keyProperties.name.substring(1);
                    keyPropertiesReader.listKeys.add(keyProperties);
                } else {
                    if (comments.length() == 0 && (readLine.trim().equals("#") || readLine.trim().length() == 0))
                        continue; // empty first line
                    else
                        comments += readLine + "\n";
                }
            }
        } catch (Exception e) {
            keyPropertiesReader.listEvents.add(new BEvent(EVENT_DECODEPROPERTIESERROR, e, "File [" + file.getAbsolutePath() + "]"));
        }
        return keyPropertiesReader;
    }

}
