import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionFactory {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:localhost:/home/yury/IdeaProjects/Lesson2_7/" +
                    "ru.geekbrains.yukjdev/ServerModule/src/serverdir/dbresurce/.mainDB");
        } catch (SQLException e) {
            throw new RuntimeException("SWW during connection establishing", e);
        }
    }


}