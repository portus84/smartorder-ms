package it.portus.smartorder.ms.orderservice.business.conf;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration
@ConditionalOnClass(org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class)
@EnableMongoAuditing
@EnableMongoRepositories(
    basePackages = "it.portus.smartorder.ms.orderservice.business.domain.repositories")
public class MongoAutoConfiguration {

  private static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
    @Override
    public OffsetDateTime convert(Date date) {
      return date.toInstant().atOffset(ZoneOffset.UTC);
    }
  }

  private static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
    @Override
    public Date convert(OffsetDateTime offsetDateTime) {
      return Date.from(offsetDateTime.toInstant());
    }
  }

  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(
        Arrays.asList(new OffsetDateTimeWriteConverter(), new OffsetDateTimeReadConverter()));
  }
}
