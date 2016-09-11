package com.github.dbunit.rules.cdi;

import com.github.dbunit.rules.cdi.api.configuration.DBUnit;
import com.github.dbunit.rules.cdi.api.connection.ConnectionHolder;
import com.github.dbunit.rules.cdi.api.dataset.DataSet;
import com.github.dbunit.rules.cdi.api.dataset.DataSetExecutor;
import com.github.dbunit.rules.cdi.api.dataset.ExpectedDataSet;
import com.github.dbunit.rules.cdi.api.leak.LeakHunter;
import com.github.dbunit.rules.configuration.DBUnitConfig;
import com.github.dbunit.rules.configuration.DataSetConfig;
import com.github.dbunit.rules.cdi.connection.ConnectionHolderImpl;
import com.github.dbunit.rules.cdi.dataset.DataSetExecutorImpl;
import com.github.dbunit.rules.cdi.leak.LeakHunterException;
import com.github.dbunit.rules.cdi.leak.LeakHunterFactory;
import org.dbunit.DatabaseUnitException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static com.github.dbunit.rules.cdi.util.EntityManagerProvider.em;
import static com.github.dbunit.rules.cdi.util.EntityManagerProvider.isEntityManagerActive;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
public class DBUnitRule implements TestRule {


    private String currentMethod;

    private DataSetExecutor executor;

    private DBUnitRule() {
    }

    public final static DBUnitRule instance(Connection connection) {
        return instance(new ConnectionHolderImpl(connection));
    }

    public final static DBUnitRule instance(String executorName, Connection connection) {
        return instance(executorName, new ConnectionHolderImpl(connection));
    }

    public final static DBUnitRule instance(ConnectionHolder connectionHolder) {
        return instance(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public final static DBUnitRule instance(String executorName, ConnectionHolder connectionHolder) {
        DBUnitRule instance = new DBUnitRule();
        instance.init(executorName, connectionHolder);
        return instance;
    }


    @Override
    public Statement apply(final Statement statement, final Description description) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                currentMethod = description.getMethodName();
                DataSet dataSet = resolveDataSet(description);
                if (dataSet != null) {
                    final DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);
                    final String datasetExecutorId = dataSetConfig.getExecutorId();
                    DBUnitConfig dbUnitConfig = resolveDBUnitConfig(description);
                    LeakHunter leakHunter = null;
                    boolean executorNameIsProvided = datasetExecutorId != null && !"".equals(datasetExecutorId.trim());
                    if (executorNameIsProvided) {
                        executor = DataSetExecutorImpl.getExecutorById(datasetExecutorId);
                    }
                    try {
                        executor.setDBUnitConfig(dbUnitConfig);
                        executor.createDataSet(dataSetConfig);
                    } catch (final Exception e) {
                        throw new RuntimeException("Could not create dataset due to following error " + e.getMessage(), e);
                    }
                    boolean isTransactional = false;
                    try {
                        isTransactional = dataSetConfig.isTransactional() && isEntityManagerActive();
                        if (isTransactional) {
                            em().getTransaction().begin();
                        }
                        boolean leakHunterActivated = dbUnitConfig.isLeakHunter();
                        int openConnectionsBefore = 0;
                        if (leakHunterActivated) {
                            leakHunter = LeakHunterFactory.from(executor.getConnectionHolder().getConnection());
                            openConnectionsBefore = leakHunter.openConnections();
                        }
                        statement.evaluate();

                        int openConnectionsAfter = 0;
                        if(leakHunterActivated){
                            openConnectionsAfter = leakHunter.openConnections();
                            if(openConnectionsAfter > openConnectionsBefore){
                                throw new LeakHunterException(currentMethod,openConnectionsAfter - openConnectionsBefore);
                            }
                        }

                        if (isTransactional) {
                            em().getTransaction().commit();
                        }
                        performDataSetComparison(description);
                    } catch (Exception e) {
                        if (isTransactional) {
                            em().getTransaction().rollback();
                        }
                        throw e;
                    } finally {

                        if (dataSetConfig != null && dataSetConfig.getExecuteStatementsAfter() != null && dataSetConfig.getExecuteStatementsAfter().length > 0) {
                            try {
                                executor.executeStatements(dataSetConfig.getExecuteStatementsAfter());
                            } catch (Exception e) {
                                LoggerFactory.getLogger(getClass().getName()).error(currentMethod + "() - Could not execute statements after:" + e.getMessage(), e);
                            }
                        }//end execute statements
                        if (dataSetConfig != null && dataSetConfig.getExecuteScriptsAfter() != null && dataSetConfig.getExecuteScriptsAfter().length > 0) {
                            try {
                                for (int i = 0; i < dataSetConfig.getExecuteScriptsAfter().length; i++) {
                                    executor.executeScript(dataSetConfig.getExecuteScriptsAfter()[i]);
                                }
                            } catch (Exception e) {
                                if (e instanceof DatabaseUnitException) {
                                    throw e;
                                }
                                LoggerFactory.getLogger(getClass().getName()).error(currentMethod + "() - Could not execute scriptsAfter:" + e.getMessage(), e);
                            }
                        }//end execute scripts

                        if (dataSetConfig.isCleanAfter()) {
                            executor.clearDatabase(dataSetConfig);
                        }
                    }
                } else {
                    statement.evaluate();
                    performDataSetComparison(description);
                }

            }


        };
    }

    private DataSet resolveDataSet(Description description) {
        DataSet dataSet = description.getAnnotation(DataSet.class);
        if (dataSet == null) {
            dataSet = description.getTestClass().getAnnotation(DataSet.class);
        }
        return dataSet;
    }

    private DBUnitConfig resolveDBUnitConfig(Description description) {
        DBUnit dbUnitConfig = description.getAnnotation(DBUnit.class);
        if (dbUnitConfig == null) {
            dbUnitConfig = description.getTestClass().getAnnotation(DBUnit.class);
        }

        if (dbUnitConfig != null) {
            return DBUnitConfig.from(dbUnitConfig);
        } else {
            return DBUnitConfig.fromGlobalConfig();
        }
    }

    private void performDataSetComparison(Description description) throws DatabaseUnitException {
        ExpectedDataSet expectedDataSet = description.getAnnotation(ExpectedDataSet.class);
        if (expectedDataSet == null) {
            //try to infer from class level annotation
            expectedDataSet = description.getTestClass().getAnnotation(ExpectedDataSet.class);
        }
        if (expectedDataSet != null) {
            executor.compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value()).disableConstraints(true), expectedDataSet.ignoreCols());
        }
    }

    private void init(String name, ConnectionHolder connectionHolder) {
        DataSetExecutorImpl instance = DataSetExecutorImpl.getExecutorById(name);
        if (instance == null) {
            instance = DataSetExecutorImpl.instance(name, connectionHolder);
            DataSetExecutorImpl.getExecutors().put(name, instance);
        } else {
            instance.setConnectionHolder(connectionHolder);
        }
        executor = instance;

    }

    public DataSetExecutor getDataSetExecutor() {
        return executor;
    }


}