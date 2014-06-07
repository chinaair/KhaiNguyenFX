package controllers;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import entity.Customer;
import entity.Product;

@FXMLController("/fxml/CustomerScreen.fxml")
public class CustomerScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXML
	private TextField searchBox;
	
	@FXML
	private TableView<Customer> customerTableView;
	
	@FXML
	private TableColumn<Customer, String> codeCol;
	
	@FXML
	private TableColumn<Customer, String> nameCol;
	
	@FXML
	private TableColumn<Customer, String> contactPersonCol;
	
	@FXML
	private TableColumn<Customer, String> phoneCol;
	
	@FXML
	private TableColumn<Customer, String> addressCol;
	
	private EntityManager em;
	
	private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
	
	@FlowAction("gotoEditCustomer")
	private Button editCustBtn = new Button();
	
	@FXML
	@FlowAction("gotoMain")
	private Button backBtn;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	private FilteredList<Customer> filteredData;
	
	private String searchBoxInputValue;
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		List<Customer> custList = em.createQuery("select cust from Customer cust", Customer.class).getResultList();
		ObservableList<Customer> cObservableList = FXCollections.observableArrayList(custList);
		ContextMenu rowMenu = createContextMenu();
		if(custList==null || custList.size()==0) {
			makeDisableMenu(rowMenu, 0, 1, 2);
			customerTableView.setContextMenu(rowMenu);
		} else {
			customerTableView.setContextMenu(null);
		}
		ContextCellFactory<Customer, String> customerStrCellFactory =
				new ContextCellFactory<>(rowMenu, 0, 1, 2);
		codeCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("code"));
		codeCol.setCellFactory(customerStrCellFactory);
		nameCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("name"));
		nameCol.setCellFactory(customerStrCellFactory);
		contactPersonCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("contactPerson"));
		contactPersonCol.setCellFactory(customerStrCellFactory);
		phoneCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("phone"));
		phoneCol.setCellFactory(customerStrCellFactory);
		addressCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));
		addressCol.setCellFactory(customerStrCellFactory);
		filteredData = new FilteredList<>(cObservableList, new Predicate<Customer>() {
			@Override
			public boolean test(Customer c) {
				return true;
			}
		});
		customerTableView.setItems(filteredData);
		setListenerForSearchbox();
	}
	
	private void setListenerForSearchbox() {
		searchBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchBoxInputValue = newValue;
				filteredData.setPredicate(new Predicate<Customer>() {
					@Override
					public boolean test(Customer c) {
						if(searchBoxInputValue == null || searchBoxInputValue.isEmpty()) {
							return true;
						}
						
						String lowerinputValue = searchBoxInputValue.toLowerCase();
						if(c.getName().toLowerCase().indexOf(lowerinputValue) != -1
								|| c.getCode().toLowerCase().indexOf(lowerinputValue) != -1) {
							return true;
						}
						return false;
					}
				});
				
			}
		});
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
				viewContext.register("editingCustomer", customerTableView.getSelectionModel().getSelectedItem());
				editCustBtn.fire();
			}
		});
		deleteItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Customer currentCustomer = customerTableView.getSelectionModel().getSelectedItem();
				Long custQuantity = em
						.createQuery(
								"select count(s) from Sale s where s.customer.id = :customerId",
								Long.class).setParameter("customerId", currentCustomer.getId()).getSingleResult();
				if(custQuantity != null && custQuantity > 0) {
					Dialogs.create().nativeTitleBar().title("Error")
					.message("Khách hàng đang được sử dụng!")
					.showError();
					return;
				}
				em.getTransaction().begin();
				Customer removeObj = em.find(Customer.class, currentCustomer.getId());
				if(removeObj!=null) {
					em.remove(removeObj);
				}
				em.getTransaction().commit();
				ContextMenu colMenu = ((ContextCellFactory<Customer, String>)nameCol.getCellFactory()).getContextMenu();
				customerTableView.getItems().remove(customerTableView.getSelectionModel().getSelectedIndex());
				if(customerTableView.getItems().size()==0) {
					makeDisableMenu(colMenu, 0, 1, 2);
					customerTableView.setContextMenu(colMenu);
				}
			}
		});
		copyItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "0");
				viewContext.register("editingCustomer", customerTableView.getSelectionModel().getSelectedItem());
				//viewContext.register("editingList", customerTableView.getItems());
				editCustBtn.fire();
			}
		});
		addItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "0");
				editCustBtn.fire();
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
