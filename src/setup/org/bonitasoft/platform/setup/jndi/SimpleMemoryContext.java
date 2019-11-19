/*   1:    */ package org.bonitasoft.platform.setup.jndi;
/*   2:    */ 
/*   3:    */ import java.util.Hashtable;
/*   4:    */ import java.util.Map;
/*   5:    */ import java.util.concurrent.ConcurrentHashMap;
/*   6:    */ import javax.naming.Binding;
/*   7:    */ import javax.naming.Context;
/*   8:    */ import javax.naming.Name;
/*   9:    */ import javax.naming.NameAlreadyBoundException;
/*  10:    */ import javax.naming.NameClassPair;
/*  11:    */ import javax.naming.NameNotFoundException;
/*  12:    */ import javax.naming.NameParser;
/*  13:    */ import javax.naming.NamingEnumeration;
/*  14:    */ import javax.naming.NamingException;
/*  15:    */ 
/*  16:    */ public class SimpleMemoryContext
/*  17:    */   implements Context
/*  18:    */ {
/*  19:    */   private static final String NOT_SUPPORTED_YET = "Not supported yet.";
/*  20: 37 */   private final Map<String, Object> dictionary = new ConcurrentHashMap();
/*  21:    */   
/*  22:    */   public void clear()
/*  23:    */   {
/*  24: 40 */     this.dictionary.clear();
/*  25:    */   }
/*  26:    */   
/*  27:    */   public Object lookup(Name name)
/*  28:    */     throws NamingException
/*  29:    */   {
/*  30: 45 */     return lookup(name.toString());
/*  31:    */   }
/*  32:    */   
/*  33:    */   public Object lookup(String name)
/*  34:    */     throws NamingException
/*  35:    */   {
/*  36: 50 */     if (this.dictionary.containsKey(name)) {
/*  37: 51 */       return this.dictionary.get(name);
/*  38:    */     }
/*  39: 53 */     throw new NameNotFoundException("Name " + name + " is not bound !");
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void bind(Name name, Object o)
/*  43:    */     throws NamingException
/*  44:    */   {
/*  45: 58 */     bind(name.toString(), o);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public void bind(String name, Object o)
/*  49:    */     throws NamingException
/*  50:    */   {
/*  51: 63 */     if (this.dictionary.containsKey(name)) {
/*  52: 64 */       throw new NameAlreadyBoundException("Name " + name + " already bound!");
/*  53:    */     }
/*  54: 66 */     rebind(name, o);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void rebind(Name name, Object o)
/*  58:    */   {
/*  59: 71 */     rebind(name.toString(), o);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void rebind(String name, Object o)
/*  63:    */   {
/*  64: 76 */     this.dictionary.put(name, o);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void unbind(Name name)
/*  68:    */     throws NamingException
/*  69:    */   {
/*  70: 81 */     unbind(name.toString());
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void unbind(String name)
/*  74:    */     throws NamingException
/*  75:    */   {
/*  76: 86 */     if (!this.dictionary.containsKey(name)) {
/*  77: 87 */       throw new NameNotFoundException("No such name " + name + " is bound!");
/*  78:    */     }
/*  79: 89 */     this.dictionary.remove(name);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void rename(Name oldName, Name newName)
/*  83:    */     throws NamingException
/*  84:    */   {
/*  85: 94 */     rename(oldName.toString(), newName.toString());
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void rename(String oldName, String newName)
/*  89:    */     throws NamingException
/*  90:    */   {
/*  91: 99 */     Object object = lookup(oldName);
/*  92:100 */     bind(newName, object);
/*  93:101 */     unbind(oldName);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public NamingEnumeration<NameClassPair> list(Name name)
/*  97:    */   {
/*  98:106 */     throw new UnsupportedOperationException("Not supported yet.");
/*  99:    */   }
/* 100:    */   
/* 101:    */   public NamingEnumeration<NameClassPair> list(String string)
/* 102:    */   {
/* 103:111 */     throw new UnsupportedOperationException("Not supported yet.");
/* 104:    */   }
/* 105:    */   
/* 106:    */   public NamingEnumeration<Binding> listBindings(Name name)
/* 107:    */   {
/* 108:116 */     throw new UnsupportedOperationException("Not supported yet.");
/* 109:    */   }
/* 110:    */   
/* 111:    */   public NamingEnumeration<Binding> listBindings(String string)
/* 112:    */   {
/* 113:121 */     throw new UnsupportedOperationException("Not supported yet.");
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void destroySubcontext(Name name)
/* 117:    */   {
/* 118:126 */     destroySubcontext(name.toString());
/* 119:    */   }
/* 120:    */   
/* 121:    */   public void destroySubcontext(String name)
/* 122:    */   {
/* 123:131 */     this.dictionary.remove(name);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public Context createSubcontext(Name name)
/* 127:    */     throws NamingException
/* 128:    */   {
/* 129:136 */     return createSubcontext(name.toString());
/* 130:    */   }
/* 131:    */   
/* 132:    */   public Context createSubcontext(String name)
/* 133:    */     throws NamingException
/* 134:    */   {
/* 135:141 */     Context subContext = new SimpleMemoryContext();
/* 136:142 */     bind(name, subContext);
/* 137:143 */     return subContext;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public Object lookupLink(Name name)
/* 141:    */   {
/* 142:148 */     throw new UnsupportedOperationException("Not supported yet.");
/* 143:    */   }
/* 144:    */   
/* 145:    */   public Object lookupLink(String string)
/* 146:    */   {
/* 147:153 */     throw new UnsupportedOperationException("Not supported yet.");
/* 148:    */   }
/* 149:    */   
/* 150:    */   public NameParser getNameParser(Name name)
/* 151:    */   {
/* 152:158 */     throw new UnsupportedOperationException("Not supported yet.");
/* 153:    */   }
/* 154:    */   
/* 155:    */   public NameParser getNameParser(String name)
/* 156:    */   {
/* 157:163 */     return new SimpleNameParser(name);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public Name composeName(Name name, Name name1)
/* 161:    */   {
/* 162:168 */     throw new UnsupportedOperationException("Not supported yet.");
/* 163:    */   }
/* 164:    */   
/* 165:    */   public String composeName(String string, String string1)
/* 166:    */   {
/* 167:173 */     throw new UnsupportedOperationException("Not supported yet.");
/* 168:    */   }
/* 169:    */   
/* 170:    */   public Object addToEnvironment(String string, Object o)
/* 171:    */   {
/* 172:178 */     throw new UnsupportedOperationException("Not supported yet.");
/* 173:    */   }
/* 174:    */   
/* 175:    */   public Object removeFromEnvironment(String string)
/* 176:    */   {
/* 177:183 */     throw new UnsupportedOperationException("Not supported yet.");
/* 178:    */   }
/* 179:    */   
/* 180:    */   public Hashtable<?, ?> getEnvironment()
/* 181:    */   {
/* 182:188 */     throw new UnsupportedOperationException("Not supported yet.");
/* 183:    */   }
/* 184:    */   
/* 185:    */   public void close() {}
/* 186:    */   
/* 187:    */   public String getNameInNamespace()
/* 188:    */   {
/* 189:199 */     throw new UnsupportedOperationException("Not supported yet.");
/* 190:    */   }
/* 191:    */ }


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.jndi.SimpleMemoryContext
 * JD-Core Version:    0.7.0.1
 */