package com.example.demo.domain;

import com.example.demo.domain.events.LimitChangedEvent;
import com.example.demo.domain.events.LimitPartPaidEvent;
import com.example.demo.domain.events.LimitPartUsedEvent;
import com.example.demo.domain.exceptions.LimitExceededException;
import com.example.demo.domain.exceptions.NegativeLimitException;
import com.example.demo.domain.exceptions.NegativeLimitPartException;
import com.example.demo.domain.exceptions.TooHighLimitPaymentException;
import com.example.demo.events.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.example.demo.utils.MathContext.add;
import static com.example.demo.utils.MathContext.subtract;
import static java.math.BigDecimal.ZERO;

@RequiredArgsConstructor
public class Card {

    @Getter
    private final UUID id;
    private BigDecimal limit = ZERO;
    private BigDecimal usedLimit = ZERO;
    @Getter
    private List<Event> stateChanges = new LinkedList<>();

    public void changeLimit(BigDecimal limit) {
        if (limit.compareTo(ZERO) < 0) {
            throw new NegativeLimitException();
        }

        LimitChangedEvent event = new LimitChangedEvent(id, limit);
        stateChanges.add(event);
        handle(event);
    }

    public void handle(LimitChangedEvent event) {
        limit = event.getLimit();
    }

    public BigDecimal availableLimit() {
        return subtract(limit, usedLimit);
    }

    public void useLimit(BigDecimal limitPart) {
        declineNegativeLimitParts(limitPart);

        var usedLimitCandidate = add(usedLimit, limitPart);

        declineLimitOverflow(usedLimitCandidate);

        LimitPartUsedEvent event = new LimitPartUsedEvent(id, limitPart);
        stateChanges.add(event);
        handle(event);
    }

    private void declineNegativeLimitParts(BigDecimal limitPart) {
        if (limitPart.compareTo(ZERO) < 0) {
            throw new NegativeLimitPartException();
        }
    }

    private void declineLimitOverflow(BigDecimal usedLimitCandidate) {
        if (usedLimitCandidate.compareTo(limit) > 0) {
            throw new LimitExceededException();
        }
    }

    public void handle(LimitPartUsedEvent event) {
        usedLimit = add(usedLimit, event.getLimitPartValue());
    }

    public void payLimit(BigDecimal payment) {
        if (subtract(usedLimit, payment).compareTo(ZERO) < 0) {
            throw new TooHighLimitPaymentException();
        }

        LimitPartPaidEvent event = new LimitPartPaidEvent(id, payment);
        stateChanges.add(event);
        handle(event);
    }

    public void handle(LimitPartPaidEvent event) {
        usedLimit = subtract(usedLimit, event.getLimitPartValue());
    }
}
