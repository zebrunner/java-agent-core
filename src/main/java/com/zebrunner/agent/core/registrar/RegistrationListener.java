package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;

interface RegistrationListener {

    default void onBeforeTestStart(TestStartDescriptor startDescriptor) {
    }

    default void onAfterTestStart(TestStartDescriptor startDescriptor) {
    }

    default void onBeforeTestFinish(TestFinishDescriptor finishDescriptor) {
        Status status = finishDescriptor.getStatus();
        if (status == Status.PASSED) {
            this.onBeforeTestPass(finishDescriptor);
        } else if (status == Status.FAILED) {
            this.onBeforeTestFail(finishDescriptor);
        } else if (status == Status.SKIPPED) {
            this.onBeforeTestSkip(finishDescriptor);
        }
    }

    default void onBeforeTestPass(TestFinishDescriptor finishDescriptor) {
    }

    default void onBeforeTestFail(TestFinishDescriptor finishDescriptor) {
    }

    default void onBeforeTestSkip(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestFinish(TestFinishDescriptor finishDescriptor) {
        Status status = finishDescriptor.getStatus();
        if (status == Status.PASSED) {
            this.onAfterTestPass(finishDescriptor);
        } else if (status == Status.FAILED) {
            this.onAfterTestFail(finishDescriptor);
        } else if (status == Status.SKIPPED) {
            this.onAfterTestSkip(finishDescriptor);
        }
    }

    default void onAfterTestPass(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestFail(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestSkip(TestFinishDescriptor finishDescriptor) {
    }

}
