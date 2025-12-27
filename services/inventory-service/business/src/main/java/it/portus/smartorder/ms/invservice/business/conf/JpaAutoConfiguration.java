package it.portus.smartorder.ms.invservice.business.conf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ConditionalOnClass(org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration.class)
@EnableJpaAuditing
@EntityScan(basePackages = "it.portus.smartorder.ms.invservice.business.domain")
@EnableJpaRepositories(
    basePackages = "it.portus.smartorder.ms.invservice.business.domain.repositories")
public class JpaAutoConfiguration {}
