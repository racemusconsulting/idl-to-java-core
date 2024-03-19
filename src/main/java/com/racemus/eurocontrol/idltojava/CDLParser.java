package com.racemus.eurocontrol.idltojava;

import com.racemus.eurocontrol.idltojava.domain.PublishAndConsume;
import com.racemus.eurocontrol.idltojava.domain.SubjectAndDto;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CDLParser {

    public static final String CONSUMES_PATTERN = "(consumes)\\s+(\\S+)::(\\S+)::(\\S+)";
    public static final String PUBLISHES_PATTERN = "(publishes)\\s+(\\S+)::(\\S+)";

    public PublishAndConsume parseCDL(String pathToCdls ) {
        Path path = Paths.get(pathToCdls);
        //Path path = Paths.get("/opt/workspace/java/atg/idl-to-java-generator/src/main/resources/CDL/AceAPW.cdl");
        List<SubjectAndDto> publishes = new ArrayList<>();
        List<SubjectAndDto> consumes = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(path);
            for (String line : allLines) {
                Matcher matcher = Pattern.compile(CONSUMES_PATTERN).matcher(line.trim());
                if (matcher.find()) {
                    final SubjectAndDto result = parse(matcher);
                    consumes.add(result);
                } else {
                    matcher = Pattern.compile(PUBLISHES_PATTERN).matcher(line.trim());
                    if (matcher.find()) {
                        final SubjectAndDto result = parse(matcher);
                        publishes.add(result);
                    }
                }
            }
            return new PublishAndConsume(consumes, publishes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SubjectAndDto parse(Matcher matcher) {
        String action = matcher.group(1);
        String subjectPart1 = matcher.group(2);
        String subjectPart2 = matcher.group(3);
        String subject = "%s.%s".formatted(subjectPart1, subjectPart2);
        String dto = matcher.group(3);
        log.info(action + " " + "Subject: " + subject);
        log.info(action + " " + "DTO " + dto);
        return new SubjectAndDto(subject, dto);
    }


}

