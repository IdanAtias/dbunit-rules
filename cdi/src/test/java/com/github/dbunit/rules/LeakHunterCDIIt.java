package com.github.dbunit.rules;

import com.github.dbunit.rules.api.configuration.DBUnit;
import com.github.dbunit.rules.api.dataset.DataSet;
import com.github.dbunit.rules.cdi.api.UsingDataSet;
import com.github.dbunit.rules.connection.ConnectionHolderImpl;
import com.github.dbunit.rules.leak.LeakHunterException;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiTestRunner.class)
@DBUnit(leakHunter = true)
public class LeakHunterCDIIt {

	@Inject
	EntityManager em;


	@Rule
	public ExpectedException exception = ExpectedException.none();


	private Connection getConnection() {
		try {
			return DriverManager.getConnection("jdbc:hsqldb:mem:test-cdi;DB_CLOSE_DELAY=-1", "sa", "");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void createLeak() throws SQLException {
		Connection connection = getConnection();
		try (Statement stmt = connection.createStatement()) {
			ResultSet resultSet = stmt.executeQuery("select count(*) from user");
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt(1)).isEqualTo(2);
		}
	}

	private void createAndCloseConnection() throws SQLException {
		Connection connection = getConnection();
		try (Statement stmt = connection.createStatement()) {
			ResultSet resultSet = stmt.executeQuery("select count(*) from user");
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt(1)).isEqualTo(2);
		} finally {
			connection.close();
		}
	}


	@Test(expected = LeakHunterException.class)
	@UsingDataSet("yml/users.yml")
	public void shouldFindConnectionLeak() throws SQLException {
		createLeak();
	}

	@Test(expected = LeakHunterException.class)
	@UsingDataSet("yml/users.yml")
	public void shouldFindTwoConnectionLeaks() throws SQLException {
		createLeak();
		createLeak();
	}


	@Test
	@UsingDataSet("yml/users.yml")
	@DBUnit(leakHunter = false)
	public void shouldNotFindConnectionLeakWhenHunterIsDisabled() throws SQLException {
		createLeak();
	}

	@Test
	@UsingDataSet("yml/users.yml")
	public void shouldNotFindConnectionLeakWhenConnectionIsClosed() throws SQLException {
		createAndCloseConnection();
	}



}