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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.SimpleVariableStore;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * Abstract superclass for all query types.
 *
 * @author Joram Barrez
 */
public abstract class AbstractQuery<T extends Query<?,?>, U> extends ListQueryParameterObject implements Command<Object>, Query<T,U>, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String SORTORDER_ASC = "asc";
  public static final String SORTORDER_DESC = "desc";



  private static enum ResultType {
    LIST, LIST_PAGE, SINGLE_RESULT, COUNT;
  }
  protected transient CommandExecutor commandExecutor;
  protected transient CommandContext commandContext;
  protected String orderBy;

  protected ResultType resultType;
  protected QueryProperty orderProperty;
  protected ExpressionManager expressionManager;

  protected Map<String, String> expressions = new HashMap<String, String>();

  protected VariableScope variableScope;

  protected AbstractQuery() {
  }

  protected AbstractQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public AbstractQuery(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public AbstractQuery<T, U> setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  @SuppressWarnings("unchecked")
  public T orderBy(QueryProperty property) {
    this.orderProperty = property;
    return (T) this;
  }

  public T asc() {
    return direction(Direction.ASCENDING);
  }

  public T desc() {
    return direction(Direction.DESCENDING);
  }

  @SuppressWarnings("unchecked")
  public T direction(Direction direction) {
    ensureNotNull(NotValidException.class, "You should call any of the orderBy methods first before specifying a direction", "orderProperty", orderProperty);
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return (T) this;
  }

  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new NotValidException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }

  @SuppressWarnings("unchecked")
  public U singleResult() {
    this.resultType = ResultType.SINGLE_RESULT;
    if (commandExecutor!=null) {
      return (U) commandExecutor.execute(this);
    }
    return executeSingleResult(Context.getCommandContext());
  }

  @SuppressWarnings("unchecked")
  public List<U> list() {
    this.resultType = ResultType.LIST;
    if (commandExecutor!=null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return evaluateExpressionsAndExecuteList(Context.getCommandContext(), null);
  }

  @SuppressWarnings("unchecked")
  public List<U> listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    this.resultType = ResultType.LIST_PAGE;
    if (commandExecutor!=null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return evaluateExpressionsAndExecuteList(Context.getCommandContext(), new Page(firstResult, maxResults));
  }

  public long count() {
    this.resultType = ResultType.COUNT;
    if (commandExecutor!=null) {
      return (Long) commandExecutor.execute(this);
    }
    return evaluateExpressionsAndExecuteCount(Context.getCommandContext());
  }

  public Object execute(CommandContext commandContext) {
    if (resultType==ResultType.LIST) {
      return evaluateExpressionsAndExecuteList(commandContext, null);
    } else if (resultType==ResultType.SINGLE_RESULT) {
      return executeSingleResult(commandContext);
    } else if (resultType==ResultType.LIST_PAGE) {
      return evaluateExpressionsAndExecuteList(commandContext, null);
    } else {
      return evaluateExpressionsAndExecuteCount(commandContext);
    }
  }

  public long evaluateExpressionsAndExecuteCount(CommandContext commandContext) {
    evaluateExpressions(commandContext);
    return executeCount(commandContext);
  }

  public abstract long executeCount(CommandContext commandContext);

  public List<U> evaluateExpressionsAndExecuteList(CommandContext commandContext, Page page) {
    evaluateExpressions(commandContext);
    return executeList(commandContext, page);
  }

  /**
   * Executes the actual query to retrieve the list of results.
   * @param page used if the results must be paged. If null, no paging will be applied.
   */
  public abstract List<U> executeList(CommandContext commandContext, Page page);

  public U executeSingleResult(CommandContext commandContext) {
    List<U> results = evaluateExpressionsAndExecuteList(commandContext, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ProcessEngineException("Query return "+results.size()+" results instead of max 1");
    }
    return null;
  }

  protected void addOrder(String column, String sortOrder) {
    if (orderBy==null) {
      orderBy = "";
    } else {
      orderBy = orderBy+", ";
    }
    orderBy = orderBy+column+" "+sortOrder;
  }

  public String getOrderBy() {
    if(orderBy == null) {
      return super.getOrderBy();
    } else {
      return orderBy;
    }
  }

  protected void evaluateExpressions(CommandContext commandContext) {
    for (Map.Entry<String, String> entry : expressions.entrySet()) {
      String fieldName = entry.getKey();
      String expression = entry.getValue();
      Object value = getExpressionManager().createExpression(expression).getValue(null);
      try {
        Field field = getClass().getDeclaredField(fieldName);
        getAuthUserId();
        field.set(this, value);
      } catch (NoSuchFieldException e) {
        throw new ProcessEngineException("Unable to find field '" + fieldName + "' on class " + getClass().getCanonicalName());
      } catch (IllegalAccessException e) {
        throw new ProcessEngineException("Unable to access field '" + fieldName + "' on class " + getClass().getCanonicalName());
      }
    }
  }

  protected ExpressionManager getExpressionManager() {
    if (expressionManager == null) {
      expressionManager = new ExpressionManager();
    }
    return expressionManager;
  }

}
