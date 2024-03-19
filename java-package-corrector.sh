#!/bin/bash
TARGET_DIR="src/main/java/com/racemus/eurocontrol/idltojava/generated/dto"
ROOT_PACKAGE="com.racemus.eurocontrol.idltojava.generated.dto"

find "$TARGET_DIR" -type f -name '*.java' | while IFS= read -r file; do
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|^package \(.*\);|package ${ROOT_PACKAGE}.\1;|" "$file"
    else
        sed -i "s|^package \(.*\);|package ${ROOT_PACKAGE}.\1;|" "$file"
    fi
done
