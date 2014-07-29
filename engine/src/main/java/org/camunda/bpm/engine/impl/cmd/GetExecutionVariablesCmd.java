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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 */
public class GetExecutionVariablesCmd extends AbstractGetVariablesCmd<Map<String, Object>>
  implements Serializable {

  private static final long serialVersionUID = 1L;

  public GetExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    super(executionId, variableNames, null, isLocal);
  }

  protected VariableScope getVariableScope(CommandContext commandContext) {
    ensureNotNull("executionId", variableScopeId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(variableScopeId);

    ensureNotNull("execution " + variableScopeId + " doesn't exist", "execution", execution);

    return execution;
  }

  protected Map<String, Object> getVariables(CommandContext commandContext, VariableScope scope) {
    return getVariablesAsMap(scope);
  }
}
