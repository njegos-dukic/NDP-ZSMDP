package mdp.rmi.server;

import java.io.Serializable;
import java.util.Date;

public class Report implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] fileContent;
	private String fileName;
	private String userName;
	private Date uploadTime;
	private int size;

	public Report() {
	}

	public Report(byte[] fileContent, String fileName, String userName, Date uploadTime) {
		super();
		this.fileContent = fileContent;
		this.fileName = fileName;
		this.userName = userName;
		this.uploadTime = uploadTime;
		this.size = fileContent.length;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
