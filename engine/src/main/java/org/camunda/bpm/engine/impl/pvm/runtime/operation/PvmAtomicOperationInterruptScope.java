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

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 *
 */
public abstract class PvmAtomicOperationInterruptScope implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {

    ActivityImpl interruptingActivity = getInterruptingActivity(execution);

    // Either the execution is the scope execution or the execution is a concurrent non-scope execution.
    // The latter can happen with terminate end events.
    execution = execution.isScope() ? execution : execution.getParent();

    execution.interruptScope("Interrupting activity "+interruptingActivity+" executed.");

    execution.setActivity(interruptingActivity);
    scopeInterrupted(execution);
  }

  protected abstract void scopeInterrupted(PvmExecutionImpl execution);

  protected abstract ActivityImpl getInterruptingActivity(PvmExecutionImpl execution);

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

}
