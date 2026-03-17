package it.portus.business.commons.stream.publisher;

@FunctionalInterface
public interface EventPublisher<T> {
  void publish(T event);
}
