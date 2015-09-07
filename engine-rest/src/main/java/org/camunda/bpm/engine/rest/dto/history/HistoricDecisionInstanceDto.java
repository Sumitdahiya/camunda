/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricDecisionInstance;

public class HistoricDecisionInstanceDto {

  protected String id;
  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected Date evaluationTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String activityId;
  protected String activityInstanceId;

  public String getId() {
    return id;
  }

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public Date getEvaluationTime() {
    return evaluationTime;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public static HistoricDecisionInstanceDto fromHistoricDecisionInstance(HistoricDecisionInstance historicDecisionInstance) {
    HistoricDecisionInstanceDto dto = new HistoricDecisionInstanceDto();

    dto.id = historicDecisionInstance.getId();
    dto.decisionDefinitionId = historicDecisionInstance.getDecisionDefinitionId();
    dto.decisionDefinitionKey = historicDecisionInstance.getDecisionDefinitionKey();
    dto.decisionDefinitionName = historicDecisionInstance.getDecisionDefinitionName();
    dto.evaluationTime = historicDecisionInstance.getEvaluationTime();
    dto.processDefinitionId = historicDecisionInstance.getProcessDefinitionId();
    dto.processDefinitionKey = historicDecisionInstance.getProcessDefinitionKey();
    dto.processInstanceId = historicDecisionInstance.getProcessInstanceId();
    dto.activityId = historicDecisionInstance.getActivityId();
    dto.activityInstanceId = historicDecisionInstance.getActivityInstanceId();

    return dto;
  }

}
