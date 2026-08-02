package loop.reviews.model;

/**
 * Customer specialisation of {@link User} (role = CUSTOMER), matching the
 * generalisation shown in the Assessment A class diagram.
 */
public class Customer extends User {
    public Customer() { setRole("CUSTOMER"); }
    public Customer(int id, String name, String email, String password, String address) {
        super(id, name, email, password, "CUSTOMER", address);
    }
}
