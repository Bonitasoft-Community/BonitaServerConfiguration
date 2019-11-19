/*   1:    */ package org.bonitasoft.platform.setup.command;
/*   2:    */ 
/*   3:    */ import java.io.PrintStream;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.List;
/*   6:    */ import org.apache.commons.cli.CommandLine;
/*   7:    */ import org.apache.commons.cli.HelpFormatter;
/*   8:    */ import org.apache.commons.cli.Options;
/*   9:    */ import org.apache.commons.lang3.StringUtils;
/*  10:    */ import org.bonitasoft.platform.exception.PlatformException;
/*  11:    */ 
/*  12:    */ public class HelpCommand
/*  13:    */   extends PlatformSetupCommand
/*  14:    */ {
/*  15:    */   private List<PlatformSetupCommand> commands;
/*  16:    */   
/*  17:    */   public HelpCommand()
/*  18:    */   {
/*  19: 36 */     super("help", "Display the help", "Display the help", null);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public void execute(Options options, CommandLine commandLine)
/*  23:    */     throws PlatformException, CommandException
/*  24:    */   {
/*  25: 41 */     String[] args = commandLine.getArgs();
/*  26: 42 */     if (args.length == 0)
/*  27:    */     {
/*  28: 43 */       printUsage(options);
/*  29: 44 */       throw new CommandException("Need to specify a command, see usage above.");
/*  30:    */     }
/*  31: 45 */     if (getName().equals(args[0]))
/*  32:    */     {
/*  33: 46 */       if (args.length > 1) {
/*  34: 47 */         printHelpFor(options, args[1]);
/*  35:    */       } else {
/*  36: 49 */         printCommonHelp(options);
/*  37:    */       }
/*  38:    */     }
/*  39:    */     else
/*  40:    */     {
/*  41: 52 */       printUsage(options);
/*  42: 53 */       throw new CommandException("ERROR: no command named: " + args[0]);
/*  43:    */     }
/*  44:    */   }
/*  45:    */   
/*  46:    */   private void printHelpFor(Options options, String commandNameForHelp)
/*  47:    */     throws CommandException
/*  48:    */   {
/*  49: 58 */     PlatformSetupCommand platformSetupCommand = getCommand(commandNameForHelp);
/*  50: 59 */     if (platformSetupCommand == null)
/*  51:    */     {
/*  52: 60 */       printCommonHelp(options);
/*  53: 61 */       throw new CommandException("ERROR: no command named: " + commandNameForHelp);
/*  54:    */     }
/*  55: 63 */     printHelpFor(options, platformSetupCommand);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void setCommands(List<PlatformSetupCommand> commands)
/*  59:    */   {
/*  60: 67 */     this.commands = commands;
/*  61:    */   }
/*  62:    */   
/*  63:    */   private PlatformSetupCommand getCommand(String commandName)
/*  64:    */   {
/*  65: 71 */     PlatformSetupCommand command = null;
/*  66: 72 */     for (PlatformSetupCommand platformSetupCommand : this.commands) {
/*  67: 73 */       if (commandName.equals(platformSetupCommand.getName()))
/*  68:    */       {
/*  69: 74 */         command = platformSetupCommand;
/*  70: 75 */         break;
/*  71:    */       }
/*  72:    */     }
/*  73: 78 */     return command;
/*  74:    */   }
/*  75:    */   
/*  76:    */   private void printCommonHelp(Options options)
/*  77:    */   {
/*  78: 82 */     printUsage(options);
/*  79: 83 */     printGlobalHelpHeader();
/*  80: 84 */     printCommandsUsage();
/*  81: 85 */     printGlobalHelpFooter();
/*  82:    */   }
/*  83:    */   
/*  84:    */   private void printUsage(Options options)
/*  85:    */   {
/*  86: 89 */     List<String> names = new ArrayList(this.commands.size());
/*  87: 90 */     for (PlatformSetupCommand command : this.commands) {
/*  88: 91 */       if (!command.equals(this)) {
/*  89: 92 */         names.add(command.getName());
/*  90:    */       }
/*  91:    */     }
/*  92: 95 */     String footer = "use `setup help` or `setup help <command>` for more details" + System.lineSeparator();
/*  93: 96 */     printUsageFor(options, "( " + StringUtils.join(names.iterator(), " | ") + " )", footer);
/*  94:    */   }
/*  95:    */   
/*  96:    */   private void printCommandsUsage()
/*  97:    */   {
/*  98:100 */     StringBuilder usage = new StringBuilder();
/*  99:101 */     usage.append(System.lineSeparator());
/* 100:102 */     usage.append("Available commands:").append(System.lineSeparator()).append(System.lineSeparator());
/* 101:103 */     for (PlatformSetupCommand command : this.commands) {
/* 102:105 */       usage.append(" ").append(command.getName()).append("  --  ").append(command.getSummary()).append(System.lineSeparator());
/* 103:    */     }
/* 104:107 */     System.out.println(usage.toString());
/* 105:    */   }
/* 106:    */   
/* 107:    */   private void printHelpFor(Options options, PlatformSetupCommand command)
/* 108:    */   {
/* 109:111 */     printUsageFor(options, command.getName(), System.lineSeparator());
/* 110:112 */     printCommandDescriptionHeader(command);
/* 111:113 */     printCommandUsage(command);
/* 112:114 */     printCommandDescriptionFooter(command);
/* 113:    */   }
/* 114:    */   
/* 115:    */   private void printCommandDescriptionHeader(PlatformSetupCommand command)
/* 116:    */   {
/* 117:118 */     if (command.getDescriptionHeader() != null) {
/* 118:119 */       System.out.println("  " + command.getDescriptionHeader().replace(System.lineSeparator(), new StringBuilder().append(System.lineSeparator()).append("  ").toString()));
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   private void printCommandDescriptionFooter(PlatformSetupCommand command)
/* 123:    */   {
/* 124:124 */     if (command.getDescriptionFooter() != null) {
/* 125:125 */       System.out.println("  " + command.getDescriptionFooter().replace(System.lineSeparator(), new StringBuilder().append(System.lineSeparator()).append("  ").toString()));
/* 126:    */     }
/* 127:    */   }
/* 128:    */   
/* 129:    */   private void printUsageFor(Options options, String commandName, String footer)
/* 130:    */   {
/* 131:130 */     HelpFormatter formatter = new HelpFormatter();
/* 132:131 */     formatter.printHelp("setup " + commandName, System.lineSeparator() + "Available options:", options, footer, true);
/* 133:    */   }
/* 134:    */   
/* 135:    */   private void printCommandUsage(PlatformSetupCommand command)
/* 136:    */   {
/* 137:135 */     System.out.println(System.lineSeparator() + " " + command
/* 138:136 */       .getName() + "  --  " + command.getSummary() + System.lineSeparator());
/* 139:    */   }
/* 140:    */   
/* 141:    */   private void printGlobalHelpHeader()
/* 142:    */   {
/* 143:140 */     System.out.println(CommandUtils.getFileContentFromClassPath("global_usage_header.txt"));
/* 144:    */   }
/* 145:    */   
/* 146:    */   private void printGlobalHelpFooter()
/* 147:    */   {
/* 148:144 */     System.out.println(CommandUtils.getFileContentFromClassPath("global_usage_footer.txt"));
/* 149:    */   }
/* 150:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.HelpCommand
 * JD-Core Version:    0.7.0.1
 */