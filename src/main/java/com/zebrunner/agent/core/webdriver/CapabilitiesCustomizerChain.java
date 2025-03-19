package com.zebrunner.agent.core.webdriver;

import org.openqa.selenium.Capabilities;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class CapabilitiesCustomizerChain {

    @Getter
    private static final CapabilitiesCustomizerChain instance = new CapabilitiesCustomizerChain();

    private final List<CapabilitiesCustomizer> customizers = new ArrayList<>(1);

    private CapabilitiesCustomizerChain() {
        this.customizers.add(new ZebrunnerCapabilitiesCustomizer());
    }

    Capabilities customize(Capabilities capabilities) {
        for (CapabilitiesCustomizer customizer : this.customizers) {
            capabilities = customizer.customize(capabilities);
        }
        return capabilities;
    }

    public synchronized void addFirst(CapabilitiesCustomizer customizer) {
        this.customizers.add(0, customizer);
    }

    public synchronized void addBefore(CapabilitiesCustomizer newCustomizer, Class<? extends CapabilitiesCustomizer> beforeCustomizer) {
        int index = this.indexOf(beforeCustomizer);

        if (index != -1) {
            this.customizers.add(index, newCustomizer);
        }
    }

    public synchronized void addAfter(CapabilitiesCustomizer newCustomizer, Class<? extends CapabilitiesCustomizer> afterCustomizer) {
        int index = this.indexOf(afterCustomizer);

        if (index != -1) {
            if (index == this.customizers.size() - 1) {
                this.addLast(newCustomizer);
            } else {
                this.customizers.add(index + 1, newCustomizer);
            }
        }
    }

    public synchronized void addLast(CapabilitiesCustomizer customizer) {
        this.customizers.add(customizer);
    }

    public synchronized void remove(CapabilitiesCustomizer customizer) {
        this.customizers.remove(customizer);
    }

    public synchronized void remove(Class<? extends CapabilitiesCustomizer> customizer) {
        int index = this.indexOf(customizer);

        if (index != -1) {
            this.customizers.remove(index);
        }
    }

    private int indexOf(Class<? extends CapabilitiesCustomizer> customizerClass) {
        for (int i = 0; i < this.customizers.size(); i++) {
            if (this.customizers.get(i).getClass() == customizerClass) {
                return i;
            }
        }
        return -1;
    }

}
