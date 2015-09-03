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

package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.dmn.entity.repository.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Philipp Ossler
 */
public class DefaultDmnHistoryEventProducer implements DmnHistoryEventProducer {

  @Override
  public HistoryEvent createDecisionEvaluatedEvt(DelegateExecution execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(executionEntity, decisionTable, decisionTableResult);
    // initialize event
    initDecisionInstanceEvent(event, executionEntity, decisionTable, decisionTableResult, HistoryEventTypes.DMN_DECISION_EVALUATE);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    return event;
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(ExecutionEntity executionEntity, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    return new HistoricDecisionInstanceEntity();
  }

  protected void initDecisionInstanceEvent(HistoricDecisionInstanceEntity event, ExecutionEntity execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult, HistoryEventTypes eventType) {
    event.setEventType(eventType.getEventName());

    event.setDecisionDefinitionKey(decisionTable.getKey());
    event.setDecisionDefinitionName(decisionTable.getName());

    event.setProcessDefinitionKey(getProcessDefinitionKey(execution));
    event.setProcessDefinitionId(execution.getProcessDefinitionId());

    event.setProcessInstanceId(execution.getProcessInstanceId());
    event.setExecutionId(execution.getId());

    event.setActivityId(execution.getActivityId());
    event.setActivityInstanceId(execution.getActivityInstanceId());
  }

  protected String getProcessDefinitionKey(ExecutionEntity execution) {
    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    if (definition != null) {
      return definition.getKey();
    } else {
      return null;
    }
  }
}
