package com.example.demo.domain;

import com.example.demo.domain.events.LimitChangedEvent;
import com.example.demo.domain.events.LimitPartPaidEvent;
import com.example.demo.domain.events.LimitPartUsedEvent;
import com.example.demo.domain.exceptions.LimitExceededException;
import com.example.demo.domain.exceptions.NegativeLimitException;
import com.example.demo.domain.exceptions.NegativeLimitPartException;
import com.example.demo.domain.exceptions.TooHighLimitPaymentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.demo.utils.MathContext.add;
import static com.example.demo.utils.MathContext.subtract;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CardTest {

    private final UUID cardId = UUID.randomUUID();

    @Test
    void shouldChangeLimit() {
        // given
        var limit = TEN;
        var card = new Card(cardId);

        assertThat(card.availableLimit()).isZero();

        // when
        card.changeLimit(limit);

        // then
        assertThat(card.availableLimit()).isEqualTo(limit);
    }

    @Test
    void shouldDeclineNegativeLimit() {
        // given
        var limit = TEN.negate();
        var card = new Card(cardId);

        // when
        var thrown = catchThrowable(() -> card.changeLimit(limit));

        // then
        assertThat(thrown).isExactlyInstanceOf(NegativeLimitException.class);
    }

    @Test
    void shouldCreateStateChangeEventOnLimitChange() {
        // given
        var limit = TEN;
        var card = new Card(cardId);

        // when
        card.changeLimit(limit);

        // then
        assertThat(card.getStateChanges())
            .hasSize(1)
            .containsExactlyInAnyOrder(new LimitChangedEvent(card.getId(), card.availableLimit()));
    }

    @Test
    void shouldApplyLimitChangedEvent() {
        // given
        var limit = TEN;
        var card = new Card(cardId);

        var event = new LimitChangedEvent(cardId, limit);

        // when
        card.handle(event);

        // then
        assertThat(card.availableLimit())
            .isEqualTo(limit)
            .isEqualTo(event.getLimit());
    }

    @Test
    void shouldAllowIncreasedLimit() {
        // given
        var limitPart = ONE;
        var newLimit = limitPart;
        var card = new Card(cardId);

        // when
        var failedLimitPartUsage = catchThrowable(() -> card.useLimit(limitPart));

        // then
        assertThat(failedLimitPartUsage).isInstanceOf(LimitExceededException.class);

        // when
        card.changeLimit(newLimit);
        card.useLimit(limitPart);

        // then
        assertThat(card.availableLimit()).isEqualTo(subtract(newLimit, limitPart));
    }

    @Test
    void shouldUseLimitPart() {
        // given
        var limit = TEN;
        var limitPart = ONE;

        var card = new Card(cardId);
        card.changeLimit(limit);

        // when
        card.useLimit(limitPart);

        // then
        assertThat(card.availableLimit()).isEqualTo(subtract(limit, limitPart));
    }

    @Test
    void shouldDeclineNegativeLimitPart() {
        // given
        var limit = TEN;
        var negativeLimitPart = ONE.negate();

        var card = new Card(cardId);
        card.changeLimit(limit);

        // when
        var thrown = catchThrowable(() -> card.useLimit(negativeLimitPart));

        // then
        assertThat(thrown).isExactlyInstanceOf(NegativeLimitPartException.class);
    }

    @Test
    void shouldDeclineLimitPartExceedingLimit() {
        // given
        var limit = ONE;
        var limitPart = TEN;

        var card = new Card(cardId);
        card.changeLimit(limit);

        // when
        var thrown = catchThrowable(() -> card.useLimit(limitPart));

        // then
        assertThat(thrown).isExactlyInstanceOf(LimitExceededException.class);
    }

    private static final BigDecimal TWO = new BigDecimal("2");

    @Test
    void shouldHandleMultipleLimitParts() {
        var limitPart = ONE;
        var limit = add(limitPart, limitPart);

        var card = new Card(cardId);
        card.handle(new LimitChangedEvent(cardId, limit));

        // when
        card.useLimit(limitPart);
        card.useLimit(limitPart);
        var thrown = catchThrowable(() -> card.useLimit(limitPart));

        // then
        assertThat(card.availableLimit()).isEqualTo(ZERO);
        assertThat(thrown).isInstanceOf(LimitExceededException.class);
    }

    @Test
    void shouldCreateLimitPartUsedEventStateChange() {
        // given
        var limit = TEN;
        var limitPart = ONE;

        var card = new Card(cardId);
        card.handle(new LimitChangedEvent(cardId, limit));

        // when
        card.useLimit(limitPart);

        // then
        assertThat(card.getStateChanges())
            .hasSize(1)
            .containsExactlyInAnyOrder(
                new LimitPartUsedEvent(cardId, limitPart));
    }

    @Test
    void shouldHandleLimitPartUsedEvent() {
        // given
        var limit = TEN;
        var limitPart = ONE;

        var card = new Card(cardId);
        card.handle(new LimitChangedEvent(cardId, limit));

        // when
        card.handle(new LimitPartUsedEvent(cardId, limitPart));

        // then
        assertThat(card.availableLimit()).isEqualTo(subtract(limit, limitPart));
    }

    @Test
    void shouldPayTheLimit() {
        // given
        var limit = ONE;
        var card = new Card(cardId);

        card.handle(new LimitChangedEvent(cardId, limit));
        card.handle(new LimitPartUsedEvent(cardId, limit));

        // when
        card.payLimit(limit);

        // then
        assertThat(card.availableLimit()).isEqualTo(limit);
    }

    @Test
    void shouldDeclinePaymentExceedingUsedLimit() {
        // given
        var limit = ONE;
        var card = new Card(cardId);
        card.handle(new LimitChangedEvent(cardId, limit));

        // when
        var thrown = catchThrowable(() -> card.payLimit(limit));

        // then
        assertThat(thrown).isExactlyInstanceOf(TooHighLimitPaymentException.class);
    }

    @Test
    void shouldCreateStateChangeLimitPartPaidEvent() {
        // given
        var limit = ONE;
        var payment = ZERO;

        var card = new Card(cardId);
        card.handle(new LimitChangedEvent(cardId, limit));

        // when
        card.payLimit(payment);

        // then
        assertThat(card.getStateChanges())
            .hasSize(1)
            .containsExactlyInAnyOrder(new LimitPartPaidEvent(cardId, payment));
    }

    @Test
    void shouldHandleLimitPartPaidEvent() {
        // given
        var limit = ONE;
        var card = new Card(cardId);

        card.handle(new LimitChangedEvent(cardId, limit));
        card.handle(new LimitPartUsedEvent(cardId, limit));

        // when
        card.handle(new LimitPartPaidEvent(cardId, limit));

        // then
        assertThat(card.availableLimit()).isEqualTo(limit);
    }
}
