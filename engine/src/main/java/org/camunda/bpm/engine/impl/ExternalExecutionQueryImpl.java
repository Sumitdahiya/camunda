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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalExecutionEntity;


/**
 * @author Daniel Meyer
 */
public class ExternalExecutionQueryImpl extends AbstractQuery<ExternalExecutionQueryImpl, ExternalExecutionEntity> {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String activityId;
  protected String executionId;
  protected String processInstanceId;

  public ExternalExecutionQueryImpl() {
  }

  public ExternalExecutionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ExternalExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ExternalExecutionQueryImpl processDefinitionId(String processDefinitionId) {
    ensureNotNull("Process definition id", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExternalExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("Process definition key", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ExternalExecutionQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("Process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ExternalExecutionQueryImpl executionId(String executionId) {
    ensureNotNull("Execution id", executionId);
    this.executionId = executionId;
    return this;
  }

  public ExternalExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  //ordering ////////////////////////////////////////////////////

  public ExternalExecutionQueryImpl orderByProcessInstanceId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }

  public ExternalExecutionQueryImpl orderByProcessDefinitionId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }

  public ExternalExecutionQueryImpl orderByProcessDefinitionKey() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }

  //results ////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getExternalExecutionManager()
      .findExternalExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  public List<ExternalExecutionEntity> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return (List) commandContext
      .getExternalExecutionManager()
      .findExternalExecutionsByQueryCriteria(this, page);
  }

  //getters ////////////////////////////////////////////////////

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

}
