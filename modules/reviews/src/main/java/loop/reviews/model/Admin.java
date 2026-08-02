package loop.reviews.model;

/**
 * Admin specialisation of {@link User} (role = ADMIN), matching the
 * generalisation shown in the Assessment A class diagram.
 */
public class Admin extends User {
    public Admin() { setRole("ADMIN"); }
    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "ADMIN", null);
    }
}
