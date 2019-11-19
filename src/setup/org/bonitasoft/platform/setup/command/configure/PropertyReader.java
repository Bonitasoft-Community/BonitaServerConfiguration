/*  1:   */ package org.bonitasoft.platform.setup.command.configure;
/*  2:   */ 
/*  3:   */ import java.util.Properties;
/*  4:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  5:   */ import org.slf4j.Logger;
/*  6:   */ import org.slf4j.LoggerFactory;
/*  7:   */ 
/*  8:   */ public class PropertyReader
/*  9:   */ {
/* 10:28 */   private static final Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);
/* 11:   */   private final Properties properties;
/* 12:   */   
/* 13:   */   public PropertyReader(Properties properties)
/* 14:   */   {
/* 15:33 */     this.properties = properties;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public String getPropertyAndFailIfNull(String propertyName)
/* 19:   */     throws PlatformException
/* 20:   */   {
/* 21:38 */     String sysPropValue = System.getProperty(propertyName);
/* 22:39 */     if (sysPropValue != null)
/* 23:   */     {
/* 24:40 */       LOGGER.info("System property '" + propertyName + "' set to '" + sysPropValue + "', overriding value from file database.properties.");
/* 25:41 */       return sysPropValue;
/* 26:   */     }
/* 27:44 */     String property = this.properties.getProperty(propertyName);
/* 28:45 */     if (property == null) {
/* 29:46 */       throw new PlatformException("Mandatory property '" + propertyName + "' is missing. Ensure you did not remove lines from file 'database.properties' (neither from file 'internal.properties') and that the line is NOT commented out with a '#' character at start of line.");
/* 30:   */     }
/* 31:51 */     return property;
/* 32:   */   }
/* 33:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.PropertyReader
 * JD-Core Version:    0.7.0.1
 */