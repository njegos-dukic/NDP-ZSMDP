package mdp.rmi.server;

import java.io.Serializable;
import java.util.Date;

public class ReportMetadata implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fileName;
	private String userName;
	private Date uploadTime;
	private int size;

	public ReportMetadata() {
	}

	public ReportMetadata(String fileName, String userName, Date uploadTime, int size) {
		super();
		this.fileName = fileName;
		this.userName = userName;
		this.uploadTime = uploadTime;
		this.size = size;
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
