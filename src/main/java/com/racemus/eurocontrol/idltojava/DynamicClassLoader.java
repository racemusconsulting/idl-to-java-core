package com.racemus.eurocontrol.idltojava;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class DynamicClassLoader extends ClassLoader {
    public static void main(String[] args) {
        String packageName = "com.example.myapp";
        Set<Class<?>> classes = discoverClassesInPackage(packageName);

        // Example operation: print discovered class names
        for (Class<?> cls : classes) {
            System.out.println(cls.getName());
        }

        // Further operations on discovered classes (instantiation, method invocation, etc.) can be performed here
    }

    public static Set<Class<?>> discoverClassesInPackage(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new SubTypesScanner()));

        return reflections.getSubTypesOf(Object.class);
    }
}
