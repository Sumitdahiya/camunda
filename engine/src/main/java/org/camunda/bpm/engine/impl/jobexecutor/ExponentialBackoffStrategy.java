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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.logging.Logger;

import org.python.modules.math;


/**
 * @author Thorben Lindhauer
 *
 */
public class ExponentialBackoffStrategy implements AcquisitionStrategy {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  // TODO: these are redundant with job executor configuration
  protected int initialJobsPerAcquisition = 3;
  protected int initialBackoffTimeMillis = 10;
  protected float backoffFactor = 2;
  protected int maxBackoffTime = 50;
  protected int maxBackoffJitter = 3;
  protected int backoffDecreaseDelay = 5;
  protected int maxBackoffLevel = 3;

  public AcquisitionConfiguration reconfigure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs) {
    BackoffConfiguration backoffConfiguration = (BackoffConfiguration) currentConfiguration;

    int newWaitTime = currentConfiguration.getWaitTimeBetweenAcquistions();
    int newNumJobsToAcquire = currentConfiguration.getNumJobsToAcquire();
    int newNumAcquisitionAttemptsWithoutFailure = backoffConfiguration.getAcquistionAttemptsWithoutFailure() + 1;

    int newBackoffLevel = backoffConfiguration.getBackoffLevel();

    if (acquiredJobs.getNumberOfJobsFailedToLock() > 0 || acquiredJobs.size() == 0) {
      // increase timeout
      if (newBackoffLevel < maxBackoffLevel) {
        newBackoffLevel++;
      }
      newNumAcquisitionAttemptsWithoutFailure = 0;
    }
    else if (backoffConfiguration.getAcquistionAttemptsWithoutFailure() >= backoffDecreaseDelay && newBackoffLevel > 0) {
      // decrease timeout if no failure occurred for n times in a row
      newBackoffLevel--;
      newNumAcquisitionAttemptsWithoutFailure = 0;
    }

    newNumJobsToAcquire = (int) (initialJobsPerAcquisition * Math.pow(backoffFactor, newBackoffLevel));

    if (backoffFactor > 0) {
      newWaitTime = (int) (initialBackoffTimeMillis * Math.pow(backoffFactor, newBackoffLevel - 1));
      newWaitTime = newWaitTime + backoffConfiguration.getJitter();
    }
    else {
      newWaitTime = 0;
    }


    newNumJobsToAcquire = Math.max(0, newNumJobsToAcquire - acquiredJobs.getUnprocessedBatches().size());
    log.fine("Backing off job acquisition. Timeout: " + newWaitTime + " ms; numJobs: " + newNumJobsToAcquire);

    return new BackoffConfiguration(newWaitTime, newNumJobsToAcquire,
        newNumAcquisitionAttemptsWithoutFailure, newBackoffLevel, backoffConfiguration.getJitter());
  }

  public AcquisitionConfiguration getInitialConfiguration() {
    return new BackoffConfiguration(0, initialJobsPerAcquisition, 0, 0, (int) (Math.random() * maxBackoffJitter));
  }

  public AcquisitionConfiguration reconfigureOnAcquisitionFailure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs) {
    return currentConfiguration;
  }

  public int getInitialJobsPerAcquisition() {
    return initialJobsPerAcquisition;
  }

  public void setInitialJobsPerAcquisition(int initialJobsPerAcquisition) {
    this.initialJobsPerAcquisition = initialJobsPerAcquisition;
  }

  public int getInitialBackoffTimeMillis() {
    return initialBackoffTimeMillis;
  }

  public void setInitialBackoffTimeMillis(int initialBackoffTimeMillis) {
    this.initialBackoffTimeMillis = initialBackoffTimeMillis;
  }

  public float getBackoffFactor() {
    return backoffFactor;
  }

  public void setBackoffFactor(float backoffFactor) {
    this.backoffFactor = backoffFactor;
  }

  public int getMaxBackoffTime() {
    return maxBackoffTime;
  }

  public void setMaxBackoffTime(int maxBackoffTime) {
    this.maxBackoffTime = maxBackoffTime;
  }

  public int getMaxBackoffJitter() {
    return maxBackoffJitter;
  }

  public void setMaxBackoffJitter(int maxBackoffJitter) {
    this.maxBackoffJitter = maxBackoffJitter;
  }

  public int getBackoffDecreaseDelay() {
    return backoffDecreaseDelay;
  }

  public void setBackoffDecreaseDelay(int backoffDecreaseDelay) {
    this.backoffDecreaseDelay = backoffDecreaseDelay;
  }



  public static class BackoffConfiguration extends AcquisitionConfiguration {

    protected int backoffLevel = 0;
    protected int acquisitionAttemptsWithoutFailure = 0;
    protected int jitter = 0;

    public BackoffConfiguration(int waitTimeBetweenAcquistions, int numJobsToAcquire,
        int acquisitionAttemptsWithoutFailure, int backoffLevel, int jitter) {
      super(waitTimeBetweenAcquistions, numJobsToAcquire);
      this.acquisitionAttemptsWithoutFailure = acquisitionAttemptsWithoutFailure;
      this.backoffLevel = backoffLevel;
      this.jitter = jitter;
    }

    public int getBackoffLevel() {
      return backoffLevel;
    }

    public void setBackoffLevel(int backoffLevel) {
      this.backoffLevel = backoffLevel;
    }

    public int getAcquistionAttemptsWithoutFailure() {
      return acquisitionAttemptsWithoutFailure;
    }

    public void setAcquistionAttemptsWithoutFailure(int acquistionAttemptsWithoutFailure) {
      this.acquisitionAttemptsWithoutFailure = acquistionAttemptsWithoutFailure;
    }

    public int getJitter() {
      return jitter;
    }

    public void setJitter(int jitter) {
      this.jitter = jitter;
    }
  }
}
