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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;



/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  public void testBasicJobExecutorOperation() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.send(createTweetMessage("message-one"));
        jobManager.send(createTweetMessage("message-two"));
        jobManager.send(createTweetMessage("message-three"));
        jobManager.send(createTweetMessage("message-four"));

        jobManager.schedule(createTweetTimer("timer-one", new Date()));
        jobManager.schedule(createTweetTimer("timer-two", new Date()));
        return null;
      }
    });

    waitForJobExecutorToProcessAllJobs(8000L);

    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");

    assertEquals(new TreeSet<String>(expectedMessages), new TreeSet<String>(messages));
  }

  public void testJobPrioritization() {
    processEngineConfiguration.getJobExecutor().setPrioritizedAcquisition(true);

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.send(createTweetMessage("message-one", 20));
        jobManager.send(createTweetMessage("message-two", 60));
        jobManager.send(createTweetMessage("message-three", 30));
        jobManager.send(createTweetMessage("message-four", 90));
        return null;
      }
    });

    waitForJobExecutorToProcessAllJobs(8000L);

    List<String> messages = tweetHandler.getMessages();
    assertEquals(4, messages.size());
    assertEquals("message-four", messages.get(0));
    assertEquals("message-two", messages.get(1));
    assertEquals("message-three", messages.get(2));
    assertEquals("message-one", messages.get(3));
  }

  public void testJobAging() {
    processEngineConfiguration.getJobExecutor().setPrioritizedAcquisition(true);

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();

        Date dueDateOne = ClockUtil.getCurrentTime();
        MessageEntity messageOne = createTweetMessage("message-one", 60);
        messageOne.setDuedate(dueDateOne);
        jobManager.send(messageOne);

        Date dueDateTwo = new Date(dueDateOne.getTime() - 30 * 60 * 1000);
        MessageEntity messageTwo = createTweetMessage("message-two", 40);
        messageTwo.setDuedate(dueDateTwo);
        jobManager.send(messageTwo);

        Date dueDateThree = new Date(dueDateTwo.getTime() - 30 * 60 * 1000);
        MessageEntity messageThree = createTweetMessage("message-three", 20);
        messageThree.setDuedate(dueDateThree);
        jobManager.send(messageThree);
        return null;
      }
    });

    waitForJobExecutorToProcessAllJobs(8000L);

    List<String> messages = tweetHandler.getMessages();
    assertEquals(3, messages.size());
    assertEquals("message-three", messages.get(0));
    assertEquals("message-two", messages.get(1));
    assertEquals("message-one", messages.get(2));
  }
}
