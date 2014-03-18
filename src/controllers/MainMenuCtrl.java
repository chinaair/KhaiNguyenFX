package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.FlowAction;

@FXMLController("/fxml/MainMenuScreen.fxml")
public class MainMenuCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXML
	@FlowAction("gotoProduct")
	private Button gotoProductBtn;
	
	@FXML
	@FlowAction("gotoQoh")
	private Button gotoQohBtn;
	
	@FXML
	@FlowAction("gotoImportParcel")
	private Button gotoImportParcelBtn;
	
	@FXML
	@FlowAction("gotoCustomer")
	private Button gotoCustomerBtn;
	
	@FXML
	@FlowAction("gotoSaleProduct")
	private Button gotoSaleBtn;
	
	@FXML
	@FlowAction("gotoSaleList")
	private Button gotoSaleListBtn;
	
	@FXML
	@FlowAction("gotoSearchSummary")
	private Button summaryBtn;
	
	@FXML
	@FlowAction("gotoImportParcelList")
	private Button parcelListBtn;
	
	public void gotoProductBtnOnclick(ActionEvent event) {
		
	}
	
	@PostConstruct
	public void init() {
		
	}
	
	@PreDestroy
	public void destroy() {
		
	}

}
