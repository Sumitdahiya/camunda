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

package org.camunda.bpm.engine.test.api.task;

import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.After;
import org.junit.Before;

/**
 * @author Sebastian Menski
 */
public class TaskQueryExpressionTest extends PluggableProcessEngineTestCase {

  protected Task task;
  protected User user;
  protected User anotherUser;
  protected TaskQuery taskQuery;
  private String groupId;

  @Before
  public void setUp() {
    groupId = "group1";

    user = createTestUser("user", groupId, "group2");
    anotherUser = createTestUser("anotherUser", "group3");

    task = createTestTask("task", user);
    taskService.addCandidateUser(task.getId(), user.getId());
    taskService.addCandidateGroup(task.getId(), groupId);

    taskQuery = taskService.createTaskQuery();
  }

  public void testQueryByAssigneeExpression() {
    assertOne(taskQuery.taskAssigneeExpression("${'" + user.getId() + "'}"));
    assertNone(taskQuery.taskAssigneeExpression("${'" + anotherUser.getId() + "'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskAssigneeExpression("${currentUser()}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskAssigneeExpression("${currentUser()}"));
  }

  public void testQueryByAssigneeLikeExpression() {
    assertOne(taskQuery.taskAssigneeLikeExpression("${'%" + user.getId().substring(2) + "'}"));
    assertNone(taskQuery.taskAssigneeLikeExpression("${'%" + anotherUser.getId().substring(2) + "'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskAssigneeLikeExpression("${'%'.concat(currentUser())}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskAssigneeLikeExpression("${'%'.concat(currentUser())}"));
  }

  public void testQueryByOwnerExpression() {
    assertOne(taskQuery.taskOwnerExpression("${'" + user.getId() + "'}"));
    assertNone(taskQuery.taskOwnerExpression("${'" + anotherUser.getId() + "'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskOwnerExpression("${currentUser()}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskOwnerExpression("${currentUser()}"));
  }

  public void testQueryByInvolvedUserExpression() {
    assertOne(taskQuery.taskInvolvedUserExpression("${'" + user.getId() + "'}"));
    assertNone(taskQuery.taskInvolvedUserExpression("${'" + anotherUser.getId() + "'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskInvolvedUserExpression("${currentUser()}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskInvolvedUserExpression("${currentUser()}"));
  }

  public void testQueryByCandidateUserExpression() {
    assertOne(taskQuery.taskCandidateUserExpression("${'" + user.getId() + "'}"));
    assertNone(taskQuery.taskCandidateUserExpression("${'" + anotherUser.getId() + "'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskCandidateUserExpression("${currentUser()}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskCandidateUserExpression("${currentUser()}"));
  }

  public void testQueryByCandidateGroupExpression() {
    assertOne(taskQuery.taskCandidateGroupExpression("${'" + groupId + "'}"));
    assertNone(taskQuery.taskCandidateGroupExpression("${'unknown'}"));

    setCurrentUser(user);
    assertOne(taskQuery.taskCandidateGroupExpression("${currentUserGroups()[0]}"));

    setCurrentUser(anotherUser);
    assertNone(taskQuery.taskCandidateGroupExpression("${currentUserGroups()[0]}"));
  }

  public void testQueryByCandidateGroupsExpression() {
    setCurrentUser(user);
    assertOne(taskQuery.taskCandidateGroupInExpression("${currentUserGroups()}"));

    setCurrentUser(anotherUser);

    try {
      // currentUserGroups returns null
      taskQuery.taskCandidateGroupInExpression("${currentUserGroups()}");
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  public void testQueryByTaskCreatedBeforeExpression() {
    adjustTime(1);

    assertOne(taskQuery.taskCreatedBeforeExpression("${now()}"));

    adjustTime(-60);

    assertNone(taskQuery.taskCreatedBeforeExpression("${now()}"));

    setTime(task.getCreateTime());

    assertOne(taskQuery.taskCreatedBeforeExpression("${dateTime().plusMonths(2)}"));

    assertNone(taskQuery.taskCreatedBeforeExpression("${dateTime().minusYears(1)}"));
  }

  public void testQueryByTaskCreatedOnExpression() {
    setTime(task.getCreateTime());
    assertOne(taskQuery.taskCreatedOnExpression("${now()}"));

    adjustTime(10);

    assertOne(taskQuery.taskCreatedOnExpression("${dateTime().minusSeconds(10)}"));

    assertNone(taskQuery.taskCreatedOnExpression("${now()}"));
  }

  public void testQueryByTaskCreatedAfterExpression() {
    adjustTime(1);

    assertNone(taskQuery.taskCreatedAfterExpression("${now()}"));

    adjustTime(-60);

    assertOne(taskQuery.taskCreatedAfterExpression("${now()}"));

    setTime(task.getCreateTime());

    assertNone(taskQuery.taskCreatedAfterExpression("${dateTime().plusMonths(2)}"));

    assertOne(taskQuery.taskCreatedAfterExpression("${dateTime().minusYears(1)}"));
  }

  public void testQueryByDueBeforeExpression() {
    adjustTime(1);

    assertOne(taskQuery.dueBeforeExpression("${now()}"));

    adjustTime(-60);

    assertNone(taskQuery.dueBeforeExpression("${now()}"));

    setTime(task.getCreateTime());

    assertOne(taskQuery.dueBeforeExpression("${dateTime().plusMonths(2)}"));

    assertNone(taskQuery.dueBeforeExpression("${dateTime().minusYears(1)}"));
  }

  public void testQueryByDueDateExpression() {
    setTime(task.getDueDate());
    assertOne(taskQuery.dueDateExpression("${now()}"));

    adjustTime(10);

    assertOne(taskQuery.dueDateExpression("${dateTime().minusSeconds(10)}"));

    assertNone(taskQuery.dueDateExpression("${now()}"));
  }

  public void testQueryByDueAfterExpression() {
    adjustTime(1);

    assertNone(taskQuery.dueAfterExpression("${now()}"));

    adjustTime(-60);

    assertOne(taskQuery.dueAfterExpression("${now()}"));

    setTime(task.getCreateTime());

    assertNone(taskQuery.dueAfterExpression("${dateTime().plusMonths(2)}"));

    assertOne(taskQuery.dueAfterExpression("${dateTime().minusYears(1)}"));
  }

  public void testQueryByFollowUpBeforeExpression() {
    adjustTime(1);

    assertOne(taskQuery.followUpBeforeExpression("${now()}"));

    adjustTime(-60);

    assertNone(taskQuery.followUpBeforeExpression("${now()}"));

    setTime(task.getCreateTime());

    assertOne(taskQuery.followUpBeforeExpression("${dateTime().plusMonths(2)}"));

    assertNone(taskQuery.followUpBeforeExpression("${dateTime().minusYears(1)}"));
  }

  public void testQueryByFollowUpDateExpression() {
    setTime(task.getFollowUpDate());
    assertOne(taskQuery.followUpDateExpression("${now()}"));

    adjustTime(10);

    assertOne(taskQuery.followUpDateExpression("${dateTime().minusSeconds(10)}"));

    assertNone(taskQuery.followUpDateExpression("${now()}"));
  }

  public void testQueryByFollowUpAfterExpression() {
    adjustTime(1);

    assertNone(taskQuery.followUpAfterExpression("${now()}"));

    adjustTime(-60);

    assertOne(taskQuery.followUpAfterExpression("${now()}"));

    setTime(task.getCreateTime());

    assertNone(taskQuery.followUpAfterExpression("${dateTime().plusMonths(2)}"));

    assertOne(taskQuery.followUpAfterExpression("${dateTime().minusYears(1)}"));
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  protected void assertOne(Query query) {
    assertEquals(1, query.count());
  }

  protected void assertNone(Query query) {
    assertEquals(0, query.count());
  }

  protected void setCurrentUser(User user) {
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(user.getId());
  }

  protected Task createTestTask(String taskId, User user) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      task = taskService.newTask(taskId);
      task.setDueDate(now());
      task.setFollowUpDate(now());

      if (user != null) {
        task.setAssignee(user.getId());
        task.setOwner(user.getId());
      }

      taskService.saveTask(task);
    }

    return task;
  }

  protected User createTestUser(String userId, String... groupIds) {
    User user = identityService.createUserQuery().userId(userId).singleResult();
    if (user == null) {
      user = identityService.newUser(userId);
      identityService.saveUser(user);
    }

    if (groupIds != null) {
      for (String groupId : groupIds) {
        Group group = createTestGroup(groupId);
        identityService.createMembership(user.getId(), group.getId());
      }
    }

    return user;
  }

  protected Group createTestGroup(String groupId) {
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();

    if (group == null) {
      group = identityService.newGroup(groupId);
      identityService.saveGroup(group);
    }

    return group;
  }

  protected Date now() {
    return ClockUtil.getCurrentTime();
  }

  protected void setTime(long time) {
    setTime(new Date(time));
  }

  protected void setTime(Date time) {
    ClockUtil.setCurrentTime(time);
  }

  /**
   * Changes the current time about the given amount in seconds.
   *
   * @param amount the amount to adjust the current time
   */
  protected void adjustTime(int amount) {
    long time = now().getTime() + amount * 1000;
    setTime(time);
  }

}
