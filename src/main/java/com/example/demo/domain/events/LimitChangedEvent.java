package com.example.demo.domain.events;

import com.example.demo.events.Event;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class LimitChangedEvent implements Event {
    UUID cardId;
    BigDecimal limit;
}
