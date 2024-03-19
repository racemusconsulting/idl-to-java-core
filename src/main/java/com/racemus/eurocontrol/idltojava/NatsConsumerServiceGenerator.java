package com.racemus.eurocontrol.idltojava;

import com.racemus.eurocontrol.idltojava.domain.SubjectAndDto;
import com.squareup.javapoet.*;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class NatsConsumerServiceGenerator {

    private static final String PACKAGE_NAME = "com.racemus.eurocontrol.idltojava.generated.nats.service.consumer";

    public static void generateConsumerServiceClass(SubjectAndDto subjectAndDto) {
        String interfaceName = "INatsConsumerService" + subjectAndDto.getSubject().replaceAll("\\W+", ""); // Sanitize subject for interface name
        String className = "NatsConsumerService" + subjectAndDto.getSubject().replaceAll("\\W+", ""); // Sanitize subject for class name

        // Generate the interface with additional consume method
        TypeSpec consumerInterface = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("consumeMessages")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(MessageHandler.class, "messageHandler")
                        .build())
                .addMethod(MethodSpec.methodBuilder("stop")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .build();

        // Generate the service class
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(PACKAGE_NAME, interfaceName))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j")).build())
                .addJavadoc("""
                        ****************************************************************************
                        *                       Copyright (c) 2024 Eurocontrol.                    *
                        *                            All Rights Reserved                           *
                        *        THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Eurocontrol        *
                        *        The copyright notice above does not evidence any actual or        *
                        *                intended publication of such source code.                 *
                        ****************************************************************************
                                                    
                        Automatically generated NATS consumer service for subject $L.
                        """, subjectAndDto.getSubject())
                .addField(Connection.class, "natsConnection", Modifier.PRIVATE, Modifier.FINAL)
                .addField(Dispatcher.class, "dispatcher", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Connection.class, "natsConnection")
                        .addStatement("this.natsConnection = natsConnection")
                        .build())
                .addMethod(MethodSpec.methodBuilder("consumeMessages")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(MessageHandler.class, "messageHandler")
                        .addStatement("this.dispatcher = this.natsConnection.createDispatcher(messageHandler)")
                        .addStatement("this.dispatcher.subscribe($S)", subjectAndDto.getSubject())
                        .build())
                .addMethod(MethodSpec.methodBuilder("stop")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("this.dispatcher.unsubscribe($S)", subjectAndDto.getSubject())
                        .build());

        // Write the interface and class to files
        writeJavaFile(consumerInterface, interfaceName);
        writeJavaFile(classBuilder.build(), className);
    }

    private static void writeJavaFile(TypeSpec typeSpec, String fileName) {
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
        try {
            javaFile.writeTo(Paths.get("./src/main/java/"));
            log.info("Successfully wrote the generated {} class/interface", fileName);
        } catch (IOException e) {
            log.error("Failed to write the generated class/interface {}: {}", fileName, e.getMessage());
        }
    }
}
