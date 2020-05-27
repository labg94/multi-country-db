package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MultiCountryConfiguration {

  private final DataSourceProperties properties;

  @Value("classpath:countries")
  private Resource countriesFolder;

  /**
   * Defines the data source for the application
   *
   * @return -
   */
  @Bean
  public DataSource dataSource() {

    Map<Object, Object> resolvedDataSources = new HashMap<>();

    try {
      DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(countriesFolder.getURI()));

      for (Path path : stream) {
        if (!Files.isDirectory(path)) {
          File propertyFile = path.toFile();

          Properties tenantProperties = new Properties();
          DataSourceBuilder<?> dataSourceBuilder =
              DataSourceBuilder.create(this.getClass().getClassLoader());

          try {
            tenantProperties.load(new FileInputStream(propertyFile));

            String tenantId = tenantProperties.getProperty("name");

            dataSourceBuilder
                .driverClassName(properties.getDriverClassName())
                .url(tenantProperties.getProperty("url"))
                .username(tenantProperties.getProperty("username"))
                .password(tenantProperties.getProperty("password"));

            if (properties.getType() != null) {
              dataSourceBuilder.type(properties.getType());
            }

            resolvedDataSources.put(tenantId, dataSourceBuilder.build());
          } catch (IOException e) {
            e.printStackTrace();

            return null;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Create the final multi-tenant source.
    // It needs a default database to connect to.
    // Make sure that the default database is actually an empty tenant database.
    // Don't use that for a regular tenant if you want things to be safe!
    MultiCountryDataSource dataSource = new MultiCountryDataSource();
    dataSource.setDefaultTargetDataSource(defaultDataSource());
    dataSource.setTargetDataSources(resolvedDataSources);

    // Call this to finalize the initialization of the data source.
    dataSource.afterPropertiesSet();

    return dataSource;
  }

  /**
   * Creates the default data source for the application
   *
   * @return -
   */
  private DataSource defaultDataSource() {
    DataSourceBuilder<?> dataSourceBuilder =
        DataSourceBuilder.create(this.getClass().getClassLoader())
            .driverClassName(properties.getDriverClassName())
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword());

    if (properties.getType() != null) {
      dataSourceBuilder.type(properties.getType());
    }

    return dataSourceBuilder.build();
  }
}
