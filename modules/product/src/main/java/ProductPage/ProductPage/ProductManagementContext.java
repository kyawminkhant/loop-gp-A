package ProductPage.ProductPage;

final class ProductManagementContext {

    private static Runnable returnToAdmin;

    private ProductManagementContext() {
    }

    static void setReturnToAdmin(Runnable navigator) {
        returnToAdmin = navigator;
    }

    static void clearReturnToAdmin() {
        returnToAdmin = null;
    }

    static boolean hasAdminReturn() {
        return returnToAdmin != null;
    }

    static boolean returnToAdmin() {
        Runnable navigator = returnToAdmin;
        if (navigator == null) {
            return false;
        }
        returnToAdmin = null;
        navigator.run();
        return true;
    }
}
