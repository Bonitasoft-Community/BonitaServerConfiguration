/*  1:   */ package org.bonitasoft.platform.setup.jndi;
/*  2:   */ 
/*  3:   */ import javax.naming.CompositeName;
/*  4:   */ import javax.naming.Name;
/*  5:   */ import javax.naming.NameParser;
/*  6:   */ import javax.naming.NamingException;
/*  7:   */ 
/*  8:   */ public class SimpleNameParser
/*  9:   */   implements NameParser
/* 10:   */ {
/* 11:   */   public SimpleNameParser(String name) {}
/* 12:   */   
/* 13:   */   public Name parse(String name)
/* 14:   */     throws NamingException
/* 15:   */   {
/* 16:29 */     return new CompositeName(name);
/* 17:   */   }
/* 18:   */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.jndi.SimpleNameParser
 * JD-Core Version:    0.7.0.1
 */