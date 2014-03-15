package components;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;

public class ContextCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
	
	private ContextMenu menu;
	private int[] emptyCellDisabledMenuIndex;
	
	public ContextCellFactory() {
		super();
	}
	
	public ContextCellFactory(ContextMenu ctxMenu, int... emptyCellDisabledMenuIndex) {
		super();
		menu = ctxMenu;
		this.emptyCellDisabledMenuIndex = emptyCellDisabledMenuIndex;
	}

	@Override
	public TableCell<S, T> call(TableColumn<S, T> param) {
		ContextTableCell<S, T> cell = new ContextTableCell<>();
		if(menu!=null) {
			cell.setContextMenu(menu);
			cell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
				
				@Override
				public void handle(ContextMenuEvent event) {
					TableCell<?, ?> sourceCell = (TableCell<?, ?>)event.getSource();//TableCell<?, ?>
					int clickedIndex = sourceCell.getIndex(); 
					TableView<?> sourceTableView = (TableView<?>)sourceCell
							.getParent()//TableRow
							.getParent()//Group
							.getParent()//ClippedContainer
							.getParent()//VirtualFlow
							.getParent();
					ObservableList<?> sourceData = sourceTableView.getItems();
					try {
						sourceData.get(clickedIndex);
						setDisableMenu(false);
						sourceTableView.getSelectionModel().select(clickedIndex);
					} catch(IndexOutOfBoundsException e) {
						if(emptyCellDisabledMenuIndex!=null) {
							for(int i : emptyCellDisabledMenuIndex) {
								menu.getItems().get(i).setDisable(true);
							}
						}
						System.out.println("Null row clicked!");
					}
				}
				
				private void setDisableMenu(boolean isDisable) {
					ObservableList<MenuItem> ctxMenuItems = menu.getItems();
					for(MenuItem item : ctxMenuItems) {
						item.setDisable(isDisable);
					}
				}
			});
		}
		return cell;
	}
	
	public ContextMenu getContextMenu() {
		return menu;
	}

}
