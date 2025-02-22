package worker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.sql.*;
import org.json.JSONObject;

class Worker {
  public static void main(String[] args) {
    try {
      Jedis redis = connectToRedis(System.getenv("REDIS_HOST"));
      Connection dbConn = connectToDB();

      System.err.println("Watching vote queue");

      while (true) {
        String voteJSON = redis.blpop(0, "applications").get(1);
        JSONObject voteData = new JSONObject(voteJSON);
        String user_id = voteData.getString("user_id");
        String first_name = voteData.getString("first_name");
        String last_name = voteData.getString("last_name");

        System.err.printf("Processing application by '%s' '%s'\n", first_name, last_name);
        updateVote(dbConn, user_id, first_name, last_name);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void updateVote(Connection dbConn, String user_id, String first_name, String last_name) throws SQLException {
    PreparedStatement insert = dbConn.prepareStatement("INSERT INTO applications (user_id, first_name, last_name) VALUES (?, ?, ?)");
    insert.setString(1, user_id);
    insert.setString(2, first_name);
    insert.setString(3, last_name);

    try {
      insert.executeUpdate();
    } catch (SQLException e) {
      PreparedStatement update = dbConn.prepareStatement("UPDATE applications SET first_name = ?, last_name = ? WHERE id = ?");
      update.setString(1, first_name);
      update.setString(2, last_name);
      update.setString(3, user_id);
      update.executeUpdate();
    }
  }

  static Jedis connectToRedis(String host) {
    Jedis conn = new Jedis(host, 6379);

    while (true) {
      try {
        conn.keys("*");
        break;
      } catch (JedisConnectionException e) {
        System.err.println("Waiting for redis");
        sleep(1000);
      }
    }

    System.err.println("Connected to redis");
    return conn;
  }

  static Connection connectToDB() throws SQLException {
    Connection conn = null;

    try {

      Class.forName("org.postgresql.Driver");
      String url = "jdbc:postgresql://" + System.getenv("POSTGRES_HOST") + ':' + System.getenv("POSTGRES_PORT")
          + "/" + System.getenv("POSTGRES_DB");

      while (conn == null) {
        try {
          conn = DriverManager.getConnection(url, System.getenv("POSTGRES_USER"),
              System.getenv("POSTGRES_PASSWORD"));
        } catch (SQLException e) {
          System.err.println("Waiting for db");
          sleep(1000);
        }
      }

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.err.println("Connected to db");
    return conn;
  }

  static void sleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      System.exit(1);
    }
  }
}
