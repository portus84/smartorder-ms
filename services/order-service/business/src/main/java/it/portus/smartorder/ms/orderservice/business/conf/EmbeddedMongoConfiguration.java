package it.portus.smartorder.ms.orderservice.business.conf;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(
    name = "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
@AutoConfigureAfter(
    name = "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
public class EmbeddedMongoConfiguration {

  @Bean
  CommandLineRunner initReplicaSet(MongoClient mongoClient) {
    return args -> {
      MongoDatabase adminDb = mongoClient.getDatabase("admin");

      try {
        adminDb.runCommand(new Document("replSetInitiate", new Document()));
      } catch (MongoCommandException e) {
        if (e.getErrorCode() != 23 && e.getErrorCode() != 400) {
          throw e;
        }
      }
    };
  }
}
