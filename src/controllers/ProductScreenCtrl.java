package controllers;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import components.ContextCellFactory;

import entity.Inventory;
import entity.Product;

@FXMLController("/fxml/ProductScreen.fxml")
public class ProductScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXML
	private TableView<Product> productTableView;
	
	@FXML
	private TableColumn<Product, String> codeCol;
	
	@FXML
	private TableColumn<Product, String> nameCol;
	
	@FXML
	private TableColumn<Product, String> descriptionCol;
	
	private EntityManager em;
	
	private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
	
	@FlowAction("gotoEditProduct")
	private Button editItemBtn = new Button();
	
	@FXML
	@FlowAction("gotoMain")
	private Button backBtn;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		List<Product> productList =
				em.createQuery("select p from Product p", Product.class)
				.getResultList();
		ObservableList<Product> pObservableList =
				FXCollections.observableArrayList(productList);
		ContextMenu rowMenu = createContextMenu();
		if(productList==null || productList.size()==0) {
			makeDisableMenu(rowMenu, 0, 1, 2);
			productTableView.setContextMenu(rowMenu);
		} else {
			productTableView.setContextMenu(null);
		}
		ContextCellFactory<Product, String> productStrCellFactory =
				new ContextCellFactory<>(rowMenu, 0, 1, 2);
		codeCol.setCellValueFactory(new PropertyValueFactory<Product, String>("code"));
		codeCol.setCellFactory(productStrCellFactory);
		nameCol.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
		nameCol.setCellFactory(productStrCellFactory);
		descriptionCol.setCellValueFactory(new PropertyValueFactory<Product, String>("description"));
		descriptionCol.setCellFactory(productStrCellFactory);
		productTableView.setItems(pObservableList);
		//productTableView.getSelectionModel().selectedItemProperty().addListener(listener);
	}
	
	public void beforeOpenContextMenu(ActionEvent event) {
		event.getSource();
	}
	
	private ContextMenu createContextMenu() {
		ContextMenu rowMenu = new ContextMenu();
		MenuItem editItem = new MenuItem("Sua", fontAwesome.fontColor(Color.GOLD).create("EDIT"));
		MenuItem deleteItem = new MenuItem("Xoa", fontAwesome.fontColor(Color.RED).create("REMOVE"));
		MenuItem copyItem = new MenuItem("Tham khao", fontAwesome.fontColor(Color.CORNFLOWERBLUE).create("COPY"));
		MenuItem addItem = new MenuItem("Tao moi", fontAwesome.fontColor(Color.GREEN).create("PLUS"));
		editItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "1");
				viewContext.register("editingProduct", productTableView.getSelectionModel().getSelectedItem());
				editItemBtn.fire();
			}
		});
		deleteItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Product currentProduct = productTableView.getSelectionModel().getSelectedItem();
				List<Inventory> inventoryList = em.createQuery("select i from Inventory i where i.product.id = :productId", Inventory.class)
				.setParameter("productId", currentProduct.getId())
				.getResultList();
				boolean isRemoved = false;
				em.getTransaction().begin();
				if(inventoryList!=null && !inventoryList.isEmpty()) {//remove inventory
					for(Inventory inv : inventoryList) {
						em.remove(inv);
					}
					isRemoved = true;
				} else{//or remove product
					Product removeObj = em.find(Product.class, currentProduct.getId());
					if(removeObj!=null) {
						em.remove(removeObj);
					}
					isRemoved = true;
				}
				em.getTransaction().commit();
				if(isRemoved) {
					ContextMenu colMenu = ((ContextCellFactory<Product, String>)nameCol.getCellFactory()).getContextMenu();
					productTableView.getItems().remove(productTableView.getSelectionModel().getSelectedIndex());
					if(productTableView.getItems().size()==0) {
						makeDisableMenu(colMenu, 0, 1, 2);
						productTableView.setContextMenu(colMenu);
					}
				}
			}
		});
		copyItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "0");
				viewContext.register("editingProduct", productTableView.getSelectionModel().getSelectedItem());
				viewContext.register("editingList", productTableView.getItems());
				editItemBtn.fire();
			}
		});
		addItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "0");
				editItemBtn.fire();
			}
		});
		rowMenu.getItems().addAll(editItem, deleteItem, copyItem, addItem);
		return rowMenu;
	}
	
	private void makeDisableMenu(ContextMenu menu, int... emptyCellDisabledMenuIndex) {
		for(int i : emptyCellDisabledMenuIndex) {
			menu.getItems().get(i).setDisable(true);
		}
	}
	
	/*public void displayEditProductDialog() {
		Dialog editProductDialog = new Dialog(null, "Edit thong tin san pham");
		editProductDialog.setResizable(false);
		editProductDialog.setIconifiable(false);
		//editProductDialog.setGraphic(fontAwesome.fontColor(Color.GREEN).fontSize(48).create("EDIT"));
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("/fxml/EditProductScreen.fxml"));
		try {
			AnchorPane pane = (AnchorPane)fxmlLoader.load();
			EditProductScreenCtrl editProController = (EditProductScreenCtrl)fxmlLoader.getController();
			editProController.setEditingProduct(productTableView.getSelectionModel().getSelectedItem());
			editProController.setEntityManager(em);
			editProductDialog.setContent(pane);
			editProductDialog.getActions().addAll(getUpdateProductAction(), Dialog.Actions.CANCEL);
			editProductDialog.show();
		} catch(IOException e) {
			System.out.println(e);
		}
	}
	
	private Action getUpdateProductAction() {
		Action updateAction = new AbstractAction("Update") {
			{
				ButtonBar.setType(this, ButtonType.OK_DONE);
			}
			
			@Override
			public void execute(ActionEvent event) {
				Dialog dlg = (Dialog)event.getSource();
				dlg.hide();
			}
		};
		return updateAction;
	}*/
	
	@PreDestroy
	public void destroy() {
		
	}
	
}
