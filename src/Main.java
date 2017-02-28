import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    public static void main(String[] args) {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, text VARCHAR, is_done BOOLEAN);");
        System.out.println("GameTracker starting...");
        Spark.init();
        Spark.get("/", (request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");
            User user = users.get(name);

            HashMap m = new HashMap();
            if(user == null) {
                return new ModelAndView(m, "login.html");
            }else{
                return new ModelAndView(user, "home.html");
            }
        }, new MustacheTemplateEngine());

        Spark.post("/create-game", (request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");
            User user = users.get(name);
            if (user == null) {
                throw new Exception("User is not logged in");
            }

            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String platform = request.queryParams("platform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            Game game = new Game(gameName, gameGenre, platform, gameYear);
            user.games.add(game);

            response.redirect("/");

            return "";
        });

        Spark.post("/logout", (request, response)->{
            Session session = request.session();
            session.invalidate();
            response.redirect("/");
            return "";
        });

        Spark.post("/login", (request, response)->{
            String name = request.queryParams("loginName");
            users.putIfAbsent(name, new User(name));  //simple way to register new user.

            Session session = request.session();
            session.attribute("userName", name);  //new or old user gets a session to be tracker from.
            response.redirect("/");

            return "";
        });
    }


}
