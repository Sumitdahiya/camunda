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
package org.camunda.bpm.engine.rest.dto.task;

import static org.camunda.bpm.engine.impl.util.StringUtil.isExpression;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

public class TaskQueryDto extends AbstractQueryDto<TaskQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_CASE_INSTANCE_ID_VALUE = "caseInstanceId";
  private static final String SORT_BY_DUE_DATE_VALUE = "dueDate";
  private static final String SORT_BY_FOLLOW_UP_VALUE = "followUpDate";
  private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  private static final String SORT_BY_CASE_EXECUTION_ID_VALUE = "caseExecutionId";
  private static final String SORT_BY_ASSIGNEE_VALUE = "assignee";
  private static final String SORT_BY_CREATE_TIME_VALUE = "created";
  private static final String SORT_BY_DESCRIPTION_VALUE = "description";
  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_PRIORITY_VALUE = "priority";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_FOLLOW_UP_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DESCRIPTION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY_VALUE);
  }

  private String processInstanceBusinessKey;
  private String processInstanceBusinessKeyLike;
  private String processDefinitionKey;
  private String processDefinitionId;
  private String executionId;
  private String[] activityInstanceIdIn;
  private String processDefinitionName;
  private String processDefinitionNameLike;
  private String processInstanceId;
  private String assignee;
  private String assigneeLike;
  private String candidateGroup;
  private String candidateUser;
  private String taskDefinitionKey;
  private String taskDefinitionKeyLike;
  private String description;
  private String descriptionLike;
  private String involvedUser;
  private Integer maxPriority;
  private Integer minPriority;
  private String name;
  private String nameLike;
  private String owner;
  private Integer priority;
  private Boolean unassigned;
  private Boolean active;
  private Boolean suspended;

  private String caseDefinitionKey;
  private String caseDefinitionId;
  private String caseDefinitionName;
  private String caseDefinitionNameLike;
  private String caseInstanceId;
  private String caseInstanceBusinessKey;
  private String caseInstanceBusinessKeyLike;
  private String caseExecutionId;

  private Date dueAfter;
  private Date dueBefore;
  private Date dueDate;
  private Date followUpAfter;
  private Date followUpBefore;
  private Date followUpDate;
  private Date createdAfter;
  private Date createdBefore;
  private Date createdOn;

  private String delegationState;

  private List<String> candidateGroups;

  private List<VariableQueryParameterDto> taskVariables;
  private List<VariableQueryParameterDto> processVariables;
  private List<VariableQueryParameterDto> caseInstanceVariables;

  public TaskQueryDto() {

  }

  public TaskQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String businessKeyLike) {
    this.processInstanceBusinessKeyLike = businessKeyLike;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @CamundaQueryParam("processDefinitionNameLike")
  public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("assignee")
  public void setAssignee(String assignee) {
    if (isExpression(assignee)) {
      expressions.put("assignee", assignee);
    }
    else {
      this.assignee = assignee;
    }
  }

  @CamundaQueryParam("assigneeLike")
  public void setAssigneeLike(String assigneeLike) {
    if (isExpression(assigneeLike)) {
      expressions.put("assigneeLike", assigneeLike);
    }
    else {
      this.assigneeLike = assigneeLike;
    }
  }

  @CamundaQueryParam("candidateGroup")
  public void setCandidateGroup(String candidateGroup) {
    if (isExpression(candidateGroup)) {
      expressions.put("candidateGroup", candidateGroup);
    }
    else {
      this.candidateGroup = candidateGroup;
    }
  }

  @CamundaQueryParam("candidateUser")
  public void setCandidateUser(String candidateUser) {
    if (isExpression(candidateUser)) {
      expressions.put("candidateUser", candidateUser);
    }
    else {
      this.candidateUser = candidateUser;
    }
  }

  @CamundaQueryParam("taskDefinitionKey")
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  @CamundaQueryParam("taskDefinitionKeyLike")
  public void setTaskDefinitionKeyLike(String taskDefinitionKeyLike) {
    this.taskDefinitionKeyLike = taskDefinitionKeyLike;
  }

  @CamundaQueryParam("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @CamundaQueryParam("descriptionLike")
  public void setDescriptionLike(String descriptionLike) {
    this.descriptionLike = descriptionLike;
  }

  @CamundaQueryParam("involvedUser")
  public void setInvolvedUser(String involvedUser) {
    if (isExpression(involvedUser)) {
      expressions.put("involvedUser", involvedUser);
    }
    else {
      this.involvedUser = involvedUser;
    }
  }

  @CamundaQueryParam(value = "maxPriority", converter = IntegerConverter.class)
  public void setMaxPriority(Integer maxPriority) {
    this.maxPriority = maxPriority;
  }

  @CamundaQueryParam(value = "minPriority", converter = IntegerConverter.class)
  public void setMinPriority(Integer minPriority) {
    this.minPriority = minPriority;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("owner")
  public void setOwner(String owner) {
    if (isExpression(owner)) {
      expressions.put("owner", owner);
    }
    else {
      this.owner = owner;
    }
  }

  @CamundaQueryParam(value = "priority", converter = IntegerConverter.class)
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @CamundaQueryParam(value = "unassigned", converter = BooleanConverter.class)
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "dueAfter")
  public void setDueAfter(String dueAfter) {
    if (isExpression(dueAfter)) {
      expressions.put("dueAfter", dueAfter);
    }
    else {
      this.dueAfter = DateConverter.convert(dueAfter);
    }
  }

  @CamundaQueryParam(value = "dueBefore")
  public void setDueBefore(String dueBefore) {
    if (isExpression(dueBefore)) {
      expressions.put("dueBefore", dueBefore);
    }
    else {
      this.dueBefore = DateConverter.convert(dueBefore);
    }
  }

  @CamundaQueryParam(value = "due")
  public void setDueDate(String dueDate) {
    if (isExpression(dueDate)) {
      expressions.put("due", dueDate);
    }
    else {
      this.dueDate = DateConverter.convert(dueDate);
    }
  }

  @CamundaQueryParam(value = "followUpAfter")
  public void setFollowUpAfter(String followUpAfter) {
    if (isExpression(followUpAfter)) {
      expressions.put("followUpAfter", followUpAfter);
    }
    else {
      this.followUpAfter = DateConverter.convert(followUpAfter);
    }
  }

  @CamundaQueryParam(value = "followUpBefore")
  public void setFollowUpBefore(String followUpBefore) {
    if (isExpression(followUpBefore)) {
      expressions.put("followUpBefore", followUpBefore);
    }
    else {
      this.followUpBefore = DateConverter.convert(followUpBefore);
    }
  }

  @CamundaQueryParam(value = "followUp")
  public void setFollowUpDate(String followUp) {
    if (isExpression(followUp)) {
      expressions.put("followUp", followUp);
    }
    else {
      this.followUpDate = DateConverter.convert(followUp);
    }
  }

  @CamundaQueryParam(value = "createdAfter")
  public void setCreatedAfter(String createdAfter) {
    if (isExpression(createdAfter)) {
      expressions.put("createdAfter", createdAfter);
    }
    else {
      this.createdAfter = DateConverter.convert(createdAfter);
    }
  }

  @CamundaQueryParam(value = "createdBefore")
  public void setCreatedBefore(String createdBefore) {
    if (isExpression(createdBefore)) {
      expressions.put("createdBefore", createdBefore);
    }
    else {
      this.createdBefore = DateConverter.convert(createdBefore);
    }
  }

  @CamundaQueryParam(value = "created")
  public void setCreatedOn(String createdOn) {
    if (isExpression(createdOn)) {
      expressions.put("created", createdOn);
    }
    else {
      this.createdOn = DateConverter.convert(createdOn);
    }
  }

  @CamundaQueryParam(value = "delegationState")
  public void setDelegationState(String taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @CamundaQueryParam(value = "candidateGroups", converter = StringListConverter.class)
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @CamundaQueryParam(value = "candidateGroupsExpression")
  public void setCandidateGroupsExpression(String candidateGroupsExpression) {
    if (isExpression(candidateGroupsExpression)) {
      expressions.put("candidateGroups", candidateGroupsExpression);
    }
    else {
      this.candidateGroups = StringListConverter.convert(candidateGroupsExpression);
    }
  }

  @CamundaQueryParam(value = "taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @CamundaQueryParam(value = "processVariables", converter = VariableListConverter.class)
  public void setProcessVariables(List<VariableQueryParameterDto> processVariables) {
    this.processVariables = processVariables;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam("caseDefinitionName")
  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  @CamundaQueryParam("caseDefinitionNameLike")
  public void setCaseDefinitionNameLike(String caseDefinitionNameLike) {
    this.caseDefinitionNameLike = caseDefinitionNameLike;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam("caseInstanceBusinessKey")
  public void setCaseInstanceBusinessKey(String caseInstanceBusinessKey) {
    this.caseInstanceBusinessKey = caseInstanceBusinessKey;
  }

  @CamundaQueryParam("caseInstanceBusinessKeyLike")
  public void setCaseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike) {
    this.caseInstanceBusinessKeyLike = caseInstanceBusinessKeyLike;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "caseInstanceVariables", converter = VariableListConverter.class)
  public void setCaseInstanceVariables(List<VariableQueryParameterDto> caseInstanceVariables) {
    this.caseInstanceVariables = caseInstanceVariables;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected TaskQuery createNewQuery(ProcessEngine engine) {
    return engine.getTaskService().createTaskQuery();
  }

  @Override
  protected void applyFilters(TaskQuery query) {
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (processDefinitionNameLike != null) {
      query.processDefinitionNameLike(processDefinitionNameLike);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (assignee != null) {
      query.taskAssignee(assignee);
    }
    if (assigneeLike != null) {
      query.taskAssigneeLike(assigneeLike);
    }
    if (candidateGroup != null) {
      query.taskCandidateGroup(candidateGroup);
    }
    if (candidateUser != null) {
      query.taskCandidateUser(candidateUser);
    }
    if (taskDefinitionKey != null) {
      query.taskDefinitionKey(taskDefinitionKey);
    }
    if (taskDefinitionKeyLike != null) {
      query.taskDefinitionKeyLike(taskDefinitionKeyLike);
    }
    if (description != null) {
      query.taskDescription(description);
    }
    if (descriptionLike != null) {
      query.taskDescriptionLike(descriptionLike);
    }
    if (involvedUser != null) {
      query.taskInvolvedUser(involvedUser);
    }
    if (maxPriority != null) {
      query.taskMaxPriority(maxPriority);
    }
    if (minPriority != null) {
      query.taskMinPriority(minPriority);
    }
    if (name != null) {
      query.taskName(name);
    }
    if (nameLike != null) {
      query.taskNameLike(nameLike);
    }
    if (owner != null) {
      query.taskOwner(owner);
    }
    if (priority != null) {
      query.taskPriority(priority);
    }
    if (unassigned != null && unassigned == true) {
      query.taskUnassigned();
    }
    if (dueAfter != null) {
      query.dueAfter(dueAfter);
    }
    if (dueBefore != null) {
      query.dueBefore(dueBefore);
    }
    if (dueDate != null) {
      query.dueDate(dueDate);
    }
    if (followUpAfter != null) {
      query.followUpAfter(followUpAfter);
    }
    if (followUpBefore != null) {
      query.followUpBefore(followUpBefore);
    }
    if (followUpDate != null) {
      query.followUpDate(followUpDate);
    }
    if (createdAfter != null) {
      query.taskCreatedAfter(createdAfter);
    }
    if (createdBefore != null) {
      query.taskCreatedBefore(createdBefore);
    }
    if (createdOn != null) {
      query.taskCreatedOn(createdOn);
    }
    if (delegationState != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      DelegationState state = converter.convertQueryParameterToType(delegationState);
      query.taskDelegationState(state);
    }
    if (candidateGroups != null) {
      query.taskCandidateGroupIn(candidateGroups);
    }
    if (active != null && active == true) {
      query.active();
    }
    if (suspended != null && suspended == true) {
      query.suspended();
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseDefinitionName != null) {
      query.caseDefinitionName(caseDefinitionName);
    }
    if (caseDefinitionNameLike != null) {
      query.caseDefinitionNameLike(caseDefinitionNameLike);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (caseInstanceBusinessKey != null) {
      query.caseInstanceBusinessKey(caseInstanceBusinessKey);
    }
    if (caseInstanceBusinessKeyLike != null) {
      query.caseInstanceBusinessKeyLike(caseInstanceBusinessKeyLike);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.taskVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.taskVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid task variable comparator specified: " + op);
        }

      }
    }

    if (processVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : processVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.processVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.processVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.processVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.processVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid process variable comparator specified: " + op);
        }

      }
    }

    if (caseInstanceVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : caseInstanceVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid case variable comparator specified: " + op);
        }

      }
    }
  }

  protected void applyExpressions(TaskQuery query) {
    super.applyExpressions(query);

    if (expressions.containsKey("assignee")) {
      query.taskAssigneeExpression(expressions.get("assignee"));
    }
    if (expressions.containsKey("assigneeLike")) {
      query.taskAssigneeLikeExpression(expressions.get("assigneeLike"));
    }
    if (expressions.containsKey("owner")) {
      query.taskOwnerExpression(expressions.get("owner"));
    }
    if (expressions.containsKey("involvedUser")) {
      query.taskInvolvedUserExpression(expressions.get("involvedUser"));
    }
    if (expressions.containsKey("candidateUser")) {
      query.taskCandidateUserExpression(expressions.get("candidateUser"));
    }
    if (expressions.containsKey("candidateGroup")) {
      query.taskCandidateGroupExpression(expressions.get("candidateGroup"));
    }
    if (expressions.containsKey("candidateGroups")) {
      query.taskCandidateGroupInExpression(expressions.get("candidateGroups"));
    }
    if (expressions.containsKey("createdBefore")) {
      query.taskCreatedBeforeExpression(expressions.get("createdBefore"));
    }
    if (expressions.containsKey("created")) {
      query.taskCreatedOnExpression(expressions.get("created"));
    }
    if (expressions.containsKey("createdAfter")) {
      query.taskCreatedAfterExpression(expressions.get("createdAfter"));
    }
    if (expressions.containsKey("dueBefore")) {
      query.dueBeforeExpression(expressions.get("dueBefore"));
    }
    if (expressions.containsKey("due")) {
      query.dueDateExpression(expressions.get("due"));
    }
    if (expressions.containsKey("dueAfter")) {
      query.dueAfterExpression(expressions.get("dueAfter"));
    }
    if (expressions.containsKey("followUpBefore")) {
      query.followUpBeforeExpression(expressions.get("followUpBefore"));
    }
    if (expressions.containsKey("followUp")) {
      query.followUpDateExpression(expressions.get("followUp"));
    }
    if (expressions.containsKey("followUpAfter")) {
      query.followUpAfterExpression(expressions.get("followUpAfter"));
    }
  }

  @Override
  protected void applySortingOptions(TaskQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
        query.orderByProcessInstanceId();
      } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_ID_VALUE)) {
        query.orderByCaseInstanceId();
      } else if (sortBy.equals(SORT_BY_DUE_DATE_VALUE)) {
        query.orderByDueDate();
      } else if (sortBy.equals(SORT_BY_FOLLOW_UP_VALUE)) {
        query.orderByFollowUpDate();
      } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
        query.orderByExecutionId();
      } else if (sortBy.equals(SORT_BY_CASE_EXECUTION_ID_VALUE)) {
        query.orderByCaseExecutionId();
      } else if (sortBy.equals(SORT_BY_ASSIGNEE_VALUE)) {
        query.orderByTaskAssignee();
      } else if (sortBy.equals(SORT_BY_CREATE_TIME_VALUE)) {
        query.orderByTaskCreateTime();
      } else if (sortBy.equals(SORT_BY_DESCRIPTION_VALUE)) {
        query.orderByTaskDescription();
      } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
        query.orderByTaskId();
      } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
        query.orderByTaskName();
      } else if (sortBy.equals(SORT_BY_PRIORITY_VALUE)) {
        query.orderByTaskPriority();
      }
    }

    if (sortOrder != null) {
      if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
        query.asc();
      } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
        query.desc();
      }
    }
  }

}
