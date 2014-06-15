package controllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

@FXMLController("/fxml/SearchScreen.fxml")
public class SearchScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXML
	@FlowAction("gotoMain")
	private Button backBtn;
	
	@FlowAction("gotoSummaryScreen")
	private Button gotoSummaryScreen = new Button();
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@FXML
	private DatePicker fromDate;
	
	@FXML
	private DatePicker toDate;
	
	private Date currentFromDate;
	
	private Date currentToDate;
	
	@PostConstruct
	public void init() {
		fromDate.setValue(LocalDate.now().withDayOfMonth(1));
		toDate.setValue(LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1));
	}
	
	@FXML
	public void viewSummary(ActionEvent event) {
		if(checkValid()) {
			viewContext.register("fromDate", currentFromDate);
			viewContext.register("toDate", currentToDate);
			gotoSummaryScreen.fire();
		} else {
			Dialogs.create().style(DialogStyle.NATIVE)
		      .title("Error")
		      .message( "Hãy nhập thông tin đúng định dạng...")
		      .showError();
			return;
		}
	}
	
	private boolean checkValid() {
		if(fromDate.getValue()==null
				|| toDate.getValue()==null) {
			return false;
		}
		GregorianCalendar cal = GregorianCalendar.from(fromDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		Date fromD = cal.getTime();
		cal = GregorianCalendar.from(toDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()));
		Date toD = cal.getTime();
		if(fromD.compareTo(toD) > 0) {
			return false;
		}
		currentFromDate = fromD;
		currentToDate = toD;
		return true;
	}

}
