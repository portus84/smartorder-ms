package it.portus.smartorder.ms.orderservice.business.domain.repositories;

import it.portus.smartorder.ms.orderservice.business.domain.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OrderRepository extends MongoRepository<Order, ObjectId>, QuerydslPredicateExecutor<Order> {}
