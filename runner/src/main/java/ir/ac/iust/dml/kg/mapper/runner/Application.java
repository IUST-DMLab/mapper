package ir.ac.iust.dml.kg.mapper.runner;

import ir.ac.iust.dml.kg.mapper.runner.commander.Commander;
import ir.ac.iust.dml.kg.mapper.runner.web.Jackson2ObjectMapperPrettier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

@SpringBootApplication
@ImportResource(value = {
    "classpath:access-context.xml",
    "classpath:template-equals-context.xml",
    "classpath:mapper-context.xml",
    "classpath:ontology-translation-context.xml"
})
@EnableAutoConfiguration(exclude = {
    Jackson2ObjectMapperPrettier.class,
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@EnableCaching
public class Application {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    Properties properties = new Properties();
    if (args.length > 0) properties.put("server.port", 9200 + (new Random().nextInt(1000)));
    else properties.put("server.port", 8090);
    app.setDefaultProperties(properties);
    ConfigurableApplicationContext context = app.run(args);
    if (args.length > 0) {
      Commander commander = context.getBeansOfType(Commander.class).values().iterator().next();
      commander.processArgs(args[0], Arrays.copyOfRange(args, 1, args.length));
    }
  }

}
