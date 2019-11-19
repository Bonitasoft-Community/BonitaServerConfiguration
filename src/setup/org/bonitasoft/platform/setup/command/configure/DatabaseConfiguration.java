/*   1:    */ package org.bonitasoft.platform.setup.command.configure;
/*   2:    */ 
/*   3:    */ import java.nio.file.Path;
/*   4:    */ import java.util.Properties;
/*   5:    */ import org.bonitasoft.platform.exception.PlatformException;
/*   6:    */ 
/*   7:    */ public class DatabaseConfiguration
/*   8:    */ {
/*   9:    */   public static final String H2_DB_VENDOR = "h2";
/*  10:    */   private String dbVendor;
/*  11:    */   private String nonXaDriverClassName;
/*  12:    */   private String xaDriverClassName;
/*  13:    */   private String xaDataSourceFactory;
/*  14:    */   private String databaseUser;
/*  15:    */   private String databasePassword;
/*  16:    */   private String databaseName;
/*  17:    */   private String serverName;
/*  18:    */   private String serverPort;
/*  19:    */   private String url;
/*  20:    */   private String testQuery;
/*  21:    */   private PropertyReader propertyReader;
/*  22:    */   
/*  23:    */   public DatabaseConfiguration(String prefix, Properties properties, Path rootPath)
/*  24:    */     throws PlatformException
/*  25:    */   {
/*  26: 42 */     this.propertyReader = new PropertyReader(properties);
/*  27: 43 */     this.dbVendor = getMandatoryProperty(prefix + "db.vendor");
/*  28:    */     
/*  29: 45 */     this.nonXaDriverClassName = getMandatoryProperty(this.dbVendor + ".nonXaDriver");
/*  30: 46 */     this.xaDriverClassName = getMandatoryProperty(this.dbVendor + ".xaDriver");
/*  31: 47 */     this.xaDataSourceFactory = getMandatoryProperty(this.dbVendor + ".xaDSFactory");
/*  32: 48 */     this.databaseName = getMandatoryProperty(prefix + "db.database.name");
/*  33: 49 */     this.url = getMandatoryProperty(this.dbVendor + "." + prefix + "url");
/*  34: 51 */     if ("h2".equals(this.dbVendor))
/*  35:    */     {
/*  36: 52 */       String h2DatabaseDir = getMandatoryProperty("h2.database.dir");
/*  37:    */       
/*  38: 54 */       this.url = this.url.replace("${h2.database.dir}", rootPath.resolve("setup").resolve(h2DatabaseDir).toAbsolutePath()
/*  39: 55 */         .normalize().toString());
/*  40:    */     }
/*  41:    */     else
/*  42:    */     {
/*  43: 57 */       this.serverName = getMandatoryProperty(prefix + "db.server.name");
/*  44: 58 */       this.url = this.url.replace("${" + prefix + "db.server.name}", this.serverName);
/*  45: 59 */       this.serverPort = getMandatoryProperty(prefix + "db.server.port");
/*  46: 60 */       this.url = this.url.replace("${" + prefix + "db.server.port}", this.serverPort);
/*  47:    */     }
/*  48: 62 */     this.url = this.url.replace("${" + prefix + "db.database.name}", this.databaseName);
/*  49: 63 */     this.databaseUser = getMandatoryProperty(prefix + "db.user");
/*  50: 64 */     this.databasePassword = getMandatoryProperty(prefix + "db.password");
/*  51: 65 */     this.testQuery = getMandatoryProperty(this.dbVendor + "." + prefix + "testQuery");
/*  52:    */   }
/*  53:    */   
/*  54:    */   public String getDbVendor()
/*  55:    */   {
/*  56: 69 */     return this.dbVendor;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public String getNonXaDriverClassName()
/*  60:    */   {
/*  61: 73 */     return this.nonXaDriverClassName;
/*  62:    */   }
/*  63:    */   
/*  64:    */   String getXaDriverClassName()
/*  65:    */   {
/*  66: 77 */     return this.xaDriverClassName;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public String getXaDataSourceFactory()
/*  70:    */   {
/*  71: 81 */     return this.xaDataSourceFactory;
/*  72:    */   }
/*  73:    */   
/*  74:    */   String getDatabaseUser()
/*  75:    */   {
/*  76: 85 */     return this.databaseUser;
/*  77:    */   }
/*  78:    */   
/*  79:    */   String getDatabasePassword()
/*  80:    */   {
/*  81: 89 */     return this.databasePassword;
/*  82:    */   }
/*  83:    */   
/*  84:    */   String getDatabaseName()
/*  85:    */   {
/*  86: 93 */     return this.databaseName;
/*  87:    */   }
/*  88:    */   
/*  89:    */   String getServerName()
/*  90:    */   {
/*  91: 97 */     return this.serverName != null ? this.serverName : "";
/*  92:    */   }
/*  93:    */   
/*  94:    */   String getServerPort()
/*  95:    */   {
/*  96:101 */     return this.serverPort != null ? this.serverPort : "";
/*  97:    */   }
/*  98:    */   
/*  99:    */   String getUrl()
/* 100:    */   {
/* 101:105 */     return this.url;
/* 102:    */   }
/* 103:    */   
/* 104:    */   String getTestQuery()
/* 105:    */   {
/* 106:109 */     return this.testQuery;
/* 107:    */   }
/* 108:    */   
/* 109:    */   private String getMandatoryProperty(String s)
/* 110:    */     throws PlatformException
/* 111:    */   {
/* 112:113 */     return this.propertyReader.getPropertyAndFailIfNull(s);
/* 113:    */   }
/* 114:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.command.configure.DatabaseConfiguration
 * JD-Core Version:    0.7.0.1
 */