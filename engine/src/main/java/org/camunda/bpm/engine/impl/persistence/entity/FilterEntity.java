/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONException;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public class FilterEntity implements Filter, Serializable, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  public final static Map<String, JsonObjectConverter<?>> queryConverter = new HashMap<String, JsonObjectConverter<?>>();

  static {
    queryConverter.put(EntityTypes.TASK, new JsonTaskQueryConverter());
  }

  protected String id;
  protected String resourceType;
  protected String name;
  protected String owner;
  protected AbstractQuery<?, ?> query;
  protected Map<String, Object> properties;
  protected int revision = 0;

  protected FilterEntity() {

  }

  public FilterEntity(String resourceType) {
    setResourceType(resourceType);
    setQueryInternal("{}");
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Filter setResourceType(String resourceType) {
    ensureNotEmpty(NotValidException.class, "Filter resource type must not be null or empty", "resourceType", resourceType);
    ensureNull(NotValidException.class, "Cannot overwrite filter resource type", "resourceType", this.resourceType);

    this.resourceType = resourceType;
    return this;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getName() {
    return name;
  }

  public Filter setName(String name) {
    ensureNotEmpty(NotValidException.class, "Filter name must not be null or empty", "name", name);
    this.name = name;
    return this;
  }

  public String getOwner() {
    return owner;
  }

  public Filter setOwner(String owner) {
    this.owner = owner;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T extends Query<?, ?>> T getQuery() {
    return (T) query;
  }

  public String getQueryInternal() {
    JsonObjectConverter<Object> converter = getConverter();
    return converter.toJson(query);
  }

  public <T extends Query<?, ?>> Filter setQuery(T query) {
    ensureNotNull(NotValidException.class, "query", query);
    this.query = (AbstractQuery<?, ?>) query;
    return this;
  }

  public void setQueryInternal(String query) {
    ensureNotNull(NotValidException.class, "query", query);
    JsonObjectConverter<Object> converter = getConverter();
    this.query = (AbstractQuery<?, ?>) converter.toObject(new JSONObject(query));
  }

  public Map<String, Object> getProperties() {
    if (properties != null) {
      return JsonUtil.jsonObjectAsMap(new JSONObject(properties));
    }
    else {
      return null;
    }
  }

  public String getPropertiesInternal() {
    return new JSONObject(properties).toString();
  }

  public Filter setProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public void setPropertiesInternal(String properties) {
    if (properties != null) {
      JSONObject jsonObject = new JSONObject(properties);
      this.properties = JsonUtil.jsonObjectAsMap(jsonObject);
    }
    else {
      this.properties = null;
    }
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public Filter extend(Map<String, Object> extendingQuery) {
    ensureNotEmpty(NotValidException.class, "extendingQuery", extendingQuery);
    try {
      JSONObject json = new JSONObject(extendingQuery);
      Query<?, ?> query  = (Query<?, ?>) getConverter().toObject(json);
      return extend(query);
    }
    catch (JSONException e) {
      throw new NotValidException("Query string has to be a JSON object", e);
    }
  }

  public <T extends Query<?, ?>> Filter extend(T extendingQuery) {
    ensureNotNull(NotValidException.class, "extendingQuery", extendingQuery);

    // convert extendingQuery to JSON
    JsonObjectConverter<T> converter = getConverter();
    JSONObject extendingQueryJson = converter.toJsonObject(extendingQuery);

    return extendQuery(extendingQueryJson);
  }

  @SuppressWarnings("unchecked")
  protected Filter extendQuery(JSONObject extendingQuery) {
    // parse query to JSON
    JSONObject queryJson = new JSONObject(query);

    // merge queries by keys
    Iterator<String> extendingKeys = extendingQuery.keys();
    while (extendingKeys.hasNext()) {
      String key = extendingKeys.next();
      if (key.equals("orderBy") && queryJson.has("orderBy")) {
        // if extending query also contains a orderBy attribute we append it to the existing
        String orderBy = queryJson.getString("orderBy") + ", " + extendingQuery.get(key);
        queryJson.put(key, orderBy);
      }
      else {
        queryJson.put(key, extendingQuery.get(key));
      }
    }

    // create copy of the filter with the new query
    FilterEntity copy = copyFilter();
    copy.setQueryInternal(queryJson.toString());

    return copy;
  }

  @SuppressWarnings("unchecked")
  protected <T> JsonObjectConverter<T> getConverter() {
    JsonObjectConverter<T> converter = (JsonObjectConverter<T>) queryConverter.get(resourceType);
    if (converter != null) {
      return converter;
    }
    else {
      throw new ProcessEngineException("Unsupported resource type '" + resourceType + "'");
    }
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", this.name);
    persistentState.put("owner", this.owner);
    persistentState.put("query", this.query);
    persistentState.put("properties", this.properties);
    return persistentState;
  }

  protected FilterEntity copyFilter() {
    FilterEntity copy = new FilterEntity(getResourceType());
    copy.setName(getName());
    copy.setOwner(getOwner());
    copy.setQueryInternal(getQueryInternal());
    copy.setPropertiesInternal(getPropertiesInternal());
    return copy;
  }

}
