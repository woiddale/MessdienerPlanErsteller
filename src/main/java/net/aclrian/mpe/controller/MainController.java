package net.aclrian.mpe.controller;

import java.io.IOException;
import java.net.URL;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import net.aclrian.mpe.controller.Select.Selecter;
import net.aclrian.mpe.messdiener.Messdiener;
import net.aclrian.mpe.utils.Dialogs;

import static net.aclrian.mpe.utils.Log.getLogger;

public class MainController {
	@FXML
	private ImageView gen_pic, medi_pic, messe_pic;
	@FXML
	private GridPane gen_pane, medi_pane, messe_pane;
	@FXML
	private GridPane grid;
	@FXML
	private AnchorPane apane;

	private Controller control;
	// private AData adata = new AData();

	public MainController() {
		;
	}

	private EnumPane ep;

	public enum EnumPane {
		messdiener("/view/messdiener.fxml"), messe(""), start("/view/mainmlg.fxml"), plan("/view/Aplan.fxml"),
		ferien(""), stdmesse(""), selectMedi("/view/select.fxml");
		private String location;

		EnumPane(String location) {
			this.location = location;
		}

		public String getLocation() {
			return location;
		}
	}

	public void initialize() {
		NumberBinding b = Bindings.min(Bindings.divide(gen_pane.heightProperty(), 1.5d),
				Bindings.divide(gen_pane.widthProperty(), 1.5d));
		gen_pic.fitWidthProperty().bind(Bindings.min(200, b));
		gen_pic.fitHeightProperty().bind(Bindings.min(200, b));
		medi_pic.fitWidthProperty().bind(Bindings.min(200, b));
		medi_pic.fitHeightProperty().bind(Bindings.min(200, b));
		messe_pic.fitWidthProperty().bind(Bindings.min(200, b));
		messe_pic.fitHeightProperty().bind(Bindings.min(200, b));
		changePane(EnumPane.start);
		getLogger().info("Startbildschirm geladen");
	}

	public void changePane(Messdiener messdiener) {
			this.ep = EnumPane.messdiener;
			apane.getChildren().removeIf(p -> true);
			URL u = getClass().getResource(ep.getLocation());
			FXMLLoader fl = new FXMLLoader(u);
			Parent p;
			try {
				p = fl.load();
				AnchorPane.setBottomAnchor(p, 0d);
				AnchorPane.setRightAnchor(p, 0d);
				AnchorPane.setLeftAnchor(p, 0d);
				AnchorPane.setTopAnchor(p, 0d);
				apane.getChildren().add(p);
				control = fl.getController();
				control.afterstartup();
				if (control instanceof MediController) {
					((MediController)control).setMedi(messdiener);
				}
			} catch (IOException e) {
				Dialogs.error(e, "Auf " + ep.getLocation() + " konnte nicht zugegriffen werden!");
			}
		}

	public void changePane(EnumPane ep) {
		if(this.ep == ep) {
			return;
		} else if (control == null || !control.isLocked()) {
			this.ep = ep;
			apane.getChildren().removeIf(p -> true);
			
				URL u = getClass().getResource(ep.getLocation());
				FXMLLoader fl = new FXMLLoader(u);
				Parent p;
				try {
					if (ep == EnumPane.selectMedi) {
						control = new Select(apane,Selecter.Messdiener, this);
						fl.setController(control);
					}
					p = fl.load();
					AnchorPane.setBottomAnchor(p, 0d);
					AnchorPane.setRightAnchor(p, 0d);
					AnchorPane.setLeftAnchor(p, 0d);
					AnchorPane.setTopAnchor(p, 0d);
					apane.getChildren().add(p);
					control = fl.getController();
					control.afterstartup();
				} catch (IOException e) {
					Dialogs.error(e, "Auf " + ep.getLocation() + " konnte nicht zugegriffen werden!");
				}

		} else {
			Dialogs.warn("Der Fensterbereich ist durch die Bearbeitung gesperrt!");
		}
	}

	public void messe(ActionEvent actionEvent) {
	}

	public void medi(ActionEvent actionEvent) {
		getLogger().info("zu Messdienern wechseln");
		changePane(EnumPane.selectMedi);
	}

	public void generieren(ActionEvent actionEvent) {
	}

	public void pfarrei_aendern(ActionEvent actionEvent) {
		// TODO
	}

	public void speicherort(ActionEvent actionEvent) {
	}

	public void ferienplan(ActionEvent actionEvent) {
	}

	public void standardmesse(ActionEvent actionEvent) {
	}

}