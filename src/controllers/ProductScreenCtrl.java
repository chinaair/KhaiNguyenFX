package controllers;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;
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
	private TextField searchBox;
	
	@FXML
	private TableView<Product> productTableView;
	
	@FXML
	private TableColumn<Product, String> codeCol;
	
	@FXML
	private TableColumn<Product, String> nameCol;
	
	@FXML
	private TableColumn<Product, String> typeCol;
	
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
	
	private FilteredList<Product> filteredData;
	
	private String searchBoxInputValue;
	
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
		typeCol.setCellValueFactory(new PropertyValueFactory<Product, String>("type"));
		typeCol.setCellFactory(productStrCellFactory);
		descriptionCol.setCellValueFactory(new PropertyValueFactory<Product, String>("description"));
		descriptionCol.setCellFactory(productStrCellFactory);
		//productTableView.getSelectionModel().selectedItemProperty().addListener(listener);
		// 1. Wrap the ObservableList in a FilteredList (initially display all data).
		filteredData = new FilteredList<>(pObservableList, new Predicate<Product>() {
			@Override
			public boolean test(Product t) {
				return true;
			}
		});
		// 3. Wrap the FilteredList in a SortedList.
		SortedList<Product> sortedData = new SortedList<>(filteredData);
		// 4. Bind the SortedList comparator to the TableView comparator.
		sortedData.comparatorProperty().bind(productTableView.comparatorProperty());
		// 5. Add sorted (and filtered) data to the table.
		productTableView.setItems(sortedData);
		// 2. Set the filter Predicate whenever the filter changes.
		setListenerForSearchbox();
	}
	
	private void setListenerForSearchbox() {
		searchBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchBoxInputValue = newValue;
				filteredData.setPredicate(new Predicate<Product>() {
					@Override
					public boolean test(Product p) {
						if(searchBoxInputValue == null || searchBoxInputValue.isEmpty()) {
							return true;
						}
						
						String lowerinputValue = searchBoxInputValue.toLowerCase();
						if(p.getCode().toLowerCase().indexOf(lowerinputValue) != -1
								|| p.getName().toLowerCase().indexOf(lowerinputValue) != -1
								|| p.getType().toLowerCase().indexOf(lowerinputValue) != -1
								|| p.getDescription().toLowerCase().indexOf(lowerinputValue) != -1) {
							return true;
						}
						return false;
					}
				});
				
			}
		});
	}
	
	public void beforeOpenContextMenu(ActionEvent event) {
		event.getSource();
	}
	
	private ContextMenu createContextMenu() {
		ContextMenu rowMenu = new ContextMenu();
		MenuItem editItem = new MenuItem("Sửa", fontAwesome.fontColor(Color.GOLD).create("EDIT"));
		MenuItem deleteItem = new MenuItem("Xóa", fontAwesome.fontColor(Color.RED).create("REMOVE"));
		MenuItem copyItem = new MenuItem("Tham khảo", fontAwesome.fontColor(Color.CORNFLOWERBLUE).create("COPY"));
		MenuItem addItem = new MenuItem("Tạo mới", fontAwesome.fontColor(Color.GREEN).create("PLUS"));
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
				Long importQuantity = em
						.createQuery(
								"select count(p) from ParcelItem p where p.product.id = :productId",
								Long.class).setParameter("productId", currentProduct.getId()).getSingleResult();
				Long saleQuantity = em
						.createQuery(
								"select count(s) from SaleItem s where s.product.id = :productId",
								Long.class).setParameter("productId", currentProduct.getId()).getSingleResult();
				if((importQuantity != null && importQuantity > 0)
						|| (saleQuantity != null && saleQuantity > 0)) {
					Dialogs.create().style(DialogStyle.NATIVE).title("Error")
					.message("Product are being used!")
					.showError();
					return;
				}
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
				//viewContext.register("editingList", productTableView.getItems());
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
	
	@PreDestroy
	public void destroy() {
		
	}
	
}
