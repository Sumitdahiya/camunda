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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.VariableInstance;

public abstract class AbstractGetVariablesCmd<T> implements Command<T> {

  protected String variableScopeId;
  protected String singleVariableName;
  protected Collection<String> variableNames;
  protected boolean isLocal;

  public AbstractGetVariablesCmd(String variableScopeId, Collection<String> variableNames,
      String singleVariableName, boolean isLocal) {
    this.variableScopeId = variableScopeId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
    this.singleVariableName = singleVariableName;
  }

  @Override
  public T execute(CommandContext commandContext) {
    VariableScope scope = getVariableScope(commandContext);
    return getVariables(commandContext, scope);
  }

  protected abstract VariableScope getVariableScope(CommandContext commandContext);

  protected abstract T getVariables(CommandContext commandContext, VariableScope scope);

  protected Map<String, Object> getVariablesAsMap(VariableScope scope) {
    Map<String, Object> variables;
    if (isLocal) {
      variables = scope.getVariablesLocal();
    } else {
      variables = scope.getVariables();
    }

    if (variableNames != null && variableNames.size() > 0) {
      // if variableNames is not empty, return only variable names mentioned in it
      Map<String, Object> tempVariables = new HashMap<String, Object>();
      for (String variableName : variableNames) {
        if (variables.containsKey(variableName)) {
          tempVariables.put(variableName, variables.get(variableName));
        }
      }
      variables = tempVariables;
    }

    return variables;
  }

  protected Object getSingleVariable(VariableScope scope) {
    ensureNotNull("variableName", singleVariableName);

    Object value;

    if (isLocal) {
      value = scope.getVariableLocal(singleVariableName);
    } else {
      value = scope.getVariable(singleVariableName);
    }

    return value;
  }

  protected Map<String, VariableInstance> getVariableInstancesAsMap(VariableScope scope) {
    Map<String, VariableInstance> instances;
    if (isLocal) {
      instances = scope.getVariableInstancesLocal();
    } else {
      instances = scope.getVariableInstances();
    }

    if (variableNames != null && variableNames.size() > 0) {
      // if variableNames is not empty, return only variable names mentioned in it
      Map<String, VariableInstance> tempInstances = new HashMap<String, VariableInstance>();
      for (String variableName : variableNames) {
        if (instances.containsKey(variableName)) {
          tempInstances.put(variableName, instances.get(variableName));
        }
      }
      instances = tempInstances;
    }

    return instances;
  }

  protected VariableInstance getSingleVariableInstance(VariableScope scope) {

    ensureNotNull("variableName", singleVariableName);

    VariableInstance instance;

    if (isLocal) {
      instance = scope.getVariableInstanceLocal(singleVariableName);
    } else {
      instance = scope.getVariableInstance(singleVariableName);
    }

    return instance;
  }

}
