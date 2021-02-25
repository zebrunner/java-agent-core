package com.zebrunner.agent.core.registrar.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTargetDTO {

    private Type type;
    private String value;

    public enum Type {
        EMAIL_RECIPIENTS,
        MS_TEAMS_CHANNELS,
        SLACK_CHANNELS
    }

}
