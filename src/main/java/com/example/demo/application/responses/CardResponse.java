package com.example.demo.application.responses;

import com.example.demo.domain.Card;
import lombok.Value;

import java.util.UUID;

@Value
public class CardResponse {
    UUID cardId;
    String availableLimit;

    public static CardResponse from(Card card) {
        return new CardResponse(card.getId(), card.availableLimit().toPlainString());
    }
}
