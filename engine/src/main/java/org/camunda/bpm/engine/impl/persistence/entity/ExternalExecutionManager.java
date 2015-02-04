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

import java.util.List;

import org.camunda.bpm.engine.impl.ExternalExecutionQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Daniel Meyer
 *
 */
public class ExternalExecutionManager extends AbstractManager {

  public void create(ExecutionEntity executionEntity) {
    ExternalExecutionEntity externalExecution = new ExternalExecutionEntity(executionEntity);
    insert(externalExecution);
  }

  public long findExternalExecutionCountByQueryCriteria(ExternalExecutionQueryImpl query) {
    return (Long) getDbEntityManager().selectOne("selectExternalExecutionCountByQueryCriteria", query);
  }

  public List findExternalExecutionsByQueryCriteria(ExternalExecutionQueryImpl query, Page page) {
    return getDbEntityManager().selectList("selectExternalExecutionByQueryCriteria", query, page);
  }

}
