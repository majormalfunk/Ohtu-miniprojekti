
package ohtu;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import ohtu.authentication.AuthenticationService;
import ohtu.data_access.Database;
import ohtu.data_access.FileUserDao;
import ohtu.data_access.UserDao;
import ohtu.data_access.VinkkiDao;
import ohtu.util.CreationStatus;
import spark.ModelAndView;
import static spark.Spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Main {
    
    static String LAYOUT = "templates/layout.html";
  
    static UserDao dao;
    static AuthenticationService authService;
    
    public static void main(String[] args) throws SQLException {
        port(findOutPort());
        Database database = new Database("jdbc:sqlite:vinkit.db");
              
        VinkkiDao vinkit = new VinkkiDao(database);
        System.out.println(vinkit.findOne(1).getOtsikko());
        
        get("/", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            model.put("template", "templates/index.html");
            return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());            
        
         get("/vinkit", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("vinkit", vinkit.findAll());
            return new ModelAndView(map, "vinkit");
        }, new ThymeleafTemplateEngine());
        
               
        get("/ohtu", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            model.put("template", "templates/ohtu.html");
            return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());            
        
        get("/welcome", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            model.put("template", "templates/welcome.html");
            return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());            
        
        get("/login", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            model.put("template", "templates/login.html");
            return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());     
        
        get("/user", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            model.put("template", "templates/user.html");
            return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());            
        
        post("/login", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            
            if ( !authenticationService().logIn(username, password) ) {
                model.put("error", "invalid username or password");
                model.put("template", "templates/login.html");
                return new ModelAndView(model, LAYOUT);
            }
                
           response.redirect("/ohtu");
           return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());
        
        post("/user", (request, response) -> {
            HashMap<String, String> model = new HashMap<>();
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            String passwordConf = request.queryParams("passwordConfirmation");
            
            CreationStatus status = authenticationService().createUser(username, password, passwordConf);
            
            if ( !status.isOk()) {
                model.put("error", String.join(",  ", status.errors()));
                model.put("template", "templates/user.html");
                return new ModelAndView(model, LAYOUT);
            }
                
           response.redirect("/welcome");
           return new ModelAndView(model, LAYOUT);
        }, new VelocityTemplateEngine());
    }

    public static void setDao(UserDao dao) {
        Main.dao = dao;
    }
    
    public static AuthenticationService authenticationService(){
        if ( dao==null ) {
          dao = new FileUserDao("salasanat.txt");  
        } if (authService==null) {
           authService = new AuthenticationService(dao); 
        }

        return authService;
    }    
      
    static int findOutPort() {
        if ( portFromEnv!=null ) {
            return Integer.parseInt(portFromEnv);
        }
        
        return 4567;
    }
    
    static String portFromEnv = new ProcessBuilder().environment().get("PORT");
    
    static void setEnvPort(String port){
        portFromEnv = port;
    }
}
