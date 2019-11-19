/*  1:   */ package org.bonitasoft.platform.setup.command;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.io.InputStream;
/*  5:   */ import java.io.PrintStream;
/*  6:   */ import java.util.Properties;
/*  7:   */ import org.apache.commons.cli.CommandLine;
/*  8:   */ import org.apache.commons.cli.Options;
/*  9:   */ import org.bonitasoft.platform.exception.PlatformException;
/* 10:   */ import org.bonitasoft.platform.setup.PlatformSetup;
/* 11:   */ import org.bonitasoft.platform.setup.command.configure.PropertyReader;
/* 12:   */ import org.slf4j.Logger;
/* 13:   */ import org.slf4j.LoggerFactory;
/* 14:   */ 
/* 15:   */ public class InitCommand
/* 16:   */   extends PlatformSetupCommand
/* 17:   */ {
/* 18:34 */   private static final Logger LOGGER = LoggerFactory.getLogger(InitCommand.class);
/* 19:   */   
/* 20:   */   public InitCommand()
/* 21:   */   {
/* 22:37 */     super("init", "Initialise the database so that Bonita is ready to run with this database", 
/* 23:38 */       CommandUtils.getFileContentFromClassPath("init_header.txt"), 
/* 24:39 */       CommandUtils.getFileContentFromClassPath("init_footer.txt"));
/* 25:   */   }
/* 26:   */   
/* 27:   */   public void execute(Options options, CommandLine commandLine)
/* 28:   */     throws PlatformException, CommandException
/* 29:   */   {
/* 30:44 */     askConfirmationIfH2();
/* 31:45 */     getPlatformSetup(commandLine.getArgs()).init();
/* 32:   */   }
/* 33:   */   
/* 34:   */   void askConfirmationIfH2()
/* 35:   */     throws PlatformException, CommandException
/* 36:   */   {
/* 37:49 */     Properties properties = new Properties();
/* 38:   */     try
/* 39:   */     {
/* 40:51 */       properties.load(getClass().getResourceAsStream("/database.properties"));
/* 41:52 */       PropertyReader propertyReader = new PropertyReader(properties);
/* 42:53 */       if (("h2".equals(propertyReader.getPropertyAndFailIfNull("db.vendor"))) && 
/* 43:54 */         ("h2".equals(propertyReader.getPropertyAndFailIfNull("bdm.db.vendor"))) && 
/* 44:55 */         (System.getProperty("h2.noconfirm") == null))
/* 45:   */       {
/* 46:56 */         warn("Default H2 configuration detected. This is not recommended for production. If this is not the required configuration, change file 'database.properties' and run again.");
/* 47:57 */         System.out.print("Are you sure you want to continue? (y/n): ");
/* 48:58 */         String answer = readAnswer();
/* 49:59 */         if (!"y".equalsIgnoreCase(answer)) {
/* 50:60 */           throw new CommandException("Default H2 configuration not confirmed. Exiting.");
/* 51:   */         }
/* 52:   */       }
/* 53:   */     }
/* 54:   */     catch (IOException e)
/* 55:   */     {
/* 56:64 */       throw new PlatformException("Error reading configuration file database.properties. Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted", e);
/* 57:   */     }
/* 58:   */   }
/* 59:   */   
/* 60:   */   String readAnswer()
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:71 */     byte[] read = new byte[1];
/* 64:72 */     System.in.read(read);
/* 65:73 */     return new String(read);
/* 66:   */   }
/* 67:   */   
/* 68:   */   void warn(String message)
/* 69:   */   {
/* 70:77 */     LOGGER.warn(message);
/* 71:   */   }
/* 72:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.InitCommand
 * JD-Core Version:    0.7.0.1
 */