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
package org.camunda.spin.plugin;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.spin.json.SpinJsonNode;

import static org.camunda.bpm.engine.variable.Variables.*;

/**
 * @author Daniel Meyer
 *
 */
public class JavaDelegateExample implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {


    stringValue("foo")

    jsonValue(json)

    execution.setVariable("customers ", jsonValue("{}"));
    SpinJsonNode json = (SpinJsonNode) execution.getVariable("customers");

    ${customers.xPath()}

    JsonValue jsonValue = execution.getVariableTyped("customer", false);

    jsonValue.
  }

}
