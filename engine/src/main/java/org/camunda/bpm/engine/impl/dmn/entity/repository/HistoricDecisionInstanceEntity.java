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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * History entry for an evaluated decision.
 *
 * @author Philipp Ossler
 *
 */
public class HistoricDecisionInstanceEntity extends HistoryEvent implements HistoricDecisionInstance {

  private static final long serialVersionUID = 1L;

  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;

  protected String activityInstanceId;
  protected String activityId;

  protected Date evaluationTime;

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public void setDecisionDefinitionKey(String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public void setDecisionDefinitionName(String decisionDefinitionName) {
    this.decisionDefinitionName = decisionDefinitionName;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Date getEvaluationTime() {
    return evaluationTime;
  }

  public void setEvaluationTime(Date evaluationTime) {
    this.evaluationTime = evaluationTime;
  }

}
