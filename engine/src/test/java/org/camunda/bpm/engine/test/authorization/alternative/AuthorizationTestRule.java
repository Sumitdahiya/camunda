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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.junit.Assert;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestRule implements MethodRule {

  protected AuthorizationScenario scenario;

  public Statement apply(final Statement base, FrameworkMethod method, final Object target) {

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        Method scenarioGetter = findMethod(target.getClass(), AuthorizationScenarioInstance.class);
        scenario = (AuthorizationScenario) scenarioGetter.invoke(target);

        base.evaluate();

        scenario = null;
      }
    };
  }

  public <T> T when(Callable<T> callable) {
    T result = null;
    try {
      result = callable.call();
    } catch (AuthorizationException e) {
      scenario.assertAuthorizationException(e);
    } catch (Exception e) {
      Assert.fail("could not perform operation");
    }

    return result;
  }

  public void setup(ProcessEngine engine) {
    engine.getIdentityService().setAuthentication("userId", null);
    engine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  public void tearDown(ProcessEngine engine) {
    engine.getProcessEngineConfiguration().setAuthorizationEnabled(false);

  }

  protected Method findMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(annotationClass)) {
        return method;
      }
    }

    return null;
  }

  public <T> T doUnauthorized(ProcessEngine engine, Callable<T> callable) {
    engine.getProcessEngineConfiguration().setAuthorizationEnabled(false);

    T result;
    try {
      result = callable.call();
    } catch (Exception e) {
      throw new ProcessEngineException("Could not perform action");
    }

    engine.getProcessEngineConfiguration().setAuthorizationEnabled(true);

    return result;
  }
}
