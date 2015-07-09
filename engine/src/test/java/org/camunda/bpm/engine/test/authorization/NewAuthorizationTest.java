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
package org.camunda.bpm.engine.test.authorization;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.authorization.util.AuthorizationTestRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
//@RunWith(Parameterized.class)
public class NewAuthorizationTest {

//  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

//  @Rule
  public AuthorizationTestRule rule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(rule);

//  @Parameters
//  public static Collection<AuthorizationScenario[]> scenarios() {
//    return asParameters(
//        scenario()
//          .failsDueToMissing(
//            grant("task", "taskId", "someUserId", Permissions.UPDATE)
//        ),
//        scenario(
//            grant("task", "someTaskId", "someUserId", Permissions.UPDATE))
//          .success(),
//        scenario(
//            grant("task", "*", "someUserId", Permissions.UPDATE))
//          .success(),
//
//        queryScenario(grant(..)).hasResults(1)
//        );
//  }

  protected Task task;

//  @Before
//  public void setUp() {
//    task = createTask();
//  }
//
//  @After
//  public void tearDown() {
//    delete(task);
//  }

  @Test
  public void test1() {

    String taskId = "someTask";
    Task task = engineRule.getTaskService().newTask(taskId);
    engineRule.getTaskService().saveTask(task);

    Authorization auth = engineRule.getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    auth.setUserId("user");
    auth.setResourceId(taskId);
    auth.setResource(Resources.TASK);
    auth.addPermission(Permissions.DELETE);

    engineRule.getIdentityService().setAuthenticatedUserId("user");

    engineRule.getAuthorizationService().saveAuthorization(auth);

    // sets up the scenario; activates authorization
//    rule.start(new Map("taskId", task.getId()));
    rule.start();

    engineRule.getTaskService().deleteTask(taskId);

    // assert authorization exception as specified in scenario
    rule.assertSuccess();

    Assert.assertEquals(0, engineRule.getTaskService().createTaskQuery().count());
  }

  @Test
  public void test2() {

  }

  @Test
  public void test3() {

  }

}
