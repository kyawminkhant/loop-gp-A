package Utils;

import java.io.IOException;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import model.User;

public class ProfileMenu {

    public static void attach(ImageView profileIcon) {

        User user = Session.getUser();

        if(user == null) {
            return;
        }


        ContextMenu menu = new ContextMenu();


        MenuItem username =
                new MenuItem(user.getUsername());

        username.setDisable(true);


        MenuItem logout =
                new MenuItem("Logout");


        logout.setOnAction(e -> {

            AuditLogger.log(
                    "LOGOUT",
                    "User logged out"
            );


            Session.logout();


            try {
            	
                App.setRoot("login");

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        });


        menu.getItems().addAll(
                username,
                new SeparatorMenuItem(),
                logout
        );


        profileIcon.setOnMouseClicked(e ->

            menu.show(
                profileIcon,
                e.getScreenX(),
                e.getScreenY()
            )

        );

    }

}