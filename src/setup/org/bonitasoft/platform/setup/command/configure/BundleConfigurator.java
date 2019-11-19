/*   1:    */ package org.bonitasoft.platform.setup.command.configure;
/*   2:    */ 
/*   3:    */ import java.io.File;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.net.URISyntaxException;
/*   6:    */ import java.net.URL;
/*   7:    */ import java.nio.charset.StandardCharsets;
/*   8:    */ import java.nio.file.CopyOption;
/*   9:    */ import java.nio.file.Files;
/*  10:    */ import java.nio.file.LinkOption;
/*  11:    */ import java.nio.file.OpenOption;
/*  12:    */ import java.nio.file.Path;
/*  13:    */ import java.nio.file.Paths;
/*  14:    */ import java.nio.file.attribute.FileAttribute;
/*  15:    */ import java.text.SimpleDateFormat;
/*  16:    */ import java.util.Collection;
/*  17:    */ import java.util.Date;
/*  18:    */ import java.util.Map;
/*  19:    */ import java.util.Map.Entry;
/*  20:    */ import java.util.Properties;
/*  21:    */ import java.util.regex.Matcher;
/*  22:    */ import org.apache.commons.io.FileUtils;
/*  23:    */ import org.apache.commons.io.IOCase;
/*  24:    */ import org.apache.commons.io.filefilter.RegexFileFilter;
/*  25:    */ import org.apache.commons.lang3.StringEscapeUtils;
/*  26:    */ import org.bonitasoft.platform.exception.PlatformException;
/*  27:    */ import org.slf4j.Logger;
/*  28:    */ import org.slf4j.LoggerFactory;
/*  29:    */ 
/*  30:    */ abstract class BundleConfigurator
/*  31:    */ {
/*  32: 51 */   protected static final Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);
/*  33:    */   static final String H2 = "h2";
/*  34:    */   static final String MYSQL = "mysql";
/*  35:    */   static final String ORACLE = "oracle";
/*  36:    */   static final String POSTGRES = "postgres";
/*  37:    */   static final String SQLSERVER = "sqlserver";
/*  38:    */   private static final String TOMCAT_TEMPLATES_FOLDER = "tomcat-templates";
/*  39:    */   static final String APPSERVER_FOLDERNAME = "server";
/*  40:    */   private Path rootPath;
/*  41:    */   DatabaseConfiguration standardConfiguration;
/*  42:    */   DatabaseConfiguration bdmConfiguration;
/*  43:    */   private Path backupsFolder;
/*  44:    */   private String timestamp;
/*  45:    */   
/*  46:    */   BundleConfigurator(Path rootPath)
/*  47:    */     throws PlatformException
/*  48:    */   {
/*  49:    */     try
/*  50:    */     {
/*  51: 72 */       this.rootPath = rootPath.toRealPath(new LinkOption[0]);
/*  52:    */     }
/*  53:    */     catch (IOException e)
/*  54:    */     {
/*  55: 74 */       throw new PlatformException("Unable to determine root path for " + getBundleName());
/*  56:    */     }
/*  57: 76 */     this.timestamp = new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss's'").format(new Date());
/*  58:    */   }
/*  59:    */   
/*  60:    */   void loadProperties()
/*  61:    */     throws PlatformException
/*  62:    */   {
/*  63: 80 */     Properties properties = new PropertyLoader().loadProperties();
/*  64:    */     
/*  65: 82 */     this.standardConfiguration = new DatabaseConfiguration("", properties, this.rootPath);
/*  66: 83 */     this.bdmConfiguration = new DatabaseConfiguration("bdm.", properties, this.rootPath);
/*  67:    */     try
/*  68:    */     {
/*  69: 85 */       Path dbFile = Paths.get(getClass().getResource("/database.properties").toURI());
/*  70: 86 */       LOGGER.info(getBundleName() + " environment detected with root " + this.rootPath);
/*  71: 87 */       LOGGER.info("Running auto-configuration using file " + dbFile.normalize());
/*  72:    */     }
/*  73:    */     catch (URISyntaxException e)
/*  74:    */     {
/*  75: 89 */       throw new PlatformException("Configuration file 'database.properties' not found");
/*  76:    */     }
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected abstract String getBundleName();
/*  80:    */   
/*  81:    */   abstract void configureApplicationServer()
/*  82:    */     throws PlatformException;
/*  83:    */   
/*  84:    */   void createBackupFolderIfNecessary(String backupFolder)
/*  85:    */     throws PlatformException
/*  86:    */   {
/*  87: 98 */     this.backupsFolder = getPath(backupFolder);
/*  88: 99 */     if (Files.notExists(this.backupsFolder, new LinkOption[0])) {
/*  89:    */       try
/*  90:    */       {
/*  91:101 */         Files.createDirectory(this.backupsFolder, new FileAttribute[0]);
/*  92:    */       }
/*  93:    */       catch (IOException e)
/*  94:    */       {
/*  95:103 */         throw new PlatformException("Could not create backup folder: " + backupFolder, e);
/*  96:    */       }
/*  97:    */     }
/*  98:    */   }
/*  99:    */   
/* 100:    */   Path getTemplateFolderPath(String templateFile)
/* 101:    */     throws PlatformException
/* 102:    */   {
/* 103:109 */     return getPath("setup").resolve("tomcat-templates").resolve(templateFile);
/* 104:    */   }
/* 105:    */   
/* 106:    */   void backupAndReplaceContentIfNecessary(Path path, String newContent, String message)
/* 107:    */     throws PlatformException
/* 108:    */   {
/* 109:113 */     String previousContent = readContentFromFile(path);
/* 110:114 */     if (!previousContent.equals(newContent))
/* 111:    */     {
/* 112:115 */       makeBackupOfFile(path);
/* 113:116 */       writeContentToFile(path, newContent);
/* 114:117 */       LOGGER.info(message);
/* 115:    */     }
/* 116:    */     else
/* 117:    */     {
/* 118:119 */       LOGGER.info("Same configuration detected for file '" + getRelativePath(path) + "'. No need to change it.");
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   boolean copyDatabaseDriversIfNecessary(Path srcDriverFile, Path targetDriverFile, String dbVendor)
/* 123:    */     throws PlatformException
/* 124:    */   {
/* 125:125 */     if ((srcDriverFile == null) || (targetDriverFile == null)) {
/* 126:126 */       return false;
/* 127:    */     }
/* 128:128 */     if (Files.exists(targetDriverFile, new LinkOption[0]))
/* 129:    */     {
/* 130:129 */       LOGGER.info("Your " + dbVendor + " driver file '" + getRelativePath(targetDriverFile) + "' already exists. Skipping the copy.");
/* 131:    */       
/* 132:131 */       return false;
/* 133:    */     }
/* 134:133 */     copyDriverFile(srcDriverFile, targetDriverFile, dbVendor);
/* 135:134 */     return true;
/* 136:    */   }
/* 137:    */   
/* 138:    */   Path getRelativePath(Path pathToRelativize)
/* 139:    */   {
/* 140:138 */     return this.rootPath.toAbsolutePath().relativize(pathToRelativize.toAbsolutePath());
/* 141:    */   }
/* 142:    */   
/* 143:    */   void copyDriverFile(Path srcDriverFile, Path targetDriverFile, String dbVendor)
/* 144:    */     throws PlatformException
/* 145:    */   {
/* 146:    */     try
/* 147:    */     {
/* 148:143 */       Path targetDriverFolder = targetDriverFile.getParent();
/* 149:144 */       targetDriverFolder.toFile().mkdirs();
/* 150:145 */       Files.copy(srcDriverFile, targetDriverFile, new CopyOption[0]);
/* 151:146 */       LOGGER.info("Copying your " + dbVendor + " driver file '" + getRelativePath(srcDriverFile) + "' to tomcat lib folder '" + 
/* 152:    */       
/* 153:148 */         getRelativePath(targetDriverFolder) + "'");
/* 154:    */     }
/* 155:    */     catch (IOException e)
/* 156:    */     {
/* 157:152 */       throw new PlatformException("Fail to copy driver file lib/" + srcDriverFile.getFileName() + " to " + targetDriverFile.toAbsolutePath() + ": " + e.getMessage(), e);
/* 158:    */     }
/* 159:    */   }
/* 160:    */   
/* 161:    */   static String replaceValues(String content, Map<String, String> replacementMap)
/* 162:    */   {
/* 163:157 */     for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
/* 164:158 */       content = content.replaceAll((String)entry.getKey(), (String)entry.getValue());
/* 165:    */     }
/* 166:160 */     return content;
/* 167:    */   }
/* 168:    */   
/* 169:    */   static String convertWindowsBackslashes(String value)
/* 170:    */   {
/* 171:166 */     return value.replaceAll("\\\\", "/");
/* 172:    */   }
/* 173:    */   
/* 174:    */   void writeContentToFile(Path path, String content)
/* 175:    */     throws PlatformException
/* 176:    */   {
/* 177:    */     try
/* 178:    */     {
/* 179:171 */       Files.write(path, content.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
/* 180:    */     }
/* 181:    */     catch (IOException e)
/* 182:    */     {
/* 183:173 */       throw new PlatformException("Fail to replace content in file " + path + ": " + e.getMessage(), e);
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   String readContentFromFile(Path bonitaXmlFile)
/* 188:    */     throws PlatformException
/* 189:    */   {
/* 190:    */     try
/* 191:    */     {
/* 192:179 */       return new String(Files.readAllBytes(bonitaXmlFile), StandardCharsets.UTF_8);
/* 193:    */     }
/* 194:    */     catch (IOException e)
/* 195:    */     {
/* 196:182 */       throw new PlatformException("Cannot read content of text file " + bonitaXmlFile.toAbsolutePath().toString(), e);
/* 197:    */     }
/* 198:    */   }
/* 199:    */   
/* 200:    */   void restoreOriginalFile(Path bonitaXmlFile)
/* 201:    */     throws PlatformException
/* 202:    */   {
/* 203:    */     try
/* 204:    */     {
/* 205:188 */       if (Files.exists(bonitaXmlFile, new LinkOption[0])) {
/* 206:189 */         Files.delete(bonitaXmlFile);
/* 207:    */       }
/* 208:191 */       Path backupFile = getBackupFile(bonitaXmlFile);
/* 209:192 */       if (Files.exists(backupFile, new LinkOption[0])) {
/* 210:193 */         Files.move(backupFile, bonitaXmlFile, new CopyOption[0]);
/* 211:    */       }
/* 212:    */     }
/* 213:    */     catch (IOException e)
/* 214:    */     {
/* 215:196 */       throw new PlatformException("Fail to restore original file for " + bonitaXmlFile + ": " + e.getMessage(), e);
/* 216:    */     }
/* 217:    */   }
/* 218:    */   
/* 219:    */   private Path makeBackupOfFile(Path originalFile)
/* 220:    */     throws PlatformException
/* 221:    */   {
/* 222:201 */     Path originalFileName = originalFile.getFileName();
/* 223:202 */     Path backup = getBackupFile(originalFile);
/* 224:203 */     LOGGER.info("Creating a backup of configuration file '" + 
/* 225:204 */       getRelativePath(originalFile).normalize() + "' to '" + 
/* 226:205 */       getRelativePath(backup).normalize() + "'");
/* 227:    */     try
/* 228:    */     {
/* 229:207 */       Files.copy(originalFile, backup, new CopyOption[0]);
/* 230:208 */       return backup;
/* 231:    */     }
/* 232:    */     catch (IOException e)
/* 233:    */     {
/* 234:210 */       throw new PlatformException("Fail to make backup file for " + originalFileName + ": " + e.getMessage(), e);
/* 235:    */     }
/* 236:    */   }
/* 237:    */   
/* 238:    */   private Path getBackupFile(Path originalFile)
/* 239:    */   {
/* 240:215 */     return this.backupsFolder.resolve(originalFile.getFileName() + "." + getTimestamp());
/* 241:    */   }
/* 242:    */   
/* 243:    */   private String getTimestamp()
/* 244:    */   {
/* 245:219 */     return this.timestamp;
/* 246:    */   }
/* 247:    */   
/* 248:    */   File getDriverFile(String dbVendor)
/* 249:    */     throws PlatformException
/* 250:    */   {
/* 251:223 */     Path driverFolder = getPath("setup/lib");
/* 252:224 */     if (!Files.exists(driverFolder, new LinkOption[0])) {
/* 253:225 */       throw new PlatformException("Drivers folder not found: " + driverFolder.toString() + ". Make sure it exists and put a jar or zip file containing drivers there.");
/* 254:    */     }
/* 255:228 */     Collection<File> driversFiles = FileUtils.listFiles(driverFolder.toFile(), getDriverFilter(dbVendor), null);
/* 256:230 */     if (driversFiles.size() == 0) {
/* 257:231 */       throw new PlatformException("No " + dbVendor + " drivers found in folder " + driverFolder.toString() + ". Make sure to put a jar or zip file containing drivers there.");
/* 258:    */     }
/* 259:233 */     if (driversFiles.size() == 1) {
/* 260:234 */       return (File)driversFiles.toArray()[0];
/* 261:    */     }
/* 262:237 */     throw new PlatformException("Found more than 1 file containing " + dbVendor + " drivers  in folder " + driverFolder.toString() + ". Make sure to put only 1 jar or zip file containing drivers there.");
/* 263:    */   }
/* 264:    */   
/* 265:    */   RegexFileFilter getDriverFilter(String dbVendor)
/* 266:    */   {
/* 267:243 */     return new RegexFileFilter(getDriverPattern(dbVendor), IOCase.INSENSITIVE);
/* 268:    */   }
/* 269:    */   
/* 270:    */   private String getDriverPattern(String dbVendor)
/* 271:    */   {
/* 272:247 */     if ("oracle".equals(dbVendor)) {
/* 273:248 */       return ".*(ojdbc|oracle).*\\.(jar|zip)";
/* 274:    */     }
/* 275:250 */     if ("sqlserver".equals(dbVendor)) {
/* 276:251 */       return ".*(sqlserver|mssql|sqljdbc).*\\.(jar|zip)";
/* 277:    */     }
/* 278:253 */     return ".*" + dbVendor + ".*";
/* 279:    */   }
/* 280:    */   
/* 281:    */   protected Path getPath(String partialPath)
/* 282:    */     throws PlatformException
/* 283:    */   {
/* 284:263 */     return getPath(partialPath, false);
/* 285:    */   }
/* 286:    */   
/* 287:    */   protected Path getPath(String partialPath, boolean failIfNotExist)
/* 288:    */     throws PlatformException
/* 289:    */   {
/* 290:274 */     String[] paths = partialPath.split("/");
/* 291:275 */     Path build = this.rootPath;
/* 292:276 */     for (String path : paths) {
/* 293:277 */       build = build.resolve(path);
/* 294:    */     }
/* 295:279 */     if ((failIfNotExist) && (Files.notExists(build, new LinkOption[0]))) {
/* 296:280 */       throw new PlatformException("File " + build.getFileName() + " is mandatory but is not found");
/* 297:    */     }
/* 298:282 */     return build;
/* 299:    */   }
/* 300:    */   
/* 301:    */   static String escapeXmlCharacters(String url)
/* 302:    */   {
/* 303:287 */     return StringEscapeUtils.escapeXml11(url);
/* 304:    */   }
/* 305:    */   
/* 306:    */   protected Path getOptionalPathUnderAppServer(String path)
/* 307:    */     throws PlatformException
/* 308:    */   {
/* 309:291 */     return getPath("server/" + path, false);
/* 310:    */   }
/* 311:    */   
/* 312:    */   protected Path getPathUnderAppServer(String path, boolean failIfNotExist)
/* 313:    */     throws PlatformException
/* 314:    */   {
/* 315:295 */     return getPath("server/" + path, failIfNotExist);
/* 316:    */   }
/* 317:    */   
/* 318:    */   protected static String getDatabaseConnectionUrlForXmlFile(DatabaseConfiguration configuration)
/* 319:    */   {
/* 320:299 */     return escapeXmlCharacters(Matcher.quoteReplacement(getDatabaseConnectionUrl(configuration)));
/* 321:    */   }
/* 322:    */   
/* 323:    */   protected static String getDatabaseConnectionUrlForPropertiesFile(DatabaseConfiguration configuration)
/* 324:    */   {
/* 325:303 */     String url = getDatabaseConnectionUrl(configuration);
/* 326:304 */     if ("h2".equals(configuration.getDbVendor())) {
/* 327:305 */       url = StringEscapeUtils.escapeJava(url);
/* 328:    */     }
/* 329:307 */     return Matcher.quoteReplacement(url);
/* 330:    */   }
/* 331:    */   
/* 332:    */   static String getDatabaseConnectionUrl(DatabaseConfiguration configuration)
/* 333:    */   {
/* 334:311 */     String url = configuration.getUrl();
/* 335:312 */     if ("h2".equals(configuration.getDbVendor())) {
/* 336:313 */       url = convertWindowsBackslashes(url);
/* 337:    */     }
/* 338:315 */     return url;
/* 339:    */   }
/* 340:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.BundleConfigurator
 * JD-Core Version:    0.7.0.1
 */