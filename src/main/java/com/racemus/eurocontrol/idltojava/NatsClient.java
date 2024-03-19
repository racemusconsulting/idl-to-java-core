package com.racemus.eurocontrol.idltojava;

import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NatsClient {

    /**
     * Constructs a NatsClient and establishes a connection to the NATS server.
     *
     * @param natsUrl the URL of the NATS server.
     */
    public Connection NatsClient(String natsUrl) {
        try {
            final Connection connection = Nats.connect(natsUrl);
            log.info("Connected to NATS server at {}", natsUrl);
            return connection;
        } catch (Exception e) {
            log.error("Failed to connect to NATS server at {}: {}", natsUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to connect to NATS server", e);
        }
    }
}
