package org.stubby.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 4:45 PM
 */
public class Repository {

   private final static Logger logger = LoggerFactory.getLogger(Repository.class);

   private static Connection dbConnection = null;
   private final List<StubHttpLifecycle> httpLifecycles;

   public Repository(final List<StubHttpLifecycle> httpLifecycles) throws ClassNotFoundException, SQLException {
      this.httpLifecycles = httpLifecycles;
      initConnection();
      createEndpointTable();
   }

   private void createEndpointTable() throws SQLException {
      final Statement st = dbConnection.createStatement();
      st.execute("CREATE TABLE ENDPOINTS.PERSON (ID INT PRIMARY KEY, FIRSTNAME VARCHAR(64), LASTNAME VARCHAR(64)) NOT PERSISTENT");
      st.execute("INSERT INTO ENDPOINTS.PERSON (ID, FIRSTNAME, LASTNAME) VALUES (1, 'John-1', 'Doe-1')");
      st.execute("INSERT INTO ENDPOINTS.PERSON (ID, FIRSTNAME, LASTNAME) VALUES (2, 'John-2', 'Doe-2')");
      st.execute("INSERT INTO ENDPOINTS.PERSON (ID, FIRSTNAME, LASTNAME) VALUES (3, 'John-3', 'Doe-3')");
      st.close();
   }

   private void initConnection() throws ClassNotFoundException, SQLException {
      Class.forName("org.h2.Driver");
      final String url = "jdbc:h2:mem:endpoints;";
      final String init = "INIT=CREATE SCHEMA IF NOT EXISTS ENDPOINTS\\;SET SCHEMA ENDPOINTS;";
      final String options = "DB_CLOSE_DELAY=-1\\;SET DEFAULT_TABLE_TYPE=MEMORY;";
      if (dbConnection == null) {
         dbConnection = DriverManager.getConnection(url + init + options, "sa", "");
         logger.info("Opened DB connection at " + url);
      }
   }

   public final String executeQueryWithParams(final String... params) {
      final String query = String.format(Queries.SELECT_FROM_PERSON_WHERE, (Object[]) params);
      return executeQuery(query);
   }

   public final String executeQuery(final String rawSQL) {

      final StringBuilder builder = new StringBuilder();

      try {

         final Statement statement = dbConnection.createStatement();
         final ResultSet resultSet = statement.executeQuery(rawSQL);

         while (resultSet.next()) {
            final String id = resultSet.getString("ID");
            final String firstname = resultSet.getString("FIRSTNAME");
            final String lastname = resultSet.getString("LASTNAME");
            builder.append(id).append(" :: ").append(firstname).append(" :: ").append(lastname).append("\n");
         }
         statement.close();

         if (!builder.toString().isEmpty()) {
            return builder.toString();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return String.format("No results found in database for query [%s]", rawSQL);
   }
}
