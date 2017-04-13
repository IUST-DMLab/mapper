package ir.ac.iust.dml.kg.mapper.runner;

import ir.ac.iust.dml.kg.mapper.runner.web.Jackson2ObjectMapperPrettier;
import ir.ac.iust.dml.kg.mapper.runner.web.filter.FilterRegistrationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(value = {
        "classpath:access-context.xml",
        "classpath:persistence-context.xml",
        "classpath:template-equals-context.xml",
        "classpath:mapper-context.xml",
        "classpath:ontology-translation-context.xml"
})
@EnableAutoConfiguration(exclude = {
        Jackson2ObjectMapperPrettier.class,
        FilterRegistrationConfiguration.class,
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
