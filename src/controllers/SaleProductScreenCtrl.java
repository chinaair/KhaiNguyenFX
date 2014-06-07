package controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
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
import entity.Customer;
import entity.ImportParcel;
import entity.Inventory;
import entity.ParcelItem;
import entity.Product;
import entity.Sale;
import entity.SaleItem;

@FXMLController("/fxml/SaleProductScreen.fxml")
public class SaleProductScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	private EntityManager em;
	
	private ObservableList<Customer> obserCustList;
	
	private ListView<Customer> selectCustList;
	
	private TextField custSearchBox;
	
	private Customer selectedCustomer;
	
	@FXML
	private DatePicker saleDate;
	
	@FXML
	private TextArea descriptionTxt;
	
	@FXML
	private TextField selectedCustTxt;
	
	@FXML
	private TextField searchBox;
	
	@FXML
	private ListView<Inventory> inventoryList;
	
	@FXML
	private ListView<SaleItem> saleItemList;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private Button cancelBtn;
	
	@FXML
	private Button chooseCustBtn;
	
	@FXML
	private Button selectProductBtn;
	
	@FXML
	private Button removeProductBtn;
	
	@FXML
	private Button registerSaleBtn;
	
	@FXML
	private CheckBox addressCheckbox;
	
	@FlowAction("gotoMain")
	private Button backToMainBtn = new Button();
	
	@FlowAction("gotoSaleList")
	private Button backToSaleList = new Button();
	
	private final TextField quantity_txt = new TextField();
	
	private final TextField salePrice_txt = new TextField();
	
	private final ComboBox<ParcelItem> itemsFromSaleCB = new ComboBox<>();
	
	private Customer currentSelectedCust;
	
	private Sale editingSale;
	
	private String mode;
	
	private FilteredList<Inventory> inventoryfilteredData;
	
	private FilteredList<Customer> custfilteredData;
	
	private String searchBoxInputValue;
	
	private String searchCustomerValue;
	
	private final DialogAction okSelectCustomer = new AbstractDialogAction("Ok") {
		{
			ButtonBar.setType(this, ButtonType.OK_DONE);
		}

		@Override
		public void execute(ActionEvent ae) {
			if (! this.isDisabled() && ae.getSource() instanceof Dialog) {
				Dialog dlg = (Dialog) ae.getSource();
				selectedCustomer = selectCustList.getSelectionModel().getSelectedItem();
				if(selectedCustomer!=null) {
					currentSelectedCust = selectedCustomer;
					selectedCustTxt.setText(selectedCustomer.getName());
				}
				dlg.hide();
			}
			
		}
		
	};
	
	private final DialogAction okSelectQuantity = new AbstractDialogAction("Ok") {
		{
			ButtonBar.setType(this, ButtonType.OK_DONE);
		}

		@Override
		public void execute(ActionEvent ae) {
			if (! this.isDisabled() && ae.getSource() instanceof Dialog) {
            	Dialog dlg = (Dialog) ae.getSource();
            	if(isValidData()) {
            		Inventory selectedInventory = inventoryList.getSelectionModel().getSelectedItem();
            		ParcelItem selectedParcelItem = itemsFromSaleCB.getSelectionModel().getSelectedItem();
            		Product invProduct = selectedInventory.getProduct();
            		int quantity = Integer.parseInt(quantity_txt.getText());
            		if(quantity > itemsFromSaleCB.getSelectionModel().getSelectedItem().getRemain().intValue()) {
						Dialogs.create().nativeTitleBar().title("Error")
								.message("Số lượng bán không được lớn hơn số lượng tồn kho...")
								.showError();
						return;
            		}
            		boolean added = false;
            		for(SaleItem item : saleItemList.getItems()) {
            			if(item.getProduct().equals(invProduct)
            					&& item.getParcelItem().getId().equals(itemsFromSaleCB.getSelectionModel().getSelectedItem().getId())) {
            				item.setQuantity(item.getQuantity() + quantity);
            				item.setParcelItem(selectedParcelItem);
            				item.setSalePrice(new BigDecimal(salePrice_txt.getText()));
            				added = true;
            				break;
            			}
            		}
            		if(!added) {
            			SaleItem item = new SaleItem();
            			item.setProduct(invProduct);
            			item.setQuantity(quantity);
            			item.setParcelItem(itemsFromSaleCB.getSelectionModel().getSelectedItem());
            			item.setSalePrice(new BigDecimal(salePrice_txt.getText()));
            			saleItemList.getItems().add(item);
            		}
            		selectedInventory.setQoh(selectedInventory.getQoh() - quantity);
            		BigDecimal remainValue = selectedInventory.getTotalValue().subtract(selectedParcelItem.getCost_vnd().multiply(new BigDecimal(quantity)));
            		selectedInventory.setTotalValue(remainValue);
            		ObservableList<Inventory> tmpList = inventoryList.getItems();
            		inventoryList.setItems(null);
            		inventoryList.setItems(tmpList);
    				dlg.hide();
    				resetSaleItemCellFactory();
    			} else {
    				Dialogs.create().nativeTitleBar()
    			      .title("Error")
    			      .message( "hãy nhập thông tin đúng định dạng...")
    			      .showError();
    				return;
    			}
            }
			
		}

	};
	
	@PostConstruct
	public void init() {
		mode = (String)viewContext.getRegisteredObject("isUpdate");
		editingSale = (Sale)viewContext.getRegisteredObject("editingSale");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		List<Inventory> iList = em.createQuery("select i from Inventory i", Inventory.class).getResultList();
		ObservableList<Inventory> obserProductList = FXCollections.observableArrayList(iList);
		inventoryfilteredData = new FilteredList<>(obserProductList, new Predicate<Inventory>() {
			@Override
			public boolean test(Inventory i) {
				return true;
			}
		});
		inventoryList.setItems(inventoryfilteredData);
		setListenerForSearchbox();
		resetInventoryListCellFactory();
		resetSaleItemCellFactory();
		
		if(("1".equals(mode) || "2".equals(mode)) && editingSale!=null) {
			Instant instant = Instant.ofEpochMilli(editingSale.getSaleDate().getTime());
			LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
			saleDate.setValue(res);
			currentSelectedCust = editingSale.getCustomer();
			selectedCustTxt.setText(currentSelectedCust.getName());
			descriptionTxt.setText(editingSale.getDescription());
			ObservableList<SaleItem> obserSaleItems = FXCollections.observableArrayList(editingSale.getSaleItems());
			saleItemList.setItems(obserSaleItems);
			if("2".equals(mode)) {
				chooseCustBtn.setDisable(true);
				saleDate.setEditable(false);
				saleDate.setDisable(true);
				descriptionTxt.setEditable(false);
				selectProductBtn.setDisable(true);
				removeProductBtn.setDisable(true);
				registerSaleBtn.setDisable(true);
			}
		} else {
			editingSale = new Sale();
			saleDate.setValue(LocalDate.now());
		}
	}
	
	private void setListenerForSearchbox() {
		searchBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchBoxInputValue = newValue;
				inventoryfilteredData.setPredicate(new Predicate<Inventory>() {
					@Override
					public boolean test(Inventory i) {
						if(searchBoxInputValue == null || searchBoxInputValue.isEmpty()) {
							return true;
						}
						
						String lowerinputValue = searchBoxInputValue.toLowerCase();
						if(i.getProductName().toLowerCase().indexOf(lowerinputValue) != -1
								|| i.getProductCode().toLowerCase().indexOf(lowerinputValue) != -1) {
							resetInventoryListCellFactory();
							return true;
						}
						return false;
					}
				});
				
			}
		});
	}
	
	@FXML
	public void openCustPopup(ActionEvent event) {
		List<Customer> custList = em.createQuery("select cust from Customer cust", Customer.class).getResultList();
		custSearchBox = new TextField();
		obserCustList = FXCollections.observableArrayList(custList);
		Dialog dlg = new Dialog(null, "Chọn khách hàng", false, true);
		GridPane content = new GridPane();
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(100);
		content.getColumnConstraints().add(col);
		content.setHgap(10);
		content.setVgap(10);
		content.setPrefHeight(200);
		custSearchBox.setPromptText("Nhập chuỗi cần tìm");
		setSearchCustomerTextboxListener();
		content.add(custSearchBox, 0, 0);
		custfilteredData = new FilteredList<>(obserCustList, new Predicate<Customer>() {
			@Override
			public boolean test(Customer c) {
				return true;
			}
		});
		selectCustList = new ListView<Customer>(custfilteredData);
		resetCustomerListCellFactory();
		content.add(selectCustList, 0, 1);
		dlg.setResizable(false);
		dlg.setIconifiable(false);
		dlg.setContent(content);
		dlg.getActions().addAll(okSelectCustomer, Dialog.Actions.CANCEL);
		dlg.show();
	}
	
	private void setSearchCustomerTextboxListener() {
		custSearchBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				searchCustomerValue = newValue;
				custfilteredData.setPredicate(new Predicate<Customer>() {
					@Override
					public boolean test(Customer c) {
						if(searchCustomerValue == null || searchCustomerValue.isEmpty()) {
							return true;
						}
						
						String lowerinputValue = searchCustomerValue.toLowerCase();
						if(c.getName().toLowerCase().indexOf(lowerinputValue) != -1
								|| c.getCode().toLowerCase().indexOf(lowerinputValue) != -1) {
							resetCustomerListCellFactory();
							return true;
						}
						return false;
					}
				});
				
			}
		});
	}
	
	private void resetCustomerListCellFactory() {
		selectCustList.setCellFactory(new Callback<ListView<Customer>, ListCell<Customer>>() {
			@Override
			public ListCell<Customer> call(ListView<Customer> param) {
				ListCell<Customer> cell = new ListCell<Customer>(){
					@Override
					protected void updateItem(Customer c, boolean bln) {
						super.updateItem(c, bln);
						if(c != null) {
							setText("[" + c.getCode() + "]" + c.getName());
						}
					}
				};
				return cell;
			}
		});
	}
	
	@FXML
	public void registerSale(ActionEvent event) {
		if(saleItemList.getItems().isEmpty()) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Hãy chọn 1 sản phẩm...")
		      .showError();
			return;
		}
		if(currentSelectedCust==null) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Hãy chọn khách hàng...")
		      .showError();
			return;
		}
		BigDecimal total = new BigDecimal(0);
		for(SaleItem item : saleItemList.getItems()) {
			total = total.add(item.getSalePrice().multiply(new BigDecimal(item.getQuantity())));
		}
		Date lastUpdate = new Date();
		//Sale newSale = new Sale();
		editingSale.setCustomer(currentSelectedCust);
		GregorianCalendar cal = GregorianCalendar.from(saleDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		editingSale.setSaleDate(cal.getTime());
		editingSale.setSaleAmount(total);
		editingSale.setUnPayAmount(total);
		editingSale.setDescription(descriptionTxt.getText());
		editingSale.setStatus("0");
		editingSale.setLastupdate(lastUpdate);
		em.getTransaction().begin();
		if("1".equals(mode)) {//mode update
			em.merge(editingSale);
			List<SaleItem> itemList = em.createQuery("SELECT s FROM SaleItem s WHERE s.sale.id = :saleId", SaleItem.class).setParameter("saleId", editingSale.getId()).getResultList();
			if(itemList != null) {//revert inventory quantity
				for(SaleItem item : itemList) {
					ParcelItem pi = item.getParcelItem();
					pi.setRemain(pi.getRemain() + new Long(item.getQuantity()));
					em.merge(pi);
				}
			}
			em.createQuery("DELETE FROM SaleItem s WHERE s.sale.id = :saleId").setParameter("saleId", editingSale.getId()).executeUpdate();
		} else {
			em.persist(editingSale);
		}
		Map<Long, ImportParcel> mergedImportParcel = new HashMap<>();
		for(SaleItem item : saleItemList.getItems()) {
			item.setSale(editingSale);
			item.setLastupdate(lastUpdate);
			ParcelItem saleParcelItem = em.find(ParcelItem.class, item.getParcelItem().getId());
			saleParcelItem.setRemain(saleParcelItem.getRemain() - item.getQuantity());
			em.merge(saleParcelItem);
			ImportParcel parcel = saleParcelItem.getParcel();
			if(!mergedImportParcel.containsKey(parcel.getId())) {
				parcel.setStatus("1");
				em.merge(parcel);
				mergedImportParcel.put(parcel.getId(), parcel);
			}
			em.persist(item);
		}
		for(Inventory inv : inventoryList.getItems()) {
			inv.setLastupdate(lastUpdate);
			em.merge(inv);
		}
		em.getTransaction().commit();
		printReport(editingSale, saleItemList.getItems());
		resetFields();
		em.clear();
		if("1".equals(mode)) {
			backToSaleList.fire();
		}
	}
	
	@FXML
	public void selectProduct(ActionEvent event) {
		Inventory selectedInventory = inventoryList.getSelectionModel().getSelectedItem();
		if(selectedInventory == null) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Hãy chọn 1 sản phẩm...")
		      .showError();
			return;
		}
		List<ParcelItem> pItems = em
				.createQuery(
						"select pi from ParcelItem pi where pi.product.id = :productId and pi.remain > 0",
						ParcelItem.class)
				.setParameter("productId",
						selectedInventory.getProduct().getId()).getResultList();
		itemsFromSaleCB.setItems(FXCollections.observableArrayList(pItems));
		itemsFromSaleCB.setCellFactory(new Callback<ListView<ParcelItem>, ListCell<ParcelItem>>() {
			SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
			@Override
			public ListCell<ParcelItem> call(ListView<ParcelItem> param) {
				ListCell<ParcelItem> cell = new ListCell<ParcelItem>(){
					@Override
					protected void updateItem(ParcelItem i, boolean bln) {
						super.updateItem(i, bln);
						if(i != null) {
							setText(i.getCost_vnd()+ " (" + dFormat.format(i.getParcel().getImportDate()) + ")");
						} else {
							setText(null);
						}
					}
				};
				return cell;
			}
		});
		itemsFromSaleCB.setConverter(new StringConverter<ParcelItem>() {
			private ParcelItem item;
			SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
			@Override
			public String toString(ParcelItem object) {
				if(object != null) {
					item = object;
					return object.getCost_vnd()+ " (" + dFormat.format(object.getParcel().getImportDate()) + ")";
				}
				return null;
			}
			
			@Override
			public ParcelItem fromString(String string) {
				return item;
			}
		});
		itemsFromSaleCB.getSelectionModel().select(0);
		Dialog dlg = new Dialog(null, "Nhập thông tin", false, true);
		GridPane content = new GridPane();
		content.setHgap(10);
		content.setVgap(10);
		content.add(new Label("Đợt hàng"), 0, 0);
		content.add(itemsFromSaleCB, 1, 0);
		GridPane.setHgrow(itemsFromSaleCB, Priority.ALWAYS);
		quantity_txt.setText("");
		content.add(new Label("Số lượng"), 0, 1);
		content.add(quantity_txt, 1, 1);
		GridPane.setHgrow(quantity_txt, Priority.ALWAYS);
		salePrice_txt.setText("");
		content.add(new Label("Giá bán"), 0, 2);
		content.add(salePrice_txt, 1, 2);
		GridPane.setHgrow(salePrice_txt, Priority.ALWAYS);

		// create the dialog with a custom graphic and the gridpane above as the
		// main content region
		dlg.setResizable(false);
		dlg.setIconifiable(false);
		// dlg.setGraphic(new
		// ImageView(HelloDialog.class.getResource("login.png").toString()));
		dlg.setContent(content);
		dlg.getActions().addAll(okSelectQuantity, Dialog.Actions.CANCEL);
		Platform.runLater(new Runnable() {
			public void run() {
				quantity_txt.requestFocus();
			}
		});
		dlg.show();
	}
	
	@FXML
	public void removeProduct(ActionEvent event) {
		SaleItem selectedSaleItem = saleItemList.getSelectionModel().getSelectedItem();
		if(selectedSaleItem==null) {
			Dialogs.create().nativeTitleBar().title("Error")
					.message("Please select item to remove...").showError();
			return;
		}
		for(Inventory inv : inventoryList.getItems()) {
			if(inv.getProduct().getId().equals(selectedSaleItem.getProduct().getId())) {
				inv.setQoh(inv.getQoh() + selectedSaleItem.getQuantity());
				BigDecimal itemValue = selectedSaleItem.getParcelItem().getCost_vnd().multiply(new BigDecimal(selectedSaleItem.getQuantity()));
				inv.setTotalValue(itemValue);
				break;
			}
		}
		saleItemList.getItems().remove(selectedSaleItem);
		ObservableList<Inventory> tmpList = inventoryList.getItems();
		inventoryList.setItems(null);
		inventoryList.setItems(tmpList);
		resetSaleItemCellFactory();
	}
	
	@FXML
	private void backToPreviousScreen(ActionEvent event) {
		viewContext.register("isUpdate", null);
		viewContext.register("editingSale", null);
		if("1".equals(mode) || "2".equals(mode)) {
			backToSaleList.fire();
		} else {
			backToMainBtn.fire();
		}
	}
	
	@PreDestroy
	public void destroy() {
		
	}
	
	private boolean isValidData() {
		try {
			new BigDecimal(quantity_txt.getText());
			new BigDecimal(salePrice_txt.getText());
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private void resetFields() {
		saleDate.setValue(LocalDate.now());
		currentSelectedCust = null;
		selectedCustTxt.setText("");
		descriptionTxt.setText("");
		List<Inventory> iList = em.createQuery("select i from Inventory i", Inventory.class).getResultList();
		ObservableList<Inventory> obserProductList = FXCollections.observableArrayList(iList);
		inventoryList.setItems(null);
		inventoryList.setItems(obserProductList);
		saleItemList.setItems(null);
		saleItemList.setItems(FXCollections.observableArrayList(new ArrayList<SaleItem>()));
	}
	
	private void printReport(Sale sale, List<SaleItem> saleItemList) {
		//Sale sale = em.find(Sale.class, 2L);
		//List<SaleItem> saleItemList = sale.getSaleItems();
		InputStream jasperFileStream = null;
		try {
			String jasperFileName = "saleInvoice.jasper";
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			/*Enumeration<URL> names = cl.getResources("jasper/" + jasperFileName);
			while (names.hasMoreElements()) {
				URL jasperUrl = names.nextElement();
				jasperPath = jasperUrl.getPath();
				break;
			}*/
			jasperFileStream = cl.getResourceAsStream("jasper/" + jasperFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//prepare data
		Map<String, Object> parameters = new HashMap<String, Object>();
		int totalQuantity = 0;
		BigDecimal totalPrice = new BigDecimal(0);
		for(SaleItem item : saleItemList) {
			totalQuantity += item.getQuantity();
			if(item.getSalePrice()!=null) {
				totalPrice = totalPrice.add(item.getSalePrice().multiply(
						new BigDecimal(item.getQuantity())));
			}
		}
		if(sale!=null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(sale.getSaleDate());
			parameters.put("SALEID", Util.leftPadStringWithChar(sale.getId().toString(), 5, '0'));
			parameters.put("CUSTNAME", sale.getCustomer().getName());
			parameters.put("CUSTADDRESS", sale.getCustomer().getAddress());
			parameters.put("TOTALQUANTITY", totalQuantity);
			parameters.put("TOTAL", totalPrice);
			parameters.put("MONEYTEXT", Util.tranlate(totalPrice.toPlainString()));
			parameters.put("SALEDAY", Util.leftPadStringWithChar(cal.get(Calendar.DAY_OF_MONTH) + "", 2, '0'));
			parameters.put("SALEMONTH", Util.leftPadStringWithChar((cal.get(Calendar.MONTH) + 1) + "", 2, '0'));
			parameters.put("SALEYEAR", cal.get(Calendar.YEAR) + "");
			parameters.put("HASSADDRESS", addressCheckbox.isSelected());
		}
		JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(saleItemList);
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFileStream, parameters, datasource);
			String folderName = "PDF";
			File pdfFolder = new File(folderName);
			if(!pdfFolder.exists()) {
				pdfFolder.mkdir();
			}
			String pdfFilePath = folderName + "/" + sale.getId() + "_invoice.pdf";
			JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFilePath);
			File pdfFile = new File(pdfFilePath);
			if (pdfFile.exists()) {
				if(Desktop.isDesktopSupported()) {
					Desktop.getDesktop().open(pdfFile);
				} else {
					Dialogs.create().nativeTitleBar().title("Error")
							.message("Application library is not supported!")
							.showError();
					return;
				}
			} else {
				Dialogs.create().nativeTitleBar().title("Error")
						.message("File is not exists!")
						.showError();
				return;
			}
		} catch(JRException jrEx) {
			jrEx.printStackTrace();
			Dialogs.create().nativeTitleBar().title("Error")
					.message(jrEx.getMessage()).showError();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
			Dialogs.create().nativeTitleBar().title("Error")
					.message(ioEx.getMessage()).showError();
		}
	}
	
	private void resetSaleItemCellFactory() {
		saleItemList
				.setCellFactory(new Callback<ListView<SaleItem>, ListCell<SaleItem>>() {
					@Override
					public ListCell<SaleItem> call(ListView<SaleItem> param) {
						return new SaleItemCell();
					}
				});
	}
	
	private void resetInventoryListCellFactory() {
		inventoryList.setCellFactory(new Callback<ListView<Inventory>, ListCell<Inventory>>() {
			@Override
			public ListCell<Inventory> call(ListView<Inventory> param) {
				ListCell<Inventory> cell = new ListCell<Inventory>(){
					@Override
					protected void updateItem(Inventory i, boolean bln) {
						super.updateItem(i, bln);
						if(i != null) {
							setText("[" + i.getProductCode() + "]" + i.getProductName()+ " (" + i.getQoh() + ")");
						}
					}
				};
				return cell;
			}
		});
	}
	
	static class SaleItemCell extends ListCell<SaleItem> {
		@Override
		protected void updateItem(SaleItem s, boolean bln) {
			super.updateItem(s, bln);
			if(s != null) {
				setText(s.getProduct().getName()+ " (" + s.getQuantity() + ")");
			}
		}
	}

	
}
