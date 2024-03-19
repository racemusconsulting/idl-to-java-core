package com.racemus.eurocontrol.idltojava;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DtoGenerator {

    private static final String IDL_COMPILER_COMMAND = "/bin/idl";
    private static final String JAVA_SOURCE_DIRECTORY = "src/main/java";
    private static final String IDL_PACKAGE_MAPPING = ":com.racemus.eurocontrol.idltojava.generated.dto";
    private static final String IDL_INCLUDE_DIRECTORY = "/opt/workspace/java/atg/idl-to-java-generator/src/main/resources/ALL_IDL";

    public static int generate(String path) throws IOException, InterruptedException {
        validateJacorbHomeEnvironment();

        File idlDir = new File(path);
        List<File> idlFiles = new ArrayList<>();
        findIDLFilesRecursively(idlDir, idlFiles);

        for (File idlFile : idlFiles) {
            executeIdlCompiler(idlFile, path);
        }

        return idlFiles.size();
    }

    private static void validateJacorbHomeEnvironment() {
        if (System.getenv("JACORB_HOME") == null) {
            throw new IllegalStateException("JACORB_HOME environment variable is not set.");
        }
    }

    private static void findIDLFilesRecursively(File directory, List<File> idlFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findIDLFilesRecursively(file, idlFiles);
                } else if (file.getName().endsWith(".idl")) {
                    idlFiles.add(file);
                }
            }
        }
    }

    private static void executeIdlCompiler(File idlFile, String classpath) throws IOException, InterruptedException {
        List<String> command = buildCompilerCommand(idlFile.getAbsolutePath(), classpath);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        logProcessOutput(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Compilation error for file: " + idlFile.getName());
        }
    }

    private static List<String> buildCompilerCommand(String filePath, String classpath) {
        List<String> command = new ArrayList<>();
        command.add(System.getenv("JACORB_HOME") + IDL_COMPILER_COMMAND);
        command.add("-d");
        command.add(JAVA_SOURCE_DIRECTORY);
        command.add("-i2jpackage");
        command.add(IDL_PACKAGE_MAPPING);
        command.add("-I" + classpath);
        command.add(filePath);
        return command;
    }

    private static void logProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
    }

    public static void consolidateIDLFiles(Path sourceDir, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".idl")) {
                    Path targetFile = targetDir.resolve(file.getFileName());
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            final String pathToIdls = args[0];
            Path targetDir = Paths.get(pathToIdls);
            consolidateIDLFiles(targetDir, targetDir);
            generate(targetDir.toString());
        } else {
            log.error("No path provided for IDL generation.");
        }
    }
}