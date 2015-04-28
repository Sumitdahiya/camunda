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
package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class UpgradeTestRule extends ProcessEngineRule {

  protected String scenarioName;

  public UpgradeTestRule() {
    super("process-engine-config-new.xml");
  }

  public void starting(Description description) {
    ScenarioUnderTest testScenarioAnnotation = description.getAnnotation(ScenarioUnderTest.class);
    if (testScenarioAnnotation != null) {
      scenarioName = testScenarioAnnotation.value();
    }

    if (scenarioName == null) {
      throw new RuntimeException("Could not determine scenario under test for test " + description.getDisplayName());
    }

    super.starting(description);
  }

  public TaskQuery taskQuery() {
    return taskService.createTaskQuery().processInstanceBusinessKey(scenarioName);
  }

  public ExecutionQuery executionQuery() {
    return runtimeService.createExecutionQuery().processInstanceBusinessKey(scenarioName);
  }

  public ProcessInstanceQuery processInstanceQuery() {
    return runtimeService
        .createProcessInstanceQuery()
        .processInstanceBusinessKey(scenarioName);
  }

  public ProcessInstance processInstance() {
    ProcessInstance instance = processInstanceQuery().singleResult();

    if (instance == null) {
      throw new RuntimeException("There is no process instance for scenario " + scenarioName);
    }

    return instance;
  }

  public boolean scenarioEnded() {
    return processInstanceQuery().singleResult() == null;
  }
}
