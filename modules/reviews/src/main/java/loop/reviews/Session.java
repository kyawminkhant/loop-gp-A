package loop.reviews;

import loop.reviews.model.User;

/**
 * Simple in-memory holder for cross-screen state: the logged-in user and the
 * currently selected product / review being acted upon. Because SceneManager
 * swaps roots on one Stage, controllers read the "context" for the screen here.
 */
public final class Session {

    private static User currentUser;
    private static int selectedProductId = -1;
    private static int editingReviewId = -1;
    private static Runnable adminReturnNavigator;
    private static Runnable productReturnNavigator;

    private Session() { }

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
    public static void logout() {
        currentUser = null;
        selectedProductId = -1;
        editingReviewId = -1;
        productReturnNavigator = null;
    }

    public static int getSelectedProductId() { return selectedProductId; }
    public static void setSelectedProductId(int id) { selectedProductId = id; }

    public static int getEditingReviewId() { return editingReviewId; }
    public static void setEditingReviewId(int id) { editingReviewId = id; }

    public static void setAdminReturnNavigator(Runnable navigator) {
        adminReturnNavigator = navigator;
    }

    public static boolean hasAdminGateway() {
        return adminReturnNavigator != null;
    }

    public static void returnFromAdmin() {
        if (adminReturnNavigator != null) {
            Runnable navigator = adminReturnNavigator;
            adminReturnNavigator = null;
            navigator.run();
        }
    }

    public static void setProductReturnNavigator(Runnable navigator) {
        productReturnNavigator = navigator;
    }

    public static void clearProductReturnNavigator() {
        productReturnNavigator = null;
    }

    public static boolean hasProductReturnNavigator() {
        return productReturnNavigator != null;
    }

    public static boolean returnToProduct() {
        if (productReturnNavigator == null) {
            return false;
        }
        Runnable navigator = productReturnNavigator;
        productReturnNavigator = null;
        navigator.run();
        return true;
    }
}
