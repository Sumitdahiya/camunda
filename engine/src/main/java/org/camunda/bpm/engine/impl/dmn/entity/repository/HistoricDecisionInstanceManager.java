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

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * Manager for {@link HistoricDecisionInstanceEntity}.
 *
 * @author Philipp Ossler
 */
public class HistoricDecisionInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricDecisionInstancesByDecisionDefinitionKey(String decisionDefinitionKey) {
    getDbEntityManager().delete(HistoricDecisionInstanceEntity.class, "deleteHistoricDecisionInstancesByDecisionDefinitionKey", decisionDefinitionKey);
  }

  public void insertHistoricDecisionInstance(HistoricDecisionInstanceEntity historicDecisionInstance) {
    getDbEntityManager().insert(historicDecisionInstance);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInstance> findHistoricDecisionInstancesByQueryCriteria(HistoricDecisionInstanceQueryImpl query, Page page) {
    return getDbEntityManager().selectList("selectHistoricDecisionInstancesByQueryCriteria", query, page);
  }

  public long findHistoricDecisionInstanceCountByQueryCriteria(HistoricDecisionInstanceQueryImpl query) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInstance> findHistoricDecisionInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricDecisionInstancesByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricDecisionInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByNativeQuery", parameterMap);
  }
}
