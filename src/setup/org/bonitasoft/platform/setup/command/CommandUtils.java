/*  1:   */ package org.bonitasoft.platform.setup.command;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.apache.commons.io.IOUtils;
/*  5:   */ 
/*  6:   */ public class CommandUtils
/*  7:   */ {
/*  8:   */   public static String getFileContentFromClassPath(String filename)
/*  9:   */   {
/* 10:   */     try
/* 11:   */     {
/* 12:28 */       return IOUtils.toString(PlatformSetupCommand.class.getResourceAsStream("/" + filename), "UTF-8");
/* 13:   */     }
/* 14:   */     catch (IOException e)
/* 15:   */     {
/* 16:30 */       throw new IllegalStateException(e);
/* 17:   */     }
/* 18:   */   }
/* 19:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.CommandUtils
 * JD-Core Version:    0.7.0.1
 */