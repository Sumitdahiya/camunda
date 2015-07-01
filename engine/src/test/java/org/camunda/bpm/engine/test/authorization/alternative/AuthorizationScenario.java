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

import org.camunda.bpm.engine.AuthorizationException;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationScenario {
  protected AuthorizationSpec[] givenAuthorizations = new AuthorizationSpec[]{};
  protected AuthorizationSpec[] missingAuthorizations = new AuthorizationSpec[]{};

  public static AuthorizationScenario scenario(AuthorizationSpec... givenAuthorizations) {
    AuthorizationScenario scenario = new AuthorizationScenario();
    scenario.givenAuthorizations = givenAuthorizations;
    return scenario;
  }

  public AuthorizationScenario success() {
    return this;
  }

  public AuthorizationScenario failsDueToMissing(AuthorizationSpec... expectedMissingAuthorizations) {
    this.missingAuthorizations = expectedMissingAuthorizations;
    return this;
  }

  public void assertAuthorizationException(AuthorizationException e) {
    if (missingAuthorizations.length > 0) {
      AuthorizationSpec firstMissingAuthorization = missingAuthorizations[0];

      // TODO: old assertions
      String message = e.getMessage();
      Assert.assertTrue(message.contains(firstMissingAuthorization.userId));
      Assert.assertTrue(message.contains(firstMissingAuthorization.permissions[0].getName()));
      Assert.assertTrue(message.contains(firstMissingAuthorization.resourceId));
      Assert.assertTrue(message.contains(firstMissingAuthorization.resource.resourceName()));

      Assert.assertEquals(firstMissingAuthorization.resourceId, e.getResourceId());
      Assert.assertEquals(firstMissingAuthorization.resource, e.getResourceType());
      Assert.assertEquals(firstMissingAuthorization.userId, e.getUserId());
      Assert.assertEquals(firstMissingAuthorization.permissions[0].getName(), e.getViolatedPermissionName());

    }
    else {
      Assert.fail("Did not expect to fail due to missing authorizations");
    }
  }

  // TODO: optional success and failure handlers?
}
