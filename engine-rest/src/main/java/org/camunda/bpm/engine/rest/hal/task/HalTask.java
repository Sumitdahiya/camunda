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
package org.camunda.bpm.engine.rest.hal.task;

import java.util.Date;

import org.camunda.bpm.engine.rest.CaseDefinitionRestService;
import org.camunda.bpm.engine.rest.CaseExecutionRestService;
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Daniel Meyer
 *
 */
public class HalTask extends HalResource<HalTask> {

  public static HalRelation REL_SELF = HalRelation.build("self", TaskRestService.class);
  public static HalRelation REL_ASSIGNEE = HalRelation.build("assignee", UserRestService.class);
  public static HalRelation REL_OWNER = HalRelation.build("owner", UserRestService.class);
  public static HalRelation REL_EXECUTION = HalRelation.build("execution", ExecutionRestService.class);
  public static HalRelation REL_PARENT_TASK = HalRelation.build("parentTask", TaskRestService.class);
  public static HalRelation REL_PROCESS_DEFINITION = HalRelation.build("processDefinition", ProcessDefinitionRestService.class);
  public static HalRelation REL_PROCESS_INSTANCE = HalRelation.build("processInstance", ProcessInstanceRestService.class);
  public static HalRelation REL_CASE_INSTANCE = HalRelation.build("caseInstance", CaseInstanceRestService.class);
  public static HalRelation REL_CASE_EXECUTION = HalRelation.build("caseExecution", CaseExecutionRestService.class);
  public static HalRelation REL_CASE_DEFINITION = HalRelation.build("caseDefinition", CaseDefinitionRestService.class);

  private String name;
  private String description;
  private String taskDefinitionKey;

  private Date created;
  private Date due;
  private Date followUp;

  private DelegationState delegationState;
  private int priority;

  public static HalTask fromTask(Task task) {
    HalTask halTask = new HalTask();

    // task state

    halTask.name = task.getName();
    halTask.description = task.getDescription();
    halTask.taskDefinitionKey = task.getTaskDefinitionKey();

    halTask.created = task.getCreateTime();
    halTask.due = task.getDueDate();
    halTask.followUp = task.getFollowUpDate();

    halTask.delegationState = task.getDelegationState();
    halTask.priority = task.getPriority();

    // links
    halTask.linker.createLink(REL_SELF, task.getId());
    halTask.linker.createLink(REL_ASSIGNEE, task.getAssignee());
    halTask.linker.createLink(REL_OWNER, task.getOwner());
    halTask.linker.createLink(REL_EXECUTION,task.getExecutionId());
    halTask.linker.createLink(REL_PARENT_TASK, task.getParentTaskId());
    halTask.linker.createLink(REL_PROCESS_DEFINITION, task.getProcessDefinitionId());
    halTask.linker.createLink(REL_PROCESS_INSTANCE, task.getProcessInstanceId());
    halTask.linker.createLink(REL_CASE_INSTANCE, task.getCaseInstanceId());
    halTask.linker.createLink(REL_CASE_EXECUTION, task.getCaseExecutionId());
    halTask.linker.createLink(REL_CASE_DEFINITION, task.getCaseDefinitionId());

    return halTask;
  }

  // getters //////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public Date getCreated() {
    return created;
  }

  public Date getDue() {
    return due;
  }

  public Date getFollowUp() {
    return followUp;
  }

  public DelegationState getDelegationState() {
    return delegationState;
  }

  public int getPriority() {
    return priority;
  }

}
