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
package org.camunda.bpm.engine.rest.hal.cache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.hal.Hal;

/**
 * @author Daniel Meyer
 *
 */
public class HalCacheBootstrap implements ServletContextListener {

  public void contextInitialized(ServletContextEvent sce) {
    String initParameter = sce.getServletContext().getInitParameter("org.camunda.hal.cache.config");
    // read config...
    Hal hal = Hal.getInstance();
    hal.registerCache(User.class, new DefaultHalResourceCache(100, 1000*60*15));
  }

  public void contextDestroyed(ServletContextEvent sce) {
    // cleanup !
  }

}
