package mdp.zsmdp.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.rpc.ServiceException;

import org.glassfish.jersey.client.ClientConfig;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import mdp.logger.MDPLogger;
import mdp.rest.model.Station;
import mdp.rest.model.StationArrivals;
import mdp.rest.model.Stations;
import mdp.rest.model.Timetable;
import mdp.rest.model.Timetables;
import mdp.rmi.server.ArchivatorI;
import mdp.rmi.server.Report;
import mdp.soap.server.SerializableStation;
import mdp.soap.server.UserManagementService;
import mdp.soap.server.UserManagementServiceServiceLocator;

public class MainScreenController {
	@FXML
	private Pane rootPane;

	@FXML
	private Pane chatBoxArea;

	@FXML
	private Text idText;

	@FXML
	private TextArea messageInput;

	@FXML
	private ImageView logoImageView;

	@FXML
	private VBox comboBoxes;

	@FXML
	private VBox chatArea;

	@FXML
	private Button attachButton;

	@FXML
	private Button sendMessageButton;

	@FXML
	private ScrollPane chatScrollPane;

	@FXML
	private VBox currentChats;

	@FXML
	private Button sendNotificationButton;

	@FXML
	private Button notificationButton;

	private SerializableStation thisStation;

	private ArrayList<String> receivedNotifications = new ArrayList<>();

	private ComboBox<String> zsmdpComboBox;

	private ComboBox<String> userComboBox;

	private File attachedFile;

	private String username;

	private String selectedZSMDP;

	private String selectedUser;

	private HashMap<String, VBox> chats = new HashMap<>();

	private boolean receivingMessages = true;

	private ServerSocket ss = null;

	private Socket client = null;

	private String newUserSelected = "";

	private ScrollPane notificationsSP = new ScrollPane();

	private VBox notificationsVBox = new VBox();

	private VBox notificationsVBoxWrapper = new VBox();

	private ScrollPane timetableSP = new ScrollPane();

	private VBox timetableVBox = new VBox();

	private VBox timetableVBoxWrapper = new VBox();

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
		notificationsDialog.setHeaderText(prop.getProperty("SHOW_NOTIFICATIONS_HEADER"));
		notificationsDialog.setTitle(prop.getProperty("SHOW_NOTIFICATIONS_TITLE"));
		((Stage) notificationsDialog.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(prop.getProperty("LOGO_FILE")));
		notificationsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		notificationsDialog.getDialogPane().getChildren().add(notificationsVBoxWrapper);
		notificationsDialog.showAndWait();
		notificationButton.setStyle(null);
	}

	@FXML
	void openNotificationDialog(ActionEvent event) {
		TextInputDialog input = new TextInputDialog();
		input.setTitle(prop.getProperty("NEW_NOTIFICATION_TITLE"));
		input.setHeaderText(prop.getProperty("NEW_NOTIFICATION_HEADER"));
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
			alert.setContentText(prop.getProperty("NEW_NOTIFICATION_NO_CONTENT_WARNING"));
			alert.showAndWait();
			return;
		}

		(new Thread(() -> {
			String INET_ADDR = prop.getProperty("MULTICAST_IP");
			int PORT = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));

			try {
				InetAddress addr = InetAddress.getByName(INET_ADDR);
				byte[] buf = (username + ": " + notification).getBytes();
				DatagramSocket socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, PORT);
				socket.send(packet);
				socket.close();
			} catch (UnknownHostException e1) {
				MDPLogger.log(Level.WARNING, "Multicast unknown host.");
			} catch (IOException ioe) {
				MDPLogger.log(Level.WARNING, "Multicast IOException.");
			}
		})).start();
	}

	@FXML
	void getTimetables(ActionEvent event) {
		Dialog<Void> timetableDialog = new Dialog<>();
		timetableDialog.getDialogPane().setMinHeight(400.0);
		timetableDialog.setHeaderText(prop.getProperty("TIMETABLE_HEADER") + thisStation.getName());
		timetableDialog.setTitle(prop.getProperty("TIMETABLE_TITLE"));
		((Stage) timetableDialog.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(prop.getProperty("LOGO_FILE")));
		timetableDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		getTimetable();
		timetableDialog.getDialogPane().getChildren().add(timetableVBoxWrapper);
		timetableDialog.showAndWait();
	}

	@FXML
	void attachFile(ActionEvent event) {
		ArrayList<ExtensionFilter> filters = new ArrayList<>();
		filters.add(new ExtensionFilter(prop.getProperty("ATTACH_FILE_DESCRIPTOR"), "*.*"));
		attachedFile = pickFile(event, filters);

		if (attachedFile != null)
			attachButton.setStyle("-fx-background-color: #e0b6b6; -fx-border-radius: 3; -fx-border-width: 1px; -fx-border-color: #4a4a4a");

		else
			attachButton.setStyle(null);
	}

	@FXML
	void sendMessage(ActionEvent event) {
		Thread clientThread = new Thread() {
			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				UserManagementService service = null;

				try {
					service = locator.getUserManagementService();
					if (!service.isOnline(selectedUser)) {
						sendMessageButton.setDisable(true);
						attachButton.setDisable(true);
						messageInput.setDisable(true);

						Platform.runLater(() -> {
							userComboBox.setValue(null);
							Dialog<Void> alert = new Dialog<>();
							alert.setTitle(prop.getProperty("PRIMARY_STAGE_TITLE"));
							((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
									.add(new Image(prop.getProperty("LOGO_FILE")));
							alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
							alert.setContentText(prop.getProperty("USER_LOGGED_OUT"));
							alert.showAndWait();
							messageInput.setText("");
						});

						return;
					}

					String HOST = prop.getProperty("LOCALHOST_IP");
					int PORT = service.getPort(selectedUser);

					String message = messageInput.getText().trim();
					if (message == "" && attachedFile == null)
						return;

					try {
						SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
						client = ssf.createSocket(HOST, PORT);

						PrintWriter out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
						{

							out.println(username);
							if (message != "")
								out.println(message);

							if (attachedFile != null) {
								out.println(prop.getProperty("PROTOCOL_BEGIN_FILE"));
								String fileName = attachedFile.getName().toString();
								out.println(fileName.split("\\.")[0]);
								out.println("." + fileName.split("\\.")[1]);

								byte[] fileToSend = new byte[10 * 1024 * 1024];
								InputStream in = new FileInputStream(attachedFile);
								OutputStream outS = client.getOutputStream();

								int count;
								while ((count = in.read(fileToSend)) > 0) {
									outS.write(fileToSend, 0, count);
								}

								outS.close();
								in.close();
							}

							if (!chats.containsKey(selectedUser)) {
								VBox newChat = new VBox();

								Text userText = new Text(selectedUser);
								userText.setStyle("-fx-font-weight: bold;");
								TextFlow usernameTextFlow = new TextFlow(userText);

								HBox userTopHBox = new HBox();

								userTopHBox.setAlignment(Pos.CENTER);
								userTopHBox.getChildren().add(usernameTextFlow);

								newChat.getChildren().add(userTopHBox);
								newChat.getChildren().add(new Separator());

								HBox sideChat = new HBox();
								Text sideChatUserText = new Text(selectedUser);
								sideChatUserText.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
								TextFlow sideChatUsernameTextFlow = new TextFlow(sideChatUserText);
								sideChat.setAlignment(Pos.CENTER);
								sideChat.getChildren().add(sideChatUsernameTextFlow);
								sideChat.setAccessibleText(selectedUser);

								sideChat.setOnMouseEntered(new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent t) {
										sideChatUserText.setStyle(
												"-fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: #f00;");
									}
								});

								sideChat.setOnMouseExited(new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent t) {
										sideChatUserText.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
									}
								});

								sideChat.setOnMouseClicked(e -> {
									Platform.runLater(() -> {
										userComboBox.setValue(sideChat.getAccessibleText());
									});
								});

								Platform.runLater(() -> {
									currentChats.getChildren().add(sideChat);
									currentChats.getChildren().add(new Separator());
								});

								chats.put(selectedUser, newChat);
							}

							updateChatBox(username, message, chats.get(selectedUser));
							updateChatLayout(chats.get(selectedUser), "");

							out.close();
							client.close();
						}

					} catch (UnknownHostException e) {
						MDPLogger.log(Level.WARNING, "Host Unknown.");
					} catch (IOException e) {
						MDPLogger.log(Level.WARNING, "IO Exception occured");
					}

				} catch (Exception e) {
					MDPLogger.log(Level.INFO, "Sending message failed.");
				}
			}
		};
		clientThread.setDaemon(true);
		clientThread.start();
	}

	@FXML
	void onZSMDPChanged(ActionEvent event) {
		updateChatLayout(null, "");
		userComboBox.getItems().clear();
		selectedZSMDP = zsmdpComboBox.getValue();

		(new Thread() {
			String[] activeUsers = null;

			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				try {
					UserManagementService service = locator.getUserManagementService();
					activeUsers = service.getAllActiveUsersByStationName(selectedZSMDP);
				} catch (Exception e) {
					MDPLogger.log(Level.WARNING, "Error getting active users from SOAP.");
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (activeUsers == null)
							return;

						for (String s : activeUsers) {
							if (!s.equals(username))
								userComboBox.getItems().add(s);
						}
					}
				});
			}
		}).start();
	}

	@FXML
	void onUserChanged(ActionEvent event) {
		updateChatLayout(null, "");
		if (userComboBox.getValue() != null || !"".equals(newUserSelected)) {
			String oldUser = this.selectedUser;
			this.selectedUser = userComboBox.getValue();
			sendMessageButton.setDisable(false);
			messageInput.setDisable(false);
			attachButton.setDisable(false);
			updateChatLayout(chats.get(selectedUser), oldUser);
		}

		else if (!"".equals(newUserSelected)) {
			String oldUser = this.selectedUser;
			this.selectedUser = newUserSelected;
			sendMessageButton.setDisable(false);
			messageInput.setDisable(false);
			attachButton.setDisable(false);
			updateChatLayout(chats.get(selectedUser), oldUser);
			newUserSelected = "";
		}
	}

	@FXML
	void sendReport(ActionEvent event) {
		try {
			ArrayList<ExtensionFilter> filters = new ArrayList<>();
			filters.add(new ExtensionFilter(prop.getProperty("REPORT_FILE_DESCRIPTOR"), "*.pdf"));
			File report = pickFile(event, filters);
			byte[] bytes = Files.readAllBytes(report.toPath());

			String nm = prop.getProperty("RMI_NAME");
			ArchivatorI srv = (ArchivatorI) Naming.lookup(prop.getProperty("RMI_ADDRESS") + nm);
			Report reportMetadata = new Report(bytes, report.getName(), username, new Date());
			srv.upload(reportMetadata);

			Dialog<Void> alert = new Dialog<>();
			alert.setTitle(prop.getProperty("REPORT_TITLE"));
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
					.add(new Image(prop.getProperty("LOGO_FILE")));
			alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			alert.setContentText(prop.getProperty("RMI_FILE_UPLOADED"));
			alert.showAndWait();

		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Error sending report.");
		}
	}

	@FXML
	public void logOut(ActionEvent event) {
		receivingMessages = false;

		try {
			ss.close();
			client.close();
		} catch (Exception e1) {
			MDPLogger.log(Level.INFO, "Socket closed.");
		}

		(new Thread() {
			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				try {
					UserManagementService service = locator.getUserManagementService();
					service.logOutUser(username);
				} catch (Exception e) {
					MDPLogger.log(Level.WARNING, "Error logging user out.");
				}
			}
		}).start();

		if (event == null)
			Platform.exit();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((Button) event.getSource()).getScene().getWindow().hide();
				StartScreenController controller = new StartScreenController();
				try {
					controller.start(new Stage());
				} catch (IOException e) {
					MDPLogger.log(Level.WARNING, "Error starting new stage.");
				}
			}
		});
	}

	private void updateChatBox(String user, String messageString, VBox userChat) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				HBox usernameHBox = new HBox();
				HBox message = new HBox();
				HBox file = new HBox();

				Text userText = new Text(user);
				userText.setStyle("-fx-font-weight: bold");
				TextFlow usernameText = new TextFlow(userText);
				usernameHBox.getChildren().add(usernameText);

				Text newTextMessage = new Text(messageString);
				TextFlow newMessage = null;

				Hyperlink linkToFile = new Hyperlink();

				if (!"".equals(messageString)) {
					newMessage = new TextFlow(newTextMessage);
					message.setStyle("-fx-padding: 0 0 10 0;");
					message.getChildren().add(newMessage);
				}

				if (attachedFile != null) {
					linkToFile.setWrapText(true);
					linkToFile.setText(attachedFile.getName());
					linkToFile.setOnAction(new EventHandler<ActionEvent>() {
						File linkedFile = attachedFile;

						@Override
						public void handle(ActionEvent e) {
							try {
								Desktop.getDesktop().open(linkedFile);
							} catch (IOException e1) {
								MDPLogger.log(Level.INFO, "Error opening file.");
							}
						}
					});

					file.setStyle("-fx-padding: 0 0 10 0;");
					file.getChildren().add(linkToFile);
				}

				if (!user.startsWith(username)) {
					message.setStyle("-fx-padding: 0 140 0 0;");
					file.setStyle("-fx-padding: 0 140 0 0;");

					usernameHBox.setAlignment(Pos.BASELINE_LEFT);
					message.setAlignment(Pos.BASELINE_LEFT);
					file.setAlignment(Pos.BASELINE_LEFT);
				}

				else {
					message.setStyle("-fx-padding: 0 0 0 140;");
					file.setStyle("-fx-padding: 0 0 0 140;");

					usernameHBox.setAlignment(Pos.BASELINE_RIGHT);
					message.setAlignment(Pos.BASELINE_RIGHT);
					file.setAlignment(Pos.BASELINE_RIGHT);
				}

				if (message.getChildren().size() > 0 || file.getChildren().size() > 0)
					userChat.getChildren().add(usernameHBox);

				if (message.getChildren().size() > 0)
					userChat.getChildren().add(message);

				if (file.getChildren().size() > 0)
					userChat.getChildren().add(file);

				messageInput.setText("");
				attachedFile = null;
				attachButton.setStyle(null);

				chatScrollPane.layout();
				chatScrollPane.setVvalue(chatScrollPane.vmaxProperty().doubleValue());
			}
		});
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(prop.getProperty("REST_BASELINE_URI")).build();
	}

	private void updateChatLayout(VBox chat, String oldUser) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					chats.get(oldUser).getChildren().addAll(chatArea.getChildren());
				} catch (Exception e) { }

				try {
					chatArea.getChildren().addAll(chat.getChildren());
				} catch (Exception e) { }
			}
		});
	}

	private void receiveMessage() {
		Thread serverThread = new Thread() {
			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				UserManagementService service = null;
				try {
					service = locator.getUserManagementService();
				} catch (ServiceException e1) {
					MDPLogger.log(Level.INFO, "Error getting SOAP service info: Receiving messages.");
				}
				try {
					if (service == null)
						return;

					int PORT = service.getPort(username);

					SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
					ss = ssf.createServerSocket(PORT);
					Socket sc = null;
					BufferedReader in = null;

					while (receivingMessages) {
						sc = ss.accept();
						in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
						String message = "";

						String user = "";
						String line = "";

						while (((line = in.readLine()) != null)) {
							if (user == "")
								user = line;

							else if (prop.getProperty("PROTOCOL_BEGIN_FILE").equals(line)) {
								String fileName = in.readLine();
								String fileExtension = in.readLine();

								InputStream is = sc.getInputStream();
								int nRead;
								byte[] file = new byte[1];
								OutputStream buffer = new ByteArrayOutputStream();

								while ((nRead = is.read(file, 0, file.length)) != -1)
									buffer.write(file, 0, nRead);

								buffer.flush();
								byte[] targetArray = ((ByteArrayOutputStream) buffer).toByteArray();

								attachedFile = Files.createTempFile(fileName, fileExtension).toFile();

								is.close();
								buffer = new FileOutputStream(attachedFile);
								buffer.write(targetArray);
								buffer.close();
							}

							else
								message += line;
						}

						if (!"".equals(user) && !chats.containsKey(user)) {
							VBox newChat = new VBox();

							Text userText = new Text(user);
							userText.setStyle("-fx-font-weight: bold;");
							TextFlow usernameTextFlow = new TextFlow(userText);

							HBox userTopHBox = new HBox();
							userTopHBox.setAlignment(Pos.CENTER);
							userTopHBox.getChildren().add(usernameTextFlow);

							newChat.getChildren().add(userTopHBox);
							newChat.getChildren().add(new Separator());

							chats.put(user, newChat);

							HBox sideChat = new HBox();
							Text sideChatUserText = new Text(user);
							sideChatUserText.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
							TextFlow sideChatUsernameTextFlow = new TextFlow(sideChatUserText);
							sideChat.setAlignment(Pos.CENTER);
							sideChat.getChildren().add(sideChatUsernameTextFlow);
							sideChat.setAccessibleText(user);

							sideChat.setOnMouseEntered(new EventHandler<MouseEvent>() {
								@Override
								public void handle(MouseEvent t) {
									sideChatUserText
											.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: #f00;");
								}
							});

							sideChat.setOnMouseExited(new EventHandler<MouseEvent>() {
								@Override
								public void handle(MouseEvent t) {
									sideChatUserText.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
								}
							});

							sideChat.setOnMouseClicked(e -> {
								Platform.runLater(() -> {
									userComboBox.setValue(sideChat.getAccessibleText());
								});
							});

							Platform.runLater(() -> {
								currentChats.getChildren().add(sideChat);
								currentChats.getChildren().add(new Separator());
							});
						}

						updateChatBox(user, message, chats.get(user));
						if (user.equals(selectedUser))
							updateChatLayout(chats.get(selectedUser), "");
					}

					in.close();
					ss.close();
				} catch (Exception e) {
					MDPLogger.log(Level.INFO, "Receiving message exception.");
				}
			}
		};

		serverThread.setDaemon(true);
		serverThread.start();
	}

	public void setup() {
		sendMessageButton.setDisable(true);
		messageInput.setDisable(true);
		attachButton.setDisable(true);

		ComboBox<String> zsmdp = new ComboBox<>();
		ComboBox<String> users = new ComboBox<>();

		zsmdp.setMinHeight(35);
		zsmdp.setPrefHeight(35);
		zsmdp.setMaxHeight(35);
		zsmdp.setMinWidth(190);
		zsmdp.setPrefWidth(190);
		zsmdp.setMaxWidth(190);

		users.setMinHeight(35);
		users.setPrefHeight(35);
		users.setMaxHeight(35);
		users.setMinWidth(190);
		users.setPrefWidth(190);
		users.setMaxWidth(190);

		zsmdp.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				onZSMDPChanged(event);
			}
		});

		users.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				onUserChanged(event);
			}
		});

		zsmdpComboBox = zsmdp;
		userComboBox = users;


		(new Thread() {
			SerializableStation[] zsmdps = null;

			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				try {
					UserManagementService service = locator.getUserManagementService();
					zsmdps = service.getAllStations();
					thisStation = service.getByIdZSMDP(service.getIdZsmdp(username));

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							idText.setText(thisStation.getName().toUpperCase() + ": " + username.toUpperCase());
						}
					});
				} catch (Exception e) {
					MDPLogger.log(Level.INFO, "Error getting SOAP service.");
				}

				ClientConfig clientConfig = new ClientConfig();
				Client client = ClientBuilder.newClient(clientConfig);
				WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("STATIONS_REST_PATH"));
				Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
				Response response = invocationBuilder.get();

				Stations stationsObject = response.readEntity(Stations.class);

				for (Station ss : stationsObject.getStations()) {
					zsmdp.getItems().add(ss.getName());
					if (ss.getName().equals(thisStation.getName()))
						Platform.runLater(() -> zsmdpComboBox.setValue(ss.getName()));
				}
			}
		}).start();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				comboBoxes.getChildren().add(zsmdp);
				comboBoxes.getChildren().add(users);

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

				timetableVBoxWrapper.getChildren().add(timetableSP);
				timetableVBoxWrapper.setMinWidth(340.0);
				timetableVBoxWrapper.setMinHeight(350.0);
				timetableVBoxWrapper.setMaxWidth(340.0);
				timetableVBoxWrapper.setMaxHeight(350.0);
				timetableVBoxWrapper.setStyle("-fx-margin: 0 0 50 0; -fx-padding: 70 80 15 20;");

				timetableVBox.setMinWidth(290.0);
				timetableVBox.setMinHeight(255.0);
				timetableVBox.setMaxWidth(290.0);

				timetableSP.setContent(timetableVBox);
				timetableSP.setMinWidth(320.0);
				timetableSP.setMinHeight(265.0);
				timetableSP.setMaxWidth(320.0);
			}
		});

		receiveMessage();

		Thread multicastClient = new Thread(() -> {
			byte[] buf = new byte[512];
			try (MulticastSocket socket = new MulticastSocket(Integer.parseInt(prop.getProperty("MULTICAST_PORT")))) {
				InetAddress groupAddress = InetAddress.getByName(prop.getProperty("MULTICAST_IP"));
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
						notificationsVBox.getChildren().add(new Separator());
					});

					buf = new byte[512];
					receivedNotification = "";
				}
			} catch (IOException ioe) {
				MDPLogger.log(Level.INFO, "Receiving messagge IOE.");
			}
		});

		multicastClient.setDaemon(true);
		multicastClient.start();
	}

	private void getTimetable() {
		timetableVBox.getChildren().clear();

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("TIMETABLES_REST_PATH")).path(thisStation.getName() + "");
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();

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
				expectedArrivalText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

				Text stationText = new Text(stationsString);
				stationText.setStyle("-fx-font-size: 16px;");

				Text listedArrivalText = new Text(listedArrivalString);
				listedArrivalText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

				TextFlow expectedArrivalTextFlow = new TextFlow(expectedArrivalText);
				if (isFirst)
					expectedArrivalTextFlow.setStyle("-fx-padding: 6 0 0 4;");

				else
					expectedArrivalTextFlow.setStyle("-fx-padding: 0 0 0 4;");

				expectedArrivalTextFlow.setMinWidth(50.0);
				expectedArrivalTextFlow.setPrefWidth(50.0);
				expectedArrivalTextFlow.setMaxWidth(50.0);

				TextFlow stationTextFlow = new TextFlow(stationText);
				if (isFirst)
					stationTextFlow.setStyle("-fx-padding: 6 0 0 4;");

				else
					stationTextFlow.setStyle("-fx-padding: 0 0 0 4;");

				stationTextFlow.setMinWidth(100.0);
				stationTextFlow.setPrefWidth(100.0);
				stationTextFlow.setMaxWidth(100.0);

				TextFlow listedArrivalTextFlow = new TextFlow(listedArrivalText);
				if (isFirst) {
					listedArrivalTextFlow.setStyle("-fx-padding: 6 0 0 4;");
					isFirst = false;
				}

				else
					listedArrivalTextFlow.setStyle("-fx-padding: 0 0 0 4;");

				listedArrivalTextFlow.setMinWidth(50.0);
				listedArrivalTextFlow.setPrefWidth(50.0);
				listedArrivalTextFlow.setMaxWidth(50.0);

				singleEntry.setAlignment(Pos.CENTER_LEFT);
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

			VBox postArrivalVBox = new VBox();

			TextField textInput = new TextField();
			textInput.setAlignment(Pos.CENTER);
			textInput.setMinWidth(65.0);
			textInput.setPrefWidth(65.0);
			textInput.setMaxWidth(65.0);

			textInput.setMinHeight(30.0);
			textInput.setPrefHeight(30.0);
			textInput.setMaxHeight(30.0);

			Button updateButton = new Button();

			updateButton.setStyle("-fx-padding: 6 0 0 0;");
			updateButton.setText("✔");
			updateButton.setMinWidth(65.0);
			updateButton.setPrefWidth(65.0);
			updateButton.setMaxWidth(65.0);

			updateButton.setMinHeight(30.0);
			updateButton.setPrefHeight(30.0);
			updateButton.setMaxHeight(30.0);
			updateButton.setStyle("-fx-margin: 0 0 10 0;");

			updateButton.setOnAction(new EventHandler<ActionEvent>() {
				int id = t.getId();
				TextField textField = textInput;

				@Override
				public void handle(ActionEvent e) {
					String time = textField.getText();
					if ("".equals(time) || !time.matches(prop.getProperty("TIME_VALIDATION_REGEX")))
						return;

					try {
						WebTarget webTarget = client.target(getBaseURI()).path(prop.getProperty("TIMETABLES_REST_PATH")).path(id + "")
								.queryParam("stationName", thisStation.getName());
						Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
						invocationBuilder.put(Entity.entity(time, MediaType.TEXT_PLAIN));

						getTimetable();
					} catch (Exception e1) {
						MDPLogger.log(Level.INFO, "Error updating timetable.");
					}
				}
			});

			postArrivalVBox.setAlignment(Pos.CENTER);
			postArrivalVBox.getChildren().addAll(textInput, updateButton);

			timetableHBox.setAlignment(Pos.CENTER_LEFT);
			timetableHBox.getChildren().add(postArrivalVBox);
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

	public void setUsername(String username) {
		this.username = username;
	}

	public void setLogo(Image logo) {
		logoImageView.setImage(logo);
	}

	private File pickFile(ActionEvent event, ArrayList<ExtensionFilter> filters) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Odaberite fajl");
		fileChooser.getExtensionFilters().addAll(filters);

		File choosenFile = fileChooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
		if (choosenFile.length() > 10 * 1024 * 1024) {
			Dialog<Void> alert = new Dialog<>();
			alert.setTitle("ZSMDP");
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
					.add(new Image(prop.getProperty("LOGO_FILE")));
			alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			alert.setContentText(prop.getProperty("ATTACHED_FILE_TOO_BIG"));
			alert.showAndWait();

			return null;
		}

		else
			return choosenFile;
	}
}
