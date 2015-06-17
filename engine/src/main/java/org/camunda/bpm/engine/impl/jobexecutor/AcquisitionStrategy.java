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
public interface AcquisitionStrategy {

  AcquisitionConfiguration reconfigure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs);

  // TODO: could also be handed the exception
  AcquisitionConfiguration reconfigureOnAcquisitionFailure(AcquisitionConfiguration currentConfiguration, AcquiredJobs acquiredJobs);

  AcquisitionConfiguration getInitialConfiguration();

  public static class AcquisitionConfiguration {

    protected int waitTimeBetweenAcquistions;
    protected int numJobsToAcquire;

    public AcquisitionConfiguration(int waitTimeBetweenAcquistions, int numJobsToAcquire) {
      this.waitTimeBetweenAcquistions = waitTimeBetweenAcquistions;
      this.numJobsToAcquire = numJobsToAcquire;
    }

    public int getWaitTimeBetweenAcquistions() {
      return waitTimeBetweenAcquistions;
    }
    public void setWaitTimeBetweenAcquistions(int waitTimeBetweenAcquistions) {
      this.waitTimeBetweenAcquistions = waitTimeBetweenAcquistions;
    }
    public int getNumJobsToAcquire() {
      return numJobsToAcquire;
    }
    public void setNumJobsToAcquire(int numJobsToAcquire) {
      this.numJobsToAcquire = numJobsToAcquire;
    }
  }

}