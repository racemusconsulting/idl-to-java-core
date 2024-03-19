package com.racemus.eurocontrol.idltojava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class IdlToJavaGeneratorApplication {


    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(IdlToJavaGeneratorApplication.class, args);
    }

}
