/*   1:    */ package org.bonitasoft.platform.setup.command.configure;
/*   2:    */ 
/*   3:    */ import java.io.File;
/*   4:    */ import java.nio.file.Path;
/*   5:    */ import java.util.Collections;
/*   6:    */ import java.util.HashMap;
/*   7:    */ import java.util.Map;
/*   8:    */ import java.util.regex.Matcher;
/*   9:    */ import org.bonitasoft.platform.exception.PlatformException;
/*  10:    */ import org.slf4j.Logger;
/*  11:    */ 
/*  12:    */ class TomcatBundleConfigurator
/*  13:    */   extends BundleConfigurator
/*  14:    */ {
/*  15:    */   private static final String TOMCAT_BACKUP_FOLDER = "tomcat-backups";
/*  16:    */   
/*  17:    */   TomcatBundleConfigurator(Path rootPath)
/*  18:    */     throws PlatformException
/*  19:    */   {
/*  20: 34 */     super(rootPath);
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected String getBundleName()
/*  24:    */   {
/*  25: 39 */     return "Tomcat";
/*  26:    */   }
/*  27:    */   
/*  28:    */   public void configureApplicationServer()
/*  29:    */     throws PlatformException
/*  30:    */   {
/*  31: 44 */     loadProperties();
/*  32:    */     
/*  33: 46 */     String dbVendor = this.standardConfiguration.getDbVendor();
/*  34: 47 */     String bdmDbVendor = this.bdmConfiguration.getDbVendor();
/*  35: 48 */     Path setEnvUnixFile = getPathUnderAppServer("bin/setenv.sh", true);
/*  36: 49 */     Path setEnvWindowsFile = getPathUnderAppServer("bin/setenv.bat", true);
/*  37: 50 */     Path bonitaXmlFile = getPathUnderAppServer("conf/Catalina/localhost/bonita.xml", true);
/*  38: 51 */     File bonitaDbDriverFile = getDriverFile(dbVendor);
/*  39: 52 */     File bdmDriverFile = getDriverFile(bdmDbVendor);
/*  40:    */     try
/*  41:    */     {
/*  42: 55 */       createBackupFolderIfNecessary("setup/tomcat-backups");
/*  43:    */       
/*  44:    */ 
/*  45: 58 */       String newContent = readContentFromFile(getTemplateFolderPath("setenv.bat"));
/*  46: 59 */       newContent = updateSetEnvFile(newContent, dbVendor, "sysprop.bonita.db.vendor");
/*  47: 60 */       newContent = updateSetEnvFile(newContent, bdmDbVendor, "sysprop.bonita.bdm.db.vendor");
/*  48: 61 */       backupAndReplaceContentIfNecessary(setEnvWindowsFile, newContent, "Setting Bonita internal database vendor to '" + dbVendor + "' and Business Data database vendor to '" + bdmDbVendor + "' in 'setenv.bat' file");
/*  49:    */       
/*  50:    */ 
/*  51:    */ 
/*  52: 65 */       newContent = readContentFromFile(getTemplateFolderPath("setenv.sh"));
/*  53: 66 */       newContent = updateSetEnvFile(newContent, dbVendor, "sysprop.bonita.db.vendor");
/*  54: 67 */       newContent = updateSetEnvFile(newContent, bdmDbVendor, "sysprop.bonita.bdm.db.vendor");
/*  55: 68 */       backupAndReplaceContentIfNecessary(setEnvUnixFile, newContent, "Setting Bonita internal database vendor to '" + dbVendor + "' and Business Data database vendor to '" + bdmDbVendor + "' in 'setenv.sh' file");
/*  56:    */       
/*  57:    */ 
/*  58:    */ 
/*  59:    */ 
/*  60: 73 */       newContent = readContentFromFile(getTemplateFolderPath("bonita.xml"));
/*  61: 74 */       newContent = updateBonitaXmlFile(newContent, this.standardConfiguration, "ds1");
/*  62: 75 */       newContent = updateBonitaXmlFile(newContent, this.bdmConfiguration, "ds2");
/*  63: 76 */       backupAndReplaceContentIfNecessary(bonitaXmlFile, newContent, "Configuring file 'conf/Catalina/localhost/bonita.xml' with your DB values for Bonita internal database on '" + dbVendor + "' and for Business Data database on '" + bdmDbVendor + "'");
/*  64:    */       
/*  65:    */ 
/*  66:    */ 
/*  67:    */ 
/*  68: 81 */       Path srcDriverFile = bonitaDbDriverFile.toPath();
/*  69: 82 */       Path targetBonitaDbDriverFile = getPathUnderAppServer("lib/bonita", true).resolve(srcDriverFile.getFileName());
/*  70: 83 */       copyDatabaseDriversIfNecessary(srcDriverFile, targetBonitaDbDriverFile, dbVendor);
/*  71: 84 */       Path srcBdmDriverFile = bdmDriverFile.toPath();
/*  72: 85 */       Path targetBdmDriverFile = getPathUnderAppServer("lib/bonita", true).resolve(srcBdmDriverFile.getFileName());
/*  73: 86 */       copyDatabaseDriversIfNecessary(srcBdmDriverFile, targetBdmDriverFile, bdmDbVendor);
/*  74: 87 */       LOGGER.info("Tomcat auto-configuration complete.");
/*  75:    */     }
/*  76:    */     catch (PlatformException e)
/*  77:    */     {
/*  78: 89 */       restorePreviousConfiguration(setEnvUnixFile, setEnvWindowsFile, bonitaXmlFile);
/*  79: 90 */       throw e;
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   private String updateBonitaXmlFile(String content, DatabaseConfiguration configuration, String datasourceAlias)
/*  84:    */   {
/*  85: 95 */     Map<String, String> replacements = new HashMap(5);
/*  86: 96 */     replacements.put("@@" + datasourceAlias + ".database_connection_user@@", Matcher.quoteReplacement(configuration.getDatabaseUser()));
/*  87: 97 */     replacements.put("@@" + datasourceAlias + ".database_connection_password@@", Matcher.quoteReplacement(configuration.getDatabasePassword()));
/*  88: 98 */     replacements.put("@@" + datasourceAlias + ".driver_class_name@@", configuration.getNonXaDriverClassName());
/*  89: 99 */     replacements.put("@@" + datasourceAlias + ".xa.driver_class_name@@", configuration.getXaDriverClassName());
/*  90:100 */     replacements.put("@@" + datasourceAlias + ".xa_datasource_factory@@", configuration.getXaDataSourceFactory());
/*  91:101 */     replacements.put("@@" + datasourceAlias + ".database_connection_url@@", getDatabaseConnectionUrlForXmlFile(configuration));
/*  92:102 */     replacements.put("@@" + datasourceAlias + "_database_server_name@@", configuration.getServerName());
/*  93:103 */     replacements.put("@@" + datasourceAlias + "_database_port_number@@", configuration.getServerPort());
/*  94:104 */     replacements.put("@@" + datasourceAlias + "_database_database_name@@", Matcher.quoteReplacement(configuration.getDatabaseName()));
/*  95:105 */     replacements.put("@@" + datasourceAlias + ".database_test_query@@", configuration.getTestQuery());
/*  96:106 */     return replaceValues(content, replacements);
/*  97:    */   }
/*  98:    */   
/*  99:    */   private String updateSetEnvFile(String setEnvFileContent, String dbVendor, String systemPropertyName)
/* 100:    */   {
/* 101:110 */     Map<String, String> replacementMap = Collections.singletonMap("-D" + systemPropertyName + "=.*\"", "-D" + systemPropertyName + "=" + dbVendor + "\"");
/* 102:    */     
/* 103:    */ 
/* 104:113 */     return replaceValues(setEnvFileContent, replacementMap);
/* 105:    */   }
/* 106:    */   
/* 107:    */   void restorePreviousConfiguration(Path setEnvUnixFile, Path setEnvWindowsFile, Path bonitaXmlFile)
/* 108:    */     throws PlatformException
/* 109:    */   {
/* 110:117 */     LOGGER.warn("Problem encountered, restoring previous configuration");
/* 111:    */     
/* 112:119 */     restoreOriginalFile(bonitaXmlFile);
/* 113:120 */     restoreOriginalFile(setEnvUnixFile);
/* 114:121 */     restoreOriginalFile(setEnvWindowsFile);
/* 115:    */   }
/* 116:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.TomcatBundleConfigurator
 * JD-Core Version:    0.7.0.1
 */