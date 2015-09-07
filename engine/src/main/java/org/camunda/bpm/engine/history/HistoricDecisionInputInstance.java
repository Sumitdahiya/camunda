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

package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Represents one input variable of a decision evaluation.
 *
 * @author Philipp Ossler
 */
public interface HistoricDecisionInputInstance extends ValueFields {

  /** The unique identifier of this historic decision input instance. */
  String getId();

  /** The unique identifier of the historic decision instance. */
  String getDecisionInstanceId();

  /** The unique identifier of the clause that the value is assigned for. */
  String getClauseId();

  /** The name of the clause that the value is assigned for. */
  String getClauseName();

  /**
   * Returns the type name of the variable
   */
  String getTypeName();

  /**
   * Returns the value of this variable instance.
   */
  Object getValue();

  /**
   * Returns the {@link TypedValue} for this value.
   */
  TypedValue getTypedValue();

  /**
   * Returns the {@link TypedValue} for this value.
   *
   * @param deserializeValue return only a deserialized value, if <code>true</code>
   */
  TypedValue getTypedValue(boolean deserializeValue);

  /**
   * If the variable value could not be loaded, this returns the error message.
   *
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();

  /** Returns the name of the {@link TypedValueSerializer} what serialize the value.  */
  String getSerializerName();

  /** Set the name of the {@link TypedValueSerializer} what serialize the value. */
  void setSerializerName(String serializerName);
}
