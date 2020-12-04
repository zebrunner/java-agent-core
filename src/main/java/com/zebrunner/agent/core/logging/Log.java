package com.zebrunner.agent.core.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    private String testId;
    private String message;
    private String level;
    private long timestamp;

}
