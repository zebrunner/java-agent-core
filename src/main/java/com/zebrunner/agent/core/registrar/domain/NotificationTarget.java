package com.zebrunner.agent.core.registrar.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTarget {

    Type type;
    String value;

    public enum Type {

        EMAIL_RECIPIENTS,
        MS_TEAMS_CHANNELS,
        SLACK_CHANNELS

    }

    public static NotificationTarget email(String emails) {
        return new NotificationTarget(Type.EMAIL_RECIPIENTS, emails);
    }

    public static NotificationTarget teams(String channels) {
        return new NotificationTarget(Type.MS_TEAMS_CHANNELS, channels);
    }

    public static NotificationTarget slack(String channels) {
        return new NotificationTarget(Type.SLACK_CHANNELS, channels);
    }

}
