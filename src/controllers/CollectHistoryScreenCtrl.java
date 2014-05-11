package controllers;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import entity.CollectMoney;

@FXMLController("/fxml/CollectHistoryScreen.fxml")
public class CollectHistoryScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	private EntityManager em;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private TableView<CollectMoney> collectTableView;
	
	@FXML
	private TableColumn<CollectMoney, String> collectDateCol;
	
	@FXML
	private TableColumn<CollectMoney, String> customerCol;
	
	@FXML
	private TableColumn<CollectMoney, String> amtCol;
	
	@FXML
	@FlowAction("gotoSaleList")
	private Button backBtn;
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		Long saleId = (Long)viewContext.getRegisteredObject("saleId");
		List<CollectMoney> collectList = em
				.createQuery(
						"SELECT c FROM CollectMoney c WHERE c.saleId = :saleId",
						CollectMoney.class).setParameter("saleId", saleId)
				.getResultList();
		ObservableList<CollectMoney> cObservableList = FXCollections.observableArrayList(collectList);
		collectDateCol.setCellValueFactory(new PropertyValueFactory<CollectMoney, String>("collectDateString"));
		customerCol.setCellValueFactory(new PropertyValueFactory<CollectMoney, String>("customerName"));
		amtCol.setCellValueFactory(new PropertyValueFactory<CollectMoney, String>("amount"));
		collectTableView.setItems(cObservableList);
	}

}
