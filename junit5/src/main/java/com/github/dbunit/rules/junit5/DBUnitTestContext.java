package com.github.dbunit.rules.junit5;

import com.github.dbunit.rules.api.dataset.DataSetExecutor;
import com.github.dbunit.rules.api.leak.LeakHunter;
import com.github.dbunit.rules.configuration.DataSetConfig;

public class DBUnitTestContext {
	
	
	private DataSetExecutor executor;
	
	private LeakHunter leakHunter;
	
	private DataSetConfig dataSetConfig;
	
	private int openConnections;


	public DBUnitTestContext DBUnitTestContext(Class<?> aClass){
		return new DBUnitTestContext();
	}

	public DataSetExecutor getExecutor() {
		return executor;
	}

	public DBUnitTestContext setExecutor(DataSetExecutor executor) {
		this.executor = executor;
		return this;
	}

	public LeakHunter getLeakHunter() {
		return leakHunter;
	}

	public DBUnitTestContext setLeakHunter(LeakHunter leakHunter) {
		this.leakHunter = leakHunter;
		return this;
	}


	public DataSetConfig getDataSetConfig() {
		return dataSetConfig;
	}
	
	public DBUnitTestContext setDataSetConfig(DataSetConfig dataSetConfig) {
		this.dataSetConfig = dataSetConfig;
		return this;
	}

	public int getOpenConnections() {
		return openConnections;
	}

	public DBUnitTestContext setOpenConnections(int openConnections) {
		this.openConnections = openConnections;
		return this;
	}

	
	

	
	
}
