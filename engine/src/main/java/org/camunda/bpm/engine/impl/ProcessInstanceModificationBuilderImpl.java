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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.AbstractInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationBuilderImpl implements ProcessInstanceModificationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String processInstanceId;

  protected List<AbstractProcessInstanceModificationCommand> operations = new ArrayList<AbstractProcessInstanceModificationCommand>();

  protected AbstractInstantiationCmd currentInstantiation;

  public ProcessInstanceModificationBuilderImpl(CommandExecutor commandExecutor, String processInstanceId) {
    this(processInstanceId);
    this.commandExecutor = commandExecutor;
  }

  public ProcessInstanceModificationBuilderImpl(CommandContext commandContext, String processInstanceId) {
    this(processInstanceId);
    this.commandContext = commandContext;
  }

  public ProcessInstanceModificationBuilderImpl(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
  }

  public ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId) {
    ensureNotNull("activityInstanceId", activityInstanceId);
    operations.add(new ActivityCancellationCmd(processInstanceId, activityInstanceId));
    return this;
  }

  public ProcessInstanceModificationBuilder startBeforeActivity(String activityId) {
    ensureNotNull("activityId", activityId);
    currentInstantiation = new ActivityInstantiationCmd(processInstanceId, activityId);
    operations.add(currentInstantiation);
    return this;
  }

  public ProcessInstanceModificationBuilder startAfterActivity(String activityId) {
    ensureNotNull("activityId", activityId);
    // TODO Implement
//    currentInstantiation = new ActivityInstantiationAfterCmd(processInstanceId, activityId);
//    operations.add(currentInstantiation);
    return this;
  }

  public ProcessInstanceModificationBuilder startTransition(String transitionId) {
    ensureNotNull("transitionId", transitionId);
    currentInstantiation = new TransitionInstantiationCmd(processInstanceId, transitionId);
    operations.add(currentInstantiation);
    return this;
  }


  public ProcessInstanceModificationBuilder setVariable(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variable", currentInstantiation);

    currentInstantiation.addVariable(name, value);
    return this;
  }

  public ProcessInstanceModificationBuilder setVariableLocal(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variableLocal", currentInstantiation);

    currentInstantiation.addVariableLocal(name, value);
    return this;
  }

  public void execute() {
    ModifyProcessInstanceCmd cmd = new ModifyProcessInstanceCmd(this);
    if (commandExecutor != null) {
      commandExecutor.execute(cmd);
    } else {
      cmd.execute(commandContext);
    }
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public CommandContext getCommandContext() {
    return commandContext;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public List<AbstractProcessInstanceModificationCommand> getModificationOperations() {
    return operations;
  }


}
