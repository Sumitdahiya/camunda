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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * Manager for {@link HistoricDecisionInstanceEntity}.
 *
 * @author Philipp Ossler
 */
public class HistoricDecisionInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricDecisionInstancesByDecisionDefinitionKey(String decisionDefinitionKey) {
    if (isHistoryEnabled()) {
      // TODO insert inputs and outputs in a better way
      // getDbEntityManager().delete(HistoricDecisionInstanceEntity.class, "deleteHistoricDecisionInstancesByDecisionDefinitionKey", decisionDefinitionKey);

      @SuppressWarnings("unchecked")
      List<HistoricDecisionInstanceEntity> decisionInstances = getDbEntityManager().selectList("selectHistoricDecisionInstancesByDecisionDefinitionKey", decisionDefinitionKey);
      for(HistoricDecisionInstanceEntity decisionInstance : decisionInstances) {
        // delete decision instance
        getDbEntityManager().delete(HistoricDecisionInstanceEntity.class, "deleteHistoricDecisionInstanceById", decisionInstance.getId());
        // delete inputs of decision instance
        deleteHistoricDecisionInputInstancesByDecisionInstanceId(decisionInstance);
        // delete outputs of decision instance
        deleteHistoricDecisionOutputInstancesByDecisionInstanceId(decisionInstance);
      }
    }
  }

  protected void deleteHistoricDecisionInputInstancesByDecisionInstanceId(HistoricDecisionInstanceEntity decisionInstance) {
    getDbEntityManager().delete(HistoricDecisionInputInstanceEntity.class, "deleteHistoricDecisionInputInstancesByDecisionInstanceId", decisionInstance.getId());
  }

  protected void deleteHistoricDecisionOutputInstancesByDecisionInstanceId(HistoricDecisionInstanceEntity decisionInstance) {
    getDbEntityManager().delete(HistoricDecisionInputInstanceEntity.class, "deleteHistoricDecisionOutputInstancesByDecisionInstanceId", decisionInstance.getId());
  }

  public void insertHistoricDecisionInstance(HistoricDecisionInstanceEntity historicDecisionInstance) {
    if (isHistoryEnabled()) {
      getDbEntityManager().insert(historicDecisionInstance);

      insertHistoricDecisionInputInstances(historicDecisionInstance.getInputs(), historicDecisionInstance.getId());
      insertHistoricDecisionOutputInstances(historicDecisionInstance.getOutputs(), historicDecisionInstance.getId());
    }
  }

  protected void insertHistoricDecisionInputInstances(List<HistoricDecisionInputInstance> inputs, String decisionInstanceId) {
    for (HistoricDecisionInputInstance input : inputs) {
      HistoricDecisionInputInstanceEntity inputEntity = (HistoricDecisionInputInstanceEntity) input;
      inputEntity.setDecisionInstanceId(decisionInstanceId);

      getDbEntityManager().insert(inputEntity);
    }
  }

  protected void insertHistoricDecisionOutputInstances(List<HistoricDecisionOutputInstance> outputs, String decisionInstanceId) {
    for (HistoricDecisionOutputInstance output : outputs) {
      HistoricDecisionOutputInstanceEntity outputEntity = (HistoricDecisionOutputInstanceEntity) output;
      outputEntity.setDecisionInstanceId(decisionInstanceId);

      getDbEntityManager().insert(outputEntity);
    }
  }

 public List<HistoricDecisionInstance> findHistoricDecisionInstancesByQueryCriteria(HistoricDecisionInstanceQueryImpl query, Page page) {
    if (isHistoryEnabled()) {
      getAuthorizationManager().configureHistoricDecisionInstanceQuery(query);

      // TODO enable / disable input query
      // TODO query inputs in better way
      @SuppressWarnings("unchecked")
      List<HistoricDecisionInstance> decisionInstances = getDbEntityManager().selectList("selectHistoricDecisionInstancesByQueryCriteria", query, page);
      for(HistoricDecisionInstance decisionInstance : decisionInstances) {
        HistoricDecisionInstanceEntity historicDecisionInstanceEntity = (HistoricDecisionInstanceEntity) decisionInstance;

        List<HistoricDecisionInputInstance> decisionInputInstances = findHistoricDecisionInputInstancesByDecisionInstanceId(decisionInstance.getId());
        historicDecisionInstanceEntity.setInputs(decisionInputInstances);

        List<HistoricDecisionOutputInstance> decisionOutputInstances = findHistoricDecisionOutputInstancesByDecisionInstanceId(decisionInstance.getId());
        historicDecisionInstanceEntity.setOuputs(decisionOutputInstances);
      }

      return decisionInstances;
    } else {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInputInstance> findHistoricDecisionInputInstancesByDecisionInstanceId(String decisionInstanceId) {
    return getDbEntityManager().selectList("selectHistoricDecisionInputInstancesByDecisionInstanceId", decisionInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionOutputInstance> findHistoricDecisionOutputInstancesByDecisionInstanceId(String decisionInstanceId) {
    return getDbEntityManager().selectList("selectHistoricDecisionOutputInstancesByDecisionInstanceId", decisionInstanceId);
  }

  public long findHistoricDecisionInstanceCountByQueryCriteria(HistoricDecisionInstanceQueryImpl query) {
    if (isHistoryEnabled()) {
      getAuthorizationManager().configureHistoricDecisionInstanceQuery(query);
      return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByQueryCriteria", query);
    } else {
      return 0;
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInstance> findHistoricDecisionInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricDecisionInstancesByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricDecisionInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByNativeQuery", parameterMap);
  }
}