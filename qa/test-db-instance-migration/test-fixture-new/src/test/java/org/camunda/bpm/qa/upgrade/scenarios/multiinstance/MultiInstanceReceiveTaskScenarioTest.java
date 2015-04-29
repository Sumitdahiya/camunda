package org.camunda.bpm.qa.upgrade.scenarios.multiinstance;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("MultiInstanceReceiveTaskScenario")
public class MultiInstanceReceiveTaskScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initParallel.1")
  public void testInitParallelCompletion() {
    // when the receive task messages are correlated
    rule.messageCorrelation("Message").correlateAll();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initParallel.2")
  public void testInitParallelActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    // TODO: assert the tree
    Assert.assertNotNull(activityInstance);
  }

  @Test
  @ScenarioUnderTest("initParallel.3")
  public void testInitParallelDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initParallel.4")
  public void testInitParallelThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when a single receive task is triggered
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR, true);

    List<EventSubscription> messageEventSubscriptions = rule.getRuntimeService()
      .createEventSubscriptionQuery()
      .processInstanceId(instance.getId())
      .list();

    rule.getRuntimeService().messageEventReceived("Message", messageEventSubscriptions.get(0).getExecutionId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.1")
  public void testInitSequentialCompletion() {
    // when the receive task messages are correlated
    for (int i = 0; i < 3; i++) {
      rule.messageCorrelation("Message").correlate();
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.2")
  public void testInitSequentialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    // TODO: assert the tree
    Assert.assertNotNull(activityInstance);
  }

  @Test
  @ScenarioUnderTest("initSequential.3")
  public void testInitSequentialDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.4")
  public void testInitSequentialThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when a single receive task is triggered
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR, true);
    rule.messageCorrelation("Message").correlate();

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

}
