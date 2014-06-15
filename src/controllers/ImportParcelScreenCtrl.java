package controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import org.controlsfx.dialog.DefaultDialogAction;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.DialogAction;
import org.controlsfx.dialog.DialogStyle;
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
	
	@FXML
	private TextField searchBox;
	
	@FXML
	private Button selectProductBtn;
	
	@FXML
	private Button removeProductBtn;
	
	@FXML
	private Button registerParcelBtn;
	
	@FlowAction("gotoMain")
	private Button gotoMainMenuBtn = new Button();
	
	@FlowAction("gotoParcelList")
	private Button gotoParcelListBtn = new Button();
	
	private ImportParcel editingParcel;
	
	private String mode;
	
	private final TextField quantity_txt = new TextField();
	
	private final TextField cost_vnd_txt = new TextField();
	
	private final TextField cost_rmb_txt = new TextField();
	
	private final Label total_vnd_cost = new Label();
	
	private BigDecimal rate;
	
	private FilteredList<Product> filteredData;
	
	private String searchBoxInputValue;
	
	private final DialogAction actionInputInfo = new DefaultDialogAction("Ok") {
		{
			ButtonBar.setType(this, ButtonType.OK_DONE);
		}
		
		@Override
		public void handle(ActionEvent ae) {
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
    				resetParcelItemCellFactory();
    			} else {
    				Dialogs.create().style(DialogStyle.NATIVE)
    			      .title("Error")
    			      .message( "Hãy nhập thông tin đúng định dạng...")
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
				em.createQuery("select p from Product p order by p.type, p.name", Product.class)
				.getResultList();
		ObservableList<Product> pObservableList =
				FXCollections.observableArrayList(productList);
		filteredData = new FilteredList<>(pObservableList, new Predicate<Product>() {
			@Override
			public boolean test(Product t) {
				return true;
			}
		});
		productLst.setItems(filteredData);
		setListenerForSearchbox();
		resetProductListCellFactory();
		resetParcelItemCellFactory();
		
		if(("1".equals(mode) || "2".equals(mode)) && editingParcel != null) {//update or view
			parcel_code_txt.setText(editingParcel.getCode());
			parcel_rate_txt.setText(editingParcel.getRate().toPlainString());
			parcel_des_txt.setText(editingParcel.getDescription());
			Instant instant = Instant.ofEpochMilli(editingParcel.getImportDate().getTime());
			LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
			import_date_txt.setValue(res);
			ObservableList<ParcelItem> obserSaleItems = FXCollections.observableArrayList(editingParcel.getParcelItems());
			parcelItemLst.setItems(obserSaleItems);
			if("2".equals(mode)) {
				import_date_txt.setDisable(true);
				parcel_rate_txt.setEditable(false);
				parcel_des_txt.setEditable(false);
				selectProductBtn.setDisable(true);
				removeProductBtn.setDisable(true);
				registerParcelBtn.setDisable(true);
			}
		} else {
			editingParcel = new ImportParcel();
			String parcelCode = Util.generateString();
			parcel_code_txt.setText(parcelCode);
			import_date_txt.setValue(LocalDate.now());
		}
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
								|| p.getType().toLowerCase().indexOf(lowerinputValue) != -1) {
							resetProductListCellFactory();
							return true;
						}
						return false;
					}
				});
				
			}
		});
	}
	
	@PreDestroy
	public void destroy() {
		
	}
	
	@FXML
	public void selectProduct(ActionEvent event) {
		Product selectProduct = productLst.getSelectionModel().getSelectedItem();
		if(selectProduct==null || !isNumber(parcel_rate_txt.getText())) {
			//Please select a product to process and enter valid rate
			Dialogs.create().style(DialogStyle.NATIVE)
				      .title("Error")
				      .message( "Hãy chọn 1 sản phẩm và nhập tỷ giá đúng...")
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
			//Plaese select an item to process
			Dialogs.create().style(DialogStyle.NATIVE)
		      .title("Error")
		      .message( "Hãy chọn 1 chi tiết...")
		      .showError();
			return;
		}
		parcelItemLst.getItems().remove(selectItem);
		resetParcelItemCellFactory();
	}
	
	@FXML
	public void registerParcel() {
		if(parcel_code_txt.getText().isEmpty()
				|| import_date_txt.getValue()==null
				|| !isNumber(parcel_rate_txt.getText())) {
			//Please input correct data
			Dialogs.create().style(DialogStyle.NATIVE)
		      .title("Error")
		      .message( "Hãy nhập thông tin đúng định dạng...")
		      .showError();
			return;
		}
		Long dupCount = em.createQuery("SELECT COUNT(i) FROM ImportParcel i WHERE i.code = :code", Long.class).setParameter("code", parcel_code_txt.getText()).getSingleResult();
		if(dupCount != null && dupCount.intValue() > 0) {
			//The parcel code is duplicated
			Dialogs.create().style(DialogStyle.NATIVE)
		      .title("Error")
		      .message( "Mã số lô đã bị trùng...")
		      .showError();
			return;
		}
		em.getTransaction().begin();
		editingParcel.setCode(parcel_code_txt.getText());
		editingParcel.setDescription(parcel_des_txt.getText());
		GregorianCalendar cal = GregorianCalendar.from(import_date_txt.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		editingParcel.setImportDate(cal.getTime());
		editingParcel.setLastUpdate(new Date());
		editingParcel.setStatus("0");
		editingParcel.setRate(new BigDecimal(parcel_rate_txt.getText()));
		if("1".equals(mode)) {
			em.merge(editingParcel);
			for(ParcelItem item : editingParcel.getParcelItems()) {
				List<Inventory> invList = em
						.createQuery(
								"SELECT i FROM Inventory i WHERE i.product.id = :productid",
								Inventory.class)
						.setParameter("productid", item.getProduct().getId())
						.getResultList();
				if(invList!=null && !invList.isEmpty()) {
					Inventory invProd = invList.get(0);
					if(invProd.getQoh() > item.getQuantity()) {
						invProd.setQoh(invProd.getQoh() - item.getQuantity());
					} else {
						invProd.setQoh(0L);
					}
					BigDecimal itemValue = item.getCost_vnd().multiply(new BigDecimal(item.getQuantity()));
					if(invProd.getTotalValue().compareTo(itemValue) > 0) {
						invProd.setTotalValue(invProd.getTotalValue().subtract(itemValue));
					} else {
						invProd.setTotalValue(new BigDecimal(0));
					}
					em.merge(invProd);
				}
			}
			
			em.createQuery("DELETE FROM ParcelItem p WHERE p.parcel.id = :parcelId").setParameter("parcelId", editingParcel.getId()).executeUpdate();
		} else {
			em.persist(editingParcel);
		}
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
		if("1".equals(mode)) {
			gotoParcelListBtn.fire();
		}
	}
	
	@FXML
	public void returnPrevious() {
		viewContext.register("isUpdate", null);
		viewContext.register("editingSale", null);
		if("1".equals(mode) || "2".equals(mode)) {
			gotoParcelListBtn.fire();
		} else {
			gotoMainMenuBtn.fire();
		}
	}
	
	private Action showInputInfoDialog() {
		Dialog dlg = new Dialog(null, "Nhập thông tin", false, DialogStyle.NATIVE);
		GridPane content = new GridPane();
	     content.setHgap(10);
	     content.setVgap(10);
	     quantity_txt.setText("");
	     content.add(new Label("Số lượng"), 0, 0);
	     content.add(quantity_txt, 1, 0);
	     GridPane.setHgrow(quantity_txt, Priority.ALWAYS);
	     cost_rmb_txt.setText("");
	     content.add(new Label("Giá RMB"), 0, 1);
	     content.add(cost_rmb_txt, 1, 1);
	     GridPane.setHgrow(cost_rmb_txt, Priority.ALWAYS);
	     content.add(new Label("Giá VND"), 0, 2);
	     cost_vnd_txt.setText("");
	     cost_vnd_txt.setEditable(true);
	     content.add(cost_vnd_txt, 1, 2);
	     GridPane.setHgrow(cost_vnd_txt, Priority.ALWAYS);
	     total_vnd_cost.setText("0");
	     content.add(new Label("Tổng cộng"), 0, 3);
	     content.add(total_vnd_cost, 1, 3);
	     
	     quantity_txt.focusedProperty().addListener(txtLostFocusListener);
	     cost_rmb_txt.focusedProperty().addListener(txtLostFocusListener);
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
			BigDecimal costVND = costRMB.multiply(rate);
			cost_vnd_txt.setText(costVND.toPlainString());
			total_vnd_cost.setText(costVND.multiply(quantity).toPlainString());
		}
	}
	
	private void resetParcelItemCellFactory() {
		parcelItemLst.setCellFactory(new Callback<ListView<ParcelItem>, ListCell<ParcelItem>>() {
			@Override
			public ListCell<ParcelItem> call(ListView<ParcelItem> param) {
				return new parcelCell();
			}
		});
	}
	
	private void resetProductListCellFactory() {
		productLst.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
			@Override
			public ListCell<Product> call(ListView<Product> param) {
				ListCell<Product> cell = new ListCell<Product>(){
					@Override
					protected void updateItem(Product t, boolean bln) {
						super.updateItem(t, bln);
						if(t != null) {
							setText("[" + t.getCode() + "] " + t.getName());
						}
					}
				};
				return cell;
			}
		});
	}
	
	static class parcelCell extends ListCell<ParcelItem> {
		@Override
		protected void updateItem(ParcelItem t, boolean bln) {
			super.updateItem(t, bln);
			if(t != null) {
				String dispStr = t.getProduct().getName()+ " (SL: " + t.getQuantity() + ", RMB: "+ t.getCost_rmb() +", VND: "+ t.getCost_vnd() +")";
				setText(dispStr);
			}
		}
	}

}
