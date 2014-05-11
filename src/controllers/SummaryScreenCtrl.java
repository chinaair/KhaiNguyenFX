package controllers;

import java.math.BigDecimal;
import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

@FXMLController("/fxml/SummaryScreen.fxml")
public class SummaryScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private TextField totalCostTxt;
	
	@FXML
	private TextField totalSaleTxt;
	
	@FXML
	private TextField totalSaleProfitTxt;
	
	@FXML
	private TextField totalSaleCostTxt;
	
	@FXML
	private TextField totalCollectAmtTxt;
	
	@FXML
	private TextField totalSaleDebtTxt;
	
	@FXML
	private Hyperlink saleQuantityLink;
	
	@FXML
	private Hyperlink importQuantityLink;
	
	@FXML
	@FlowAction("gotoSearchSummary")
	private Button backBtn;
	
	@FlowAction("gotoSaleList")
	private Button gotoListSaleBtn = new Button();
	
	private EntityManager em;
	
	@PostConstruct
	public void init() {
		saleQuantityLink.setText("0");
		importQuantityLink.setText("0");
		totalCostTxt.setText("0");
		totalSaleTxt.setText("0");
		totalSaleCostTxt.setText("0");
		totalCollectAmtTxt.setText("0");
		totalSaleProfitTxt.setText("0");
		totalSaleDebtTxt.setText("0");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		Date fromDate = (Date)viewContext.getRegisteredObject("fromDate");
		Date toDate = (Date)viewContext.getRegisteredObject("toDate");
		Long importQuantity = em
				.createQuery(
						"select count(p) from ParcelItem p where p.parcel.importDate between :fromDate and :toDate",
						Long.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		Long saleQuantity = em
				.createQuery(
						"select count(s) from SaleItem s where s.sale.saleDate between :fromDate and :toDate",
						Long.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal importValue = em
				.createQuery(
						"select sum(p.importValue) from ImportParcel p where p.importDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal saleValue = em
				.createQuery(
						"select sum(s.saleAmount) from Sale s where s.saleDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal collectValue = em
				.createQuery(
						"select sum(c.amount) from CollectMoney c where c.collectDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal totalCost = em
				.createQuery(
						"select sum(s.parcelItem.cost_vnd * s.quantity) from SaleItem s where s.sale.saleDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal totalProfit = em
				.createQuery(
						"select sum((s.salePrice - s.parcelItem.cost_vnd) * s.quantity) from SaleItem s where s.sale.saleDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		BigDecimal totalDebt = em
				.createQuery(
						"select sum(s.unPayAmount) from Sale s where s.saleDate between :fromDate and :toDate",
						BigDecimal.class).setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate).getSingleResult();
		if(saleQuantity!=null) {
			saleQuantityLink.setText(saleQuantity.toString());
		}
		if(importQuantity!=null) {
			importQuantityLink.setText(importQuantity.toString());
		}
		if(importValue!=null) {
			totalCostTxt.setText(importValue.toPlainString());
		}
		if(saleValue!=null) {
			totalSaleTxt.setText(saleValue.toPlainString());
		}
		if(collectValue!=null) {
			totalCollectAmtTxt.setText(collectValue.toPlainString());
		}
		if(totalCost!=null) {
			totalSaleCostTxt.setText(totalCost.toPlainString());
		}
		if(totalProfit!=null) {
			totalSaleProfitTxt.setText(totalProfit.toPlainString());
		}
		if(totalDebt!=null) {
			totalSaleDebtTxt.setText(totalDebt.toPlainString());
		}
	}
	
	@FXML
	public void gotoSaleListScreen(ActionEvent event) {
		viewContext.register("fromSummary", "1");
		gotoListSaleBtn.fire();
	}

}
