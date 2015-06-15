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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;


/**
 * @author Nick Burch
 * @author Daniel Meyer
 */
public class AcquireJobsCmd implements Command<AcquiredJobs> {

  private static Logger log = Logger.getLogger(AcquireJobsCmd.class.getName());

  private final JobExecutor jobExecutor;

  protected AcquiredJobs acquiredJobs;
  protected int acquisitionAttempt;

  public AcquireJobsCmd(JobExecutor jobExecutor) {
    this(jobExecutor, 0);
  }

  public AcquireJobsCmd(JobExecutor jobExecutor, int acquisitionAttempt) {
    this.jobExecutor = jobExecutor;
    this.acquisitionAttempt = acquisitionAttempt;
  }

  public AcquiredJobs execute(CommandContext commandContext) {

    String lockOwner = jobExecutor.getLockOwner();

    acquiredJobs = new AcquiredJobs();

    List<JobEntity> jobs = commandContext.getJobManager().selectLockedJobs(lockOwner, acquisitionAttempt);

    acquiredJobs = new AcquiredJobs();
    for (JobEntity job: jobs) {
      List<String> jobIds = new ArrayList<String>();
      jobIds.add(job.getId());

      acquiredJobs.addJobIdBatch(jobIds);
    }

    if (log.isLoggable(Level.FINE)) {
      StringBuilder sb = new StringBuilder();
      sb.append("Acquired Jobs: ");
      for (JobEntity job : jobs) {
        sb.append(job.getId());
        sb.append(", ");
      }

      log.log(Level.FINE, sb.toString());

    }

    return acquiredJobs;
  }

  protected Date getLockDate(int lockTimeInMillis) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(ClockUtil.getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    return gregorianCalendar.getTime();
  }

  public void setAcquisitionAttempt(int acquisitionAttempt) {
    this.acquisitionAttempt = acquisitionAttempt;
  }

}
