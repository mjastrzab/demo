package com.example.demo.events;

import com.example.demo.domain.events.LimitChangedEvent;
import com.example.demo.domain.events.LimitPartPaidEvent;
import com.example.demo.domain.events.LimitPartUsedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventsPublisher implements Events {

    private final ApplicationEventPublisher publisher;

    @Override
    public void emit(LimitChangedEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void emit(LimitPartPaidEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void emit(LimitPartUsedEvent event) {
        publisher.publishEvent(event);
    }

}
