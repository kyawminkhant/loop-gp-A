package Utils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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


        menu.getItems().add(username);


        profileIcon.setOnMouseClicked(e ->

            menu.show(
                profileIcon,
                e.getScreenX(),
                e.getScreenY()
            )

        );

    }

}
