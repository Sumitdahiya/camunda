package org.camunda.bpm.qa.upgrade.scenarios.eventsubprocess;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class InterruptingEventSubprocessScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("InterruptingEventSubprocessScenario.complete")
  public void testCompletion() {
    // given
    Task task = rule.taskQuery().singleResult();

    // when
    rule.getTaskService().complete(task.getId());

    // then
    Assert.assertTrue(rule.scenarioEnded());
  }

  @Test
  @ScenarioUnderTest("InterruptingEventSubprocessScenario.activityInstanceTree")
  public void testActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    // TODO: assert the tree
    Assert.assertNotNull(activityInstance);
  }

  @Test
  @ScenarioUnderTest("InterruptingEventSubprocessScenario.delete")
  public void testDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    Assert.assertTrue(rule.scenarioEnded());
  }

}
