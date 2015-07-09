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
package org.camunda.bpm.engine.test.authorization.util;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestRule extends TestWatcher {

  protected ProcessEngineRule engineRule;

  protected AuthorizationExceptionInterceptor interceptor;
  protected CommandExecutor replacedCommandExecutor;

  public AuthorizationTestRule(ProcessEngineRule engineRule) {
    this.engineRule = engineRule;
    this.interceptor = new AuthorizationExceptionInterceptor();
  }

  public void start() {
    engineRule.getProcessEngine().getProcessEngineConfiguration().setAuthorizationEnabled(true);
    interceptor.reset();
  }

  public void assertSuccess() {
    Assert.assertNull(interceptor.getLastException());
    cleanup();
  }

  public void assertFailure() {
    Assert.assertNotNull(interceptor.getLastException());
    cleanup();
  }

  protected void cleanup() {

    engineRule.getProcessEngine().getProcessEngineConfiguration().setAuthorizationEnabled(false);
  }

  protected void starting(Description description) {
    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor);
    interceptor.setNext(engineConfiguration.getCommandInterceptorsTxRequired().get(1));

    super.starting(description);
  }

  protected void finished(Description description) {
    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor.getNext());
    interceptor.setNext(null);

    super.finished(description);
  }

}
