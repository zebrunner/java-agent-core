package com.zebrunner.agent.core.registrar.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.zebrunner.agent.core.registrar.client.response.StartTestResponse;
import com.zebrunner.agent.core.registrar.descriptor.TestFinish;
import com.zebrunner.agent.core.registrar.descriptor.TestStart;

@Getter
@Accessors(chain = true)
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Test {

    private Long id;

    private String name;
    private String correlationData;

    private Class<?> testClass;
    private Method testMethod;

    private String className;
    private String methodName;
    private Integer argumentsIndex;
    private String maintainer;

    private Instant startedAt;
    private Instant endedAt;

    private Status status;
    private String reason;

    private List<Label> labels;
    private List<ArtifactReference> artifactReferences;
    private Set<String> testGroups;

    public static Test of(StartTestResponse startResponse, TestStart testStart) {
        return new Test().setId(startResponse.getId())

                         .setName(startResponse.getName())
                         .setCorrelationData(startResponse.getCorrelationData())

                         .setTestClass(testStart.getTestClass())
                         .setTestMethod(testStart.getTestMethod())

                         .setClassName(startResponse.getClassName())
                         .setMethodName(startResponse.getMethodName())
                         .setArgumentsIndex(startResponse.getArgumentsIndex())
                         .setMaintainer(startResponse.getMaintainer())

                         .setStartedAt(startResponse.getStartedAt())
                         .setEndedAt(startResponse.getEndedAt())

                         .setLabels(startResponse.getLabels())
                         .setArtifactReferences(startResponse.getArtifactReferences())
                         .setTestGroups(startResponse.getTestGroups());
    }

    public void ended(TestFinish testFinish) {
        this.endedAt = testFinish.getEndedAt();
        this.status = testFinish.getStatus();
        this.reason = testFinish.getStatusReason();
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
