package com.zebrunner.agent.core.registrar.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateLaunchTcmConfigRequest {

    private Boolean testRailSyncEnabled;
    private Boolean testRailSyncRealTime;
    private Long testRailRunId;
    private Long testRailSuiteId;
    private String testRailRunName;
    private String testRailAssignee;
    private String testRailMilestoneName;
    private Boolean testRailIncludeAllCases;

    private Boolean zephyrSyncEnabled;
    private Boolean zephyrSyncRealTime;
    private String zephyrTestCycleKey;
    private String zephyrJiraProjectKey;

    private Boolean xraySyncEnabled;
    private Boolean xraySyncRealTime;
    private String xrayTestExecutionKey;

    private Boolean zebrunnerSyncEnabled;
    private Boolean zebrunnerSyncRealTime;
    private Long zebrunnerTestRunId;

    private String statusOnPass;
    private String statusOnFail;
    private String statusOnKnownIssue;
    private String statusOnSkip;
    private String statusOnBlock;

}
