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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.Collector;
import org.camunda.bpm.engine.impl.tree.TreeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;




/**
 * Denotes an 'activity' in the sense of BPMN 2.0:
 * a parent class for all tasks, subprocess and callActivity.
 *
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AbstractBpmnActivityBehavior extends FlowNodeActivityBehavior {

  private static final Logger LOG = Logger.getLogger(AbstractBpmnActivityBehavior.class.getName());

  /**
   * Subclasses that call leave() will first pass through this method, before
   * the regular {@link FlowNodeActivityBehavior#leave(ActivityExecution)} is
   * called. This way, we can check if the activity has loop characteristics,
   * and delegate to the behavior if this is the case.
   */
  protected void leave(ActivityExecution execution) {
    PvmActivity currentActivity = execution.getActivity();
    ActivityImpl compensationHandler = getCompensationHandler(currentActivity);
    if(compensationHandler != null) {
      createCompensateEventSubscription(execution, compensationHandler);
    }
    super.leave(execution);
  }

  protected ActivityImpl getCompensationHandler(PvmActivity activity) {
    String compensationHandlerId = (String) activity.getProperty(BpmnParse.PROPERTYNAME_COMPENSATION_HANDLER_ID);
    if(compensationHandlerId != null) {
      return (ActivityImpl) activity.getProcessDefinition().findActivity(compensationHandlerId);
    }
    else {
      return null;
    }
  }

  protected void createCompensateEventSubscription(ActivityExecution execution, ActivityImpl compensationHandler) {

    ScopeImpl compensationHandlerScope = compensationHandler.getScope();
    PvmExecutionImpl compensateEventScopeExecution = execution.findExecutionForScope(compensationHandlerScope);

    CompensateEventSubscriptionEntity compensateEventSubscriptionEntity = CompensateEventSubscriptionEntity.createAndInsert((ExecutionEntity) compensateEventScopeExecution);
    compensateEventSubscriptionEntity.setActivity(compensationHandler);
  }

  protected void propagateExceptionAsError(Exception exception, ActivityExecution execution) throws Exception {
    if (exception instanceof ProcessEngineException && exception.getCause() == null) {
      throw exception;
    } else {
      propagateError(null, exception, execution);
    }
  }

  protected void propagateBpmnError(BpmnError error, ActivityExecution execution) throws Exception {
    propagateError(error.getErrorCode(), null, execution);
  }

  protected void propagateError(String errorCode, Exception origException, ActivityExecution execution) throws Exception {

    // get the scope activity or process defintion for the current exection
    ScopeImpl scope = getCurrentScope(execution);

    // make sure we start with the scope execution for the current scope
    execution = execution.isScope() ? execution : execution.getParent();

    // walk the tree of parent scope executions and activities and search a scope in both trees which catches the error
    ExecutionScopeHierarchyWalker scopeHierarchyWalker = new ExecutionScopeHierarchyWalker((PvmExecutionImpl) execution, true);
    ErrorDeclarationFinder errorDeclarationFinder = new ErrorDeclarationFinder(scope, errorCode, origException);
    scopeHierarchyWalker.addPreCollector(errorDeclarationFinder);
    PvmExecutionImpl errorHandlingExecution = scopeHierarchyWalker.walkWhile(errorDeclarationFinder);

    ActivityImpl errorHandlingActivity = errorDeclarationFinder.getErrorHandlerActivity();
    if (errorHandlingExecution == null) {
      if (origException == null) {
        LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
            + errorCode + "', but no catching boundary event was defined. "
            +   "Execution will simply be ended (none end event semantics).");
        execution.end(true);
      } else {
        // throw original exception
        throw origException;
      }
    }
    else {
      errorHandlingExecution.executeActivity(errorHandlingActivity);
    }
  }

  /**
   * Assumption: execution is executing a transition or an activity.
   *
   * @return the scope for the transition or activity the execution is currently executing
   */
  protected ScopeImpl getCurrentScope(ActivityExecution execution) {
    ScopeImpl scope = null;
    if(execution.getTransition() != null) {
      // error may be thrown from a sequence flow listener
      scope = execution.getTransition().getDestination().getParent();
    }
    else {
      scope = (ScopeImpl) execution.getActivity();
    }
    // the current scope may not be a scope
    while (!scope.isScope()) {
      scope = scope.getParent();
    }
    return scope;
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if("compensationDone".equals(signalName)) {
      signalCompensationDone(execution, signalData);
    } else {
      super.signal(execution, signalName, signalData);
    }
  }

  protected void signalCompensationDone(ActivityExecution execution, Object signalData) {
    // default behavior is to join compensating executions and propagate the signal if all executions
    // have compensated

    // join compensating executions
    if(execution.getExecutions().isEmpty()) {
      if(execution.getParent() != null) {
        ActivityExecution parent = execution.getParent();
        execution.remove();
        parent.signal("compensationDone", signalData);
      }
    } else {
      ((ExecutionEntity)execution).forceUpdate();
    }

  }

  /**
   * Walks the execution tree hierarchy skipping all non-scope executions.
   */
  public static class ExecutionScopeHierarchyWalker extends TreeWalker<PvmExecutionImpl> {

    protected boolean considerSuperProcessInstances;

    public ExecutionScopeHierarchyWalker(PvmExecutionImpl initialElement, boolean considerSuperProcessInstances) {
      super(initialElement);
      this.considerSuperProcessInstances = considerSuperProcessInstances;
    }

    protected PvmExecutionImpl nextElement() {
      return currentElement.getParentScopeExecution(considerSuperProcessInstances);
    }

  }

  public static class ErrorDeclarationFinder implements Collector<PvmExecutionImpl>, WalkCondition<PvmExecutionImpl> {

    protected ScopeImpl currentScope;
    protected String errorCode;
    protected Exception exception;
    protected ActivityImpl errorHandlerActivity;

    public ErrorDeclarationFinder(ScopeImpl currentScope, String errorCode, Exception exception) {
      this.currentScope = currentScope;
      this.errorCode = errorCode;
      this.exception = exception;
    }

    public boolean isFulfilled(PvmExecutionImpl element) {
      if (currentScope == null) {
        return true;
      }
      else {
        List<ErrorEventDefinition> errorEventDefinitions = (List) currentScope.getProperty(BpmnParse.PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
        if(errorEventDefinitions != null) {
          for (ErrorEventDefinition errorEventDefinition : errorEventDefinitions) {
            if ((exception != null && errorEventDefinition.catchesException(exception)) ||
                (exception == null && errorEventDefinition.catchesError(errorCode))) {
              errorHandlerActivity = currentScope.getProcessDefinition().findActivity(errorEventDefinition.getHandlerActivityId());
              return true;
            }
          }
        }
        return false;
      }

    }

    public void collect(PvmExecutionImpl obj) {
      currentScope = currentScope.getParent();

      // if process definition was already reached, go one process definition up
      if (currentScope == null) {
        PvmExecutionImpl superExecution = obj.getSuperExecution();

        if (superExecution != null) {
          currentScope = superExecution.getActivity();
        }
      }

      if (currentScope != null) {
        while (!currentScope.isScope()) {
          currentScope = currentScope.getParent();
        }
      }
    }

    public ActivityImpl getErrorHandlerActivity() {
      return errorHandlerActivity;
    }

  }

}
