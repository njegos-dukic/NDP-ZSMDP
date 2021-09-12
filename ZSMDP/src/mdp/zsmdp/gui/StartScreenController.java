package mdp.zsmdp.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mdp.logger.MDPLogger;
import mdp.soap.server.UserManagementService;
import mdp.soap.server.UserManagementServiceServiceLocator;

public class StartScreenController extends Application {

	@FXML
	private Pane rootPane;

	@FXML
	private TextField usernameInput;

	@FXML
	private PasswordField passwordInput;

	@FXML
	private ImageView logoImageView;

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
	void exit(ActionEvent event) {
		try {
			Platform.exit();
		} catch (Exception e) {
			MDPLogger.log(Level.INFO, "Error exiting application.");
		}
	}

	@FXML
	void logIn(ActionEvent event) {
		(new Thread() {
			private boolean credentialsValid = false;

			@Override
			public void run() {
				UserManagementServiceServiceLocator locator = new UserManagementServiceServiceLocator();
				try {
					UserManagementService service = locator.getUserManagementService();
					credentialsValid = service.checkCredentials(usernameInput.getText(),
							digestString(passwordInput.getText()));
				} catch (Exception e) {
					MDPLogger.log(Level.WARNING, "SOAP Remote Exception while logging in.");
				}

				if (credentialsValid) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							((Button) event.getSource()).getScene().getWindow().hide();
							Stage mainStage = new Stage();
							FXMLLoader loader = new FXMLLoader(getClass().getResource(prop.getProperty("MAIN_SCREEN_FXML")));
							Parent root = null;
							try {
								root = loader.load();
							} catch (IOException e) {
								MDPLogger.log(Level.WARNING, "Failed loading FXML loader.");
							}
							MainScreenController controller = loader.getController();
							Scene sc = new Scene(root);
							sc.setFill(Color.TRANSPARENT);
							mainStage.setScene(sc);
							mainStage.setTitle(prop.getProperty("MAIN_STAGE_TITLE"));
							mainStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
							mainStage.setResizable(false);
							Image logo = new Image(prop.getProperty("LOGO_FILE"));
							logoImageView.setImage(logo);
							controller.setLogo(logo);
							controller.setUsername(usernameInput.getText());
							mainStage.getIcons().add(logo);
							mainStage.setFullScreen(false);
							mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
								@Override
								public void handle(WindowEvent e) {
									controller.logOut(null);
									Platform.exit();
									System.exit(0);
								}
							});
							mainStage.show();
							controller.setup();
						}
					});
				}

				else {
					new Thread(() -> {
						Platform.runLater(() -> {
							rootPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID,
									new CornerRadii(7), new BorderWidths(4))));
						});

						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							MDPLogger.log(Level.INFO, "Border color update failed.");
						}

						Platform.runLater(() -> {
							rootPane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
									new CornerRadii(7), new BorderWidths(4))));
						});
					}).start();
				}
			}
		}).start();
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		setAndLaunchStartScreen(primaryStage);
	}

	private void setAndLaunchStartScreen(Stage primaryStage) throws IOException {

		System.setProperty("javax.net.ssl.trustStore", prop.getProperty("TRUST_STORE_PATH"));
		System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("TRUST_STORE_PASSWORD"));

		System.setProperty("javax.net.ssl.keyStore", prop.getProperty("KEY_STORE_PATH"));
		System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("KEY_STORE_PASSWORD"));

		FXMLLoader loader = new FXMLLoader(getClass().getResource(prop.getProperty("START_SCREEN_FXML")));
		loader.setController(this);
		Parent root = loader.load();
		Scene sc = new Scene(root);
		primaryStage.setScene(sc);
		primaryStage.setTitle(prop.getProperty("PRIMARY_STAGE_TITLE"));
		primaryStage.setResizable(false);
		sc.setFill(Color.TRANSPARENT);
		Image logo = new Image(prop.getProperty("LOGO_FILE"));
		logoImageView.setImage(logo);
		primaryStage.getIcons().add(logo);
		primaryStage.show();
	}

	private String digestString(String input) {
		StringBuilder hexString = new StringBuilder("");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

			hexString = new StringBuilder(2 * encodedhash.length);
			for (byte element : encodedhash) {
				String hex = Integer.toHexString(0xff & element);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
		} catch (NoSuchAlgorithmException e) {
			MDPLogger.log(Level.WARNING, "No SHA-256 available.");
		}

		return hexString.toString();
	}
}
