package com.racemus.eurocontrol.idltojava.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PublishAndConsume {
    private List<SubjectAndDto> consumes;
    private List<SubjectAndDto> publishes;
}
