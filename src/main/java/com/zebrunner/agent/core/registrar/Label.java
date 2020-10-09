package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Label {

    private static final ThreadLocal<Map<String, List<String>>> LABELS
            = InheritableThreadLocal.withInitial(HashMap::new);

    static {
        CompositeLabelResolver.addResolver(($, $$) -> pop());
    }

    private static Map<String, List<String>> pop() {
        Map<String, List<String>> labels = LABELS.get();
        LABELS.remove();
        return labels;
    }

    public static void attach(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name is not provided.");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Label value is not provided.");
        }

        LABELS.get()
              .computeIfAbsent(name, $ -> new ArrayList<>())
              .add(value);
    }

    public static void attach(String name, String... values) {
        if (values == null) {
            throw new IllegalArgumentException("Label value is not provided.");
        }
        for (String value : values) {
            attach(name, value);
        }
    }

}
