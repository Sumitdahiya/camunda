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
package org.camunda.bpm.qa.upgrade.util;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author Thorben Lindhauer
 *
 */
public class ThrowBpmnErrorDelegate implements JavaDelegate, ExecutionListener {

  public static final String ERROR_INDICATOR = "throwError";

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throwErrorIfRequested(execution);
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    throwErrorIfRequested(execution);
  }

  protected void throwErrorIfRequested(DelegateExecution execution) {
    Boolean shouldThrowError = (Boolean) execution.getVariable(ERROR_INDICATOR);

    if (Boolean.TRUE.equals(shouldThrowError)) {
      throw new BpmnError(ThrowBpmnErrorDelegate.class.getSimpleName());
    }
  }

}
