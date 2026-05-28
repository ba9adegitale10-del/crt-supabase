package com.example.megrine.repository;

import com.example.megrine.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateAsc(Event.EventStatus status);
    List<Event> findAllByOrderByEventDateDesc();
    long countByStatus(Event.EventStatus status);
}
