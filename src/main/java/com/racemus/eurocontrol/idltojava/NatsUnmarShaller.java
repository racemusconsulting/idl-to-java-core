package com.racemus.eurocontrol.idltojava;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
@Service
public class NatsUnmarShaller {

    public Object deserializeObject(byte[] byteArray) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception e) {
            log.error("Error during deserialization", e);
            throw e;
        }
    }
}
