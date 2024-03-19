package com.racemus.eurocontrol.idltojava;

import com.racemus.eurocontrol.idltojava.domain.SubjectAndDto;
import com.squareup.javapoet.*;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class NatsPublisherServiceGenerator {

    private static final String PACKAGE_NAME = "com.racemus.eurocontrol.idltojava.generated.nats.service.publisher";

    public static void generateServiceClass(SubjectAndDto subjectAndDto) {
        String sanitizedSubject = subjectAndDto.getSubject().replaceAll("\\W+", "");
        String className = "NatsPublisherService" + sanitizedSubject;
        String interfaceName = "INatsPublisherService" + sanitizedSubject;
       // String dtoPackage = "com.racemus.eurocontrol.idltojava.generated.dto";
        String dtoPackage = PackageResolver.resolve(subjectAndDto.getDto());
        ClassName dtoClass = ClassName.get(dtoPackage, subjectAndDto.getDto());

        TypeSpec publisherInterface = buildPublisherInterface(interfaceName, dtoClass);
        TypeSpec serviceClass = buildServiceClass(className, interfaceName, subjectAndDto.getSubject(), dtoClass);

        writeJavaFile(publisherInterface, interfaceName);
        writeJavaFile(serviceClass, className);
    }

    private static TypeSpec buildPublisherInterface(String interfaceName, ClassName dtoClass) {
        return TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("publishEvent")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(dtoClass, "dto")
                        .build())
                .build();
    }

    private static TypeSpec buildServiceClass(String className, String interfaceName, String subject, ClassName dtoClass) {
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(PACKAGE_NAME, interfaceName))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j")).build())
                .addJavadoc("""
                        ****************************************************************************
                        * Copyright (c) 2024 Eurocontrol.                                          *
                        * All Rights Reserved                                                       *
                        * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Eurocontrol               *
                        * The copyright notice above does not evidence any actual or               *
                        * intended publication of such source code.                                *
                        ****************************************************************************
                                                    
                        Automatically generated NATS publisher service for subject $L.
                        """, subject)
                .addField(Connection.class, "natsConnection", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(buildConstructor())
                .addMethods(buildPublishMethods(dtoClass, subject))
                .build();
    }

    private static MethodSpec buildConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Connection.class, "natsConnection")
                .addStatement("this.natsConnection = natsConnection")
                .build();
    }

    private static Iterable<MethodSpec> buildPublishMethods(ClassName dtoClass, String subject) {
        MethodSpec publishEventInstanceMethod = MethodSpec.methodBuilder("publishEvent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dtoClass, "dto")
                .addStatement("publishEvent(this.natsConnection, $S, dto)", subject)
                .build();

        MethodSpec publishEventStaticMethod = MethodSpec.methodBuilder("publishEvent")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(Connection.class, "natsConnection")
                .addParameter(String.class, "subject")
                .addParameter(dtoClass, "dto")
                .beginControlFlow("try")
                .addStatement("$T data = dto.toBinary()", byte[].class)
                .addStatement("natsConnection.publish(subject, data)")
                .nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("log.warn(\"Error while publishing DTO '%s' on subject '%s'\", dto, subject, e)")
                .endControlFlow()
                .build();

        return List.of(publishEventInstanceMethod, publishEventStaticMethod);
    }

    private static void writeJavaFile(TypeSpec typeSpec, String fileName) {
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
        try {
            javaFile.writeTo(Paths.get("./src/main/java/"));
            log.info("Successfully wrote the generated {} class/interface", fileName);
        } catch (IOException e) {
            log.error("Failed to write the generated class/interface {}: {}", fileName, e);
        }
    }
}
