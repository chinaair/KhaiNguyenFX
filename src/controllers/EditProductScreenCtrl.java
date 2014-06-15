package controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;

import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.FXMLViewFlowContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FlowAction;

import entity.Inventory;
import entity.Product;

@FXMLController("/fxml/EditProductScreen.fxml")
public class EditProductScreenCtrl {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	private Product editingProduct;
	
	private EntityManager em;
	
	@FXML
	private TextField productCodeTxt;
	
	@FXML
	private TextField productNameTxt;
	
	@FXML
	private TextField productTypeTxt;
	
	@FXML
	private TextArea productDescriptionTxt;
	
	@FXML
	@FlowAction("backtoProduct")
	private Button cancelBtn;

	@FXML
	public void registerProduct(ActionEvent event) {
		String isUpdate = (String)viewContext.getRegisteredObject("isUpdate");
		if(productCodeTxt.getText().isEmpty()
				|| productTypeTxt.getText().isEmpty()
				|| productNameTxt.getText().isEmpty()) {
			Dialogs.create().style(DialogStyle.NATIVE)
		      .title("Error")
		      .message( "Hãy nhập thông tin cần thiết...")
		      .showError();
			return;
		}
		if("1".equals(isUpdate)) {
			updateProduct();
		} else {
			insertProduct();
		}
		viewContext.register("isUpdate", null);
		viewContext.register("editingProduct", null);
		cancelBtn.fire();
	}
	
	private void updateProduct() {
		em.getTransaction().begin();
		Product updateObj = new Product();
		updateObj.setCode(productCodeTxt.getText());
		updateObj.setDescription(productDescriptionTxt.getText());
		updateObj.setName(productNameTxt.getText());
		if(productTypeTxt.getText() != null && productTypeTxt.getText().length() > 0) {
			String insertStr = productTypeTxt.getText().trim().toLowerCase();
			updateObj.setType(insertStr);
		}
		updateObj.setEnable(editingProduct.isEnable());
		updateObj.setId(editingProduct.getId());
		updateObj.setLastupdate(new Date());
		em.merge(updateObj);
		em.getTransaction().commit();
		editingProduct = updateObj;
	}
	
	private void insertProduct() {
		/*ObservableList<Product> pList = (ObservableList<Product>)viewContext
				.getRegisteredObject("editingList");*/
		em.getTransaction().begin();
		Product updateObj = new Product();
		updateObj.setCode(productCodeTxt.getText());
		updateObj.setDescription(productDescriptionTxt.getText());
		updateObj.setName(productNameTxt.getText());
		if(productTypeTxt.getText() != null && productTypeTxt.getText().length() > 0) {
			String insertStr = productTypeTxt.getText().trim().toLowerCase();
			updateObj.setType(insertStr);
		}
		updateObj.setEnable(true);
		updateObj.setLastupdate(new Date());
		
		Inventory inv = new Inventory();
		inv.setProduct(updateObj);
		inv.setLastupdate(new Date());
		inv.setQoh(new Long(0));
		inv.setTotalValue(BigDecimal.ZERO);
		em.persist(inv);
		em.getTransaction().commit();
		/*if(pList!=null) {
			pList.add(updateObj);
		}*/
	}
	
	@PostConstruct
	public void init() {
		editingProduct = (Product)viewContext.getRegisteredObject("editingProduct");
		em = (EntityManager)appCtx.getRegisteredObject("em");
		createAutoCompleteProductTypeField();
		if(editingProduct!=null) {
			productCodeTxt.setText(editingProduct.getCode());
			productNameTxt.setText(editingProduct.getName());
			productDescriptionTxt.setText(editingProduct.getDescription());
		}
	}
	
	private void createAutoCompleteProductTypeField() {
		List<String> typeList = em.createQuery("SELECT p.type FROM Product p WHERE p.type IS NOT NULL", String.class).getResultList();
		if(typeList != null && !typeList.isEmpty()) {
			TextFields.bindAutoCompletion(productTypeTxt, typeList);
		}
	}
	
	@PreDestroy
	public void destroy() {
		
	}

}
