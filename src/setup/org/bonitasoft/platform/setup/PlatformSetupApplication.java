/*   1:    */ 
/*   2:    */ 
/*   3:    */ java.io.PrintStream
/*   4:    */ java.util.ArrayList
/*   5:    */ java.util.List
/*   6:    */ java.util.Map.Entry
/*   7:    */ java.util.Properties
/*   8:    */ org.apache.commons.cli.CommandLine
/*   9:    */ org.apache.commons.cli.CommandLineParser
/*  10:    */ org.apache.commons.cli.GnuParser
/*  11:    */ org.apache.commons.cli.Option
/*  12:    */ org.apache.commons.cli.Options
/*  13:    */ org.apache.commons.cli.ParseException
/*  14:    */ org.bonitasoft.platform.exception.PlatformException
/*  15:    */ org.bonitasoft.platform.setup.command.CommandException
/*  16:    */ org.bonitasoft.platform.setup.command.HelpCommand
/*  17:    */ org.bonitasoft.platform.setup.command.InitCommand
/*  18:    */ org.bonitasoft.platform.setup.command.PlatformSetupCommand
/*  19:    */ org.bonitasoft.platform.setup.command.PullCommand
/*  20:    */ org.bonitasoft.platform.setup.command.PushCommand
/*  21:    */ org.bonitasoft.platform.setup.command.configure.ConfigureCommand
/*  22:    */ org.bonitasoft.platform.setup.command.configure.PropertyLoader
/*  23:    */ org.bonitasoft.platform.setup.jndi.MemoryJNDISetup
/*  24:    */ org.slf4j.Logger
/*  25:    */ org.slf4j.LoggerFactory
/*  26:    */ org.springframework.beans.factory.annotation.Autowired
/*  27:    */ org.springframework.boot.SpringApplication
/*  28:    */ org.springframework.boot.autoconfigure.SpringBootApplication
/*  29:    */ org.springframework.context.ConfigurableApplicationContext
/*  30:    */ org.springframework.context.annotation.ComponentScan
/*  31:    */ 
/*  32:    */ 
/*  33:    */ "org.bonitasoft.platform.setup", "org.bonitasoft.platform.configuration", "org.bonitasoft.platform.version"
/*  34:    */ PlatformSetupApplication
/*  35:    */ 
/*  36: 52 */   LOGGER = getLogger
/*  37:    */   helpCommand
/*  38:    */   commands
/*  39:    */   options
/*  40:    */   
/*  41:    */   memoryJNDISetup
/*  42:    */   
/*  43:    */   platformSetup
/*  44:    */   
/*  45:    */   main[]
/*  46:    */   
/*  47: 64 */     ()run
/*  48:    */   
/*  49:    */   
/*  50:    */   getPlatformSetup[]
/*  51:    */     
/*  52:    */   
/*  53: 68 */     ()loadProperties()validate()
/*  54: 69 */     run, getBean
/*  55:    */   
/*  56:    */   
/*  57:    */   run[]
/*  58:    */   
/*  59: 73 */      = ()
/*  60: 74 */     options = createOptions();
/*  61: 75 */     this.commands = createCommands();
/*  62: 76 */     this.helpCommand.setCommands(this.commands);
/*  63: 77 */     CommandLine line = parseArguments(args, parser);
/*  64: 78 */     configureApplication(line);
/*  65: 79 */     execute(line);
/*  66:    */   }
/*  67:    */   
/*  68:    */   private PlatformSetupCommand getCommand(CommandLine line)
/*  69:    */   {
/*  70: 83 */     List argList = line.getArgList();
/*  71: 84 */     if (argList.isEmpty()) {
/*  72: 85 */       return this.helpCommand;
/*  73:    */     }
/*  74: 87 */     String commandName = argList.get(0).toString();
/*  75: 88 */     for (PlatformSetupCommand platformSetupCommand : this.commands) {
/*  76: 89 */       if (commandName.equals(platformSetupCommand.getName())) {
/*  77: 90 */         return platformSetupCommand;
/*  78:    */       }
/*  79:    */     }
/*  80: 93 */     return this.helpCommand;
/*  81:    */   }
/*  82:    */   
/*  83:    */   private void configureApplication(CommandLine line)
/*  84:    */   {
/*  85: 97 */     Properties systemProperties = line.getOptionProperties("D");
/*  86: 98 */     for (Map.Entry<Object, Object> systemProperty : systemProperties.entrySet()) {
/*  87: 99 */       System.setProperty(systemProperty.getKey().toString(), systemProperty.getValue().toString());
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   private void execute(CommandLine line)
/*  92:    */   {
/*  93:    */     try
/*  94:    */     {
/*  95:105 */       getCommand(line).execute(this.options, line);
/*  96:    */     }
/*  97:    */     catch (CommandException e)
/*  98:    */     {
/*  99:108 */       LOGGER.error(e.getMessage());
/* 100:109 */       System.exit(1);
/* 101:    */     }
/* 102:    */     catch (Exception e)
/* 103:    */     {
/* 104:111 */       if (LOGGER.isDebugEnabled())
/* 105:    */       {
/* 106:112 */         LOGGER.debug("ERROR: ", e);
/* 107:    */       }
/* 108:    */       else
/* 109:    */       {
/* 110:114 */         LOGGER.error(e.getMessage());
/* 111:115 */         LOGGER.error("You might get more detailed information about the error by adding '--debug' to the command line, and run again");
/* 112:    */       }
/* 113:119 */       System.exit(1);
/* 114:    */     }
/* 115:121 */     System.exit(0);
/* 116:    */   }
/* 117:    */   
/* 118:    */   private CommandLine parseArguments(String[] args, CommandLineParser parser)
/* 119:    */   {
/* 120:    */     try
/* 121:    */     {
/* 122:127 */       return parser.parse(this.options, args);
/* 123:    */     }
/* 124:    */     catch (ParseException exp)
/* 125:    */     {
/* 126:129 */       System.err.println("ERROR: error while parsing arguments " + exp.getMessage());
/* 127:130 */       System.exit(1);
/* 128:    */     }
/* 129:132 */     return null;
/* 130:    */   }
/* 131:    */   
/* 132:    */   private List<PlatformSetupCommand> createCommands()
/* 133:    */   {
/* 134:136 */     List<PlatformSetupCommand> commandList = new ArrayList();
/* 135:137 */     commandList.add(new InitCommand());
/* 136:138 */     commandList.add(new ConfigureCommand());
/* 137:139 */     commandList.add(new PullCommand());
/* 138:140 */     commandList.add(new PushCommand());
/* 139:141 */     this.helpCommand = new HelpCommand();
/* 140:142 */     commandList.add(this.helpCommand);
/* 141:143 */     return commandList;
/* 142:    */   }
/* 143:    */   
/* 144:    */   private Options createOptions()
/* 145:    */   {
/* 146:147 */     Options options = new Options();
/* 147:148 */     Option systemPropertyOption = new Option("D", "specify system property to override configuration from database.properties");
/* 148:    */     
/* 149:150 */     systemPropertyOption.setArgName("property=value");
/* 150:151 */     systemPropertyOption.setValueSeparator('=');
/* 151:152 */     systemPropertyOption.setArgs(2);
/* 152:153 */     options.addOption(systemPropertyOption);
/* 153:154 */     options.addOption("f", "force", false, "Force push even if critical folders will be deleted");
/* 154:155 */     return options;
/* 155:    */   }
/* 156:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.PlatformSetupApplication
 * JD-Core Version:    0.7.0.1
 */