package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;

interface RegistrationListener {

    default void onBeforeTestRunStart(TestRunStartDescriptor startDescriptor) {
    }

    default void onAfterTestRunStart(TestRunStartDescriptor startDescriptor) {
    }

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
        } else if (status == Status.BLOCKED) {
            this.onBeforeTestBlock(finishDescriptor);
        }
    }

    default void onBeforeTestPass(TestFinishDescriptor finishDescriptor) {
    }

    default void onBeforeTestFail(TestFinishDescriptor finishDescriptor) {
    }

    default void onBeforeTestSkip(TestFinishDescriptor finishDescriptor) {
    }

    default void onBeforeTestBlock(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestFinish(TestFinishDescriptor finishDescriptor) {
        Status status = finishDescriptor.getStatus();
        if (status == Status.PASSED) {
            this.onAfterTestPass(finishDescriptor);
        } else if (status == Status.FAILED) {
            this.onAfterTestFail(finishDescriptor);
        } else if (status == Status.SKIPPED) {
            this.onAfterTestSkip(finishDescriptor);
        } else if (status == Status.BLOCKED) {
            this.onAfterTestBlock(finishDescriptor);
        }
    }

    default void onAfterTestPass(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestFail(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestSkip(TestFinishDescriptor finishDescriptor) {
    }

    default void onAfterTestBlock(TestFinishDescriptor finishDescriptor) {
    }

}
