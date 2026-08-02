package ProductPage.ProductPage;

import java.util.ArrayList;

public final class CartStore {

    private static final ArrayList<CartItem> cartItems = new ArrayList<>();

    private CartStore() {
    }

    public static void addItem(int productId, double price, int quantity, boolean weekly) {
        addItem(productId, price, quantity, weekly, "");
    }

    public static void addItem(int productId, double price, int quantity,
            boolean weekly, String productDetails) {
        if (quantity <= 0) {
            return;
        }

        String details = productDetails == null ? "" : productDetails;

        for (CartItem item : cartItems) {
            if (item.getProductId() == productId
                    && item.isWeekly() == weekly
                    && item.getPrice() == price
                    && item.getProductDetails().equals(details)) {
                item.addQuantity(quantity);
                return;
            }
        }

        cartItems.add(new CartItem(productId, price, quantity, weekly, details));
    }
    
    public static double[][] getProductPriceArray() {
        double[][] result = new double[getTotalQuantity()][2];
        int row = 0;

        for (CartItem item : cartItems) {
            for (int index = 0; index < item.getQuantity(); index++) {
                result[row][0] = item.getProductId();
                result[row][1] = item.getPrice();
                row++;
            }
        }

        return result;
    }

    public static String[][] getProductDetailArray() {
        String[][] result = new String[getTotalQuantity()][2];
        int row = 0;

        for (CartItem item : cartItems) {
            for (int index = 0; index < item.getQuantity(); index++) {
                result[row][0] = String.valueOf(item.getProductId());
                result[row][1] = item.getProductDetails();
                row++;
            }
        }

        return result;
    }

    public static ArrayList<Integer> getProductIds() {
        ArrayList<Integer> productIds = new ArrayList<>();

        for (CartItem item : cartItems) {
            for (int index = 0; index < item.getQuantity(); index++) {
                productIds.add(item.getProductId());
            }
        }

        return productIds;
    }

    public static ArrayList<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public static ArrayList<Integer> getWeeklyProductIds() {
        return getProductIdsByWeeklyStatus(true);
    }

    public static ArrayList<Integer> getOneTimeProductIds() {
        return getProductIdsByWeeklyStatus(false);
    }

    public static void clear() {
        cartItems.clear();
    }

    public static int getTotalQuantity() {
        int totalQuantity = 0;

        for (CartItem item : cartItems) {
            totalQuantity += item.getQuantity();
        }

        return totalQuantity;
    }

    private static ArrayList<Integer> getProductIdsByWeeklyStatus(
            boolean weekly) {

        ArrayList<Integer> productIds = new ArrayList<>();

        for (CartItem item : cartItems) {
            if (item.isWeekly() != weekly) {
                continue;
            }

            for (int index = 0; index < item.getQuantity(); index++) {
                productIds.add(item.getProductId());
            }
        }

        return productIds;
    }

    public static final class CartItem {
        private final int productId;
        private final double price;
        private int quantity;
        private final boolean weekly;
        private final String productDetails;

        private CartItem(int productId, double price, int quantity,
                boolean weekly, String productDetails) {
            this.productId = productId;
            this.price = price;
            this.quantity = quantity;
            this.weekly = weekly;
            this.productDetails = productDetails;
        }

        public int getProductId() {
            return productId;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public boolean isWeekly() {
            return weekly;
        }

        public String getProductDetails() {
            return productDetails;
        }

        private void addQuantity(int quantityToAdd) {
            quantity += quantityToAdd;
        }
    }
}
