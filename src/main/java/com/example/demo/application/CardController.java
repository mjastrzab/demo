package com.example.demo.application;

import com.example.demo.application.requests.ChangeLimitRequest;
import com.example.demo.application.requests.PayLimitRequest;
import com.example.demo.application.requests.UseLimitRequest;
import com.example.demo.application.responses.CardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping(path = "/v1/cards", produces = APPLICATION_JSON_VALUE)
    public CardResponse createCard() {
        return CardResponse.from(cardService.createCard());
    }

    @GetMapping(path = "/v1/cards/{cardId}", produces = APPLICATION_JSON_VALUE)
    public CardResponse readCard(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.readCard(cardId));
    }

    @PostMapping(path = "/v1/cards/{cardId}/limits/change", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void changeLimit(@PathVariable UUID cardId, @RequestBody ChangeLimitRequest request) {
        cardService.changeLimit(cardId, request.getLimitChange());
    }

    @PostMapping(path = "/v1/cards/{cardId}/limits/use", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void useLimit(@PathVariable UUID cardId, @RequestBody UseLimitRequest request) {
        cardService.useLimit(cardId, request.getUseLimit());
    }

    @PostMapping(path = "/v1/cards/{cardId}/limits/pay", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void payLimit(@PathVariable UUID cardId, @RequestBody PayLimitRequest request) {
        cardService.payLimit(cardId, request.getPayLimit());
    }
}
