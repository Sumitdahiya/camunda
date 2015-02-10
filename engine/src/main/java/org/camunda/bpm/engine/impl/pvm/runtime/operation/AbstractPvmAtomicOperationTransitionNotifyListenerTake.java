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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractPvmAtomicOperationTransitionNotifyListenerTake extends AbstractPvmEventAtomicOperation {

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {
    ActivityImpl destination = execution.getTransition().getDestination();

    if (destination.isInterruptScope()) {
      execution.setActivity(null);
      execution.performOperation(TRANSITION_INTERRUPT_SCOPE);
    } else {
      execution.setActivity(destination);
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    }
  }

  protected CoreModelElement getScope(PvmExecutionImpl execution) {
    return execution.getTransition();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_TAKE;
  }

}
