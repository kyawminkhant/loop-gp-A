package orders;

import java.util.ArrayList;
import java.util.List;


public final class CartStore {

    private static final List<CartLine> lines = new ArrayList<>();

    private CartStore() {
    }

    public static void addItem(MenuItem item, int quantity) {
        if (item == null || quantity <= 0) {
            return;
        }

        for (CartLine line : lines) {
            if (line.menuItemId == item.id) {
                line.addQuantity(quantity);
                return;
            }
        }

        lines.add(new CartLine(item.id, item.name, item.price, quantity));
    }

    public static List<CartLine> getLines() {
        return new ArrayList<>(lines);
    }

    public static double getTotal() {
        double total = 0;
        for (CartLine line : lines) {
            total += line.lineTotal();
        }
        return total;
    }

    public static int getTotalQuantity() {
        int total = 0;
        for (CartLine line : lines) {
            total += line.getQuantity();
        }
        return total;
    }

    public static boolean isEmpty() {
        return lines.isEmpty();
    }

    public static void clear() {
        lines.clear();
    }
}
