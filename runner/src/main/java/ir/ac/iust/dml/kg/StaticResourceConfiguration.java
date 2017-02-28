package ir.ac.iust.dml.kg;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {

  private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
          "classpath:/META-INF/resources/", "classpath:/resources/",
          "classpath:/static/", "classpath:/public/", "classpath:/resources/static"};

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
  }
}