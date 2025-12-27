package it.portus.smartorder.ms.orderservice.business.conf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = "it.portus.smartorder.ms.orderservice.business")
public class BusinessAutoConfiguration {}
