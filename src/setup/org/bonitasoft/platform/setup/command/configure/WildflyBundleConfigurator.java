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
/*  12:    */ class WildflyBundleConfigurator
/*  13:    */   extends BundleConfigurator
/*  14:    */ {
/*  15:    */   private static final String WILDFLY_BACKUP_FOLDER = "wildfly-backups";
/*  16:    */   private static final String WILDFLY_TEMPLATES_FOLDER = "wildfly-templates";
/*  17: 36 */   private static final Map<String, String> wildflyModules = new HashMap(5);
/*  18:    */   
/*  19:    */   static
/*  20:    */   {
/*  21: 39 */     wildflyModules.put("h2", "com/h2database/h2");
/*  22: 40 */     wildflyModules.put("postgres", "org/postgresql");
/*  23: 41 */     wildflyModules.put("oracle", "com/oracle");
/*  24: 42 */     wildflyModules.put("sqlserver", "com/sqlserver");
/*  25: 43 */     wildflyModules.put("mysql", "com/mysql");
/*  26:    */   }
/*  27:    */   
/*  28:    */   WildflyBundleConfigurator(Path rootPath)
/*  29:    */     throws PlatformException
/*  30:    */   {
/*  31: 47 */     super(rootPath);
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected String getBundleName()
/*  35:    */   {
/*  36: 52 */     return "Wildfly";
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void configureApplicationServer()
/*  40:    */     throws PlatformException
/*  41:    */   {
/*  42: 57 */     loadProperties();
/*  43:    */     
/*  44: 59 */     String dbVendor = this.standardConfiguration.getDbVendor();
/*  45: 60 */     String bdmDbVendor = this.bdmConfiguration.getDbVendor();
/*  46: 61 */     Path standaloneXmlFile = getPathUnderAppServer("standalone/configuration/standalone.xml", true);
/*  47: 62 */     File bonitaDbDriverFile = getDriverFile(dbVendor);
/*  48: 63 */     File bdmDriverFile = getDriverFile(bdmDbVendor);
/*  49:    */     try
/*  50:    */     {
/*  51: 66 */       createBackupFolderIfNecessary("setup/wildfly-backups");
/*  52:    */       
/*  53:    */ 
/*  54: 69 */       String newContent = readContentFromFile(getPath("setup").resolve("wildfly-templates").resolve("standalone.xml"));
/*  55: 70 */       newContent = updateStandaloneXmlFile(newContent, this.standardConfiguration, "");
/*  56: 71 */       newContent = updateStandaloneXmlFile(newContent, this.bdmConfiguration, "BDM_");
/*  57: 72 */       backupAndReplaceContentIfNecessary(standaloneXmlFile, newContent, "Configuring file 'standalone/configuration/standalone.xml' with your DB values for Bonita internal database on '" + dbVendor + "' and for Business Data database on '" + bdmDbVendor + "'");
/*  58: 77 */       if (!dbVendor.equals("h2"))
/*  59:    */       {
/*  60: 78 */         Path srcDriverFile = bonitaDbDriverFile.toPath();
/*  61: 79 */         Path moduleFolder = getOptionalPathUnderAppServer("modules").resolve((String)wildflyModules.get(dbVendor)).resolve("main");
/*  62: 80 */         Path targetBonitaDbDriverFile = moduleFolder.resolve(srcDriverFile.getFileName());
/*  63: 81 */         boolean driversCopied = copyDatabaseDriversIfNecessary(srcDriverFile, targetBonitaDbDriverFile, dbVendor);
/*  64:    */         
/*  65: 83 */         copyDriverModuleFileIfNecessary(targetBonitaDbDriverFile, driversCopied, this.standardConfiguration, moduleFolder);
/*  66:    */       }
/*  67: 86 */       if (!bdmDbVendor.equals("h2"))
/*  68:    */       {
/*  69: 87 */         Path srcBdmDriverFile = bdmDriverFile.toPath();
/*  70: 88 */         Path bdmModuleFolder = getOptionalPathUnderAppServer("modules").resolve((String)wildflyModules.get(bdmDbVendor)).resolve("main");
/*  71: 89 */         Path targetBdmDriverFile = bdmModuleFolder.resolve(srcBdmDriverFile.getFileName());
/*  72: 90 */         boolean bdmDriversCopied = copyDatabaseDriversIfNecessary(srcBdmDriverFile, targetBdmDriverFile, bdmDbVendor);
/*  73:    */         
/*  74: 92 */         copyDriverModuleFileIfNecessary(targetBdmDriverFile, bdmDriversCopied, this.bdmConfiguration, bdmModuleFolder);
/*  75:    */       }
/*  76: 94 */       LOGGER.info("Wildfly auto-configuration complete.");
/*  77:    */     }
/*  78:    */     catch (PlatformException e)
/*  79:    */     {
/*  80: 96 */       restorePreviousConfiguration(standaloneXmlFile);
/*  81: 97 */       throw e;
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   void restorePreviousConfiguration(Path standaloneXmlFile)
/*  86:    */     throws PlatformException
/*  87:    */   {
/*  88:102 */     LOGGER.warn("Problem encountered, restoring previous configuration");
/*  89:    */     
/*  90:104 */     restoreOriginalFile(standaloneXmlFile);
/*  91:    */   }
/*  92:    */   
/*  93:    */   private void copyDriverModuleFileIfNecessary(Path driverFile, boolean driversCopied, DatabaseConfiguration configuration, Path moduleXmlFolder)
/*  94:    */     throws PlatformException
/*  95:    */   {
/*  96:109 */     if (driversCopied)
/*  97:    */     {
/*  98:110 */       String moduleFileContent = readContentFromFile(getPath("setup").resolve("wildfly-templates").resolve("module.xml"));
/*  99:111 */       moduleFileContent = updateModuleFile(moduleFileContent, configuration, driverFile);
/* 100:112 */       writeContentToFile(moduleXmlFolder.resolve("module.xml"), moduleFileContent);
/* 101:113 */       LOGGER.info("Creating module.xml file in folder '" + getRelativePath(moduleXmlFolder) + "' for " + configuration.getDbVendor());
/* 102:    */     }
/* 103:    */   }
/* 104:    */   
/* 105:    */   private String updateModuleFile(String moduleFileContent, DatabaseConfiguration configuration, Path driverFilename)
/* 106:    */     throws PlatformException
/* 107:    */   {
/* 108:118 */     Map<String, String> replacements = new HashMap(2);
/* 109:119 */     replacements.put("@@MODULE_NAME@@", ((String)wildflyModules.get(configuration.getDbVendor())).replaceAll("/", "."));
/* 110:120 */     replacements.put("@@DRIVERFILE_NAME@@", driverFilename.getFileName().toString());
/* 111:121 */     return replaceValues(moduleFileContent, replacements);
/* 112:    */   }
/* 113:    */   
/* 114:    */   private String updateStandaloneXmlFile(String content, DatabaseConfiguration configuration, String databasePrefix)
/* 115:    */     throws PlatformException
/* 116:    */   {
/* 117:125 */     Map<String, String> replacements = new HashMap(12);
/* 118:    */     
/* 119:127 */     replacements.put("@@" + databasePrefix + "MODULE_NAME@@", ((String)wildflyModules.get(configuration.getDbVendor())).replaceAll("/", "."));
/* 120:128 */     replacements.put("@@" + databasePrefix + "XA_DRIVER_CLASSNAME@@", configuration.getXaDriverClassName());
/* 121:131 */     if (!this.standardConfiguration.getDbVendor().equals(this.bdmConfiguration.getDbVendor())) {
/* 122:132 */       replacements.put("<!-- BDM_DRIVER_TEMPLATE (.*) BDM_DRIVER_TEMPLATE -->", "<$1>");
/* 123:    */     }
/* 124:135 */     replacements.put("@@" + databasePrefix + "DB_VENDOR@@", configuration.getDbVendor());
/* 125:136 */     replacements.put("@@" + databasePrefix + "USERNAME@@", Matcher.quoteReplacement(configuration.getDatabaseUser()));
/* 126:137 */     replacements.put("@@" + databasePrefix + "PASSWORD@@", Matcher.quoteReplacement(configuration.getDatabasePassword()));
/* 127:138 */     replacements.put("@@" + databasePrefix + "TESTQUERY@@", configuration.getTestQuery());
/* 128:139 */     replacements.put("<connection-url>@@" + databasePrefix + "DB_URL@@", "<connection-url>" + getDatabaseConnectionUrlForXmlFile(configuration));
/* 129:142 */     if ("postgres".equals(configuration.getDbVendor()))
/* 130:    */     {
/* 131:143 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_SERVER_NAME@@", configuration.getServerName()));
/* 132:144 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_SERVER_PORT@@", configuration.getServerPort()));
/* 133:145 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_DATABASE_NAME@@", Matcher.quoteReplacement(configuration.getDatabaseName())));
/* 134:    */     }
/* 135:    */     else
/* 136:    */     {
/* 137:147 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_URL@@", getDatabaseConnectionUrlForXmlFile(configuration)));
/* 138:    */     }
/* 139:149 */     if ("oracle".equals(configuration.getDbVendor()))
/* 140:    */     {
/* 141:150 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "IS_SAME_RM_OVERRIDE@@", "false"));
/* 142:151 */       replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "NO_TX_SEPARATE_POOL@@", "true"));
/* 143:    */     }
/* 144:154 */     return replaceValues(content, replacements);
/* 145:    */   }
/* 146:    */   
/* 147:    */   private Map<String, String> uncommentXmlLineAndReplace(String originalValue, String replacement)
/* 148:    */   {
/* 149:158 */     return Collections.singletonMap("<!--[ ]*(.*)" + originalValue + "(.*)[ ]*-->", "<$1" + replacement + "$2>");
/* 150:    */   }
/* 151:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.WildflyBundleConfigurator
 * JD-Core Version:    0.7.0.1
 */