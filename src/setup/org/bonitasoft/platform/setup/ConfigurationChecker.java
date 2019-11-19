/*  1:   */ package org.bonitasoft.platform.setup;
/*  2:   */ 
/*  3:   */ import java.nio.file.Paths;
/*  4:   */ import java.util.Properties;
/*  5:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  6:   */ import org.bonitasoft.platform.setup.command.configure.DatabaseConfiguration;
/*  7:   */ 
/*  8:   */ class ConfigurationChecker
/*  9:   */ {
/* 10:   */   private Properties datasourceProperties;
/* 11:   */   private String driverClassName;
/* 12:   */   private DatabaseConfiguration dbConfiguration;
/* 13:   */   
/* 14:   */   ConfigurationChecker(Properties datasourceProperties)
/* 15:   */   {
/* 16:39 */     this.datasourceProperties = datasourceProperties;
/* 17:   */   }
/* 18:   */   
/* 19:   */   void loadProperties()
/* 20:   */     throws PlatformException
/* 21:   */   {
/* 22:43 */     this.dbConfiguration = new DatabaseConfiguration("", this.datasourceProperties, Paths.get(".", new String[0]));
/* 23:44 */     this.driverClassName = this.dbConfiguration.getNonXaDriverClassName();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public void validate()
/* 27:   */     throws PlatformException
/* 28:   */   {
/* 29:48 */     loadProperties();
/* 30:49 */     tryToLoadDriverClass();
/* 31:   */   }
/* 32:   */   
/* 33:   */   void tryToLoadDriverClass()
/* 34:   */     throws PlatformException
/* 35:   */   {
/* 36:   */     try
/* 37:   */     {
/* 38:54 */       Class.forName(this.driverClassName);
/* 39:   */     }
/* 40:   */     catch (ClassNotFoundException e)
/* 41:   */     {
/* 42:57 */       throw new PlatformException("The driver class named '" + this.driverClassName + "' specified in 'internal.properties' configuration file, to connect to your '" + this.dbConfiguration.getDbVendor() + "' database, cannot be found. Either there is an error in the name of the class or the class is not available in the classpath. Make sure the driver class name is correct and that the suitable driver is available in the lib/ folder and then try again.", e);
/* 43:   */     }
/* 44:   */   }
/* 45:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.ConfigurationChecker
 * JD-Core Version:    0.7.0.1
 */