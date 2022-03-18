package com.example.demo.application;

import com.example.demo.domain.Card;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.demo.persistence.EventStore.eventStore;

@Service
public class CardService {

    public Card createCard() {
        UUID id = UUID.randomUUID();

        Card card = new Card(id);

        eventStore.put(id, card);

        return card;
    }

    public Card readCard(UUID cardId) {
        return eventStore.get(cardId);
    }

    public void changeLimit(UUID cardId, BigDecimal limit) {
        Card card = eventStore.get(cardId);

        card.changeLimit(limit);

        eventStore.put(cardId, card);
    }

    public void useLimit(UUID cardId, BigDecimal limit) {
        Card card = eventStore.get(cardId);

        card.useLimit(limit);

        eventStore.put(cardId, card);
    }

    public void payLimit(UUID cardId, BigDecimal limit) {
        Card card = eventStore.get(cardId);

        card.payLimit(limit);

        eventStore.put(cardId, card);
    }
}
