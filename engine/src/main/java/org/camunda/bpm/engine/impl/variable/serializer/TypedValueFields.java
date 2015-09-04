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

package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fields what provide a typed value. Should be used in combination with
 * {@link ValueFields}.
 *
 * @author Philipp Ossler
 */
public interface TypedValueFields {

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
