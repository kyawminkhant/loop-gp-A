package gp.loop.finance;

import javafx.scene.control.TableCell;

/**
 * Table cell that applies a fixed inline style when populated — used by both
 * the dashboard and the revenue-by-week table for alternating green/orange pills.
 */
public final class StyledCell<S> extends TableCell<S, String> {

    private final String cellStyle;

    public StyledCell(String cellStyle) {
        this.cellStyle = cellStyle;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setStyle("");
        } else {
            setText(item);
            setStyle(cellStyle);
        }
    }
}
