package com.racemus.eurocontrol.idltojava;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PostDtoGeneratorProcessor {

    public static void augmentClass(File javaClassFile) throws IOException {
        // Retrieve the class name from the file name
        String className = javaClassFile.getName().replace(".java", "");

        List<String> lines = Files.readAllLines(javaClassFile.toPath(), StandardCharsets.UTF_8);
        List<String> modifiedLines = new ArrayList<>();
        int lastBraceIndex = -1; // To hold the index of the last closing brace
        boolean importsAdded = false;

        // Find the index of the last closing brace '}'
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (lines.get(i).contains("}")) {
                lastBraceIndex = i;
                break;
            }
        }

        // Copy all lines up to the package declaration, add imports just after package declaration
        for (int i = 0; i <= lastBraceIndex - 1; i++) {
            if (lines.get(i).startsWith("public class")
                    || lines.get(i).startsWith("public final class")
                    || lines.get(i).startsWith("final public class")
                    || lines.get(i).startsWith("public abstract class")
                    || lines.get(i).startsWith("abstract public class")
                    || lines.get(i).startsWith("public interface")
            ) {
                modifiedLines.add("");
                modifiedLines.add("@Data");
                modifiedLines.add(lines.get(i));
            } else if (lines.get(i).startsWith("package") && !importsAdded) {
                // Add necessary imports after the package declaration
                modifiedLines.add(lines.get(i));
                modifiedLines.add("import lombok.Data;");
                modifiedLines.add("import com.fasterxml.jackson.databind.ObjectMapper;");
                modifiedLines.add("import com.fasterxml.jackson.core.JsonProcessingException;");
                modifiedLines.add("import java.io.*;");  // Add import for Java I/O
                importsAdded = true;
            } else {
                modifiedLines.add(lines.get(i));
            }
        }

        // Add JSON and binary utility methods before the last closing brace
        addUtilityMethods(modifiedLines, className);

        // Add the last closing brace
        if (lastBraceIndex != -1) {
            modifiedLines.add(lines.get(lastBraceIndex));
        }

        // Write the modified lines back to the file
        Files.write(javaClassFile.toPath(), modifiedLines, StandardCharsets.UTF_8);
    }

    private static void addUtilityMethods(List<String> modifiedLines, String className) {
        // JSON serialization and deserialization methods
        addJsonUtilityMethods(modifiedLines, className);

        // Binary serialization and deserialization methods
        addBinaryUtilityMethods(modifiedLines, className);

        modifiedLines.add("");
    }

    private static void addJsonUtilityMethods(List<String> modifiedLines, String className) {
        modifiedLines.add("    // Utility method for JSON serialization");
        modifiedLines.add("    public static String toJson(Object obj) {");
        modifiedLines.add("        try {");
        modifiedLines.add("            return new ObjectMapper().writeValueAsString(obj);");
        modifiedLines.add("        } catch (JsonProcessingException e) {");
        modifiedLines.add("            throw new RuntimeException(\"Unable to serialize to JSON\", e);");
        modifiedLines.add("        }");
        modifiedLines.add("    }");
        modifiedLines.add("");
    }

    private static void addBinaryUtilityMethods(List<String> modifiedLines, String className) {
        modifiedLines.add("    // Utility method for binary serialization");
        modifiedLines.add("    public byte[] toBinary() {");
        modifiedLines.add("        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();");
        modifiedLines.add("             ObjectOutputStream oos = new ObjectOutputStream(baos)) {");
        modifiedLines.add("            oos.writeObject(this);");
        modifiedLines.add("            return baos.toByteArray();");
        modifiedLines.add("        } catch (IOException e) {");
        modifiedLines.add("            throw new RuntimeException(\"Unable to serialize to binary\", e);");
        modifiedLines.add("        }");
        modifiedLines.add("    }");
        modifiedLines.add("");
        modifiedLines.add("    // Utility method for binary deserialization");
        modifiedLines.add("    public static " + className + " fromBinary(byte[] data) {");
        modifiedLines.add("        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);");
        modifiedLines.add("             ObjectInputStream ois = new ObjectInputStream(bais)) {");
        modifiedLines.add("            return (" + className + ") ois.readObject();");
        modifiedLines.add("        } catch (IOException | ClassNotFoundException e) {");
        modifiedLines.add("            throw new RuntimeException(\"Unable to deserialize from binary\", e);");
        modifiedLines.add("        }");
        modifiedLines.add("    }");
    }

    public static void main(String[] args) throws IOException {
        File dir = new File("src/main/java/com/racemus/eurocontrol/idltojava/generated/dto");
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Provided path is not a valid directory.");
            return;
        }

        processDirectory(dir);
    }

    public static void processDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file); // Recursive call to process subdirectories
                } else if (file.getName().endsWith(".java")) {
                    // Check if the file defines an interface
                    if (!isInterface(file)) {
                        augmentClass(file); // Process the .java file
                    }
                }
            }
        }
    }

    private static boolean isInterface(File file) throws IOException {
        // Read the contents of the file
        List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
        // Check if any line contains the interface declaration
        return lines.stream().anyMatch(line -> line.matches("\\s*public\\s+interface\\s+.*"));
    }

}
