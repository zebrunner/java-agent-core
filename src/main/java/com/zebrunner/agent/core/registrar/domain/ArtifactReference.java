package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactReference {

    private String name;
    private String value;

}
