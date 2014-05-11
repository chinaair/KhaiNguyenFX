package controllers;

import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.controlsfx.dialog.Dialogs;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import entity.Customer;

@FXMLController("/fxml/EditCustomerScreen.fxml")
public class EditCustomerScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	private EntityManager em;
	
	private Customer editingCustomer;
	
	@FXML
	private TextField codeTxt;
	
	@FXML
	private TextField nameTxt;
	
	@FXML
	private TextField contactPersonTxt;
	
	@FXML
	private TextField addressTxt;
	
	@FXML
	private TextField phoneTxt;
	
	@FXML
	private TextField emailTxt;
	
	@FXML
	private TextArea commentTxt;
	
	@FXML
	@FlowAction("backtoCustomerList")
	private Button cancelBtn;
	
	@FXML
	public void registerCustomer(ActionEvent event) {
		String isUpdate = (String)viewContext.getRegisteredObject("isUpdate");
		if(!checkValid()) {
			return;
		}
		if("1".equals(isUpdate)) {
			updateCustomer();
		} else {
			insertCustomer();
		}
		cancelBtn.fire();
	}
	
	private void updateCustomer() {
		em.getTransaction().begin();
		Customer updateObj = new Customer();
		updateObj.setCode(codeTxt.getText());
		updateObj.setName(nameTxt.getText());
		updateObj.setContactPerson(contactPersonTxt.getText());
		updateObj.setAddress(addressTxt.getText());
		updateObj.setPhone(phoneTxt.getText());
		updateObj.setEmail(emailTxt.getText());
		updateObj.setComment(commentTxt.getText());
		updateObj.setId(editingCustomer.getId());
		updateObj.setLastupdate(new Date());
		em.merge(updateObj);
		em.getTransaction().commit();
		editingCustomer = updateObj;
	}
	
	private void insertCustomer() {
		/*ObservableList<Customer> cList = (ObservableList<Customer>)viewContext
				.getRegisteredObject("editingList");*/
		em.getTransaction().begin();
		Customer updateObj = new Customer();
		updateObj.setCode(codeTxt.getText());
		updateObj.setName(nameTxt.getText());
		updateObj.setContactPerson(contactPersonTxt.getText());
		updateObj.setAddress(addressTxt.getText());
		updateObj.setPhone(phoneTxt.getText());
		updateObj.setEmail(emailTxt.getText());
		updateObj.setComment(commentTxt.getText());
		updateObj.setLastupdate(new Date());
		em.persist(updateObj);
		em.getTransaction().commit();
		/*if(cList!=null) {
			cList.add(updateObj);
		}*/
	}
	
	@PostConstruct
	public void init() {
		editingCustomer = (Customer)viewContext.getRegisteredObject("editingCustomer");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		if(editingCustomer!=null) {
			codeTxt.setText(editingCustomer.getCode());
			nameTxt.setText(editingCustomer.getName());
			contactPersonTxt.setText(editingCustomer.getContactPerson());
			addressTxt.setText(editingCustomer.getAddress());
			phoneTxt.setText(editingCustomer.getPhone());
			emailTxt.setText(editingCustomer.getEmail());
			commentTxt.setText(editingCustomer.getComment());
		}
	}
	
	@PreDestroy
	public void destroy() {
		
	}
	
	private boolean checkValid() {
		if(codeTxt.getText().isEmpty()
				|| nameTxt.getText().isEmpty()) {
			Dialogs.create().nativeTitleBar()
		      .title("Error")
		      .message( "Hãy nhập thông tin cần thiết...")
		      .showError();
			return false;
		}
		return true;
	}

}
