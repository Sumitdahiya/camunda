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
package org.camunda.bpm.engine.test.api.repository;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;

public class ProcessDefinitionPriorityTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testProcessDefinitionDefaultPriority() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertEquals(50, processDefinition.getJobPriority());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testProcessDefinitionPriorityAssignment() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    repositoryService.assignPriorityById(processDefinition.getId(), 70);

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertEquals(70, processDefinition.getJobPriority());
  }

  @Deployment
  public void testJobPriorityAssignment() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.assignPriorityById(processDefinition.getId(), 70);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();

    assertEquals(70, job.getPriority());
  }
}
