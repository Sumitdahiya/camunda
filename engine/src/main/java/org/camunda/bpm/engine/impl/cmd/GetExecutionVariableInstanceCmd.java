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

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;

public class GetExecutionVariableInstanceCmd extends AbstractGetVariablesCmd<VariableInstance> implements Serializable {

  private static final long serialVersionUID = 1L;

  public GetExecutionVariableInstanceCmd(String variableScopeId, String singleVariableName, boolean isLocal) {
    super(variableScopeId, null, singleVariableName, isLocal);
  }

  protected VariableScope getVariableScope(CommandContext commandContext) {
    ensureNotNull("executionId", variableScopeId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(variableScopeId);

    ensureNotNull("execution " + variableScopeId + " doesn't exist", "execution", execution);

    return execution;
  }

  protected VariableInstance getVariables(CommandContext commandContext, VariableScope scope) {
    return getSingleVariableInstance(scope);
  }

}
