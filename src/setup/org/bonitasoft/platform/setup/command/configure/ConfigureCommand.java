/*  1:   */ package org.bonitasoft.platform.setup.command.configure;
/*  2:   */ 
/*  3:   */ import org.apache.commons.cli.CommandLine;
/*  4:   */ import org.apache.commons.cli.Options;
/*  5:   */ import org.bonitasoft.platform.exception.PlatformException;
/*  6:   */ import org.bonitasoft.platform.setup.command.CommandUtils;
/*  7:   */ import org.bonitasoft.platform.setup.command.PlatformSetupCommand;
/*  8:   */ 
/*  9:   */ public class ConfigureCommand
/* 10:   */   extends PlatformSetupCommand
/* 11:   */ {
/* 12:   */   public ConfigureCommand()
/* 13:   */   {
/* 14:29 */     super("configure", "Configure a Bonita bundle to use your specific database configuration (defined in database.properties or via command line parameters)", 
/* 15:   */     
/* 16:31 */       CommandUtils.getFileContentFromClassPath("configure_header.txt"), CommandUtils.getFileContentFromClassPath("configure_footer.txt"));
/* 17:   */   }
/* 18:   */   
/* 19:   */   public void execute(Options options, CommandLine commandLine)
/* 20:   */     throws PlatformException
/* 21:   */   {
/* 22:36 */     BundleConfigurator bundleConfigurator = createBundleResolver().getConfigurator();
/* 23:37 */     if (bundleConfigurator != null) {
/* 24:38 */       bundleConfigurator.configureApplicationServer();
/* 25:   */     }
/* 26:   */   }
/* 27:   */   
/* 28:   */   BundleResolver createBundleResolver()
/* 29:   */   {
/* 30:43 */     return new BundleResolver();
/* 31:   */   }
/* 32:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.ConfigureCommand
 * JD-Core Version:    0.7.0.1
 */