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
package org.camunda.bpm.engine.impl.db.sql;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.NonTransactionalPersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;

/**
 * @author Thorben Lindhauer
 */
public class NonTransactionalDbSqlSession implements NonTransactionalPersistenceSession, Session {

  private static Logger log = Logger.getLogger(NonTransactionalDbSqlSession.class.getName());

  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected SqlSession sqlSession;

  public NonTransactionalDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
        .getSqlSessionFactory()
        .openSession(TransactionIsolationLevel.REPEATABLE_READ);
  }

  public int execute(DbOperation operation) {

    int affectedRows;
    switch(operation.getOperationType()) {

      case INSERT:
        affectedRows = insertEntity((DbEntityOperation) operation);
        break;

      case DELETE:
        affectedRows = deleteEntity((DbEntityOperation) operation);
        break;
      case DELETE_BULK:
        affectedRows =  deleteBulk((DbBulkOperation) operation);
        break;

      case UPDATE:
        affectedRows = updateEntity((DbEntityOperation) operation);
        break;
      case UPDATE_BULK:
        affectedRows = updateBulk((DbBulkOperation) operation);
        break;

      default:
        throw new ProcessEngineException("Unknown operation type");

    }
    sqlSession.commit();
    return affectedRows;
  }

  protected int deleteEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String deleteStatement = dbSqlSessionFactory.getDeleteStatement(dbEntity.getClass());
    ensureNotNull("no delete statement for " + dbEntity.getClass() + " in the ibatis mapping files", "deleteStatement", deleteStatement);

    if(log.isLoggable(Level.FINE)) {
      log.fine("deleting: " + toString(dbEntity));
    }

    // execute the delete
    return executeDelete(deleteStatement, dbEntity);
  }

  protected int deleteBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    if(log.isLoggable(Level.FINE)) {
      log.fine("deleting (bulk): " + statement + " " + parameter);
    }

    return executeDelete(statement, parameter);
  }

  protected int executeDelete(String deleteStatement, Object parameter) {
    // map the statement
    deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
    return sqlSession.delete(deleteStatement, parameter);
  }

  protected int updateEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    String updateStatement = dbSqlSessionFactory.getUpdateStatement(dbEntity);
    ensureNotNull("no update statement for " + dbEntity.getClass() + " in the ibatis mapping files", "updateStatement", updateStatement);

    if (log.isLoggable(Level.FINE)) {
      log.fine("updating: " + toString(dbEntity));
    }

    // execute update
    return executeUpdate(updateStatement, dbEntity);
  }

  protected int updateBulk(DbBulkOperation operation) {
    String statement = operation.getStatement();
    Object parameter = operation.getParameter();

    if(log.isLoggable(Level.FINE)) {
      log.fine("updating (bulk): " + statement + " " + parameter);
    }

    return executeUpdate(statement, parameter);
  }

  protected int executeUpdate(String updateStatement, Object parameter) {
    updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
    return sqlSession.update(updateStatement, parameter);
  }

  protected int insertEntity(DbEntityOperation operation) {

    final DbEntity dbEntity = operation.getEntity();

    // get statement
    String insertStatement = dbSqlSessionFactory.getInsertStatement(dbEntity);
    insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);
    ensureNotNull("no insert statement for " + dbEntity.getClass() + " in the ibatis mapping files", "insertStatement", insertStatement);

    // execute the insert
    return executeInsert(insertStatement, dbEntity);
  }

  protected int executeInsert(String insertStatement, Object parameter) {
    if(log.isLoggable(Level.FINE)) {
      log.fine("inserting: " + toString(parameter));
    }
    int rowsInserted = sqlSession.insert(insertStatement, parameter);

    // set revision of our copy to 1
    if (parameter instanceof HasDbRevision) {
      HasDbRevision versionedObject = (HasDbRevision) parameter;
      versionedObject.setRevision(1);
    }

    return rowsInserted;
  }

  protected String toString(Object object) {
    if(object == null) {
      return "null";
    }
    if(object instanceof DbEntity) {
      DbEntity dbEntity = (DbEntity) object;
      return ClassNameUtil.getClassNameWithoutPackage(dbEntity)+"["+dbEntity.getId()+"]";
    }
    return object.toString();
  }

  public void flush() {
    // nothing to do
  }

  public void close() {
    sqlSession.close();
  }
}
