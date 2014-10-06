package org.camunda.bpm.integrationtest.functional.scriptengine;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * Created by hawky4s on 01.10.14.
 */
public class GroovyJarScriptEngineSupport extends AbstractScriptEngineSupportTest {

  protected static final String EXAMPLE_SCRIPT =
      "import org.camunda.bpm.integrationtest.functional.scriptengine.SomeClass; " +
      "def someClass = new SomeClass(); " +
      "assert someClass.echo(); " +
      "execution.setVariable('foo', S('<bar/>').name()); " +
      "println 'YES IT WORKS!!!'";

  @Deployment
  public static WebArchive createProcessApplication() {
    return initWebArchiveDeployment("test.war", "singleEngineNoScriptCompilation.xml")
        .addClass(AbstractScriptEngineSupportTest.class)
        .addAsResource(createScriptTaskProcess("groovy", EXAMPLE_SCRIPT), "process.bpmn20.xml")
        .addAsLibrary(createCustomJar());
  }

  protected static JavaArchive createCustomJar() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(SomeClass.class);
  }

  @Test
  @Override
  public void shouldSetVariable() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    ProcessEngine engine = engineService.getProcessEngine("groovy");
    runtimeService = engine.getRuntimeService();
    processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_ID).getId();
  }

}
