package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.datafx.controller.ViewFactory;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.context.ViewFlowContext;
import org.datafx.controller.flow.FXMLFlowView;

import controllers.CustomerScreenCtrl;
import controllers.EditCustomerScreenCtrl;
import controllers.EditProductScreenCtrl;
import controllers.ImportListScreenCtrl;
import controllers.ImportParcelScreenCtrl;
import controllers.MainMenuCtrl;
import controllers.ProductScreenCtrl;
import controllers.QohProductScreenCtrl;
import controllers.SaleListScreenCtrl;
import controllers.SaleProductScreenCtrl;
import controllers.SearchScreenCtrl;
import controllers.SummaryScreenCtrl;


public class Main extends Application {
	
	@FXMLApplicationContext
	private ApplicationContext appCtx;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			prepareDB();
			
			ViewFlowContext flowContext = new ViewFlowContext();

			StackPane pane = new StackPane();
			ViewFactory.startFlowInPane(createAppFlow(),
					pane, flowContext);
			Scene myScene = new Scene(pane);
			primaryStage.setScene(myScene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * tao ket noi DB, giu lai trong context
	 */
	public void prepareDB() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("khainguyendb");
		EntityManager em = factory.createEntityManager();
		if(appCtx==null) {
			appCtx = ApplicationContext.getInstance();
		}
		appCtx.register("em", em);
	}
	
	/**
	 * Tao flow toan cuc cho chuong trinh
	 * @return view hien thi dau tien
	 */
	public FXMLFlowView createAppFlow() {
		FXMLFlowView mainMenuScreen = FXMLFlowView.create(MainMenuCtrl.class);
		FXMLFlowView productScreen = FXMLFlowView.create(ProductScreenCtrl.class);
		FXMLFlowView qohProductScreen = FXMLFlowView.create(QohProductScreenCtrl.class);
		FXMLFlowView editProductScreen = FXMLFlowView.create(EditProductScreenCtrl.class);
		FXMLFlowView importParcelScreen = FXMLFlowView.create(ImportParcelScreenCtrl.class);
		FXMLFlowView customerScreen = FXMLFlowView.create(CustomerScreenCtrl.class);
		FXMLFlowView editCustomerScreen = FXMLFlowView.create(EditCustomerScreenCtrl.class);
		FXMLFlowView saleProductScreen = FXMLFlowView.create(SaleProductScreenCtrl.class);
		FXMLFlowView saleListScreen = FXMLFlowView.create(SaleListScreenCtrl.class);
		FXMLFlowView SearchSummaryScreen = FXMLFlowView.create(SearchScreenCtrl.class);
		FXMLFlowView summaryScreen = FXMLFlowView.create(SummaryScreenCtrl.class);
		FXMLFlowView parcelListScreen = FXMLFlowView.create(ImportListScreenCtrl.class);
		mainMenuScreen.withChangeViewAction("gotoProduct", productScreen);
		mainMenuScreen.withChangeViewAction("gotoQoh", qohProductScreen);
		mainMenuScreen.withChangeViewAction("gotoImportParcel", importParcelScreen);
		mainMenuScreen.withChangeViewAction("gotoSaleProduct", saleProductScreen);
		mainMenuScreen.withChangeViewAction("gotoCustomer", customerScreen);
		mainMenuScreen.withChangeViewAction("gotoSaleList", saleListScreen);
		mainMenuScreen.withChangeViewAction("gotoSearchSummary", SearchSummaryScreen);
		mainMenuScreen.withChangeViewAction("gotoImportParcelList", parcelListScreen);
		productScreen.withChangeViewAction("gotoEditProduct", editProductScreen);
		productScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		editProductScreen.withChangeViewAction("backtoProduct", productScreen);
		qohProductScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		qohProductScreen.withChangeViewAction("gotoImportParcel", importParcelScreen);
		importParcelScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		importParcelScreen.withChangeViewAction("gotoParcelList", parcelListScreen);
		customerScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		customerScreen.withChangeViewAction("gotoEditCustomer", editCustomerScreen);
		editCustomerScreen.withChangeViewAction("backtoCustomerList", customerScreen);
		saleProductScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		saleProductScreen.withChangeViewAction("gotoSaleList", saleListScreen);
		saleListScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		saleListScreen.withChangeViewAction("gotoEditSale", saleProductScreen);
		SearchSummaryScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		SearchSummaryScreen.withChangeViewAction("gotoSummaryScreen", summaryScreen);
		summaryScreen.withChangeViewAction("gotoSearchSummary", SearchSummaryScreen);
		parcelListScreen.withChangeViewAction("gotoEditImport", importParcelScreen);
		parcelListScreen.withChangeViewAction("gotoMain", mainMenuScreen);
		
		return mainMenuScreen;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
