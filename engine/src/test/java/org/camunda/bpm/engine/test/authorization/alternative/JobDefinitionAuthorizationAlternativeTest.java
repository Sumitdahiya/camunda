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
package org.camunda.bpm.engine.test.authorization.alternative;

import static org.camunda.bpm.engine.test.authorization.alternative.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.authorization.alternative.AuthorizationSpec.grant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
public class JobDefinitionAuthorizationAlternativeTest {
//  createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE);

  protected static final String USER_ID = "userId";

  @Parameters
  public static Collection<AuthorizationScenario[]> scenarios() {
    return asParameters(
        scenario()
          .failsDueToMissing(
            grant(Resources.PROCESS_DEFINITION, Authorization.ANY, USER_ID, Permissions.UPDATE)
        ),
        scenario(
            grant(Resources.PROCESS_DEFINITION, Authorization.ANY, USER_ID, Permissions.UPDATE))
          .success()
        );
  }

  public static Collection<AuthorizationScenario[]> asParameters(AuthorizationScenario... scenarios) {
    List<AuthorizationScenario[]> scenarioList = new ArrayList<AuthorizationScenario[]>();
    for (AuthorizationScenario scenario : scenarios) {
      scenarioList.add(new AuthorizationScenario[]{ scenario });
    }

    return scenarioList;
  }

  protected AuthorizationScenario scenario;

  protected String deploymentId;

  @Rule
  public AuthorizationTestRule authorizationRule = new AuthorizationTestRule();

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  public JobDefinitionAuthorizationAlternativeTest(AuthorizationScenario scenario) {
    this.scenario = scenario;
  }

  @Before
  public void setup() {
    authorizationRule.doUnauthorized(processEngineRule.getProcessEngine(), new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        deploymentId = processEngineRule.getProcessEngine().getRepositoryService()
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/authorization/timerBoundaryEventProcess.bpmn20.xml")
          .deploy()
          .getId();

        return null;
      }
    });
  }

  @After
  public void tearDown() {
    authorizationRule.doUnauthorized(processEngineRule.getProcessEngine(), new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        processEngineRule.getProcessEngine().getRepositoryService()
          .deleteDeployment(deploymentId);

        return null;
      }
    });
  }

  @Test
  public void testStuff() {
    // given
    JobDefinition jobDefinition = processEngineRule
        .getManagementService()
        .createJobDefinitionQuery()
        .singleResult();
    final String jobDefinitionId = jobDefinition.getId();

    // when
    authorizationRule.when(new Callable<Void>() {
      public Void call() throws Exception {
        processEngineRule.getManagementService().setJobDefinitionPriority(jobDefinitionId, 42);
        return null;
      }
    });

    // then
    jobDefinition = processEngineRule
      .getManagementService()
      .createJobDefinitionQuery()
      .singleResult();

    Assert.assertNotNull(jobDefinition);
    Assert.assertEquals(42, (int) jobDefinition.getJobPriority());
  }

//  @OnSuccess
//  public SuccessHandler onSuccess() {
//    return new SuccessHandler() {
//
//      @Override
//      public void onSuccess() {
//        // then
//        JobDefinition jobDefinition = processEngineRule
//          .getManagementService()
//          .createJobDefinitionQuery()
//          .singleResult();
//
//        Assert.assertNotNull(jobDefinition);
//        Assert.assertEquals(42, (int) jobDefinition.getJobPriority());
//      }
//    };
//  }

  @AuthorizationScenarioInstance
  public AuthorizationScenario getScenario() {
    return scenario;
  }

}
