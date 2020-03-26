package com.zebrunner.agent.core.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunDTO {

    private Long id;
    private String uuid;
    private String name;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private String framework;
    // TODO: 3/23/20 add test run config
    private LaunchContextDTO launchContext;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LaunchContextDTO {

        private String jobNumber;
        private String upstreamJobNumber;
    }

}
