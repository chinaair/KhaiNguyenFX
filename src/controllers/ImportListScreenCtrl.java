package controllers;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import components.ParcelContextCellFactory;

import entity.ImportParcel;
import entity.ParcelItem;

@FXMLController("/fxml/ImportListScreen.fxml")
public class ImportListScreenCtrl {

	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	private EntityManager em;
	
	@FXML
	private TableColumn<ImportParcel, String> codeCol;
	
	@FXML
	private TableColumn<ImportParcel, String> dateCol;
	
	@FXML
	private TableColumn<ImportParcel, String> valueCol;
	
	@FXML
	private TableColumn<ImportParcel, String> rateCol;
	
	@FXML
	private TableColumn<ImportParcel, String> descriptionCol;
	
	@FXML
	private TableView<ImportParcel> importListView;
	
	@FlowAction("gotoEditImport")
	private Button editImportBtn = new Button();
	
	@FXML
	@FlowAction("gotoMain")
	private Button backBtn;
	
	@FXMLViewFlowContext
    private ViewFlowContext viewContext;
	
	@PostConstruct
	public void init() {
		em = (EntityManager)appCtx.getRegisteredObject("em");
		em.clear();
		List<ImportParcel> importList = em.createQuery("select i from ImportParcel i", ImportParcel.class).getResultList();
		ObservableList<ImportParcel> sObservableList = FXCollections.observableArrayList(importList);
		ParcelContextCellFactory parcelStrCellFactory = new ParcelContextCellFactory();
		setActionForContextMenu(parcelStrCellFactory);
		codeCol.setCellValueFactory(new PropertyValueFactory<ImportParcel, String>("code"));
		codeCol.setCellFactory(parcelStrCellFactory);
		dateCol.setCellValueFactory(new PropertyValueFactory<ImportParcel, String>("importDateString"));
		dateCol.setCellFactory(parcelStrCellFactory);
		valueCol.setCellValueFactory(new PropertyValueFactory<ImportParcel, String>("importValue"));
		valueCol.setCellFactory(parcelStrCellFactory);
		rateCol.setCellValueFactory(new PropertyValueFactory<ImportParcel, String>("rate"));
		rateCol.setCellFactory(parcelStrCellFactory);
		descriptionCol.setCellValueFactory(new PropertyValueFactory<ImportParcel, String>("description"));
		descriptionCol.setCellFactory(parcelStrCellFactory);
		importListView.setItems(sObservableList);
	}
	
	private void setActionForContextMenu(ParcelContextCellFactory cellFactory) {
		if(cellFactory==null) {
			return;
		}
		cellFactory.setViewItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "2");
				viewContext.register("editingParcel", importListView.getSelectionModel().getSelectedItem());
				editImportBtn.fire();
			}
		});
		cellFactory.setEditItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewContext.register("isUpdate", "1");
				viewContext.register("editingParcel", importListView.getSelectionModel().getSelectedItem());
				editImportBtn.fire();
			}
		});
		cellFactory.setDeleteItemAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ImportParcel currentParcel = importListView.getSelectionModel().getSelectedItem();
				em.getTransaction().begin();
				ImportParcel removeObj = em.find(ImportParcel.class, currentParcel.getId());
				if(removeObj!=null) {
					for(ParcelItem item : removeObj.getParcelItems()) {
						em.remove(item);
					}
					em.remove(removeObj);
				}
				em.getTransaction().commit();
				importListView.getItems().remove(importListView.getSelectionModel().getSelectedIndex());
			}
		});
	}
}
