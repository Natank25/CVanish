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
        InputOffer offer = InputOffer.createFromJson(voteData);
        System.err.printf("Processing application of '%s' '%s'\n", offer.data.get(OfferData.LAST_NAME), offer.data.get(OfferData.FIRST_NAME));
        updateVote(dbConn, offer);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void updateVote(Connection dbConn, InputOffer offer) throws SQLException {
    PreparedStatement insert = dbConn.prepareStatement(offer.createInsertStatement());

    try {
      insert.executeUpdate();
    } catch (SQLException e) {
      PreparedStatement update = dbConn.prepareStatement(offer.createUpdateStatement());
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
