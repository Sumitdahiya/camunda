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
 *
 * @author Throben Lindhauer
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class PvmAtomicOperationCancelScope implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {

    // Assumption: execution is scope
    ActivityImpl cancellingActivity = execution.getNextActivity();
    execution.setNextActivity(null);

    // first, cancel and destroy the current scope
    execution.setActive(true);

    PvmExecutionImpl propagatingExecution = null;

    if(execution.isConcurrent()) {
      execution.cancelScope("Cancel scope activity "+cancellingActivity+" executed.");
      execution.destroy();
      execution.setActivity(cancellingActivity.getParentActivity());
      execution.leaveActivityInstance();
      execution.cancelScope("Cancel scope activity "+cancellingActivity+" executed.");
      // execution is concurrent: continue with execution which remains concurrent
      // NOTE: this can happen only if executions can be both scope AND concurrent
      propagatingExecution = execution;
    }
    else {
      execution.deleteCascade("Cancel scope activity "+cancellingActivity+" executed.");
      propagatingExecution = execution.getParent();
    }

    propagatingExecution.setActivity(cancellingActivity);
    propagatingExecution.setActive(true);
    scopeCancelled(propagatingExecution);
  }

  protected abstract void scopeCancelled(PvmExecutionImpl execution);

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

}
