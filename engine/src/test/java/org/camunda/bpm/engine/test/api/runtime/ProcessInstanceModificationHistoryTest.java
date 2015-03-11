/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationHistoryTest extends PluggableProcessEngineTestCase {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_ASYNC_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGatewayAsyncTask.bpmn20.xml";
  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String SUBPROCESS_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessListeners.bpmn20.xml";
  protected static final String SUBPROCESS_BOUNDARY_EVENTS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessBoundaryEvents.bpmn20.xml";
  protected static final String ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.oneScopeTaskProcess.bpmn20.xml";
  protected static final String TRANSITION_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.transitionListeners.bpmn20.xml";

  // TODO: test historic activity instance IDs after async

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBeforeWithVariablesInHistory() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .setVariable("procInstVar", "procInstValue")
      .setVariableLocal("localVar", "localValue")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());

    HistoricVariableInstance procInstVariable = historyService.createHistoricVariableInstanceQuery()
      .variableName("procInstVar")
      .singleResult();

    assertNotNull(procInstVariable);
    assertEquals(updatedTree.getId(), procInstVariable.getActivityInstanceId());
    assertEquals("procInstVar", procInstVariable.getName());
    assertEquals("procInstValue", procInstVariable.getValue());

    HistoricDetail procInstanceVarDetail = historyService.createHistoricDetailQuery()
        .variableInstanceId(procInstVariable.getId()).singleResult();
    assertNotNull(procInstanceVarDetail);
    // current limitation: the variables do not appear in the history as if they
    // were set from within the activity to be started
    assertEquals(updatedTree.getId(), procInstanceVarDetail.getActivityInstanceId());

    HistoricVariableInstance localVariable = historyService.createHistoricVariableInstanceQuery()
      .variableName("localVar")
      .singleResult();

    assertNotNull(localVariable);
    assertEquals(updatedTree.getId(), localVariable.getActivityInstanceId());
    assertEquals("localVar", localVariable.getName());
    assertEquals("localValue", localVariable.getValue());

    HistoricDetail localInstanceVarDetail = historyService.createHistoricDetailQuery()
        .variableInstanceId(localVariable.getId()).singleResult();
    assertNotNull(localInstanceVarDetail);
    assertEquals(updatedTree.getId(), localInstanceVarDetail.getActivityInstanceId());

    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransitionWithVariablesInHistory() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startTransition("flow2")
      .setVariable("procInstVar", "procInstValue")
      .setVariableLocal("localVar", "localValue")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());

    HistoricVariableInstance procInstVariable = historyService.createHistoricVariableInstanceQuery()
      .variableName("procInstVar")
      .singleResult();

    assertNotNull(procInstVariable);
    assertEquals(updatedTree.getId(), procInstVariable.getActivityInstanceId());
    assertEquals("procInstVar", procInstVariable.getName());
    assertEquals("procInstValue", procInstVariable.getValue());

    HistoricDetail procInstanceVarDetail = historyService.createHistoricDetailQuery()
        .variableInstanceId(procInstVariable.getId()).singleResult();
    assertNotNull(procInstanceVarDetail);
    assertEquals(updatedTree.getId(), procInstVariable.getActivityInstanceId());

    HistoricVariableInstance localVariable = historyService.createHistoricVariableInstanceQuery()
      .variableName("localVar")
      .singleResult();

    assertNotNull(localVariable);
    assertEquals(updatedTree.getId(), procInstVariable.getActivityInstanceId());
    assertEquals("localVar", localVariable.getName());
    assertEquals("localValue", localVariable.getValue());

    HistoricDetail localInstanceVarDetail = historyService.createHistoricDetailQuery()
        .variableInstanceId(localVariable.getId()).singleResult();
    assertNotNull(localInstanceVarDetail);
    assertEquals(updatedTree.getId(), localInstanceVarDetail.getActivityInstanceId());

    completeTasksInOrder("task1", "task1");
    assertProcessEnded(processInstance.getId());

  }

  protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    if (activityId.equals(activityInstance.getActivityId())) {
      return activityInstance;
    }

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
      if (instance != null) {
        return instance;
      }
    }

    return null;
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }
}
