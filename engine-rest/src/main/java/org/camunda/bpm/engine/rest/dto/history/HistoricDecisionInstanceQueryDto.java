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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Bool;

public class HistoricDecisionInstanceQueryDto extends AbstractQueryDto<HistoricDecisionInstanceQuery> {

  public static final String SORT_BY_EVALUATION_TIME_VALUE = "evaluationTime";

  public static final List<String> VALID_SORT_BY_VALUES;

  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_EVALUATION_TIME_VALUE);
  }

  protected String decisionInstanceId;
  protected Set<String> decisionInstanceIds;
  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String activityId;
  protected String activityInstanceId;
  protected Date evaluatedBefore;
  protected Date evaluatedAfter;
  protected Boolean includeInputs;
  protected Boolean includeOutputs;

  public HistoricDecisionInstanceQueryDto() {}

  public HistoricDecisionInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("decisionInstanceId")
  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

  @CamundaQueryParam(value = "decisionInstanceIds", converter = StringSetConverter.class)
  public void setCaseInstanceIds(Set<String> decisionInstanceIds) {
    this.decisionInstanceIds = decisionInstanceIds;
  }

  @CamundaQueryParam("decisionDefinitionId")
  public void setDecisionDefinitionId(String decisionDefinitionId) {
    this.decisionDefinitionId = decisionDefinitionId;
  }

  @CamundaQueryParam("decisionDefinitionKey")
  public void setDecisionDefinitionKey(String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  @CamundaQueryParam("decisionDefinitionName")
  public void setDecisionDefinitionName(String decisionDefinitionName) {
    this.decisionDefinitionName = decisionDefinitionName;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @CamundaQueryParam("activityInstanceId")
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  @CamundaQueryParam(value = "evaluatedBefore", converter = DateConverter.class)
  public void setEvaluatedBefore(Date evaluatedBefore) {
    this.evaluatedBefore = evaluatedBefore;
  }

  @CamundaQueryParam(value = "evaluatedAfter", converter = DateConverter.class)
  public void setEvaluatedAfter(Date evaluatedAfter) {
    this.evaluatedAfter = evaluatedAfter;
  }

  @CamundaQueryParam(value = "includeInputs", converter = BooleanConverter.class)
  public void setIncludeInputs(Boolean includeInputs) {
    this.includeInputs = includeInputs;
  }

  @CamundaQueryParam(value = "includeOutputs", converter = BooleanConverter.class)
  public void setIncludeOutputs(Boolean includeOutputs) {
    this.includeOutputs = includeOutputs;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected HistoricDecisionInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricDecisionInstanceQuery();
  }

  protected void applyFilters(HistoricDecisionInstanceQuery query) {
    if (decisionInstanceId != null) {
      query.decisionInstanceId(decisionInstanceId);
    }
    if (decisionInstanceIds != null && !decisionInstanceIds.isEmpty()) {
      query.decisionInstanceIds(decisionInstanceIds);
    }
    if (decisionDefinitionId != null) {
      query.decisionDefinitionId(decisionDefinitionId);
    }
    if (decisionDefinitionKey != null) {
      query.decisionDefinitionKey(decisionDefinitionKey);
    }
    if (decisionDefinitionName != null) {
      query.decisionDefinitionName(decisionDefinitionName);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (activityId != null) {
      query.activityId(activityId);
    }
    if (activityInstanceId != null) {
      query.activityInstanceId(activityInstanceId);
    }
    if (evaluatedBefore != null) {
      query.evaluatedBefore(evaluatedBefore);
    }
    if (evaluatedAfter != null) {
      query.evaluatedAfter(evaluatedAfter);
    }
    if (includeInputs != null && includeInputs) {
      query.includeInputs();
    }
    if (includeOutputs != null && includeOutputs) {
      query.includeOutputs();
    }
  }

  protected void applySortBy(HistoricDecisionInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_EVALUATION_TIME_VALUE)) {
      query.orderByEvaluationTime();
    }
  }

}
