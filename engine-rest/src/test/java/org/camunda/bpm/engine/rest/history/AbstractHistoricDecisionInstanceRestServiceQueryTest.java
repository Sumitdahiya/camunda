package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;

import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricDecisionInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_DECISION_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-instance";
  protected static final String HISTORIC_DECISION_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_DECISION_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricDecisionInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(MockProvider.createMockHistoricDecisionInstances());
  }

  protected HistoricDecisionInstanceQuery setUpMockHistoricDecisionInstanceQuery(List<HistoricDecisionInstance> mockedHistoricDecisionInstances) {
    HistoricDecisionInstanceQuery mockedHistoricDecisionInstanceQuery = mock(HistoricDecisionInstanceQuery.class);
    when(mockedHistoricDecisionInstanceQuery.list()).thenReturn(mockedHistoricDecisionInstances);
    when(mockedHistoricDecisionInstanceQuery.count()).thenReturn((long) mockedHistoricDecisionInstances.size());

    when(processEngine.getHistoryService().createHistoricDecisionInstanceQuery()).thenReturn(mockedHistoricDecisionInstanceQuery);

    return mockedHistoricDecisionInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("caseDefinitionKey", queryKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "evaluationTime")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("evaluationTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByEvaluationTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("evaluationTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByEvaluationTime();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_DECISION_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricDecisionInstanceQuery() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    Response response = given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedHistoricDecisionInstanceId = from(content).getString("[0].id");
    String returnedDecisionDefinitionId = from(content).getString("[0].decisionDefinitionId");
    String returnedDecisionDefinitionKey = from(content).getString("[0].decisionDefinitionKey");
    String returnedDecisionDefinitionName = from(content).getString("[0].decisionDefinitionName");
    String returnedEvaluationTime = from(content).getString("[0].evaluationTime");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");

    assertThat(returnedHistoricDecisionInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
    assertThat(returnedDecisionDefinitionId, is(MockProvider.EXAMPLE_DECISION_DEFINITION_ID));
    assertThat(returnedDecisionDefinitionKey, is(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY));
    assertThat(returnedDecisionDefinitionName, is(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME));
    assertThat(returnedEvaluationTime, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUTION_TIME));
    assertThat(returnedProcessDefinitionId, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    assertThat(returnedProcessDefinitionKey, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY));
    assertThat(returnedProcessInstanceId, is(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    assertThat(returnedActivityId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID));
    assertThat(returnedActivityInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID));
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("decisionInstanceId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    parameters.put("decisionInstanceIds", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_IDS);
    parameters.put("decisionDefinitionId", MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    parameters.put("decisionDefinitionKey", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY);
    parameters.put("decisionDefinitionName", MockProvider.EXAMPLE_DECISION_DEFINITION_NAME);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("activityId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID);
    parameters.put("activityInstanceId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID);
    parameters.put("evaluatedBefore", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUTIED_BEFORE);
    parameters.put("evaluatedAfter", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUTIED_AFTER);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    StringSetConverter stringSetConverter = new StringSetConverter();

    verify(mockedQuery).decisionInstanceId(stringQueryParameters.get("decisionInstanceId"));
    verify(mockedQuery).decisionInstanceIds(stringSetConverter.convertQueryParameterToType(stringQueryParameters.get("decisionInstanceIds")));
    verify(mockedQuery).decisionDefinitionId(stringQueryParameters.get("decisionDefinitionId"));
    verify(mockedQuery).decisionDefinitionKey(stringQueryParameters.get("decisionDefinitionKey"));
    verify(mockedQuery).decisionDefinitionName(stringQueryParameters.get("decisionDefinitionName"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockedQuery).activityId(stringQueryParameters.get("activityId"));
    verify(mockedQuery).activityInstanceId(stringQueryParameters.get("activityInstanceId"));
    verify(mockedQuery).evaluatedBefore(DateTimeUtil.parseDate(stringQueryParameters.get("evaluatedBefore")));
    verify(mockedQuery).evaluatedAfter(DateTimeUtil.parseDate(stringQueryParameters.get("evaluatedAfter")));

    verify(mockedQuery).list();
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
      .then().expect()
      .statusCode(expectedStatus.getStatusCode())
      .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

}
