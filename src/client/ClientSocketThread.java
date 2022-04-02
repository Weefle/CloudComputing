package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.DataFile;
import data.SEND_TYPE;
import server.FileWorker;

public class ClientSocketThread extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// Receive
	InputStream is;


	// Send
	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	// Data file

	FileWorker fileWorker;
	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	DataFile m_dtf;
	File[] currentArray;

	public ClientSocketThread() throws Exception {
		this.fileWorker = new FileWorker("C:\\client");
				m_dtf = new DataFile();
	}

	public void setSocket(String serverIp, int port) {
		try {

			socket = new Socket(serverIp, port);
			// Connect to server
			System.out.println("Connected: " + socket);

			os = socket.getOutputStream();
			is = socket.getInputStream();


			SendDataThread sendDataThread = new SendDataThread();
			sendDataThread.start();
		} catch (Exception e) {
			// TODO: handle exception
			// clientHelper.connectFail();
			System.out.println("Can't connect to server");
			//iSocketListener.showDialog("Can't connect to Server", "ERROR");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//this.sendString("VIEW_ALL_FILE");
		while (!isStop) {
			try {
				readData();
				//sendString("VIEW_ALL_FILE");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				connectServerFail();
				e.printStackTrace();
				break;
			}
		}
		closeSocket();
	}

	void readData() throws Exception {
		//try {
			//System.out.println("Receiving...");
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);

			} else if (obj instanceof DataFile) {
				readFile(obj);
			}
		/*} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			connectServerFail();
			closeSocket();
		}*/
	}

	void readString(Object obj) throws Exception {
		String str = obj.toString();
		if (str.equals("STOP"))
			isStop = true;
		else if (str.contains("START_SEND_FILE")) {
			this.sendType = SEND_TYPE.START_SEND_FILE;
		} else if (str.contains("SEND_FILE")) {
			String[] fileInfor = str.split("--");
			fileNameReceived = fileInfor[1];
			fileSize = Integer.parseInt(fileInfor[2]);
			System.out.println("File Size: " + fileSize);
			currentSize = 0;
			m_dtf.clear();
			if (fileWorker.checkFile(fileNameReceived))
				this.sendString("START_SEND_FILE");
		} else if (str.contains("END_FILE")) {
			m_dtf.saveFile(fileWorker.getURL_FOLDER() + "\\" + fileNameReceived.replace("C:\\temp\\",""));
			//iSocketListener.chooserFileToSave(m_dtf);
		} else if (str.contains("ALL_FILE")) {
			str = str.replace("ALL_FILE", "");
			Gson gson = new Gson();
			File[] fis = gson.fromJson(str, File[].class);
			//iSocketListener.updateListFile(fis);
			if (!Arrays.equals(currentArray, fis)) {
				currentArray = fis;

			}
			//iSocketListener.updateListFile(fis);
			/*String[] listFile = str.split("--");
			List<File> files = new ArrayList<>();
 			for(String f : listFile){
				 files.add(new File(f));
			}
			File[] fis = files.stream().toArray(File[]::new);*/
			/*var newArray = Arrays.copyOfRange(listFile, 1, listFile.length);
			if(!Arrays.equals(currentArray, newArray)){
				currentArray = newArray;
				iSocketListener.updateListFile(currentArray);
			}*/
		}else if (str.contains("DELETE_FILE")) {
				str = str.replace("DELETE_FILE", "");
				Gson gson = new Gson();
				File fis = gson.fromJson(str, File.class);
				//String[] array = str.split("--");
				fileWorker.deleteFile(fis.getName());
			}
			/*else if (str.contains("ERROR")) {
			String[] list = str.split("--");
			iSocketListener.showDialog(list[1], "ERROR");
		}*/
	}

	void readFile(Object obj) {
		/*DataFile dtf = (DataFile) obj;
		currentSize += 1024;

		int percent = (int) (currentSize * 100 / fileSize);
		m_dtf.appendByte(dtf.data);
		iSocketListener.setProgress(percent);*/
		DataFile dtf = (DataFile) obj;
		int percent = (int) (fileSize);
		m_dtf.data = dtf.data;

	}

	class SendDataThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!isStop) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (sendType != SEND_TYPE.DO_NOT_SEND) {
					try {
						sendData();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void sendData() throws IOException {
		// TODO Auto-generated method stub
		if (sendType == SEND_TYPE.SEND_STRING) {
			sendMessage(message);
		} else if (sendType == SEND_TYPE.SEND_FILE) {
			File source = new File(fileName);
			InputStream fin;
			try {
				fin = new FileInputStream(source);
				long lenghtOfFile = source.length();
				// Send message : fileName + size
				sendMessage("SEND_FILE" + "--" + fileName + "--" + lenghtOfFile);
				fin.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (sendType == SEND_TYPE.START_SEND_FILE) {
			/*File source = new File(fileName);
			InputStream fin;
			long lenghtOfFile = source.length();
			byte[] buf = new byte[512];
			long total = 0;
			int len;
			try {
				fin = new FileInputStream(source);
				while ((len = fin.read(buf)) != -1) {
					total += len;
					DataFile dtf = new DataFile();
					dtf.data = buf;
					sendMessage(dtf);
					iSocketListener.setProgress((int) (total * 100 / lenghtOfFile));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);*/
			File myFile = new File(fileName);
			Path path = Paths.get(myFile.getPath());

			long lenghtOfFile = myFile.length();

			DataFile dtf = new DataFile();
			dtf.data = Files.readAllBytes(path);
			sendMessage(dtf);


			sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);

		}

		sendType = SEND_TYPE.DO_NOT_SEND;
	}

	void sendString(String str) {
		System.out.println("SENDING STRING	" + str);
		sendType = SEND_TYPE.SEND_STRING;
		message = str;
	}

	void sendFile(String fileName) {
		System.out.println("SENDING FILE	");
		sendType = SEND_TYPE.SEND_FILE;
		this.fileName = fileName;
	}

	// void send Message
	public synchronized void sendMessage(Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			// only send text
			if (obj instanceof String) {
				String message = obj.toString();
				oos.writeObject(message);
				oos.flush();
			}
			// send attach file
			else if (obj instanceof DataFile) {
				oos.writeObject(obj);
				oos.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void connectServerFail() {
		// TODO Auto-generated method stub
		//iSocketListener.showDialog("Can't connect to Server", "ERROR");
		isStop = true;
		closeSocket();
	}

	public void closeSocket() {
		isStop = true;
		try {
			this.sendString("STOP");

			if (is != null)
				is.close();
			if (os != null)
				os.close();
			if (socket != null)
				socket.close();


		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
