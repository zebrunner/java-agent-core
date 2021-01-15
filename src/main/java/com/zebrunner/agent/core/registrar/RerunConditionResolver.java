package com.zebrunner.agent.core.registrar;

import com.zebrunner.agent.core.registrar.descriptor.Status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RerunConditionResolver {

    /**
     * Resolves run by ci run id and tests scope to rerun
     * i.e.
     * 677d3bb5-52b5-414b-8790-b538815572f0 (will rerun all run tests)
     * 677d3bb5-52b5-414b-8790-b538815572f0:[1, 2, 3, 4] (will rerun tests by id)
     * 677d3bb5-52b5-414b-8790-b538815572f0:[passed, failed] (will rerun all run passed and failed tests, can contains any statuses)
     * 677d3bb5-52b5-414b-8790-b538815572f0:fallen (will rerun failed, skipped)
     * 677d3bb5-52b5-414b-8790-b538815572f0:unknown (will rerun aborted and in_progress)
     *
     * @param runPattern - pattern that indicates which tests should be reran. Run id is required in anyway (first part of rerun expression). Second part contains additional rerun info:
     *                   1. can contains array of test ids
     *                   2. can contains test status
     *                   3. can contains array of statuses
     *                   4. can contains keyword to identify scope of statuses
     * @return resolved conditions of type {@link RerunCondition}
     */
    public static RerunCondition resolve(String runPattern) {
        String[] runPatternSlices = runPattern.split(":");

        String runId;
        Set<Long> testIds = new HashSet<>();
        Set<Status> statusesToRerun = new HashSet<>();

        if (runPatternSlices.length > 2) {
            throw new RuntimeException("Incorrect run pattern. Symbol `:` should appears one maximum 1 time");
        }

        runId = runPatternSlices[0].trim();
        if (runId.isEmpty()) {
            throw new RuntimeException("Test run can not be empty");
        }

        if (runPatternSlices.length == 2) {
            String additionalRerunInfo = runPatternSlices[1].trim();
            boolean array = additionalRerunInfo.startsWith("[") && additionalRerunInfo.endsWith("]");

            if (array) {
                String[] arraySlices = additionalRerunInfo.substring(1, additionalRerunInfo.length() - 1).split(",");
                testIds = collectTestIds(arraySlices);

                if (testIds.isEmpty()) {
                    statusesToRerun = collectTestStatuses(arraySlices);
                }
            } else {
                statusesToRerun = collectStatusesByKeyword(additionalRerunInfo);
            }
        }
        return new RerunCondition(runId, testIds, statusesToRerun);
    }

    private static Set<Long> collectTestIds(String[] arraySlices) {
        Set<Long> testIds = Arrays.stream(arraySlices)
                                  .map(String::trim)
                                  .filter(arraySlice -> arraySlice.matches("\\d+"))
                                  .map(Long::valueOf)
                                  .collect(Collectors.toSet());

        if (!testIds.isEmpty() && testIds.size() != arraySlices.length) {
            throw new RuntimeException("Rerun additional info has invalid symbols in test ids scope");
        }
        return testIds;
    }

    private static Set<Status> collectTestStatuses(String[] arraySlices) {
        Set<String> statusValues = Arrays.stream(Status.values())
                                         .map(Enum::name)
                                         .collect(Collectors.toSet());
        Set<Status> statusesToRerun = Arrays.stream(arraySlices)
                                            .map(arraySlice -> arraySlice.trim().toUpperCase())
                                            .filter(statusValues::contains)
                                            .map(arraySlice -> Status.valueOf(arraySlice.toUpperCase()))
                                            .collect(Collectors.toSet());
        if (!statusesToRerun.isEmpty() && statusesToRerun.size() != arraySlices.length) {
            throw new RuntimeException("Rerun additional info has invalid symbols in test statuses scope");
        }
        return statusesToRerun;
    }

    private static Set<Status> collectStatusesByKeyword(String keyword) {
        Set<Status> statusesToRerun = new HashSet<>();
        switch (keyword.toLowerCase()) {
            case "fallen":
                statusesToRerun.add(Status.FAILED);
                statusesToRerun.add(Status.SKIPPED);
                break;
            case "unknown":
                statusesToRerun.add(Status.ABORTED);
                statusesToRerun.add(Status.IN_PROGRESS);
                break;
            default:
                throw new RuntimeException("Test status scopes should be `fallen` or `unknown`");
        }
        return statusesToRerun;
    }

}
