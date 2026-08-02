package Utils;

import model.User;

public class Session {


    private static User currentUser;



    // Store logged in user
    public static void setUser(User user){

        currentUser = user;

    }



    // Get logged in user
    public static User getUser(){

        return currentUser;

    }



    // Check if someone is logged in
    public static boolean isLoggedIn(){

        return currentUser != null;

    }



    // Logout
    public static void logout(){

        currentUser = null;

    }

}