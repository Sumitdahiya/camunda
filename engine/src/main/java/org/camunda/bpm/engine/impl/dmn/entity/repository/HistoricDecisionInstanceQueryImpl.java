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

package org.camunda.bpm.engine.impl.dmn.entity.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 *  @author Philipp Ossler
 */
public class HistoricDecisionInstanceQueryImpl extends AbstractQuery<HistoricDecisionInstanceQuery, HistoricDecisionInstance> implements HistoricDecisionInstanceQuery {

  private static final long serialVersionUID = 1L;

  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected String processInstanceId;
  protected String executionId;

  protected String activityInstanceId;
  protected String activityId;

  protected boolean includeInput = false;
  protected boolean includeOutputs = false;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public HistoricDecisionInstanceQueryImpl() {
  }

  public HistoricDecisionInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricDecisionInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionKey(String decisionDefinitionKey) {
    ensureNotNull(NotValidException.class, "decisionDefinitionKey", decisionDefinitionKey);
    this.decisionDefinitionKey = decisionDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionName(String decisionDefinitionName) {
    ensureNotNull(NotValidException.class, "decisionDefinitionName", decisionDefinitionName);
    this.decisionDefinitionName = decisionDefinitionName;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull(NotValidException.class, "processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull(NotValidException.class, "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processInstanceId(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery executionId(String executionId) {
    ensureNotNull(NotValidException.class, "executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery activityId(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    this.activityId = activityId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery activityInstanceId(String activityInstanceId) {
    ensureNotNull(NotValidException.class, "activityInstanceId", activityInstanceId);
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery orderByEvaluationTime() {
    orderBy(HistoricDecisionInstanceQueryProperty.EVALUATION_TIME);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDecisionInstanceManager()
      .findHistoricDecisionInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricDecisionInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstancesByQueryCriteria(this, page);
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public HistoricDecisionInstanceQuery includeInputs() {
    includeInput = true;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery includeOutputs() {
    includeOutputs = true;
    return this;
  }

  public boolean isIncludeInput() {
    return includeInput;
  }

  public boolean isIncludeOutputs() {
    return includeOutputs;
  }

  @Override
  public HistoricDecisionInstanceQuery disableBinaryFetching() {
    isByteArrayFetchingEnabled = false;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery disableCustomObjectDeserialization() {
    isCustomObjectDeserializationEnabled = false;
    return null;
  }

  public boolean isByteArrayFetchingEnabled() {
    return isByteArrayFetchingEnabled;
  }

  public boolean isCustomObjectDeserializationEnabled() {
    return isCustomObjectDeserializationEnabled;
  }
}
