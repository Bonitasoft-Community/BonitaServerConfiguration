/*  1:   */ package org.bonitasoft.platform.setup.command.configure;
/*  2:   */ 
/*  3:   */ import java.nio.file.Files;
/*  4:   */ import java.nio.file.LinkOption;
/*  5:   */ import java.nio.file.Path;
/*  6:   */ import java.nio.file.Paths;
/*  7:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  8:   */ import org.slf4j.Logger;
/*  9:   */ import org.slf4j.LoggerFactory;
/* 10:   */ 
/* 11:   */ class BundleResolver
/* 12:   */ {
/* 13:35 */   protected static final Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);
/* 14:   */   private final Path rootPath;
/* 15:   */   
/* 16:   */   BundleResolver()
/* 17:   */   {
/* 18:39 */     String setupFolder = System.getProperty("org.bonitasoft.platform.setup.folder");
/* 19:40 */     if (setupFolder != null) {
/* 20:41 */       this.rootPath = Paths.get(setupFolder, new String[0]).getParent();
/* 21:   */     } else {
/* 22:43 */       this.rootPath = Paths.get("..", new String[0]);
/* 23:   */     }
/* 24:   */   }
/* 25:   */   
/* 26:   */   private boolean fileExists(Path filePath)
/* 27:   */   {
/* 28:48 */     boolean exists = Files.exists(filePath, new LinkOption[0]);
/* 29:49 */     if (!exists) {
/* 30:50 */       LOGGER.debug("File " + filePath.toString() + " does not exist.");
/* 31:   */     }
/* 32:52 */     return exists;
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected Path getPath(String partialPath)
/* 36:   */     throws PlatformException
/* 37:   */   {
/* 38:56 */     String[] paths = partialPath.split("/");
/* 39:57 */     Path build = this.rootPath;
/* 40:58 */     for (String path : paths) {
/* 41:59 */       build = build.resolve(path);
/* 42:   */     }
/* 43:61 */     return build;
/* 44:   */   }
/* 45:   */   
/* 46:   */   private boolean isTomcatEnvironment()
/* 47:   */     throws PlatformException
/* 48:   */   {
/* 49:65 */     return (fileExists(getPath("server/bin/catalina.sh"))) || (fileExists(getPath("server/bin/catalina.bat")));
/* 50:   */   }
/* 51:   */   
/* 52:   */   private boolean isWildflyEnvironment()
/* 53:   */     throws PlatformException
/* 54:   */   {
/* 55:69 */     return (fileExists(getPath("server/bin/standalone.conf"))) || (fileExists(getPath("server/bin/standalone.conf.bat")));
/* 56:   */   }
/* 57:   */   
/* 58:   */   BundleConfigurator getConfigurator()
/* 59:   */     throws PlatformException
/* 60:   */   {
/* 61:73 */     if (isTomcatEnvironment()) {
/* 62:74 */       return new TomcatBundleConfigurator(this.rootPath);
/* 63:   */     }
/* 64:75 */     if (isWildflyEnvironment()) {
/* 65:76 */       return new WildflyBundleConfigurator(this.rootPath);
/* 66:   */     }
/* 67:78 */     LOGGER.info("No Application Server detected. You may need to manually configure the access to the database. Supported App Servers are: Tomcat 8, Wildfly 10");
/* 68:   */     
/* 69:80 */     return null;
/* 70:   */   }
/* 71:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.BundleResolver
 * JD-Core Version:    0.7.0.1
 */