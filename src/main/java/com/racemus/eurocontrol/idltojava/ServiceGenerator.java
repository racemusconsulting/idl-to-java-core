package com.racemus.eurocontrol.idltojava;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
    public class ServiceGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        final CDLParser cdlParser = new CDLParser();
        Path targetDir = Paths.get(args[0]);
        Files.createDirectories(targetDir);
        Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".cdl")) {
                    cdlParser.parseCDL(args [0] + "/" + file.getFileName().toString())
                            .getPublishes()
                            .forEach(NatsPublisherServiceGenerator::generateServiceClass);

                    cdlParser.parseCDL( args [0] + "/" + file.getFileName().toString())
                            .getConsumes()
                            .forEach(NatsConsumerServiceGenerator::generateConsumerServiceClass);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}