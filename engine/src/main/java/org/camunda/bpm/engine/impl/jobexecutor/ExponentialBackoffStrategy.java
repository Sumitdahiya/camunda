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
  protected int backoffJitter = (int) (Math.random() * 20);
  protected int backoffDecreaseDelay = 5;

  public AcquisitionConfiguration reconfigure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs) {
    BackoffConfiguration backoffConfiguration = (BackoffConfiguration) currentConfiguration;

    int newWaitTime = currentConfiguration.getWaitTimeBetweenAcquistions();
    int newNumJobsToAcquire = currentConfiguration.getNumJobsToAcquire();
    int newNumAcquisitionAttemptsWithoutFailure = backoffConfiguration.getAcquistionAttemptsWithoutFailure() + 1;

    if (acquiredJobs.getNumberOfJobsFailedToLock() > 0 || acquiredJobs.size() == 0) {
      // increase timeout
      if (backoffConfiguration.getWaitTimeBetweenAcquistions() == 0) {
        newWaitTime = initialBackoffTimeMillis + backoffJitter;

      }

      if (newWaitTime * backoffFactor <= maxBackoffTime) {
        newWaitTime = (int) (newWaitTime * backoffFactor);
        newNumJobsToAcquire = (int) (newNumJobsToAcquire * backoffFactor);
        log.fine("Backing off job acquisition. Timeout: " + newWaitTime + " ms; numJobs: " + newNumJobsToAcquire);
      }

      newNumAcquisitionAttemptsWithoutFailure = 0;
    }
    else if (backoffConfiguration.getAcquistionAttemptsWithoutFailure() >= backoffDecreaseDelay){
      // decrease timeout if no failure occurred for n times in a row
      newWaitTime /= backoffFactor;
      if (newWaitTime != backoffConfiguration.getWaitTimeBetweenAcquistions()) {
        // if the wait time has actually changed
        newNumJobsToAcquire = (int) (newNumJobsToAcquire / backoffFactor);
        log.fine("Backing off job acquisition. Timeout: " + newWaitTime + " ms; numJobs: " + newNumJobsToAcquire);
      }

      newNumAcquisitionAttemptsWithoutFailure = 0;
    }

    return new BackoffConfiguration(newWaitTime, newNumJobsToAcquire, newNumAcquisitionAttemptsWithoutFailure);
  }

  public AcquisitionConfiguration getInitialConfiguration() {
    return new BackoffConfiguration(0, initialJobsPerAcquisition, 0);
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

  public int getBackoffJitter() {
    return backoffJitter;
  }

  public void setBackoffJitter(int backoffJitter) {
    this.backoffJitter = backoffJitter;
  }

  public int getBackoffDecreaseDelay() {
    return backoffDecreaseDelay;
  }

  public void setBackoffDecreaseDelay(int backoffDecreaseDelay) {
    this.backoffDecreaseDelay = backoffDecreaseDelay;
  }



  public static class BackoffConfiguration extends AcquisitionConfiguration {

    protected int acquisitionAttemptsWithoutFailure = 0;

    public BackoffConfiguration(int waitTimeBetweenAcquistions, int numJobsToAcquire, int acquisitionAttemptsWithoutFailure) {
      super(waitTimeBetweenAcquistions, numJobsToAcquire);
      this.acquisitionAttemptsWithoutFailure = acquisitionAttemptsWithoutFailure;
    }

    public int getAcquistionAttemptsWithoutFailure() {
      return acquisitionAttemptsWithoutFailure;
    }

    public void setAcquistionAttemptsWithoutFailure(int acquistionAttemptsWithoutFailure) {
      this.acquisitionAttemptsWithoutFailure = acquistionAttemptsWithoutFailure;
    }
  }
}
