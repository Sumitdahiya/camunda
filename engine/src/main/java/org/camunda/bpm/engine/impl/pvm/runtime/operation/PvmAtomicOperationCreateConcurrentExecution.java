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

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * <p>Base atomic operation used for implementing atomic operations which
 * create a new concurrent execution for executing an activity. This atomic
 * operation makes sure the execution is created under the correct parent.</p>
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class PvmAtomicOperationCreateConcurrentExecution implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {

    // Assuption: execution is the Scope Execution for the scope in which the activity
    // should be executed concurrently.

    ActivityImpl activityToStart = execution.getNextActivity();
    execution.setNextActivity(null);

    // The following covers the three cases in which a concurrent execution may be created
    // (this execution is the root in each scenario).
    //
    // Note: this should only consider non-event-scope executions. Event-scope executions
    // are not relevant for the tree structure and should remain under their original parent.
    //
    //
    // (1) A compacted tree:
    //
    // Before:               After:
    //       -------               -------
    //       |  e1  |              |  e1 |
    //       -------               -------
    //                             /     \
    //                         -------  -------
    //                         |  e2 |  |  e3 |
    //                         -------  -------
    //
    // e2 replaces e1; e3 is the new root for the activity stack to instantiate
    //
    //
    // (2) A single child that is a scope execution
    // Before:               After:
    //       -------               -------
    //       |  e1 |               |  e1 |
    //       -------               -------
    //          |                  /     \
    //       -------           -------  -------
    //       |  e2 |           |  e3 |  |  e4 |
    //       -------           -------  -------
    //                            |
    //                         -------
    //                         |  e2 |
    //                         -------
    //
    //
    // e3 is created and is concurrent;
    // e4 is the new root for the activity stack to instantiate
    //
    //
    // (3) A single child that is a scope execution
    // Before:               After:
    //       -------                    ---------
    //       |  e1 |                    |   e1  |
    //       -------                    ---------
    //       /     \                   /    |    \
    //  -------    -------      -------  -------  -------
    //  |  e2 | .. |  eX |      |  e2 |..|  eX |  | eX+1|
    //  -------    -------      -------  -------  -------
    //
    // eX+1 is concurrent and the new root for the activity stack to instantiate
    List<? extends PvmExecutionImpl> children = execution.getNonEventScopeExecutions();

    if (children.isEmpty()) {
      // (1)
      PvmExecutionImpl replacingExecution = execution.createExecution();
      replacingExecution.setConcurrent(true);
      replacingExecution.setScope(false);
      replacingExecution.replace(execution);
      execution.setActivity(null);

    }
    else if (children.size() == 1) {
      // (2)
      PvmExecutionImpl child = children.get(0);

      PvmExecutionImpl concurrentReplacingExecution = execution.createExecution();
      concurrentReplacingExecution.setConcurrent(true);
      concurrentReplacingExecution.setScope(false);
      child.setParent(concurrentReplacingExecution);
      ((List<PvmExecutionImpl>) concurrentReplacingExecution.getExecutions()).add(child);
      execution.getExecutions().remove(child);
    }

    // (1), (2), and (3)
    PvmExecutionImpl propagatingExecution = execution.createExecution();
    propagatingExecution.setConcurrent(true);
    propagatingExecution.setScope(false);

    // set next activity on propagating execution
    propagatingExecution.setActivity(activityToStart);
    concurrentExecutionCreated(propagatingExecution);
  }

  protected abstract void concurrentExecutionCreated(PvmExecutionImpl propagatingExecution);

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

}
