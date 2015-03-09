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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractInstantiationCmd extends AbstractProcessInstanceModificationCommand {

  protected VariableMap variables;
  protected VariableMap variablesLocal;


  public AbstractInstantiationCmd(String processInstanceId) {
    super(processInstanceId);
    this.variables = new VariableMapImpl();
    this.variablesLocal = new VariableMapImpl();

  }

  public void addVariable(String name, Object value) {
    this.variables.put(name, value);
  }

  public void addVariableLocal(String name, Object value) {
    this.variablesLocal.put(name, value);
  }

  public VariableMap getVariables() {
    return variables;
  }

  public VariableMap getVariablesLocal() {
    return variablesLocal;
  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();

    CoreModelElement elementToInstantiate = getTargetElement(processDefinition);

    EnsureUtil.ensureNotNull("element", elementToInstantiate);

    // rebuild the mapping because the execution tree changes with every iteration
    ActivityExecutionMapping mapping = new ActivityExecutionMapping(commandContext, processInstanceId);

    // before instantiating an activity, two things have to be determined:
    //
    // activityStack:
    // For the activity to instantiate, we build a stack of parent flow scopes
    // for which no executions exist yet and that have to be instantiated
    //
    // scopeExecution:
    // The execution of the first parent/ancestor flow scope that has an execution.
    // This is typically the execution under which a new sub tree has to be created

    List<PvmActivity> activitiesToInstantiate = new ArrayList<PvmActivity>();

    // builds the activity stack of flow scopes for which no executions exist yet
    ScopeImpl flowScope = getTargetFlowScope(processDefinition);

    Set<ExecutionEntity> flowScopeExecutions = mapping.getExecutions(flowScope);
    while (flowScopeExecutions.isEmpty()) {
      ActivityImpl flowScopeActivity = (ActivityImpl) flowScope;
      activitiesToInstantiate.add(flowScopeActivity);

      flowScope = flowScopeActivity.getFlowScope();
      flowScopeExecutions = mapping.getExecutions(flowScope);
    }

    if (flowScopeExecutions.size() > 1) {
      throw new ProcessEngineException("Execution is ambiguous for activity " + flowScope);
    }

    Collections.reverse(activitiesToInstantiate);
    ExecutionEntity scopeExecution = flowScopeExecutions.iterator().next();

    // We have to make a distinction between
    // - "regular" activities for which the activity stack can be instantiated and started
    //   right away
    // - interrupting or cancelling activities for which we have to ensure that
    //   the interruption and cancellation takes place before we instantiate the activity stack
    ActivityImpl topMostActivity = null;
    if (!activitiesToInstantiate.isEmpty()) {
      topMostActivity = (ActivityImpl) activitiesToInstantiate.get(0);
    }
    else if (ActivityImpl.class.isAssignableFrom(elementToInstantiate.getClass())) {
      topMostActivity = (ActivityImpl) elementToInstantiate;
    }

    boolean isCancelScope = false;
    if (topMostActivity != null && topMostActivity.isCancelScope()) {
      if (!activitiesToInstantiate.isEmpty()) {
        // this is in BPMN relevant if there is an interrupting event sub process.
        // we have to distinguish between instantiation of the start event and any other activity.
        // instantiation of the start event means interrupting behavior; instantiation
        // of any other task means no interruption.
        ActivityImpl initialActivity = (ActivityImpl) topMostActivity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);
        ActivityImpl secondTopMostActivity = null;
        if (activitiesToInstantiate.size() > 1) {
          secondTopMostActivity = (ActivityImpl) activitiesToInstantiate.get(1);
        }
        else if (ActivityImpl.class.isAssignableFrom(elementToInstantiate.getClass())) {
          secondTopMostActivity = (ActivityImpl) elementToInstantiate;
        }

        if (initialActivity == secondTopMostActivity) {
          isCancelScope = true;
        }
      }
      else {
        isCancelScope = true;
      }
    }

    if (isCancelScope) {
      ScopeImpl scopeToCancel = topMostActivity.getParentScope();
      Set<ExecutionEntity> executionsToCancel = mapping.getExecutions(scopeToCancel);

      if (!executionsToCancel.isEmpty()) {
        if (executionsToCancel.size() > 1) {
          throw new ProcessEngineException("Execution to cancel/interrupt is ambiguous for activity " + topMostActivity);
        }

        ExecutionEntity interruptedExecution = executionsToCancel.iterator().next();

        // this distinguishes between interruption (e.g. event sub process) and
        // cancellation (e.g. boundary event)
        if (scopeToCancel == topMostActivity.getFlowScope()) {
          // perform interruption
          // TODO: the delete reason is a hack
          interruptedExecution.cancelScope("Interrupting event sub process "+ topMostActivity + " fired.");
          instantiate(scopeExecution, activitiesToInstantiate);
//          interruptedExecution.executeActivities(activitiesToInstantiate, activity, null, variables, variablesLocal);
        }
        else {
          // perform cancellation
          // TODO: the delete reason is a hack
          scopeExecution.cancelScope("Cancel scope activity " + topMostActivity + " executed.");
          instantiate(scopeExecution, activitiesToInstantiate);
//          scopeExecution.executeActivities(activitiesToInstantiate, activity, null, variables, variablesLocal);

        }
      }
      else {
        // if there is nothing to cancel, the activity can simply be instantiated.
        instantiateConcurrent(scopeExecution, activitiesToInstantiate);
//        scopeExecution.executeActivitiesConcurrent(activitiesToInstantiate, activity, null, variables, variablesLocal);

      }
    }
    else {
      if (scopeExecution.getExecutions().isEmpty() && scopeExecution.getActivity() == null) {
        // reuse the scope execution
        instantiate(scopeExecution, activitiesToInstantiate);
//        scopeExecution.executeActivities(activitiesToInstantiate, activity, null, variables, variablesLocal);
      } else {
        // if the activity is not cancelling/interrupting, it can simply be instantiated as
        // a concurrent child of the scopeExecution
        instantiateConcurrent(scopeExecution, activitiesToInstantiate);
//        scopeExecution.executeActivitiesConcurrent(activitiesToInstantiate, activity, null, variables, variablesLocal);

      }

    }

    return null;
  }

  protected void instantiate(ExecutionEntity ancestorScopeExecution, List<PvmActivity> parentFlowScopes) {
    CoreModelElement targetElement = getTargetElement(ancestorScopeExecution.getProcessDefinition());

    if (PvmTransition.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivities(parentFlowScopes, null, (PvmTransition) targetElement, variables, variablesLocal);
    }
    else if (PvmActivity.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivities(parentFlowScopes, (PvmActivity) targetElement, null, variables, variablesLocal);

    }
    else {
      throw new ProcessEngineException("Cannot instantiate element " + targetElement);
    }
  }


  protected void instantiateConcurrent(ExecutionEntity ancestorScopeExecution, List<PvmActivity> parentFlowScopes) {
    CoreModelElement targetElement = getTargetElement(ancestorScopeExecution.getProcessDefinition());

    if (PvmTransition.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivitiesConcurrent(parentFlowScopes, null, (PvmTransition) targetElement, variables, variablesLocal);
    }
    else if (PvmActivity.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivitiesConcurrent(parentFlowScopes, (PvmActivity) targetElement, null, variables, variablesLocal);

    }
    else {
      throw new ProcessEngineException("Cannot instantiate element " + targetElement);
    }
  }

  protected abstract ScopeImpl getTargetFlowScope(ProcessDefinitionImpl processDefinition);

  protected abstract CoreModelElement getTargetElement(ProcessDefinitionImpl processDefinition);

}
