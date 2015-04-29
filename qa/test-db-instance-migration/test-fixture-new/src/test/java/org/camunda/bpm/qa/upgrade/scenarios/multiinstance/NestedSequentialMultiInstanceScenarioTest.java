package org.camunda.bpm.qa.upgrade.scenarios.multiinstance;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("NestedSequentialMultiInstanceSubprocessScenario")
public class NestedSequentialMultiInstanceScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task innerMiSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when the first instance innerSubProcessTask and the other eight instances are completed
    rule.getTaskService().complete(innerMiSubProcessTask.getId());

    for (int i = 0; i < 8; i++) {
      innerMiSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();
      Assert.assertNotNull(innerMiSubProcessTask);
      rule.getTaskService().complete(innerMiSubProcessTask.getId());
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
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
  @ScenarioUnderTest("init.3")
  public void testInitDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.4")
  public void testInitThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerMiSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR, true);
    rule.getTaskService().complete(innerMiSubProcessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

}
