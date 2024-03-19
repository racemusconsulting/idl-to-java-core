package com.racemus.eurocontrol.idltojava;

import io.nats.client.Connection;
import io.nats.client.Nats;
import java.nio.charset.StandardCharsets;

public class EventPublisherService {

    private Connection natsConnection;

    public EventPublisherService() {
        try {
            this.natsConnection = Nats.connect("nats://localhost:4222");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishEvent(String subject, String objectType, String eventData) {
        try {
            String payload = "Type: " + objectType + ", Data: " + eventData;
            this.natsConnection.publish(subject, payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishApwEvent(String eventData) {
        publishEvent("APW_CONFLICT_CRD", "YourObjectType", eventData);
    }

    public static void main(String[] args) {
        EventPublisherService service = new EventPublisherService();
        service.publishApwEvent("{\"example\": \"data\"}");
    }
}
