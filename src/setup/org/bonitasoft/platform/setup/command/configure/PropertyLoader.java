/*  1:   */ package org.bonitasoft.platform.setup.command.configure;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.util.Arrays;
/*  5:   */ import java.util.List;
/*  6:   */ import java.util.Properties;
/*  7:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  8:   */ 
/*  9:   */ public class PropertyLoader
/* 10:   */ {
/* 11:   */   private final List<String> propertyFiles;
/* 12:   */   
/* 13:   */   public PropertyLoader(String... propertyFiles)
/* 14:   */   {
/* 15:31 */     this.propertyFiles = Arrays.asList(propertyFiles);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public PropertyLoader()
/* 19:   */   {
/* 20:35 */     this(new String[] { "/database.properties", "/internal.properties" });
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Properties loadProperties()
/* 24:   */     throws PlatformException
/* 25:   */   {
/* 26:39 */     Properties properties = new Properties();
/* 27:40 */     for (String propertyFile : this.propertyFiles) {
/* 28:   */       try
/* 29:   */       {
/* 30:42 */         properties.load(getClass().getResourceAsStream(propertyFile));
/* 31:   */       }
/* 32:   */       catch (IOException e)
/* 33:   */       {
/* 34:44 */         throw new PlatformException("Error reading configuration file " + propertyFile + ". Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted", e);
/* 35:   */       }
/* 36:   */     }
/* 37:48 */     return properties;
/* 38:   */   }
/* 39:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.PropertyLoader
 * JD-Core Version:    0.7.0.1
 */