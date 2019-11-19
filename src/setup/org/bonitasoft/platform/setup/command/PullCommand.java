/*  1:   */ package org.bonitasoft.platform.setup.command;
/*  2:   */ 
/*  3:   */ import org.apache.commons.cli.CommandLine;
/*  4:   */ import org.apache.commons.cli.Options;
/*  5:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  6:   */ import org.bonitasoft.platform.setup.PlatformSetup;
/*  7:   */ 
/*  8:   */ public class PullCommand
/*  9:   */   extends PlatformSetupCommand
/* 10:   */ {
/* 11:   */   public PullCommand()
/* 12:   */   {
/* 13:27 */     super("pull", "Pull configuration from the database", null, CommandUtils.getFileContentFromClassPath("pull.txt"));
/* 14:   */   }
/* 15:   */   
/* 16:   */   public void execute(Options options, CommandLine commandLine)
/* 17:   */     throws PlatformException
/* 18:   */   {
/* 19:32 */     getPlatformSetup(commandLine.getArgs()).pull();
/* 20:   */   }
/* 21:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.PullCommand
 * JD-Core Version:    0.7.0.1
 */