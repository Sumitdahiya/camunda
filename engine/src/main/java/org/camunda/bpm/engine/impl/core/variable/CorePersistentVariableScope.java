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
package org.camunda.bpm.engine.impl.core.variable;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.PersistentVariableScope;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * @author Daniel Meyer
 *
 */
public abstract class CorePersistentVariableScope extends CoreVariableScope implements PersistentVariableScope {

  private static final long serialVersionUID = 1L;

  protected void createSerializedVariableLocal(String variableName, Object value, String datatypeName, Map<String, Object> configuration,
      VariableScope sourceActivityVariableScope) {

    if (getVariableStore().containsVariableInstance(variableName)) {
      throw new ProcessEngineException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }

    createSerializedVariableInstance(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
  }

  public void setVariableSerialized(String variableName, Object value, String datatypeName, Map<String, Object> configuration) {
    setSerializedVariable(variableName, value, datatypeName, configuration, getSourceActivityVariableScope());
  }

  protected void setSerializedVariable(String variableName, Object value, String datatypeName, Map<String, Object> configuration,
      VariableScope sourceActivityVariableScope) {
    if (hasVariableLocal(variableName)) {
      setVariableSerializedLocal(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
      return;
    }
    CorePersistentVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityVariableScope==null) {
        parentVariableScope.setVariableSerialized(variableName, value, datatypeName, configuration);
      } else {
        parentVariableScope.setSerializedVariable(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
      }
    } else {
      createSerializedVariableLocal(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
    }
  }

  public void setVariableSerializedLocal(String variableName, Object value, String datatypeName, Map<String, Object> configuration) {
    setVariableSerializedLocal(variableName, value, datatypeName, configuration, getSourceActivityVariableScope());
  }

  protected void setVariableSerializedLocal(String variableName, Object value, String datatypeName, Map<String, Object> configuration,
      VariableScope sourceActivityVariableScope) {
    CoreVariableInstance variableInstance = getVariableStore().getVariableInstance(variableName);
    if ((variableInstance != null) && (!variableInstance.isAbleToStoreSerialized(value, datatypeName))) {
      // it seems that the type has changed -> clear the variable instance
      getVariableStore().clearForNewValueSerialized(variableInstance, datatypeName);
    }
    if (variableInstance == null) {
      createSerializedVariableLocal(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
    } else {
      updateSerializedVariableInstance(variableInstance, value, datatypeName, configuration, sourceActivityVariableScope);
    }
  }

  protected void updateSerializedVariableInstance(CoreVariableInstance variableInstance, Object value, String datatypeName, Map<String, Object> configuration,
      VariableScope sourceActivityVariableScope) {
    // update variable instance
    getVariableStore().setVariableInstanceValueSerialized(variableInstance, value, datatypeName, configuration, sourceActivityVariableScope);
  }

  protected CoreVariableInstance createSerializedVariableInstance(String variableName, Object value, String datatypeName, Map<String, Object> configuration,
      VariableScope sourceActivityVariableScope) {
    return getVariableStore().createVariableInstanceSerialized(variableName, value, datatypeName, configuration, sourceActivityVariableScope);
  }

  public abstract CorePersistentVariableScope getParentVariableScope();

  protected abstract PersistentVariableStore getVariableStore();

}
