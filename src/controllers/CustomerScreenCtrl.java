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

@FXMLController("/fxml/CustomerScreen.fxml")
public class CustomerScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
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
		customerTableView.setItems(cObservableList);
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
					.message("Customer are being used!")
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
