package controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.AbstractDialogAction;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.DialogAction;
import org.controlsfx.dialog.Dialogs;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import application.Util;
import entity.ImportParcel;
import entity.Inventory;
import entity.ParcelItem;
import entity.Product;

@FXMLController("/fxml/ImportParcelScreen.fxml")
public class ImportParcelScreenCtrl {
	
	private EntityManager em;
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private ListView<Product> productLst;
	
	@FXML
	private ListView<ParcelItem> parcelItemLst;
	
	@FXML
	private TextField parcel_code_txt;
	
	@FXML
	private DatePicker import_date_txt;
	
	@FXML
	private TextField parcel_rate_txt;
	
	@FXML
	private TextArea parcel_des_txt;
	
	@FlowAction("gotoMain")
	private Button gotoMainMenuBtn = new Button();
	
	private ImportParcel editingParcel;
	
	private String mode;
	
	private final TextField quantity_txt = new TextField();
	private final TextField cost_vnd_txt = new TextField();
	private final TextField cost_rmb_txt = new TextField();
	
	private BigDecimal rate;
	
	private final DialogAction actionInputInfo = new AbstractDialogAction("Ok") {
		{
			ButtonBar.setType(this, ButtonType.OK_DONE);
		}
		
		@Override
		public void execute(ActionEvent ae) {
			if (! this.isDisabled() && ae.getSource() instanceof Dialog) {
            	Dialog dlg = (Dialog) ae.getSource();
            	if(isValidData()) {
            		Product selectedProduct = productLst.getSelectionModel().getSelectedItem();
            		Long quantity = new Long(quantity_txt.getText());
            		boolean added = false;
            		for(ParcelItem item : parcelItemLst.getItems()) {
            			if(item.getProduct().equals(selectedProduct)
            					&& cost_rmb_txt.getText().equals(item.getCost_rmb().toPlainString())
            					&& cost_vnd_txt.getText().equals(item.getCost_vnd().toPlainString())) {
            				item.setQuantity(item.getQuantity() + quantity);
            				added = true;
            				break;
            			}
            		}
            		if(!added) {
            			ParcelItem item = new ParcelItem();
            			item.setProduct(selectedProduct);
            			item.setQuantity(quantity);
            			item.setCost_rmb(new BigDecimal(cost_rmb_txt.getText()));
            			item.setCost_vnd(new BigDecimal(cost_vnd_txt.getText()));
            			parcelItemLst.getItems().add(item);
            		}
    				dlg.hide();
    			} else {
    				Dialogs.create().nativeTitleBar()
    			      .title("Error")
    			      .message( "Please correct the input data...")
    			      .showError();
    				return;
    			}
            }
			
		}

	};
	
	private ChangeListener<Boolean> txtLostFocusListener = new ChangeListener<Boolean>() {
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable,
				Boolean oldValue, Boolean newValue) {
			if(!newValue) {
	   			 calculateVND();
	   		 }
		}
	};
	
	@PostConstruct
	public void init() {
		mode = (String)viewContext.getRegisteredObject("isUpdate");
		editingParcel = (ImportParcel)viewContext.getRegisteredObject("editingParcel");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		List<Product> productList =
				em.createQuery("select p from Product p", Product.class)
				.getResultList();
		ObservableList<Product> pObservableList =
				FXCollections.observableArrayList(productList);
		productLst.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
			
			@Override
			public ListCell<Product> call(ListView<Product> param) {
				ListCell<Product> cell = new ListCell<Product>(){
					@Override
					protected void updateItem(Product t, boolean bln) {
						super.updateItem(t, bln);
						if(t != null) {
							setText(t.getName()+ " (" + t.getCode() + ")");
						}
					}
				};
				return cell;
			}
		});
		parcelItemLst.setCellFactory(new Callback<ListView<ParcelItem>, ListCell<ParcelItem>>() {
			
			@Override
			public ListCell<ParcelItem> call(ListView<ParcelItem> param) {
				ListCell<ParcelItem> cell = new ListCell<ParcelItem>(){
					@Override
					protected void updateItem(ParcelItem t, boolean bln) {
						super.updateItem(t, bln);
						if(t != null) {
							setText(t.getProduct().getName()+ " (Qty: " + t.getQuantity() + ", Cost RMB: "+ t.getCost_rmb() +", Cost VND: "+ t.getCost_vnd() +")");
						}
					}
				};
				return cell;
			}
		});
		productLst.setItems(pObservableList);
		
		if("1".equals(mode) && editingParcel != null) {
			parcel_code_txt.setText(editingParcel.getCode());
			Instant instant = Instant.ofEpochMilli(editingParcel.getImportDate().getTime());
			LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
			import_date_txt.setValue(res);
		} else {
			editingParcel = new ImportParcel();
			String parcelCode = Util.generateString();
			parcel_code_txt.setText(parcelCode);
			import_date_txt.setValue(LocalDate.now());
		}
	}
	
	@PreDestroy
	public void destroy() {
		
	}
	
	@FXML
	public void selectProduct(ActionEvent event) {
		Product selectProduct = productLst.getSelectionModel().getSelectedItem();
		if(selectProduct==null || !isNumber(parcel_rate_txt.getText())) {
			Dialogs.create().nativeTitleBar()
				      .title("Error")
				      .message( "Please select a product to process and enter valid rate...")
				      .showError();
			return;
		}
		rate = new BigDecimal(parcel_rate_txt.getText());
		showInputInfoDialog();
	}
	
	@FXML
	public void removeProduct(ActionEvent event) {
		ParcelItem selectItem = parcelItemLst.getSelectionModel().getSelectedItem();
		if(selectItem==null) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Please select a item to process...")
		      .showError();
			return;
		}
		parcelItemLst.getItems().remove(selectItem);
	}
	
	@FXML
	public void registerParcel() {
		if(parcel_code_txt.getText().isEmpty()
				|| import_date_txt.getValue()==null
				|| !isNumber(parcel_rate_txt.getText())) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Please enter valid information ...")
		      .showError();
			return;
		}
		em.getTransaction().begin();
		editingParcel.setCode(parcel_code_txt.getText());
		editingParcel.setDescription(parcel_des_txt.getText());
		GregorianCalendar cal = GregorianCalendar.from(import_date_txt.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		editingParcel.setImportDate(cal.getTime());
		editingParcel.setLastUpdate(new Date());
		editingParcel.setRate(new BigDecimal(parcel_rate_txt.getText()));
		em.persist(editingParcel);
		//insert parcel items
		Date lastUpdate = new Date();
		BigDecimal totalValue = new BigDecimal(0);
		for(ParcelItem item : parcelItemLst.getItems()) {
			item.setParcel(editingParcel);
			item.setRemain(item.getQuantity());
			item.setLastUpdate(lastUpdate);
			totalValue = totalValue.add(item.getCost_vnd().multiply(new BigDecimal(item.getQuantity())));
			em.persist(item);
			List<Inventory> invList = em.createQuery("select i from Inventory i where i.product.id = :productid", Inventory.class).setParameter("productid", item.getProduct().getId()).getResultList();
			if(invList!=null) {
				Inventory inv = invList.get(0);
				inv.setQoh(inv.getQoh() + item.getQuantity());
				inv.setTotalValue(inv.getTotalValue().add(item.getCost_vnd().multiply(new BigDecimal(item.getQuantity()))));
				inv.setLastupdate(lastUpdate);
				em.merge(inv);
			}
		}
		editingParcel.setImportValue(totalValue);
		em.merge(editingParcel);
		em.getTransaction().commit();
		resetFields();
	}
	
	@FXML
	public void returnPrevious() {
		gotoMainMenuBtn.fire();
	}
	
	private Action showInputInfoDialog() {
		Dialog dlg = new Dialog(null, "Input information");
		GridPane content = new GridPane();
	     content.setHgap(10);
	     content.setVgap(10);
	     content.add(new Label("Quantity"), 0, 0);
	     content.add(quantity_txt, 1, 0);
	     GridPane.setHgrow(quantity_txt, Priority.ALWAYS);
	     content.add(new Label("Cost in RMB"), 0, 1);
	     content.add(cost_rmb_txt, 1, 1);
	     GridPane.setHgrow(cost_rmb_txt, Priority.ALWAYS);
	     content.add(new Label("Cost in VND"), 0, 2);
	     cost_vnd_txt.setEditable(false);
	     content.add(cost_vnd_txt, 1, 2);
	     GridPane.setHgrow(cost_vnd_txt, Priority.ALWAYS);
	     
	     quantity_txt.focusedProperty().addListener(txtLostFocusListener);
	     cost_rmb_txt.focusedProperty().addListener(txtLostFocusListener);
	     
	     // create the dialog with a custom graphic and the gridpane above as the
	     // main content region
	     dlg.setResizable(false);
	     dlg.setIconifiable(false);
	     //dlg.setGraphic(new ImageView(HelloDialog.class.getResource("login.png").toString()));
	     dlg.setContent(content);
	     dlg.getActions().addAll(actionInputInfo, Dialog.Actions.CANCEL);
	     Platform.runLater(new Runnable() {
	         public void run() {
	        	 quantity_txt.requestFocus();
	         }
	     });
	     return dlg.show();
	}
	
	private boolean isValidData() {
		try {
			new BigDecimal(quantity_txt.getText());
			new BigDecimal(cost_rmb_txt.getText());
			new BigDecimal(cost_vnd_txt.getText());
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean isNumber(String num) {
		try {
			new BigDecimal(num);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private void resetFields() {
		parcelItemLst.setItems(null);
		String parcelCode = Util.generateString();
		parcel_code_txt.setText(parcelCode);
		import_date_txt.setValue(LocalDate.now());
		parcel_rate_txt.setText("");
		parcel_des_txt.setText("");
	}
	
	private void calculateVND() {
		if(isNumber(cost_rmb_txt.getText()) && isNumber(quantity_txt.getText())) {
			BigDecimal costRMB = new BigDecimal(cost_rmb_txt.getText());
			BigDecimal quantity = new BigDecimal(quantity_txt.getText());
			cost_vnd_txt.setText(costRMB.multiply(rate).multiply(quantity).toPlainString());
		}
	}

}
