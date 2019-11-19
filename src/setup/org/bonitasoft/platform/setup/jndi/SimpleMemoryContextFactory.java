/*  1:   */ package org.bonitasoft.platform.setup.jndi;
/*  2:   */ 
/*  3:   */ import java.util.Hashtable;
/*  4:   */ import javax.naming.Context;
/*  5:   */ import javax.naming.spi.InitialContextFactory;
/*  6:   */ 
/*  7:   */ public class SimpleMemoryContextFactory
/*  8:   */   implements InitialContextFactory
/*  9:   */ {
/* 10:27 */   private static final SimpleMemoryContext context = new SimpleMemoryContext();
/* 11:   */   
/* 12:   */   public Context getInitialContext(Hashtable<?, ?> environment)
/* 13:   */   {
/* 14:31 */     return context;
/* 15:   */   }
/* 16:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.jndi.SimpleMemoryContextFactory
 * JD-Core Version:    0.7.0.1
 */