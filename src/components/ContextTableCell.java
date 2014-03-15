package components;

import javafx.scene.control.TableCell;

public class ContextTableCell<S, T> extends TableCell<S, T> {
	
	@Override
	protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty) {
        	setText(item.toString());
        } else {
        	setText(null);
        }
    }

}
