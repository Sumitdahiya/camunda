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

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmd.AbstractGetVariablesCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Roman Smirnov
 *
 */
public class GetCaseExecutionVariableCmd extends AbstractGetVariablesCmd<Object>
  implements Serializable {

  private static final long serialVersionUID = 1L;

  public GetCaseExecutionVariableCmd(String caseExecutionId, String variableName, boolean isLocal) {
    super(caseExecutionId, null, variableName, isLocal);
  }

  protected VariableScope getVariableScope(CommandContext commandContext) {
    ensureNotNull("caseExecutionId", variableScopeId);
    ensureNotNull("variableName", singleVariableName);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(variableScopeId);

    ensureNotNull("case execution " + variableScopeId + " doesn't exist", "caseExecution", caseExecution);

    return caseExecution;
  }

  protected Object getVariables(CommandContext commandContext, VariableScope scope) {
    return getSingleVariable(scope);
  }

}
