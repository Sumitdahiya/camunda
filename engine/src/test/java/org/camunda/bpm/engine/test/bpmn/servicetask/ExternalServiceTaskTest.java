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
package org.camunda.bpm.engine.test.bpmn.servicetask;

import org.camunda.bpm.engine.impl.ExternalExecutionQueryImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 *
 */
public class ExternalServiceTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSingleServiceTask() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExternalExecutionEntity externalExecution = externalExecutionQuery().singleResult();
    assertNotNull(externalExecution);

  }

  private ExternalExecutionQueryImpl externalExecutionQuery() {
    return new ExternalExecutionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }

}
