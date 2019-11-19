/*  1:   */ package org.bonitasoft.platform.setup.jndi;
/*  2:   */ 
/*  3:   */ import javax.naming.NamingException;
/*  4:   */ import javax.sql.DataSource;
/*  5:   */ import org.slf4j.Logger;
/*  6:   */ import org.slf4j.LoggerFactory;
/*  7:   */ import org.springframework.beans.factory.DisposableBean;
/*  8:   */ import org.springframework.beans.factory.InitializingBean;
/*  9:   */ import org.springframework.beans.factory.annotation.Autowired;
/* 10:   */ import org.springframework.jndi.JndiTemplate;
/* 11:   */ import org.springframework.stereotype.Component;
/* 12:   */ 
/* 13:   */ @Component
/* 14:   */ public class MemoryJNDISetup
/* 15:   */   implements DisposableBean, InitializingBean
/* 16:   */ {
/* 17:   */   public static final String BONITA_NON_MANAGED_DS_JNDI_NAME = "java:comp/env/bonitaSequenceManagerDS";
/* 18:32 */   private final Logger logger = LoggerFactory.getLogger(MemoryJNDISetup.class.getSimpleName());
/* 19:   */   private final JndiTemplate jndiTemplate;
/* 20:   */   private final DataSource datasource;
/* 21:   */   
/* 22:   */   @Autowired
/* 23:   */   public MemoryJNDISetup(DataSource datasource)
/* 24:   */     throws NamingException
/* 25:   */   {
/* 26:40 */     this.datasource = datasource;
/* 27:41 */     System.setProperty("java.naming.factory.initial", "org.bonitasoft.platform.setup.jndi.SimpleMemoryContextFactory");
/* 28:42 */     System.setProperty("java.naming.factory.url.pkgs", "org.bonitasoft.platform.setup.jndi");
/* 29:43 */     this.jndiTemplate = new JndiTemplate();
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void afterPropertiesSet()
/* 33:   */     throws NamingException
/* 34:   */   {
/* 35:47 */     this.logger.info("Binding java:comp/env/bonitaSequenceManagerDS @ " + this.datasource.toString());
/* 36:48 */     this.jndiTemplate.bind("java:comp/env/bonitaSequenceManagerDS", this.datasource);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void destroy()
/* 40:   */     throws NamingException
/* 41:   */   {
/* 42:52 */     this.logger.info("Unbinding java:comp/env/bonitaSequenceManagerDS");
/* 43:53 */     this.jndiTemplate.unbind("java:comp/env/bonitaSequenceManagerDS");
/* 44:   */   }
/* 45:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.jndi.MemoryJNDISetup
 * JD-Core Version:    0.7.0.1
 */