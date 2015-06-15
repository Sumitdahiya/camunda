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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Date;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.jobexecutor.LockResult;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class LockProcessInstanceCmd implements Command<LockResult> {

  private static Logger log = Logger.getLogger(LockProcessInstanceCmd.class.getName());

  protected String processInstanceId;
  protected Date lockDate;

  protected LockResult result = new LockResult();

  public LockProcessInstanceCmd(String processInstanceId, Date lockDate) {
    this.processInstanceId = processInstanceId;
    this.lockDate = lockDate;
  }

  public LockResult execute(CommandContext commandContext) {

    int affectedRows =
        commandContext.getExecutionManager().tryAcquireProcessInstanceLock(processInstanceId, lockDate);

    boolean successful = affectedRows > 0;

    if (successful) {
      // Update in cache
      // TODO: this should also update the cached persistent state or else
      // there will always be another update statement for the process instance at the end of the transaction
      JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
      if (jobExecutorContext != null && jobExecutorContext.getEntityCache() != null) {
        CachedDbEntity cachedDbEntity = jobExecutorContext.getEntityCache()
            .getCachedEntity(ExecutionEntity.class, processInstanceId);
        if (cachedDbEntity != null) {
          ExecutionEntity processInstance = (ExecutionEntity) cachedDbEntity.getEntity();
          processInstance.setLockExpirationTime(lockDate);
          processInstance.setRevision(processInstance.getRevision() + 1);
        }

      }
    }

    result.setSuccessful(successful);

    log.fine("Process instance " + processInstanceId + " locked: " + successful);


    return result;
  }


}
