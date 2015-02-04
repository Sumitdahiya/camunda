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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;

/**
 * @author Daniel Meyer
 *
 */
public class ExternalExecutionEntity implements DbEntity, HasDbRevision {

  protected String id;

  protected int revision;

  protected String lockOwner;

  protected Date lockTime;

  protected Date created;

  protected String processDefinitionKey;

  protected String processDefinitionId;

  protected String processInstanceId;

  protected String activityId;

  protected String executionId;

  public ExternalExecutionEntity() {
    // do not remove
  }

  public ExternalExecutionEntity(ExecutionEntity ex) {
    this.processDefinitionKey = ((ProcessDefinitionEntity)ex.getProcessDefinition()).getKey();
    this.processDefinitionId = ex.getProcessDefinitionId();
    this.processInstanceId = ex.getProcessInstanceId();
    this.activityId = ex.getActivityId();
    this.executionId = ex.getId();
  }

  public void executionCompleted() {
    ExecutionEntity execution = Context.getCommandContext()
      .getExecutionManager()
      .findExecutionById(executionId);

    // TODO check type...listener vs. activity behavior...
    execution.signal("externalComplete", null);
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevision() {
    return revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public Date getLockTime() {
    return lockTime;
  }

  public void setLockTime(Date lockTime) {
    this.lockTime = lockTime;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public Object getPersistentState() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("lockOwner", lockOwner);
    state.put("lockTime", lockTime);
    state.put("executionId", executionId);
    return state;
  }

}
