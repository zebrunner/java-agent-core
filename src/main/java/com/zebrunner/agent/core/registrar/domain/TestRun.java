package com.zebrunner.agent.core.registrar.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zebrunner.agent.core.registrar.client.response.StartTestRunResponse;

@Getter
@Accessors(chain = true)
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestRun {

    private Long id;

    private String uuid;
    private String name;
    private String framework;
    private Instant startedAt;
    private Instant endedAt;

    private String environment;
    @Setter
    private String build;
    @Setter
    private String locale;

    private List<Label> labels;
    private List<ArtifactReference> artifactReferences;

    public static TestRun of(StartTestRunResponse startResponse) {
        return new TestRun().setId(startResponse.getId())

                            .setUuid(startResponse.getUuid())
                            .setName(startResponse.getName())
                            .setFramework(startResponse.getFramework())
                            .setStartedAt(startResponse.getStartedAt())

                            .setEnvironment(startResponse.getConfig().getEnvironment())
                            .setBuild(startResponse.getConfig().getBuild());
    }

    public void ended(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public void addLabels(Collection<Label> labels) {
        if (this.labels == null) {
            this.labels = new ArrayList<>(labels.size());
        }
        this.labels.addAll(labels);
    }

    public void addArtifactReference(ArtifactReference artifactReference) {
        if (this.artifactReferences == null) {
            this.artifactReferences = new ArrayList<>(1);
        }
        this.artifactReferences.add(artifactReference);
    }

}
