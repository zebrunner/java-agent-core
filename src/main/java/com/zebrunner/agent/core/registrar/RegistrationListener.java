package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.TestFinish;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStart;
import com.zebrunner.agent.core.registrar.descriptor.TestStart;
import com.zebrunner.agent.core.registrar.domain.Status;

interface RegistrationListener {

    default void onBeforeTestRunStart(TestRunStart testRunStart) {
    }

    default void onAfterTestRunStart(TestRunStart testRunStart) {
    }

    default void onBeforeTestStart(TestStart testStart) {
    }

    default void onAfterTestStart(TestStart testStart) {
    }

    default void onBeforeTestFinish(TestFinish testFinish) {
        Status status = testFinish.getStatus();
        if (status == Status.PASSED) {
            this.onBeforeTestPass(testFinish);
        } else if (status == Status.FAILED) {
            this.onBeforeTestFail(testFinish);
        } else if (status == Status.SKIPPED) {
            this.onBeforeTestSkip(testFinish);
        } else if (status == Status.BLOCKED) {
            this.onBeforeTestBlock(testFinish);
        }
    }

    default void onBeforeTestPass(TestFinish testFinish) {
    }

    default void onBeforeTestFail(TestFinish testFinish) {
    }

    default void onBeforeTestSkip(TestFinish testFinish) {
    }

    default void onBeforeTestBlock(TestFinish testFinish) {
    }

    default void onAfterTestFinish(TestFinish testFinish) {
        Status status = testFinish.getStatus();
        if (status == Status.PASSED) {
            this.onAfterTestPass(testFinish);
        } else if (status == Status.FAILED) {
            this.onAfterTestFail(testFinish);
        } else if (status == Status.SKIPPED) {
            this.onAfterTestSkip(testFinish);
        } else if (status == Status.BLOCKED) {
            this.onAfterTestBlock(testFinish);
        }
    }

    default void onAfterTestPass(TestFinish testFinish) {
    }

    default void onAfterTestFail(TestFinish testFinish) {
    }

    default void onAfterTestSkip(TestFinish testFinish) {
    }

    default void onAfterTestBlock(TestFinish testFinish) {
    }

}
