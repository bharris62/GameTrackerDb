import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;

public class Main {
    public static void addNewGame(Connection conn, String game_name, String genre, String platform, int game_year) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?);");

        stmt.setString(1, game_name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4,game_year);
        stmt.execute();
    }

    public static ArrayList<Game> getGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games;");
        ResultSet results = stmt.executeQuery();
        while(results.next()) {
            int id = results.getInt("id");
            String game_name = results.getString("game_name");
            String genre  = results.getString("genre");
            String platform = results.getString("platform");
            int release_year = results.getInt("release_year");
            games.add(new Game(id, game_name, genre, platform, release_year ));
        }

        return games;
    }

    public static void updateGame(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games;");
        ResultSet results = stmt.executeQuery();
    }

    static ArrayList<Game> games = new ArrayList<>();
    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, game_name VARCHAR, genre VARCHAR, platform VARCHAR, release_year INT );");
        System.out.println("GameTracker starting...");

        Spark.init();

        Spark.get("/", (request, response) -> {

            return new ModelAndView(getGames(conn), "home.html");

        }, new MustacheTemplateEngine());

        Spark.post("/create-game", (request, response) -> {
            Session session = request.session();

            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String platform = request.queryParams("platform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            addNewGame(conn, gameName, gameGenre, platform, gameYear);

            response.redirect("/");

            return "";
        });

    }


}
