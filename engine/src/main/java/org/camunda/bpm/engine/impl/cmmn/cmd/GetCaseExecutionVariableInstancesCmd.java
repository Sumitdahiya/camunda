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
package org.camunda.bpm.engine.impl.cmmn.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmd.AbstractGetVariablesCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.VariableInstance;

public class GetCaseExecutionVariableInstancesCmd extends AbstractGetVariablesCmd<Map<String, VariableInstance>>
  implements Serializable {

  private static final long serialVersionUID = 1L;

  public GetCaseExecutionVariableInstancesCmd(String caseExecutionId, Collection<String> variableNames, boolean isLocal) {
    super(caseExecutionId, variableNames, null, isLocal);
  }

  protected VariableScope getVariableScope(CommandContext commandContext) {
    ensureNotNull("caseExecutionId", variableScopeId);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(variableScopeId);

    ensureNotNull("case execution " + variableScopeId + " doesn't exist", "caseExecution", caseExecution);

    return caseExecution;
  }

  protected Map<String, VariableInstance> getVariables(CommandContext commandContext, VariableScope scope) {
    return getVariableInstancesAsMap(scope);
  }
}
