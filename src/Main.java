import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void addNewGame(Connection conn, String game_name, String genre, String platform, int game_year) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?, ?, ?, ?);");

        stmt.setString(1, game_name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4,game_year);
        stmt.execute();
    }

    static ArrayList<Game> games = new ArrayList<>();
    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, game_name VARCHAR, genre VARCHAR, platform VARCHAR, release_year INT );");
        System.out.println("GameTracker starting...");

        Spark.init();

        Spark.get("/", (request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");

            HashMap m = new HashMap();

            return new ModelAndView(games, "home.html");

        }, new MustacheTemplateEngine());

        Spark.post("/create-game", (request, response) -> {
            Session session = request.session();

            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String platform = request.queryParams("platform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            Game game = new Game(gameName, gameGenre, platform, gameYear);
            games.add(game);

            response.redirect("/");

            return "";
        });

    }


}
