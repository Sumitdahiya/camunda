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

import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * @author Philipp Ossler
 */
public class HistoricDecisionInputInstanceEntity extends HistoryEvent implements HistoricDecisionInputInstance {

  private static final long serialVersionUID = 1L;

  protected String decisionInstanceId;

  protected String clauseId;
  protected String clauseName;

  @Override
  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

  @Override
  public String getClauseId() {
    return clauseId;
  }

  public void setClauseId(String clauseId) {
    this.clauseId = clauseId;
  }

  @Override
  public String getClauseName() {
    return clauseName;
  }

  public void setClauseName(String clauseName) {
    this.clauseName = clauseName;
  }

}
