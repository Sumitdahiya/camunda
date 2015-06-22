package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.zip.UnrecognizedExtraField;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquisitionStrategy.AcquisitionConfiguration;


/**
 * <p>{@link AcquireJobsRunnable} able to serve multiple process engines.</p>
 *
 * @author Daniel Meyer
 */
public class SequentialJobAcquisitionRunnable extends AcquireJobsRunnable {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  public SequentialJobAcquisitionRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
  }

  // TODO: idea for rewrite
  // * acquistion context holds: acquisition configuration + acquisition result
  // * acquisition configuration is passed to an exchangeable Callable that retrieves jobs
  //   (Callable since it may have more than one command)
  // * rejected jobs handler is passed the acquisition context (??) such that it may add
  //   rejected jobs to the acquisition result
  // * configuring the job executor consists of three parts (could be encapsulated by a container class)
  //   * acquisition strategy
  //   * rejected jobs handler
  //   * acquisition callable
  // * the context is passed between all these objects
  // * the acquisition runnable itself can be exchanged at any time to re-implement the wiring
  //   of these components

  public synchronized void run() {
    log.info(jobExecutor.getName() + " starting to acquire jobs");

    int processEngineLoopCounter = 0;
    boolean jobExecutionFailed = false;

    AcquisitionStrategy acquisitionStrategy = jobExecutor.getAcquisitionStrategy();
    AcquisitionConfiguration currentConfiguration = acquisitionStrategy.getInitialConfiguration();
    List<List<String>> additionalBatches = new ArrayList<List<String>>();

    while (!isInterrupted) {
      ProcessEngineImpl currentProcessEngine = null;

      AcquiredJobs jobsAcquiredForAllEngines = new AcquiredJobs();

      try {
        List<ProcessEngineImpl> registeredProcessEngines = jobExecutor.getProcessEngines();

        synchronized (registeredProcessEngines) {
          if (registeredProcessEngines.size() > 0) {
            if (registeredProcessEngines.size() <= processEngineLoopCounter) {
              processEngineLoopCounter = 0;
              isJobAdded = false;
            }
            currentProcessEngine = registeredProcessEngines.get(processEngineLoopCounter);
            processEngineLoopCounter++;
          }
        }

      } catch (Exception e) {
        log.log(Level.SEVERE, "exception while determining next process engine: " + e.getMessage(), e);
      }

      jobExecutionFailed = false;

      if (currentProcessEngine != null) {

        try {
          final CommandExecutor commandExecutor = currentProcessEngine.getProcessEngineConfiguration()
              .getCommandExecutorTxRequired();

          jobExecutor.logAcquisitionAttempt(currentProcessEngine);
          int acquisitionAttempt = jobExecutor.getNextAcquisitionId();

          int numAcquiredJobs = commandExecutor.execute(new LockJobsCmd(jobExecutor,
              currentConfiguration.getNumJobsToAcquire(), acquisitionAttempt));

          jobExecutor.logAcquiredJobs(currentProcessEngine, numAcquiredJobs);
          jobExecutor.logAcquisitionFailureJobs(currentProcessEngine, currentConfiguration.getNumJobsToAcquire() - numAcquiredJobs);

          AcquiredJobs acquiredJobs = new AcquiredJobs();
          if (numAcquiredJobs > 0) {
            acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor, acquisitionAttempt));
          }

          for (int i = 0; i < additionalBatches.size(); i++) {
            acquiredJobs.addJobIdBatch(additionalBatches.remove(0));
          }

          acquiredJobs.setNumberOfJobsFailedToLock(currentConfiguration.getNumJobsToAcquire() - numAcquiredJobs);

          for (List<String> jobIds : acquiredJobs.getJobIdBatches()) {
            jobExecutor.executeJobs(jobIds, currentProcessEngine);
          }

          // add number of jobs which we attempted to acquire but could not obtain a lock for -> do not wait if we could not acquire jobs.
          jobsAcquiredForAllEngines.addAll(acquiredJobs);

        } catch (Exception e) {
          log.log(Level.SEVERE, "exception during job acquisition: " + e.getMessage(), e);

          jobExecutionFailed = true;
        }
      }

      List<List<String>> unprocessedBatches = retrieveUnprocessedBatches();
      additionalBatches.addAll(unprocessedBatches);
      jobsAcquiredForAllEngines.setUnprocessedBatches(additionalBatches);

      if (!jobExecutionFailed) {
        currentConfiguration = acquisitionStrategy.reconfigure(currentConfiguration, jobsAcquiredForAllEngines);
      }
      else {
        currentConfiguration = acquisitionStrategy.reconfigureOnAcquisitionFailure(currentConfiguration, jobsAcquiredForAllEngines);
      }

      millisToWait = currentConfiguration.getWaitTimeBetweenAcquistions();

      if (millisToWait > 0) { //not relevant for benchmark: && (!isJobAdded)) {

        try {
          log.fine("job acquisition thread sleeping for " + millisToWait + " millis");
          synchronized (MONITOR) {
            if(!isInterrupted) {
              isWaiting.set(true);
              MONITOR.wait(millisToWait);
            }
          }
          log.fine("job acquisition thread woke up");
          isJobAdded = false;
        } catch (InterruptedException e) {
          log.fine("job acquisition wait interrupted");
        } finally {
          isWaiting.set(false);
        }
      }

    }
    log.info(jobExecutor.getName() + " stopped job acquisition");
  }

  public boolean isJobAdded() {
    return isJobAdded;
  }

}
