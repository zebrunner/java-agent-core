package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.client.domain.ArtifactReferenceDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtifactReference {

    private static final ThreadLocal<List<ArtifactReferenceDTO>> ARTIFACT_REFERENCES
            = InheritableThreadLocal.withInitial(ArrayList::new);

    static List<ArtifactReferenceDTO> popAll() {
        List<ArtifactReferenceDTO> artifactReferenceDtos = ARTIFACT_REFERENCES.get();
        ARTIFACT_REFERENCES.remove();
        return artifactReferenceDtos;
    }

    public static void attach(String name, String reference) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference name is not provided.");
        }
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Artifact reference is not provided.");
        }

        ARTIFACT_REFERENCES.get()
                           .add(new ArtifactReferenceDTO(name, reference));
    }

}
