package org.bonitasoft.platform.setup.dbconfig;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource({"classpath:/oracle.properties"})
@Profile({"oracle"})
public class OracleDataSourceConfig {}


/* Location:           D:\bonita\BPM-SP-7.9.0\workspace\tomcat\setup\lib\platform-setup-7.9.0.jar
 * Qualified Name:     org.bonitasoft.platform.setup.dbconfig.OracleDataSourceConfig
 * JD-Core Version:    0.7.0.1
 */