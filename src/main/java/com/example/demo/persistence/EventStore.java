package com.example.demo.persistence;

import com.example.demo.domain.Card;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EventStore {
    public static Map<UUID, Card> eventStore = new ConcurrentHashMap<>();

}
