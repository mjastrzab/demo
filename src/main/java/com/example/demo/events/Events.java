package com.example.demo.events;

import com.example.demo.domain.events.LimitChangedEvent;
import com.example.demo.domain.events.LimitPartPaidEvent;
import com.example.demo.domain.events.LimitPartUsedEvent;

public interface Events {

    void emit(LimitChangedEvent event);

    void emit(LimitPartPaidEvent event);

    void emit(LimitPartUsedEvent event);

}
