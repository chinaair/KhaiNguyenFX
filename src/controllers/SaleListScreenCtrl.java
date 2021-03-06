package controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Query;

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

import components.SaleContextCellFactory;

import entity.CollectMoney;
import entity.Inventory;
import entity.ParcelItem;
import entity.Sale;
import entity.SaleItem;

@FXMLController("/fxml/SaleListScreen.fxml")
public class SaleListScreenCtrl {

	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	private EntityManager em;
	
	@FXML
	private TableColumn<Sale, String> debtAmtCol;
	
	@FXML
	private TableColumn<Sale, String> saleDateCol;
	
	@FXML
	private TableColumn<Sale, String> customerCol;
	
	@FXML
	private TableColumn<Sale, String> statusCol;
	
	@FXML
	private TableColumn<Sale, String> descriptionCol;
	
	@FXML
	private TableView<Sale> saleTableView;
	
	@FlowAction("gotoEditSale")
	private Button editSaleBtn = new Button();
	
	@FlowAction("gotoMain")
	private Button backBtn = new Button();
	
	@FlowAction("gotoSummary")
	private Button backSummayBtn = new Button();
	
	@FlowAction("gotoCollectHistory")
	private Button collectHistoryBtn = new Button();
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	private Sale selectedSale;
	
	private final DatePicker receive_date_txt = new DatePicker();
	
	private final TextField amount_txt = new TextField();
	
	private final DialogAction actionInputInfo = new DefaultDialogAction("Ok") {
		{
			ButtonBar.setType(this, ButtonType.OK_DONE);
		}
		
		@Override
		public void handle(ActionEvent ae) {
			if (! this.isDisabled() && ae.getSource() instanceof Dialog) {
            	Dialog dlg = (Dialog) ae.getSource();
            	if(isValidData()) {
            		CollectMoney collect = new CollectMoney();
            		collect.setSaleId(selectedSale.getId());
            		collect.setAmount(new BigDecimal(amount_txt.getText()));
            		GregorianCalendar cal = GregorianCalendar.from(receive_date_txt.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
            		collect.setCollectDate(cal.getTime());
            		collect.setCustomerId(selectedSale.getCustomer().getId());
            		BigDecimal remainAmt = selectedSale.getUnPayAmount().subtract(collect.getAmount());
            		if(remainAmt.compareTo(new BigDecimal(0))==0) {
            			selectedSale.setStatus("1");
            		} else {
            			selectedSale.setStatus("2");
            		}
            		selectedSale.setUnPayAmount(remainAmt);
            		em.getTransaction().begin();
            		em.persist(collect);
            		em.merge(selectedSale);
            		em.getTransaction().commit();
            		saleTableView.getColumns().get(0).setVisible(false);
            		saleTableView.getColumns().get(0).setVisible(true);
    				dlg.hide();
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
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		Object fromSummary = viewContext.getRegisteredObject("fromSummary");
		String selectSql = "select s from Sale s";
		if("1".equals(fromSummary)) {
			selectSql += " where s.saleDate between :fromDate and :toDate";
		}
		Query q = em.createQuery(selectSql, Sale.class);
		if("1".equals(fromSummary)) {
			Date fromDate = (Date)viewContext.getRegisteredObject("fromDate");
			Date toDate = (Date)viewContext.getRegisteredObject("toDate");
			q.setParameter("fromDate", fromDate);
			q.setParameter("toDate", toDate);
		}
		@SuppressWarnings("unchecked")
		List<Sale> saleList = q.getResultList();
		ObservableList<Sale> sObservableList = FXCollections.observableArrayList(saleList);
		SaleContextCellFactory saleStrCellFactory = new SaleContextCellFactory();
		setActionForContextMenu(saleStrCellFactory);
		debtAmtCol.setCellValueFactory(new PropertyValueFactory<Sale, String>("unPayAmount"));
		debtAmtCol.setCellFactory(saleStrCellFactory);
		saleDateCol.setCellValueFactory(new PropertyValueFactory<Sale, String>("saleDateString"));
		saleDateCol.setCellFactory(saleStrCellFactory);
		customerCol.setCellValueFactory(new PropertyValueFactory<Sale, String>("customerName"));
		customerCol.setCellFactory(saleStrCellFactory);
		statusCol.setCellValueFactory(new PropertyValueFactory<Sale, String>("statusString"));
		statusCol.setCellFactory(saleStrCellFactory);
		descriptionCol.setCellValueFactory(new PropertyValueFactory<Sale, String>("description"));
		descriptionCol.setCellFactory(saleStrCellFactory);
		saleTableView.setItems(sObservableList);
	}
	
	@FXML
	public void backToPrevious(ActionEvent event) {
		Object fromSummary = viewContext.getRegisteredObject("fromSummary");
		viewContext.register("saleId", null);
		if("1".equals(fromSummary)) {
			viewContext.register("fromSummary", null);
			backSummayBtn.fire();
		} else {
			backBtn.fire();
		}
	}
	
	private void setActionForContextMenu(SaleContextCellFactory cellFactory) {
		if(cellFactory==null) {
			return;
		}
		cellFactory.setViewItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "2");
				viewContext.register("editingSale", saleTableView.getSelectionModel().getSelectedItem());
				editSaleBtn.fire();
			}
		});
		cellFactory.setEditItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "1");
				viewContext.register("editingSale", saleTableView.getSelectionModel().getSelectedItem());
				editSaleBtn.fire();
			}
		});
		cellFactory.setDeleteItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Sale currentSale = saleTableView.getSelectionModel().getSelectedItem();
				em.getTransaction().begin();
				Sale removeObj = em.find(Sale.class, currentSale.getId());
				Query q = em.createQuery("SELECT inv FROM Inventory inv WHERE inv.product.id = :productId", Inventory.class);
				if(removeObj!=null) {
					for(SaleItem item : removeObj.getSaleItems()) {
						Long saleQuan = new Long(item.getQuantity());
						ParcelItem pItem = item.getParcelItem();
						pItem.setRemain(pItem.getRemain() + saleQuan);
						em.merge(pItem);
						@SuppressWarnings("unchecked")
						List<Inventory> invs = q.setParameter("productId", item.getProduct().getId()).getResultList();
						if(invs != null && invs.size() > 0) {
							Inventory inv = invs.get(0);
							inv.setQoh(inv.getQoh() + saleQuan);
							BigDecimal saleAmt = pItem.getCost_vnd().multiply(new BigDecimal(saleQuan));
							inv.setTotalValue(inv.getTotalValue().add(saleAmt));
							em.merge(inv);
						}
						em.remove(item);
					}
					em.remove(removeObj);
				}
				em.getTransaction().commit();
				saleTableView.getItems().remove(saleTableView.getSelectionModel().getSelectedIndex());
			}
		});
		cellFactory.setProcessItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selectedSale = saleTableView.getSelectionModel().getSelectedItem();
				showInputInfoDialog();
			}
		});
		cellFactory.setHistoryItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selectedSale = saleTableView.getSelectionModel().getSelectedItem();
				viewContext.register("saleId", selectedSale.getId());
				collectHistoryBtn.fire();
			}
		});
	}
	
	private Action showInputInfoDialog() {
		Dialog dlg = new Dialog(null, "Nhập thông tin", false, DialogStyle.NATIVE);
		GridPane content = new GridPane();
	     content.setHgap(10);
	     content.setVgap(10);
	     content.add(new Label("Ngày nhận tiền"), 0, 0);
	     receive_date_txt.setValue(LocalDate.now());
	     amount_txt.setText("");
	     content.add(receive_date_txt, 1, 0);
	     GridPane.setHgrow(receive_date_txt, Priority.ALWAYS);
	     content.add(new Label("Số tiền"), 0, 1);
	     content.add(amount_txt, 1, 1);
	     GridPane.setHgrow(amount_txt, Priority.ALWAYS);
	     
	     // create the dialog with a custom graphic and the gridpane above as the
	     // main content region
	     dlg.setResizable(false);
	     dlg.setIconifiable(false);
	     //dlg.setGraphic(new ImageView(HelloDialog.class.getResource("login.png").toString()));
	     dlg.setContent(content);
	     dlg.getActions().addAll(actionInputInfo, Dialog.Actions.CANCEL);
	     Platform.runLater(new Runnable() {
	         public void run() {
	        	 receive_date_txt.requestFocus();
	         }
	     });
	     return dlg.show();
	}
	
	private boolean isValidData() {
		if(receive_date_txt.getValue()==null) {
			return false;
		}
		GregorianCalendar cal = GregorianCalendar.from(receive_date_txt.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		Date collectDate = cal.getTime();
		if(collectDate.compareTo(selectedSale.getSaleDate()) < 0
				|| collectDate.compareTo(new Date()) > 0) {
			return false;
		}
		try {
			new BigDecimal(amount_txt.getText());
		} catch(NumberFormatException e) {
			return false;
		}
		BigDecimal amt = new BigDecimal(amount_txt.getText());
		if(selectedSale.getUnPayAmount().subtract(amt).compareTo(new BigDecimal(0))==-1) {
			return false;
		}
		return true;
	}
	
}
