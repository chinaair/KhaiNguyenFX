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

import entity.ImportParcel;

public class ParcelContextCellFactory implements Callback<TableColumn<ImportParcel, String>, TableCell<ImportParcel, String>> {
	
	private ContextMenu menu;
	private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
	private MenuItem viewItem;
	private MenuItem editItem;
	private MenuItem deleteItem;
	
	public ParcelContextCellFactory() {
		super();
		menu = new ContextMenu();
		viewItem = new MenuItem("Xem", fontAwesome.fontColor(Color.GREEN).create("EYE_OPEN"));
		editItem = new MenuItem("Sửa", fontAwesome.fontColor(Color.GOLD).create("EDIT"));
		deleteItem = new MenuItem("Xóa", fontAwesome.fontColor(Color.RED).create("REMOVE"));
		menu.getItems().addAll(viewItem, editItem, deleteItem);
	}

	@Override
	public TableCell<ImportParcel, String> call(TableColumn<ImportParcel, String> param) {
		ContextTableCell<ImportParcel, String> cell = new ContextTableCell<>();
		if(menu!=null) {
			cell.setContextMenu(menu);
			cell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
				
				@Override
				public void handle(ContextMenuEvent event) {
					TableCell<?, ?> sourceCell = (TableCell<?, ?>)event.getSource();//TableCell<?, ?>
					int clickedIndex = sourceCell.getIndex();
					@SuppressWarnings("unchecked")
					TableView<ImportParcel> sourceTableView = (TableView<ImportParcel>)sourceCell
							.getParent()//TableRow
							.getParent()//Group
							.getParent()//ClippedContainer
							.getParent()//VirtualFlow
							.getParent();
					ObservableList<ImportParcel> sourceData = sourceTableView.getItems();
					try {
						ImportParcel selectParcel = sourceData.get(clickedIndex);
						setDisableMenu(false);
						if("1".equals(selectParcel.getStatus())) {//da xuat kho
							editItem.setDisable(true);
							deleteItem.setDisable(true);
						} else {// chua xuat kho
							editItem.setDisable(false);
							deleteItem.setDisable(false);
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
	
	public void setViewItemAction(EventHandler<ActionEvent> viewItemAction) {
		viewItem.setOnAction(viewItemAction);
	}

	public void setEditItemAction(EventHandler<ActionEvent> editItemAction) {
		editItem.setOnAction(editItemAction);
	}

	public void setDeleteItemAction(EventHandler<ActionEvent> deleteItemAction) {
		deleteItem.setOnAction(deleteItemAction);
	}

}
