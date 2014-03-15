package controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
	
	private Customer selectedCustomer;
	
	@FXML
	private DatePicker saleDate;
	
	@FXML
	private TextArea descriptionTxt;
	
	@FXML
	private TextField selectedCustTxt;
	
	@FXML
	private ListView<Inventory> inventoryList;
	
	@FXML
	private ListView<SaleItem> saleItemList;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	
	private Button cancelBtn;
	
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
            		Product invProduct = selectedInventory.getProduct();
            		int quantity = Integer.parseInt(quantity_txt.getText());
            		if(quantity > itemsFromSaleCB.getSelectionModel().getSelectedItem().getRemain().intValue()) {
						Dialogs.create().nativeTitleBar().title("Error")
								.message("Sale quantity can not be greater than quantity on hand")
								.showError();
						return;
            		}
            		boolean added = false;
            		for(SaleItem item : saleItemList.getItems()) {
            			if(item.getProduct().equals(invProduct)
            					&& item.getParcelItem().getId().equals(itemsFromSaleCB.getSelectionModel().getSelectedItem().getId())) {
            				item.setQuantity(item.getQuantity() + quantity);
            				item.setParcelItem(itemsFromSaleCB.getSelectionModel().getSelectedItem());
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
            		ObservableList<Inventory> tmpList = inventoryList.getItems();
            		inventoryList.setItems(null);
            		inventoryList.setItems(tmpList);
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
	
	@PostConstruct
	public void init() {
		mode = (String)viewContext.getRegisteredObject("isUpdate");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		List<Customer> custList = em.createQuery("select cust from Customer cust", Customer.class).getResultList();
		List<Inventory> iList = em.createQuery("select i from Inventory i", Inventory.class).getResultList();
		obserCustList = FXCollections.observableArrayList(custList);
		ObservableList<Inventory> obserProductList = FXCollections.observableArrayList(iList);
		inventoryList.setItems(obserProductList);
		inventoryList.setCellFactory(new Callback<ListView<Inventory>, ListCell<Inventory>>() {
			
			@Override
			public ListCell<Inventory> call(ListView<Inventory> param) {
				ListCell<Inventory> cell = new ListCell<Inventory>(){
					@Override
					protected void updateItem(Inventory i, boolean bln) {
						super.updateItem(i, bln);
						if(i != null) {
							setText(i.getProductName()+ " (" + i.getQoh() + ")");
						}
					}
				};
				return cell;
			}
		});
		saleItemList.setCellFactory(new Callback<ListView<SaleItem>, ListCell<SaleItem>>() {
			
			@Override
			public ListCell<SaleItem> call(ListView<SaleItem> param) {
				ListCell<SaleItem> cell = new ListCell<SaleItem>(){
					@Override
					protected void updateItem(SaleItem s, boolean bln) {
						super.updateItem(s, bln);
						if(s != null) {
							setText(s.getProduct().getName()+ " (" + s.getQuantity() + ")");
						}
					}
				};
				return cell;
			}
		});
		editingSale = (Sale)viewContext.getRegisteredObject("editingSale");
		if("1".equals(mode) && editingSale!=null) {
			Instant instant = Instant.ofEpochMilli(editingSale.getSaleDate().getTime());
			LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
			saleDate.setValue(res);
			currentSelectedCust = editingSale.getCustomer();
			selectedCustTxt.setText(currentSelectedCust.getName());
			descriptionTxt.setText(editingSale.getDescription());
			ObservableList<SaleItem> obserSaleItems = FXCollections.observableArrayList(editingSale.getSaleItems());
			saleItemList.setItems(obserSaleItems);
		} else {
			editingSale = new Sale();
			saleDate.setValue(LocalDate.now());
		}
		
		
	}
	
	@FXML
	public void openCustPopup(ActionEvent event) {
		Dialog dlg = new Dialog(null, "Select customer");
		GridPane content = new GridPane();
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(100);
		content.getColumnConstraints().add(col);
		content.setHgap(10);
		content.setVgap(10);
		content.setPrefHeight(200);
		selectCustList = new ListView<Customer>(obserCustList);
		selectCustList.setCellFactory(new Callback<ListView<Customer>, ListCell<Customer>>() {
			@Override
			public ListCell<Customer> call(ListView<Customer> param) {
				ListCell<Customer> cell = new ListCell<Customer>(){
					@Override
					protected void updateItem(Customer c, boolean bln) {
						super.updateItem(c, bln);
						if(c != null) {
							setText(c.getName());
						}
					}
				};
				return cell;
			}
		});
		content.add(selectCustList, 0, 0);
		dlg.setResizable(false);
		dlg.setIconifiable(false);
		dlg.setContent(content);
		dlg.getActions().addAll(okSelectCustomer, Dialog.Actions.CANCEL);
		dlg.show();
	}
	
	@FXML
	public void registerSale(ActionEvent event) {
		if(saleItemList.getItems().isEmpty()) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Please add at least one product...")
		      .showError();
			return;
		}
		if(currentSelectedCust==null) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Please select customer...")
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
			if(itemList != null) {
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
		for(SaleItem item : saleItemList.getItems()) {
			item.setSale(editingSale);
			item.setLastupdate(lastUpdate);
			ParcelItem saleParcelItem = item.getParcelItem();
			saleParcelItem.setRemain(saleParcelItem.getRemain() - item.getQuantity());
			em.merge(saleParcelItem);
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
	}
	
	@FXML
	public void selectProduct(ActionEvent event) {
		Inventory selectedInventory = inventoryList.getSelectionModel().getSelectedItem();
		List<ParcelItem> pItems =  em.createQuery("select pi from ParcelItem pi where pi.product.id = :productId and pi.remain > 0", ParcelItem.class).setParameter("productId", selectedInventory.getProduct().getId()).getResultList();
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
						}
					}
				};
				return cell;
			}
		});
		Dialog dlg = new Dialog(null, "Input information");
		GridPane content = new GridPane();
		content.setHgap(10);
		content.setVgap(10);
		content.add(new Label("Dot hang"), 0, 0);
		content.add(itemsFromSaleCB, 1, 0);
		GridPane.setHgrow(itemsFromSaleCB, Priority.ALWAYS);
		content.add(new Label("So luong"), 0, 1);
		content.add(quantity_txt, 1, 1);
		GridPane.setHgrow(quantity_txt, Priority.ALWAYS);
		content.add(new Label("Gia ban"), 0, 2);
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
				break;
			}
		}
		saleItemList.getItems().remove(selectedSaleItem);
		ObservableList<Inventory> tmpList = inventoryList.getItems();
		inventoryList.setItems(null);
		inventoryList.setItems(tmpList);
	}
	
	@FXML
	private void backToPreviousScreen(ActionEvent event) {
		if("1".equals(mode)) {
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
		String jasperPath = "";
		try {
			String jasperFileName = "saleInvoice.jasper";
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> names = cl.getResources("jasper/" + jasperFileName);
			while (names.hasMoreElements()) {
				URL jasperUrl = names.nextElement();
				jasperPath = jasperUrl.getPath();
				break;
			}
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
			parameters.put("SALEMONTH", Util.leftPadStringWithChar(cal.get(Calendar.MONTH) + "", 2, '0'));
			parameters.put("SALEYEAR", cal.get(Calendar.YEAR) + "");
		}
		JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(saleItemList);
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, datasource);
			JasperExportManager.exportReportToPdfFile(jasperPrint, "sample_report.pdf");
			File pdfFile = new File("sample_report.pdf");
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

	
}
