# Project Library Generation Guide

## Overview

This guide provides detailed instructions on how to build and generate the library for the project. The process involves using Maven, a project management tool, to compile and install the library into your local Maven repository. Please ensure Maven is installed on your system before proceeding with the build process.

## Prerequisites

- Java JDK 17 or higher installed on your system.
- Apache Maven 3.6.0 or higher installed and configured.
- Access to IDL (Interface Definition Language) and CDL (Component Definition Language) files necessary for the library generation.

## Building the Project

To build the project and generate the necessary library files, follow these steps:

1. Open your terminal or command prompt.
2. Navigate to the root directory of the project where the `pom.xml` file is located.
3. Execute the following Maven command:

   ```bash
   mvn clean install -DidlFolderPath=/PATH/TO/IDL/FILES -DcdlFolderPath=/PATH/TO/CDL/FILES

## Library Package
Upon successful build, the library classes will be available under the following Java package:
com.racemus.eurocontrol.idltojava.generated