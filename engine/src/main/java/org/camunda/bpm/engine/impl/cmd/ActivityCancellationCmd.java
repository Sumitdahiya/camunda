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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityCancellationCmd extends AbstractProcessInstanceModificationCommand {

  protected String activityInstanceId;


  public ActivityCancellationCmd(String processInstanceId, String activityInstanceId) {
    super(processInstanceId);
    this.activityInstanceId = activityInstanceId;

  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();

    ActivityInstance activityInstanceTree = new GetActivityInstanceCmd(processInstanceId).execute(commandContext);

    ActivityInstance instance = findActivityInstance(activityInstanceTree, activityInstanceId);
    ensureNotNull("activityInstance", instance);

    String activityId = instance.getActivityId();
    ScopeImpl activity = processDefinition.findActivity(activityId);

    // rebuild the mapping because the execution tree changes with every iteration
    ActivityExecutionMapping mapping = new ActivityExecutionMapping(commandContext, processInstanceId);

    Set<ExecutionEntity> executions = mapping.getExecutions(activity);
    Set<String> activityInstanceExecutions = new HashSet<String>(Arrays.asList(instance.getExecutionIds()));

    // find the scope execution for the given activity instance
    Set<ExecutionEntity> retainedExecutionsForInstance = new HashSet<ExecutionEntity>();
    for (ExecutionEntity execution : executions) {
      if (activityInstanceExecutions.contains(execution.getId())) {
        retainedExecutionsForInstance.add(execution);
      }
    }

    if (retainedExecutionsForInstance.size() != 1) {
      throw new ProcessEngineException("There are " + retainedExecutionsForInstance.size()
          + " (!= 1) executions for activity instance " + activityInstanceId);
    }

    ExecutionEntity scopeExecution = retainedExecutionsForInstance.iterator().next();


    // Outline:
    // 1. find topmost scope execution beginning at scopeExecution that has exactly
    //    one child (this is the topmost scope we can cancel)
    // 2. cancel all children of the topmost execution
    // 3. cancel the activity of the topmost execution itself (if applicable)
    // 4. remove topmost execution (and concurrent parent) if topmostExecution is not the process instance

    ExecutionEntity topmostCancellableExecution = scopeExecution;
    ExecutionEntity parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);

    // if topmostCancellabelExecution's parent is concurrent, we have reached the target execution
    while (parentScopeExecution != null && !topmostCancellableExecution.isConcurrent() && !topmostCancellableExecution.getParent().isConcurrent()) {
      topmostCancellableExecution = parentScopeExecution;
      parentScopeExecution = getParentScopeExecution(topmostCancellableExecution);
    }

    // TODO: cancel reason

    if (topmostCancellableExecution.isProcessInstanceExecution()) {
      topmostCancellableExecution.cancelScope("Cancellation via API");
      // TODO: the following instruction should go into #cancelScope but this breaks some things like
      // transaction subprocesses
      topmostCancellableExecution.leaveActivityInstance();
      topmostCancellableExecution.setActivity(null);
    } else {
      topmostCancellableExecution.deleteCascade("Cancellation via API");
      topmostCancellableExecution.removeFromParentScope();

    }

    return null;
  }

  protected ExecutionEntity getParentScopeExecution(ExecutionEntity execution) {
    ExecutionEntity parent = execution.getParent();
    if (parent == null) {
      return null;
    }

    if (!parent.isScope()) {
      parent = parent.getParent();
    }
    return parent;
  }

  protected ActivityInstance findActivityInstance(ActivityInstance tree, String activityInstanceId) {
    if (activityInstanceId.equals(tree.getId())) {
      return tree;
    } else {
      for (ActivityInstance child : tree.getChildActivityInstances()) {
        ActivityInstance matchingChildInstance = findActivityInstance(child, activityInstanceId);
        if (matchingChildInstance != null) {
          return matchingChildInstance;
        }
      }
    }

    return null;
  }
}
