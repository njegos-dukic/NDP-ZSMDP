package mdp.rmi.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.google.gson.Gson;

import mdp.logger.MDPLogger;

public class Archivator implements ArchivatorI {

	public static String RESOURCES_ROOT = "./resources/";
	public static String RESOURCES_FILENAME = "app.properties";

	public static String AZSMDP_ROOT;

	public static void main(String[] args) {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES_ROOT + RESOURCES_FILENAME);
			prop.load(input);
		} catch (FileNotFoundException e1) {
			MDPLogger.log(Level.INFO, "Propperties file not found.");
		} catch (IOException e) {
			MDPLogger.log(Level.INFO, "Error opening properties file.");
		}

		try {
			AZSMDP_ROOT = prop.getProperty("AZSMDP_ROOT");
			String name = prop.getProperty("RMI_SERVER_NAME");
			Archivator srv = new Archivator();
			ArchivatorI stub = (ArchivatorI) UnicastRemoteObject.exportObject(srv, 0);
			LocateRegistry.createRegistry(Integer.parseInt(prop.getProperty("RMI_PORT")));
			Naming.rebind(prop.getProperty("RMI_IP_ADDRESS") + name, stub);

			MDPLogger.log(Level.INFO, "Archivator server ready.");
		} catch (Exception e) {
			MDPLogger.log(Level.WARNING, "Archivator server not started.");
		}
	}

	@Override
	public boolean upload(Report report) throws RemoteException {
		try {
			OutputStream os = new FileOutputStream(new File(AZSMDP_ROOT + File.separator + report.getFileName()));
			os.write(report.getFileContent());
			os.close();

			ReportMetadata metadata = new ReportMetadata(report.getFileName(), report.getUserName(),
					report.getUploadTime(), report.getSize());
			os = new FileOutputStream(new File(AZSMDP_ROOT + File.separator + report.getFileName()) + ".json");
			Gson gson = new Gson();
			os.write(gson.toJson(metadata).getBytes());
			return true;
		}

		catch (Exception e) {
			MDPLogger.log(Level.WARNING, "RMI upload failed.");
			return false;
		}
	}

	@Override
	public Report download(String fileName) throws RemoteException {
		ArrayList<Report> returnReport = new ArrayList<>();

		Gson gson = new Gson();

		try (Stream<Path> paths = Files.walk(Paths.get(AZSMDP_ROOT))) {
			paths.filter(Files::isRegularFile).forEach(f -> {
				try {
					if (f.getFileName().toString().equals(fileName)) {
						ReportMetadata metadata = gson.fromJson(
								new String(Files.readAllBytes(Paths.get(f + ".json")), StandardCharsets.UTF_8),
								ReportMetadata.class);

						returnReport.add(new Report(Files.readAllBytes(f), metadata.getFileName(),
								metadata.getUserName(), metadata.getUploadTime()));
					}

				} catch (IOException e) {
					MDPLogger.log(Level.WARNING, "RMI download failed.");
				}
			});

			return returnReport.get(0);

		} catch (IOException e) {
			MDPLogger.log(Level.WARNING, "RMI file walk failed.");
		}

		return null;
	}

	@Override
	public ArrayList<ReportMetadata> list() throws RemoteException {
		ArrayList<ReportMetadata> returnList = new ArrayList<>();

		Gson gson = new Gson();

		try (Stream<Path> paths = Files.walk(Paths.get(AZSMDP_ROOT))) {
			paths.filter(Files::isRegularFile).forEach(f -> {
				try {
					if (!f.toString().endsWith("json")) {
						ReportMetadata metadata = gson.fromJson(
								new String(Files.readAllBytes(Paths.get(f + ".json")), StandardCharsets.UTF_8),
								ReportMetadata.class);

						returnList.add(new ReportMetadata(metadata.getFileName(), metadata.getUserName(),
								metadata.getUploadTime(), metadata.getSize()));
					}

				} catch (IOException e) {
					MDPLogger.log(Level.WARNING, "RMI list failed.");
				}
			});

			return returnList;

		} catch (IOException e) {
			MDPLogger.log(Level.WARNING, "RMI walk failed.");
		}

		return null;
	}
}
