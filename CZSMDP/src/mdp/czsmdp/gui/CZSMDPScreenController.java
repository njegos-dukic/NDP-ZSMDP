package mdp.czsmdp.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import mdp.logger.MDPLogger;
import mdp.rest.model.Station;
import mdp.rest.model.StationArrivals;
import mdp.rest.model.Stations;
import mdp.rest.model.Timetable;
import mdp.rest.model.Timetables;
import mdp.rmi.server.ArchivatorI;
import mdp.rmi.server.Report;
import mdp.rmi.server.ReportMetadata;
import mdp.soap.server.User;
import mdp.soap.server.UserManagementService;
import mdp.soap.server.UserManagementServiceServiceLocator;

public class CZSMDPScreenController extends Application {

	@FXML
	private VBox stationVBox;

	@FXML
	private VBox usersVBox;

	@FXML
	private VBox timetableVBox;

	@FXML
	private VBox reportVBox;

	@FXML
	private ImageView logoImageView;

	@FXML
	private TextField stationInputField;

	@FXML
	private Button addUser;

	@FXML
	private TextField newUserUsername;

	@FXML
	private PasswordField newUserPassword;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab stationsTab;

	@FXML
	private ListView<String> newTimetableList;

	@FXML
	private Tab usersTab;

	@FXML
	private Tab timetableTab;

	@FXML
	private Tab reportsTab;

	@FXML
	private VBox newUserStationsVBox;

	@FXML
	private TextField newTimetableTimeInput;

	@FXML
	private VBox newTimetableStationsVBox;

	@FXML
	private Button notificationButton;

	private String username = "czsmdp";

	private ArrayList<String> receivedNotifications = new ArrayList<>();

	private boolean receivingMessages = true;

	private ComboBox<String> newTimetableStationsComboBox = new ComboBox<>();

	private ComboBox<String> newUserStationsComboBox = new ComboBox<>();

	private ArrayList<Station> stations = new ArrayList<>();

	private VBox notificationsVBoxWrapper = new VBox();

	private ScrollPane notificationsSP = new ScrollPane();

	private VBox notificationsVBox = new VBox();

	public static String RESOURCES_ROOT = "./resources/";
	public static String RESOURCES_FILENAME = "app.properties";

	private static Properties prop = new Properties();

	static {
		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES_ROOT + RESOURCES_FILENAME);
			prop.load(input);
		} catch (FileNotFoundException e1) {
			MDPLogger.log(Level.INFO, "Propperties file not found.");
		} catch (IOException e) {
			MDPLogger.log(Level.INFO, "Error opening properties file.");
		}
	}

	@FXML
	void showNotification(ActionEvent event) {
		Dialog<Void> notificationsDialog = new Dialog<>();
		notificationsDialog.getDialogPane().setMinHeight(400.0);
		notificationsDialog.setHeaderText(prop.getProperty("NOTIFICATION_DIALOG_HEADER"));
		notificationsDialog.setTitle(prop.getProperty("NOTIFICATION_DIALOG_TITLE"));
		((Stage) notificationsDialog.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(prop.getProperty("LOGO_FILE")));
		notificationsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		notificationsDialog.getDialogPane().getChildren().add(notificationsVBoxWrapper);
		notificationsDialog.showAndWait();
		notificationButton.setStyle(null);
	}

	@FXML
	void addStation(ActionEvent event) {
		try {
			ClientConfig clientConfig = new ClientConfig();
			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_STATIONS_BASE_URI"));
			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

			Gson gson = new Gson();
			invocationBuilder.post(Entity.json(gson.toJson(new Station(0, stationInputField.getText()))));

			stationInputField.setText("");

			this.stationsSelected();

		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Adding station failed.");
		}
	}

	@FXML
	void stationsSelected() {
		try {
			stationVBox.getChildren().clear();

			ClientConfig clientConfig = new ClientConfig();
			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_STATIONS_BASE_URI"));
			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.get();

			Stations stationsObject = response.readEntity(Stations.class);

			stations.clear();
			stations.addAll(stationsObject.getStations());

			stationVBox.getChildren().add(new Separator());
			stationVBox.getChildren().add(new Separator());
			stationVBox.getChildren().add(new Separator());

			for (Station s : stations) {

				HBox stationHBox = new HBox();

				Separator separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				stationHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				stationHBox.getChildren().add(separator);

				Text stationText = new Text(s.getName());
				stationText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
				TextFlow stationTextFlow = new TextFlow(stationText);
				stationTextFlow.setStyle("-fx-padding: 6 0 0 4;");
				stationTextFlow.setMinWidth(545.0);
				stationTextFlow.setPrefWidth(545.0);
				stationTextFlow.setMaxWidth(545.0);

				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);

				stationHBox.getChildren().addAll(stationTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				stationHBox.getChildren().add(separator);

				Button removeButton = new Button();
				removeButton.setText(prop.getProperty("REMOVE_BUTTON_TEXT"));
				removeButton.setMinWidth(120.0);
				removeButton.setPrefWidth(120.0);
				removeButton.setMaxWidth(120.0);

				removeButton.setMinHeight(40.0);
				removeButton.setPrefHeight(40.0);
				removeButton.setMaxHeight(40.0);

				removeButton.setOnAction(new EventHandler<ActionEvent>() {
					int id = s.getId();

					@Override
					public void handle(ActionEvent e) {
						try {
							WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_STATIONS_BASE_URI")).path(id + "");
							Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
							invocationBuilder.delete();

							stationsSelected();
						} catch (Exception e1) {
							MDPLogger.log(Level.INFO, "Failed deleting station.");
						}
					}
				});

				stationHBox.getChildren().add(removeButton);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				stationHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				stationHBox.getChildren().add(separator);

				stationVBox.getChildren().add(stationHBox);

				stationVBox.getChildren().add(new Separator());
				stationVBox.getChildren().add(new Separator());
				stationVBox.getChildren().add(new Separator());
			}

		} catch (Exception e) {
			MDPLogger.log(Level.INFO, "Failed showing stations.");
		}
	}

	@FXML
	void reportsSelected() {
		reportVBox.getChildren().clear();

		try {
			String nm = prop.getProperty("RMI_SERVER_NAME");
			ArchivatorI srv = (ArchivatorI) Naming.lookup(prop.getProperty("RMI_SERVER_ADDRESS") + nm);
			ArrayList<ReportMetadata> reports = srv.list();

			reportVBox.getChildren().add(new Separator());
			reportVBox.getChildren().add(new Separator());
			reportVBox.getChildren().add(new Separator());

			for (ReportMetadata report : reports) {
				HBox reportHBox = new HBox();

				Hyperlink linkToFile = new Hyperlink();
				linkToFile.setWrapText(true);
				linkToFile.setText(report.getFileName());
				linkToFile.setOnAction(new EventHandler<ActionEvent>() {
					String fileName = report.getFileName();
					ArchivatorI srvI = (ArchivatorI) Naming.lookup(prop.getProperty("RMI_SERVER_ADDRESS") + nm);

					@Override
					public void handle(ActionEvent e) {
						try {
							Report linkedReport = srvI.download(fileName);
							byte[] fileContent = linkedReport.getFileContent();

							File file = new File(prop.getProperty("DOWNLOADED_REPORTS_FOLDER") + File.separator + fileName);

							OutputStream os = new FileOutputStream(file);
							os.write(fileContent);
							os.close();

							Desktop.getDesktop().open(file);
						} catch (IOException e1) {
							MDPLogger.log(Level.INFO, "Failed downloading and opening report.");
						}
					}
				});

				Separator separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);

				linkToFile.setMinWidth(265.0);
				linkToFile.setPrefWidth(265.0);
				linkToFile.setMaxWidth(265.0);
				reportHBox.getChildren().add(linkToFile);

				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);

				Text username = new Text(report.getUserName());
				TextFlow usernameTextFlow = new TextFlow(username);
				usernameTextFlow.setMinWidth(133.0);
				usernameTextFlow.setPrefWidth(133.0);
				usernameTextFlow.setMaxWidth(133.0);

				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().addAll(usernameTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);

				Text uploadTime = new Text(report.getUploadTime() + "");
				TextFlow uploadTextFlow = new TextFlow(uploadTime);
				uploadTextFlow.setMinWidth(133.0);
				uploadTextFlow.setPrefWidth(133.0);
				uploadTextFlow.setMaxWidth(133.0);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().addAll(uploadTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);

				double size = report.getSize();
				String unit = " B";

				if (size > 1024 * 1024 * 1024) {
					size = size / (1024 * 1024 * 1024);
					unit = " GB";
				}

				else if (size > 1024 * 1024) {
					size = size / (1024 * 1024);
					unit = " MB";
				}

				else if (size > 1024) {
					size = size / (1024);
					unit = " KB";
				}

				Text sizeText = new Text((Math.round(size * 100.0) / 100.0) + unit);
				TextFlow sizeTextFlow = new TextFlow(sizeText);
				sizeTextFlow.setMinWidth(110.0);
				sizeTextFlow.setPrefWidth(110.0);
				sizeTextFlow.setMaxWidth(110.0);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().addAll(sizeTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				reportHBox.getChildren().add(separator);

				reportVBox.getChildren().add(reportHBox);
				reportVBox.getChildren().add(new Separator());
				reportVBox.getChildren().add(new Separator());
				reportVBox.getChildren().add(new Separator());
			}
		} catch (RemoteException e) {
			MDPLogger.log(Level.WARNING, "Reports RMI failed.");
		} catch (MalformedURLException e) {
			MDPLogger.log(Level.WARNING, "Reports MalformedURL.");
		} catch (NotBoundException e) {
			MDPLogger.log(Level.WARNING, "Reports RMI Not Bound.");
		}
	}

	@FXML
	void pushToList(ActionEvent event) {
		String station = newTimetableStationsComboBox.getValue();
		String time = newTimetableTimeInput.getText();

		if (station == null || time == null || "".equals(station) || "".equals(time)
				|| !time.matches(prop.getProperty("TIME_VALIDATION_REGEX")))
			return;

		for (String s : newTimetableList.getItems())
			if (s.split(" ", 2)[1].contains(station))
				newTimetableList.getItems().remove(s);

		newTimetableList.getItems().add(time + " " + station);
		newTimetableStationsComboBox.setValue("");
		newTimetableTimeInput.setText("");
	}

	@FXML
	void clearNewTimetableList(ActionEvent event) {
		newTimetableList.getItems().clear();
	}

	@FXML
	void addTimetable(ActionEvent event) {
		if (newTimetableList.getItems().size() <= 1)
			return;

		ArrayList<StationArrivals> stationsAndArrivals = new ArrayList<>();

		for (String station : newTimetableList.getItems()) {
			Station st = stations.stream().filter(s -> s.getName().equals(station.split(" ", 2)[1])).findFirst().get();

			stationsAndArrivals.add(new StationArrivals(st, station.split(" ")[0], ""));
		}

		Timetable postTimetable = new Timetable(0, stationsAndArrivals);

		try {
			ClientConfig clientConfig = new ClientConfig();
			Client client = ClientBuilder.newClient(clientConfig);
			WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_TIMETABLES_BASE_URI"));
			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

			Gson gson = new Gson();
			invocationBuilder.post(Entity.json(gson.toJson(postTimetable)));

			newTimetableList.getItems().clear();
			timetableSelected();
		} catch (Exception e) {
			MDPLogger.log(Level.INFO, "Failed viewing timetables.");
		}
	}

	@FXML
	void timetableSelected() {
		timetableVBox.getChildren().clear();
		newTimetableStationsVBox.getChildren().clear();
		newTimetableStationsComboBox.getItems().clear();

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_TIMETABLES_BASE_URI"));
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();

		try {
			newTimetableStationsComboBox.setMinWidth(200.0);
			for (Station s : stations)
				newTimetableStationsComboBox.getItems().add(s.getName());

			newTimetableStationsVBox.getChildren().add(newTimetableStationsComboBox);
		} catch (Exception e) {
			MDPLogger.log(Level.INFO, "Error adding stations in timetables combo box.");
		}

		Timetables timetablesObject = response.readEntity(Timetables.class);

		timetableVBox.getChildren().add(new Separator());
		timetableVBox.getChildren().add(new Separator());
		timetableVBox.getChildren().add(new Separator());

		for (Timetable t : timetablesObject.getTimetables()) {

			HBox timetableHBox = new HBox();

			Separator separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableHBox.getChildren().add(separator);
			separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableHBox.getChildren().add(separator);

			String expectedArrivalString = "";
			String stationsString = "";
			String listedArrivalString = "";

			VBox timetableVBoxInner = new VBox();

			boolean isFirst = true;

			for (StationArrivals s : t.getStations()) {
				HBox singleEntry = new HBox();

				expectedArrivalString = s.getExpectedArrival();
				stationsString = s.getStation().getName();
				listedArrivalString = s.getPostedArrival();

				Text expectedArrivalText = new Text(expectedArrivalString);
				expectedArrivalText.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

				Text stationText = new Text(stationsString);
				stationText.setStyle("-fx-font-size: 17px;");

				Text listedArrivalText = new Text(listedArrivalString);
				listedArrivalText.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

				TextFlow expectedArrivalTextFlow = new TextFlow(expectedArrivalText);
				if (isFirst)
					expectedArrivalTextFlow.setStyle("-fx-padding: 6 0 0 4;");

				else
					expectedArrivalTextFlow.setStyle("-fx-padding: 0 0 0 4;");

				expectedArrivalTextFlow.setMinWidth(60.0);
				expectedArrivalTextFlow.setPrefWidth(60.0);
				expectedArrivalTextFlow.setMaxWidth(60.0);

				TextFlow stationTextFlow = new TextFlow(stationText);
				if (isFirst)
					stationTextFlow.setStyle("-fx-padding: 6 0 0 4;");

				else
					stationTextFlow.setStyle("-fx-padding: 0 0 0 4;");
				stationTextFlow.setMinWidth(183.0);
				stationTextFlow.setPrefWidth(183.0);
				stationTextFlow.setMaxWidth(183.0);

				TextFlow listedArrivalTextFlow = new TextFlow(listedArrivalText);
				if (isFirst) {
					listedArrivalTextFlow.setStyle("-fx-padding: 6 0 0 4;");
					isFirst = false;
				}

				else
					listedArrivalTextFlow.setStyle("-fx-padding: 0 0 0 4;");
				listedArrivalTextFlow.setMinWidth(60.0);
				listedArrivalTextFlow.setPrefWidth(60.0);
				listedArrivalTextFlow.setMaxWidth(60.0);

				singleEntry.getChildren().addAll(expectedArrivalTextFlow, stationTextFlow, listedArrivalTextFlow);
				timetableVBoxInner.getChildren().add(singleEntry);
			}

			separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableVBoxInner.setStyle("-fx-padding: 0 0 10 0;");
			timetableHBox.getChildren().addAll(timetableVBoxInner, separator);
			separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableHBox.getChildren().add(separator);

			Button removeButton = new Button();
			removeButton.setText(prop.getProperty("REMOVE_BUTTON_TEXT"));
			removeButton.setMinWidth(120.0);
			removeButton.setPrefWidth(120.0);
			removeButton.setMaxWidth(120.0);

			removeButton.setMinHeight(40.0);
			removeButton.setPrefHeight(40.0);
			removeButton.setMaxHeight(40.0);

			removeButton.setOnAction(new EventHandler<ActionEvent>() {
				int id = t.getId();

				@Override
				public void handle(ActionEvent e) {
					try {
						WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("REST_TIMETABLES_BASE_URI")).path(id + "");
						Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
						invocationBuilder.delete();

						timetableSelected();
					} catch (Exception e1) {
						MDPLogger.log(Level.INFO, "Failed deleting timetable.");
					}
				}
			});

			timetableHBox.setAlignment(Pos.CENTER_LEFT);
			timetableHBox.getChildren().add(removeButton);
			separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableHBox.getChildren().add(separator);
			separator = new Separator();
			separator.setOrientation(Orientation.VERTICAL);
			timetableHBox.getChildren().add(separator);

			timetableVBox.getChildren().add(timetableHBox);

			timetableVBox.getChildren().add(new Separator());
			timetableVBox.getChildren().add(new Separator());
			timetableVBox.getChildren().add(new Separator());
		}
	}

	@FXML
	void usersSelected() {
		usersVBox.getChildren().clear();
		newUserStationsVBox.getChildren().clear();
		newUserStationsComboBox.getItems().clear();

		try {
			newUserStationsComboBox.setMinWidth(200.0);
			for (Station s : stations)
				newUserStationsComboBox.getItems().add(s.getName());

			newUserStationsVBox.getChildren().add(newUserStationsComboBox);
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error adding stations to users combo box.");
		}

		UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();

		usersVBox.getChildren().add(new Separator());
		usersVBox.getChildren().add(new Separator());
		usersVBox.getChildren().add(new Separator());

		try {
			UserManagementService service = locator.getUserManagementService();
			for (User u : service.getAllUsers().getUsers()) {

				HBox userHBox = new HBox();
				userHBox.setAlignment(Pos.CENTER_LEFT);

				Separator separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);

				Text usernameText = new Text(u.getUsername());

				if (u.isIsOnline())
					usernameText.setStyle("-fx-fill: green; -fx-font-size: 18px; -fx-font-weight: bold;");
				else
					usernameText.setStyle("-fx-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");

				TextFlow usernameTextFlow = new TextFlow(usernameText);
				usernameTextFlow.setStyle("-fx-padding: 6 0 0 4;");
				usernameTextFlow.setMinWidth(290.0);
				usernameTextFlow.setPrefWidth(290.0);
				usernameTextFlow.setMaxWidth(290.0);

				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().addAll(usernameTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);
				Station station = null;

				try {
					station = stations.stream().filter(s -> s.getId() == u.getIdZS()).findFirst().get();
				} catch (NoSuchElementException e) {
					continue;
				}

				Text stationText = new Text(station.getName());
				stationText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
				TextFlow stationTextFlow = new TextFlow(stationText);
				stationTextFlow.setStyle("-fx-padding: 6 0 0 4;");
				stationTextFlow.setMinWidth(243.0);
				stationTextFlow.setPrefWidth(243.0);
				stationTextFlow.setMaxWidth(243.0);

				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().addAll(stationTextFlow, separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);

				Button removeButton = new Button();
				removeButton.setText(prop.getProperty("REMOVE_BUTTON_TEXT"));
				removeButton.setMinWidth(120.0);
				removeButton.setPrefWidth(120.0);
				removeButton.setMaxWidth(120.0);

				removeButton.setMinHeight(40.0);
				removeButton.setPrefHeight(40.0);
				removeButton.setMaxHeight(40.0);

				removeButton.setOnAction(new EventHandler<ActionEvent>() {
					String id = u.getUsername();

					@Override
					public void handle(ActionEvent e) {
						try {
							UserManagementServiceServiceLocator serviceLocator = new UserManagementServiceServiceLocator();
							UserManagementService userService = serviceLocator.getUserManagementService();
							userService.removeUser(id);
							usersSelected();
						} catch (Exception e1) {
							MDPLogger.log(Level.INFO, "Failed deleting user.");
						}
					}
				});

				userHBox.getChildren().add(removeButton);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);
				separator = new Separator();
				separator.setOrientation(Orientation.VERTICAL);
				userHBox.getChildren().add(separator);

				usersVBox.getChildren().add(userHBox);

				usersVBox.getChildren().add(new Separator());
				usersVBox.getChildren().add(new Separator());
				usersVBox.getChildren().add(new Separator());
			}
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error showing users.");
		}
	}

	@FXML
	void addUserAction(ActionEvent event) {
		try {
			UserManagementServiceServiceLocator serviceLocator = new UserManagementServiceServiceLocator();
			UserManagementService userService = serviceLocator.getUserManagementService();
			Station station = stations.stream().filter(s -> s.getName().equals(newUserStationsComboBox.getValue()))
					.findFirst().get();

			userService.addUser(newUserUsername.getText(), newUserPassword.getText(), station.getId(),
					station.getName());

			newUserUsername.setText("");
			newUserPassword.setText("");

			usersSelected();
		} catch (Exception e1) {
			MDPLogger.log(Level.INFO, "Error adding Users via SOAP.");
		}
	}

	@FXML
	void openNotificationDialog(ActionEvent event) {
		TextInputDialog input = new TextInputDialog();
		input.setTitle("ZSMDP - Novo obavještenje");
		input.setHeaderText("Novo obavještenje");
		((Stage) input.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(prop.getProperty("LOGO_FILE")));
		input.setContentText(prop.getProperty("NEW_NOTIFICATION_CONTENT"));
		input.showAndWait();
		String notification = input.getResult();

		if (notification == null)
			return;

		if ("".equals(notification)) {
			Dialog<Void> alert = new Dialog<>();
			alert.setTitle(prop.getProperty("NEW_NOTIFICATION_TITLE"));
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
					.add(new Image(prop.getProperty("LOGO_FILE")));
			alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			alert.setContentText(prop.getProperty("INVALID_NEW_NOTIFICATION_CONTENT"));
			alert.showAndWait();
			return;
		}

		(new Thread(() -> {
			String INET_ADDR = prop.getProperty("MULTICAST_IP_ADDRESS");
			int PORT = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));

			try {
				InetAddress addr = InetAddress.getByName(INET_ADDR);
				byte[] buf = (username + ": " + notification).getBytes();
				DatagramSocket socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, PORT);
				socket.send(packet);
				socket.close();
			} catch (UnknownHostException e1) {
				MDPLogger.log(Level.WARNING, "Multicast Unknown Host.");
			} catch (IOException ioe) {
				MDPLogger.log(Level.WARNING, "Multicast IOException.");
			}
		})).start();
	}

	@FXML
	void refreshContent(ActionEvent event) {
		if (stationsTab.isSelected())
			stationsSelected();

		else if (reportsTab.isSelected())
			reportsSelected();

		else if (usersTab.isSelected())
			usersSelected();

		else if (timetableTab.isSelected())
			timetableSelected();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setAndLaunchStartScreen(primaryStage);
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(prop.getProperty("REST_BASE_URI")).build();
	}

	private void setAndLaunchStartScreen(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(prop.getProperty("CZSMDP_FXML")));
		loader.setController(this);
		Parent root = loader.load();
		Scene sc = new Scene(root);
		primaryStage.setScene(sc);
		primaryStage.setTitle(prop.getProperty("WINDOW_TITLE"));
		primaryStage.setResizable(false);
		sc.setFill(Color.TRANSPARENT);
		Image logo = new Image(prop.getProperty("LOGO_FILE"));
		logoImageView.setImage(logo);
		primaryStage.getIcons().add(logo);
		setup();
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void setup() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				notificationsVBoxWrapper.getChildren().add(notificationsSP);
				notificationsVBoxWrapper.setMinWidth(340.0);
				notificationsVBoxWrapper.setMinHeight(350.0);
				notificationsVBoxWrapper.setMaxWidth(340.0);
				notificationsVBoxWrapper.setMaxHeight(350.0);
				notificationsVBoxWrapper.setStyle("-fx-margin: 0 0 50 0; -fx-padding: 70 80 15 20;");

				notificationsVBox.setMinWidth(290.0);
				notificationsVBox.setMinHeight(255.0);
				notificationsVBox.setMaxWidth(290.0);

				notificationsSP.setContent(notificationsVBox);
				notificationsSP.setMinWidth(320.0);
				notificationsSP.setMinHeight(265.0);
				notificationsSP.setMaxWidth(320.0);
			}
		});

		Thread multicastClient = new Thread(() -> {
			byte[] buf = new byte[512];
			try (MulticastSocket socket = new MulticastSocket(Integer.parseInt(prop.getProperty("MULTICAST_PORT")))) {
				InetAddress groupAddress = InetAddress.getByName(prop.getProperty("MULTICAST_IP_ADDRESS"));
				socket.joinGroup(groupAddress);
				while (receivingMessages) {
					DatagramPacket packet = new DatagramPacket(buf, 512);
					socket.receive(packet);
					String receivedNotification = new String(packet.getData());

					String[] parts = receivedNotification.split(":", 2);
					if (parts.length != 2)
						return;

					receivedNotifications.add(receivedNotification);

					Text userText = new Text(parts[0] + ": ");
					if (parts[0].equals(username)) {
						notificationButton.setStyle("-fx-text-fill: #00f;");
						userText.setStyle("-fx-font-weight: bold; -fx-fill: #00f");
					}

					else {
						notificationButton.setStyle("-fx-text-fill: #f00;");
						userText.setStyle("-fx-font-weight: bold; -fx-fill: #f00");
					}

					Text notificationText = new Text(parts[1]);
					TextFlow notificationTextFlow = new TextFlow(userText, notificationText);

					Platform.runLater(() -> {
						notificationsVBox.getChildren().add(notificationTextFlow);
						notificationsVBox.getChildren().add(new Separator());
					});

					buf = new byte[512];
					receivedNotification = "";
				}
			} catch (IOException ioe) {
				MDPLogger.log(Level.WARNING, "Error receiving multicast message.");
			}
		});

		multicastClient.setDaemon(true);
		multicastClient.start();
	}
}