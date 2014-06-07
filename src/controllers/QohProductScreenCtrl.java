package controllers;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import entity.Inventory;
import entity.Product;

@FXMLController("/fxml/QohProductScreen.fxml")
public class QohProductScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXML
	private TextField searchBox;
	
	@FXML
	private TableView<Inventory> inventoryTableView;
	
	@FXML
	private TableColumn<Inventory, String> productCodeColumn;
	
	@FXML
	private TableColumn<Inventory, String> productNameColumn;
	
	@FXML
	private TableColumn<Inventory, String> qohColumn;
	
	@FXML
	private TableColumn<Inventory, String> totalColumn;
	
	@FXML
	@FlowAction("gotoMain")
	private Button backBtn;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private CheckBox displayOutOfStock;
	
	@FlowAction("gotoImportParcel")
	private Button gotoImportParcelBtn = new Button();
	
	private EntityManager em;
	
	private FilteredList<Inventory> filteredData;
	
	private String searchBoxInputValue;
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		displayOutOfStock.setSelected(true);
		ObservableList<Inventory> iObservableList = createInventoryData(true);
		productCodeColumn.setCellValueFactory(new PropertyValueFactory<Inventory, String>("productCode"));
		productNameColumn.setCellValueFactory(new PropertyValueFactory<Inventory, String>("productName"));
		qohColumn.setCellValueFactory(new PropertyValueFactory<Inventory, String>("qoh"));
		totalColumn.setCellValueFactory(new PropertyValueFactory<Inventory, String>("totalValue"));
		filteredData = new FilteredList<>(iObservableList, new Predicate<Inventory>() {
			@Override
			public boolean test(Inventory i) {
				return true;
			}
		});
		inventoryTableView.setItems(filteredData);
		setListenerForSearchbox();
		setListenerForOutOfStockCheckBox();
	}
	
	private void setListenerForSearchbox() {
		searchBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchBoxInputValue = newValue;
				filteredData.setPredicate(new Predicate<Inventory>() {
					@Override
					public boolean test(Inventory i) {
						if(searchBoxInputValue == null || searchBoxInputValue.isEmpty()) {
							return true;
						}
						
						String lowerinputValue = searchBoxInputValue.toLowerCase();
						if(i.getProductCode().toLowerCase().indexOf(lowerinputValue) != -1
								|| i.getProductName().toLowerCase().indexOf(lowerinputValue) != -1) {
							return true;
						}
						return false;
					}
				});
				
			}
		});
	}
	
	private void setListenerForOutOfStockCheckBox() {
		displayOutOfStock.selectedProperty().addListener(new ChangeListener<Boolean>() {
	        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                if(new_val == true) {
                	inventoryTableView.setItems(createInventoryData(true));
                } else {
                	inventoryTableView.setItems(createInventoryData(false));
                }
	        }
	    });
	}
	
	private ObservableList<Inventory> createInventoryData(boolean isIncludeOutOfStock) {
		String sql = "select i from Inventory i";
		if(!isIncludeOutOfStock) {
			sql = "select i from Inventory i where i.qoh > 0";
		}
		List<Inventory> inventoryList =
				em.createQuery(sql, Inventory.class)
				.getResultList();
		return FXCollections.observableArrayList(inventoryList);
	}
	
	@PreDestroy
	public void destroy() {
		
	}

}
