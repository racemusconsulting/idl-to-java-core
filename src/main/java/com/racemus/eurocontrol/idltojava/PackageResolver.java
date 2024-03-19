package com.racemus.eurocontrol.idltojava;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class PackageResolver {

    private static Map<String, String> classNameToPackageMap;

    public static String resolve(String className) {
        if (Objects.isNull(classNameToPackageMap) || classNameToPackageMap.isEmpty()) {
            init();
        }
        return Optional.ofNullable(classNameToPackageMap.get(className))
                .orElseThrow(
                () -> new RuntimeException("Class type %s used in CDL but not found in generated DTOs from IDLs".formatted(className))
        );

    }

    public static void init() {
        ClassLoader[] classLoaders = new ClassLoader[] {
                ClasspathHelper.contextClassLoader(),
                ClasspathHelper.staticClassLoader()
        };

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("com.racemus.eurocontrol.idltojava.generated.dto", classLoaders))
                .setScanners(new SubTypesScanner(false))
                .addClassLoaders(classLoaders));

        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
        System.out.println("allClasses: " + allClasses);
        classNameToPackageMap = allClasses.stream()
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        cls -> cls.getPackage().getName(),
                        (existingValue, newValue) -> newValue));
    }

    public static void main (String args []){
        final String event = resolve("Event");
        System.out.println(event);
    }
}
