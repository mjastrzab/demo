package com.example.demo.application;

import com.example.demo.application.requests.ChangeLimitRequest;
import com.example.demo.application.requests.PayLimitRequest;
import com.example.demo.application.requests.UseLimitRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.demo.persistence.EventStore.eventStore;
import static com.example.demo.utils.MathContext.subtract;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        eventStore.clear();
    }

    private static final BigDecimal limit = TEN;
    private static final BigDecimal useLimit = new BigDecimal("5");

    @Test
    public void shouldCreateCard() throws Exception {
        // when
        var response = mockMvc.perform(post("/v1/cards"));

        // then
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$.cardId").isNotEmpty())
            .andExpect(jsonPath("$.availableLimit").value("0"));

        // and
        assertThat(eventStore).hasSize(1);
    }

    @Test
    public void shouldChangeLimit() throws Exception {
        // given
        createCard();

        UUID cardId = readCardId();
        BigDecimal newLimit = TEN;

        // when
        var changeLimitResponse = mockMvc.perform(
            post("/v1/cards/" + cardId + "/limits/change").
                contentType(APPLICATION_JSON).
                content(objectMapper.writeValueAsString(new ChangeLimitRequest(newLimit)))
        );

        // then
        changeLimitResponse.andExpect(status().isOk());

        // and
        assertThat(eventStore.get(cardId).availableLimit()).isEqualTo(newLimit);
    }

    @Test
    public void shouldUseLimit() throws Exception {
        // given
        createCard();

        UUID cardId = readCardId();

        changeLimit(limit, cardId);

        // when
        var useLimitResponse = mockMvc.perform(
            post("/v1/cards/" + cardId + "/limits/use").
                contentType(APPLICATION_JSON).
                content(objectMapper.writeValueAsString(new UseLimitRequest(useLimit)))
        );

        // then
        useLimitResponse.andExpect(status().isOk());

        // and
        assertThat(eventStore.get(cardId).availableLimit()).isEqualTo(subtract(limit, useLimit));
    }

    @Test
    public void shouldPayLimit() throws Exception {
        // given
        createCard();

        UUID cardId = readCardId();

        changeLimit(limit, cardId);
        useLimit(useLimit, cardId);

        // when
        var payLimitResponse = mockMvc.perform(
            post("/v1/cards/" + cardId + "/limits/pay").
                contentType(APPLICATION_JSON).
                content(objectMapper.writeValueAsString(new PayLimitRequest(useLimit)))
        );

        // then
        payLimitResponse.andExpect(status().isOk());

        // and
        assertThat(eventStore.get(cardId).availableLimit()).isEqualTo(limit);
    }

    private void createCard() throws Exception {
        mockMvc.perform(post("/v1/cards"));
    }

    private UUID readCardId() {
        return eventStore.keySet().stream().findAny().orElseThrow();
    }

    private void changeLimit(BigDecimal limit, UUID cardId) throws Exception {
        mockMvc.perform(
            post("/v1/cards/" + cardId + "/limits/change").
                contentType(APPLICATION_JSON).
                content(objectMapper.writeValueAsString(new ChangeLimitRequest(limit)))
        );
    }

    private void useLimit(BigDecimal limit, UUID cardId) throws Exception {
        mockMvc.perform(
            post("/v1/cards/" + cardId + "/limits/use").
                contentType(APPLICATION_JSON).
                content(objectMapper.writeValueAsString(new UseLimitRequest(limit)))
        );
    }
}
