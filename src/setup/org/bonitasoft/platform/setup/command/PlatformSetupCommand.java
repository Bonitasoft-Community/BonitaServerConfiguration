/*  1:   */ package org.bonitasoft.platform.setup.command;
/*  2:   */ 
/*  3:   */ import org.apache.commons.cli.CommandLine;
/*  4:   */ import org.apache.commons.cli.Options;
/*  5:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  6:   */ import org.bonitasoft.platform.setup.PlatformSetup;
/*  7:   */ import org.bonitasoft.platform.setup.PlatformSetupApplication;
/*  8:   */ 
/*  9:   */ public abstract class PlatformSetupCommand
/* 10:   */ {
/* 11:   */   private String name;
/* 12:   */   private String summary;
/* 13:   */   private String descriptionHeader;
/* 14:   */   private String descriptionFooter;
/* 15:   */   
/* 16:   */   public PlatformSetupCommand(String name, String summary, String descriptionHeader, String descriptionFooter)
/* 17:   */   {
/* 18:35 */     this.name = name;
/* 19:36 */     this.summary = summary;
/* 20:37 */     this.descriptionHeader = descriptionHeader;
/* 21:38 */     this.descriptionFooter = descriptionFooter;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public abstract void execute(Options paramOptions, CommandLine paramCommandLine)
/* 25:   */     throws PlatformException, CommandException;
/* 26:   */   
/* 27:   */   public String getName()
/* 28:   */   {
/* 29:44 */     return this.name;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public String getSummary()
/* 33:   */   {
/* 34:48 */     return this.summary;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public String getDescriptionHeader()
/* 38:   */   {
/* 39:52 */     return this.descriptionHeader;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public String getDescriptionFooter()
/* 43:   */   {
/* 44:56 */     return this.descriptionFooter;
/* 45:   */   }
/* 46:   */   
/* 47:   */   PlatformSetup getPlatformSetup(String[] args)
/* 48:   */     throws PlatformException
/* 49:   */   {
/* 50:60 */     return PlatformSetupApplication.getPlatformSetup(args);
/* 51:   */   }
/* 52:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.PlatformSetupCommand
 * JD-Core Version:    0.7.0.1
 */