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

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultAcquisitionStrategy implements AcquisitionStrategy {

  // TODO: these are redundant with job executor configuration
  protected int waitTimeInMillis = 5 * 1000;
  protected int maxJobsPerAcquisition = 3;
  protected float waitIncreaseFactor = 2;
  protected int maxWait = 60 * 1000;

  public AcquisitionConfiguration reconfigure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs) {
    AcquisitionConfiguration newConfig = new AcquisitionConfiguration(currentConfiguration.getWaitTimeBetweenAcquistions(),
        currentConfiguration.getNumJobsToAcquire());

    if (acquiredJobs.size() == 0) {
      newConfig.setWaitTimeBetweenAcquistions(waitTimeInMillis);
    }

    return newConfig;
  }

  public AcquisitionConfiguration getInitialConfiguration() {
    return new AcquisitionConfiguration(0, maxJobsPerAcquisition);
  }

  public AcquisitionConfiguration reconfigureOnAcquisitionFailure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs) {
    int currentWaitTime = currentConfiguration.getWaitTimeBetweenAcquistions();

    if (currentWaitTime == 0) {
      currentWaitTime = waitTimeInMillis;
    } else {
      currentWaitTime *= waitIncreaseFactor;
      if (currentWaitTime > maxWait) {
        currentWaitTime = maxWait;
      }
    }

    return new AcquisitionConfiguration(currentWaitTime, currentConfiguration.getNumJobsToAcquire());
  }
}
