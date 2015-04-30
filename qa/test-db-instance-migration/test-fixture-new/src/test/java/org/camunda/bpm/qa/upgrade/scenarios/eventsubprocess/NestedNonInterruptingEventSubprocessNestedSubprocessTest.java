package org.camunda.bpm.qa.upgrade.scenarios.eventsubprocess;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("NestedNonInterruptingEventSubprocessNestedSubprocessScenario")
public class NestedNonInterruptingEventSubprocessNestedSubprocessTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerSubProcessTask.getId());
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    Task innerSubprocessTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(innerSubprocessTask);
    rule.getTaskService().complete(innerSubprocessTask.getId());

    // and
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitCompletionCase2() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubprocessTask.getId());
    rule.getTaskService().complete(outerSubProcessTask.getId());

    // then
    Task innerSubprocessTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(innerSubprocessTask);
    rule.getTaskService().complete(innerSubprocessTask.getId());

    // and
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.3")
  public void testInitActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    // TODO: assert the tree
    Assert.assertNotNull(activityInstance);
  }

  @Test
  @ScenarioUnderTest("init.4")
  public void testInitDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.5")
  public void testInitThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubProcessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubProcessTask.getId());

    // and
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR, true);
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();
    Assert.assertNotNull(innerSubProcessTask);
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    Task afterErrorTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterErrorTask);
    Assert.assertEquals("afterErrorTask", afterErrorTask.getTaskDefinitionKey());

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.1")
  public void testInitInnerSubProcessCompletionCase1() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerSubProcessTask.getId());
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.2")
  public void testInitInnerSubProcessCompletionCase2() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(innerSubProcessTask.getId());
    rule.getTaskService().complete(outerSubProcessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.3")
  public void testInitInnerSubProcessActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    // TODO: assert the tree
    Assert.assertNotNull(activityInstance);
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.4")
  public void testInitInnerSubProcessDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.5")
  public void testInitInnerSubProcessThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR, true);
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    Task afterErrorTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterErrorTask);
    Assert.assertEquals("afterErrorTask", afterErrorTask.getTaskDefinitionKey());

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

}
