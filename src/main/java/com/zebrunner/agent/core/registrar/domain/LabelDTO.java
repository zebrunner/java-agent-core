package com.zebrunner.agent.core.registrar.domain;

@Deprecated
// for backward compatibility with carina
public class LabelDTO extends Label {

    public LabelDTO(String key, String value) {
        super(key, value);
    }

}
