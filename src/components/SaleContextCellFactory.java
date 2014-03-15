package components;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import entity.Sale;

public class SaleContextCellFactory implements Callback<TableColumn<Sale, String>, TableCell<Sale, String>> {
	
	private ContextMenu menu;
	private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
	private MenuItem editItem;
	private MenuItem deleteItem;
	private MenuItem processItem;
	
	public SaleContextCellFactory() {
		super();
		menu = new ContextMenu();
		editItem = new MenuItem("Sửa", fontAwesome.fontColor(Color.GOLD).create("EDIT"));
		deleteItem = new MenuItem("Xóa", fontAwesome.fontColor(Color.RED).create("REMOVE"));
		processItem = new MenuItem("Thu tiền", fontAwesome.fontColor(Color.CORNFLOWERBLUE).create("COPY"));
		menu.getItems().addAll(editItem, deleteItem, processItem);
	}

	@Override
	public TableCell<Sale, String> call(TableColumn<Sale, String> param) {
		ContextTableCell<Sale, String> cell = new ContextTableCell<>();
		if(menu!=null) {
			cell.setContextMenu(menu);
			cell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
				
				@Override
				public void handle(ContextMenuEvent event) {
					TableCell<?, ?> sourceCell = (TableCell<?, ?>)event.getSource();//TableCell<?, ?>
					int clickedIndex = sourceCell.getIndex();
					@SuppressWarnings("unchecked")
					TableView<Sale> sourceTableView = (TableView<Sale>)sourceCell
							.getParent()//TableRow
							.getParent()//Group
							.getParent()//ClippedContainer
							.getParent()//VirtualFlow
							.getParent();
					ObservableList<Sale> sourceData = sourceTableView.getItems();
					try {
						Sale selectSale = sourceData.get(clickedIndex);
						setDisableMenu(false);
						if("0".equals(selectSale.getStatus())) {//chua thu
							editItem.setDisable(false);
							deleteItem.setDisable(false);
							processItem.setDisable(false);
						} else if("2".equals(selectSale.getStatus())) {//chua thu du
							editItem.setDisable(true);
							deleteItem.setDisable(true);
							processItem.setDisable(false);
						} else {// da thu du
							editItem.setDisable(true);
							deleteItem.setDisable(true);
							processItem.setDisable(true);
						}
						sourceTableView.getSelectionModel().select(clickedIndex);
					} catch(IndexOutOfBoundsException e) {
						setDisableMenu(true);
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

	public void setEditItemAction(EventHandler<ActionEvent> editItemAction) {
		editItem.setOnAction(editItemAction);
	}

	public void setDeleteItemAction(EventHandler<ActionEvent> deleteItemAction) {
		deleteItem.setOnAction(deleteItemAction);
	}

	public void setProcessItemAction(EventHandler<ActionEvent> processItemAction) {
		processItem.setOnAction(processItemAction);
	}
	
}
