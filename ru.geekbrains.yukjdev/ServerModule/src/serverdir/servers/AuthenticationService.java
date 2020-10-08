import java.sql.*;

public class AuthenticationService {
   private static Connection connection;
   private static PreparedStatement statement;

   public User findByLoginAndPassword (String login, String password) {
           connection = DBConnectionFactory.getConnection();
           try {
               ResultSet rs;
               try (PreparedStatement statement = connection.prepareStatement(
                       "SELECT * FROM users WHERE login = ? AND password = ?")) {
                   statement.setString(1, login);
                   statement.setString(2, password);
                   rs = statement.executeQuery();
               }
               if (rs.next()) {
                   return new User(
                           rs.getLong("id"),
                           rs.getString("name"),
                           rs.getString("login"),
                           rs.getString("password")
                   );
               }
               return null;
           } catch (SQLException e) {
               throw new RuntimeException("SWW", e);
           } finally {
               closeConnection(connection);
           }

   }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setNewUsers(String login, String pass, String nick) {
       if (findByLoginAndPassword(login, pass).getId() != 0) {
           int hash = pass.hashCode();
           String sql = String.format("INSERT INTO users (name, login, password) VALUES ('%s', '%s', '%d')", nick, login, hash);

           try {
               boolean rs = statement.execute(sql);

           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

    }

    static int getBlackListUserById(int _nickId) {
        String idBlackListUser = String.format("SELECT id_user_denny FROM blacklist where id_user = '%s'", _nickId);

        try {
            ResultSet rs = statement.executeQuery(idBlackListUser);

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    static int getIdByNick(String _nick) {
        String idNick = String.format("SELECT id FROM users where nickname = '%s'", _nick);
        try {
            ResultSet rs = statement.executeQuery(idNick);

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    static String getNickByLoginAndPass(String login, String pass) {
        String sql = String.format("SELECT name FROM users where login = '%s' and password = '%s'", login, pass);

        try {
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                String str = rs.getString(1);
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}
