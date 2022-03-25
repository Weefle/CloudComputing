package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import data.DataFile;
import data.FileBrowser;

public class ClientFrame extends JFrame implements ActionListener, ISocketListener {
	JTextField ipInput, portInput, searchInput;
	JButton connectButton, disconnectButton, searchButton, downLoadFile, uploadFileButton, deleteFileButton;
	JProgressBar jb;
	FileBrowser browser;
	//JList<String> list;
	ClientSocketThread clientSocketThread = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new ClientFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientFrame() {
		// Connect Sever Form
		JLabel ipLabel = new JLabel("IP: ");
		ipInput = new JTextField("127.0.0.1");
		ipLabel.setBounds(100, 100, 150, 25);
		ipInput.setBounds(300, 100, 200, 25);
		this.add(ipLabel);
		this.add(ipInput);
		JLabel portLabel = new JLabel("PORT: ");
		portInput = new JTextField("10");
		portLabel.setBounds(100, 150, 150, 25);
		portInput.setBounds(300, 150, 200, 25);
		this.add(portLabel);
		this.add(portInput);

		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		connectButton.setBounds(125, 200, 150, 25);
		disconnectButton.setBounds(325, 200, 150, 25);
		this.add(disconnectButton);
		this.add(connectButton);

		// Search Form
		JLabel searchLabel = new JLabel("Search: ");
		searchInput = new JTextField();
		searchButton = new JButton("Search");
		searchLabel.setBounds(700, 100, 75, 25);
		searchInput.setBounds(900, 100, 200, 25);
		searchButton.setBounds(825, 200, 150, 25);
		this.add(searchButton);
		this.add(searchInput);
		this.add(searchLabel);

		// Result List

		browser = new FileBrowser();
		browser.run();
		browser.tree.setBounds(200, 400, 800, 350);
		this.add(browser.tree);
		/*list = new JList<>();
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBounds(200, 400, 800, 350);
		this.add(listScrollPane);*/

		// JB
		downLoadFile = new JButton("Download File");
		downLoadFile.setBounds(700, 250, 150, 25);
		this.add(downLoadFile);

		deleteFileButton = new JButton("Delete File");
		deleteFileButton.setBounds(700, 350, 150, 25);
		this.add(deleteFileButton);

		uploadFileButton = new JButton("Upload File");
		uploadFileButton.setBounds(900, 250, 150, 25);
		this.add(uploadFileButton);
		jb = new JProgressBar(0, 100);
		jb.setBounds(700, 300, 100, 25);
		jb.setValue(0);
		jb.setStringPainted(true);
		this.add(jb);

		// Add event
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		searchButton.addActionListener(this);
		deleteFileButton.addActionListener(this);
		downLoadFile.addActionListener(this);
		uploadFileButton.addActionListener(this);

		// setting Frame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Client Frame");
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setBounds(0, 0, 1200, 800);
		this.setVisible(true);
	}

	/*private int getRow(Point point)
	{
		return list.locationToIndex(point);
	}*/

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == connectButton) {
			String ip = ipInput.getText();
			String port = portInput.getText();
			System.out.println(ip + " : " + port);
			try {
				clientSocketThread = new ClientSocketThread(this);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			clientSocketThread.setSocket(ip, Integer.parseInt(port));
			clientSocketThread.start();
		} else if (e.getSource() == disconnectButton) {
			clientSocketThread.closeSocket();
		} else if (e.getSource() == searchButton) {
			String search = searchInput.getText();

			if (clientSocketThread != null) {
				if (search.isEmpty())
					clientSocketThread.sendString("VIEW_ALL_FILE");
				else
					clientSocketThread.sendString("SEARCH_FILE" + "--" + search);
			}
		/*} else if (e.getSource() == downLoadFile) {
			if (list.getSelectedIndex() != -1) {
				String str = list.getSelectedValue();
				clientSocketThread.sendString("DOWNLOAD_FILE" + "--" + str);
			}
		}else if (e.getSource() == deleteFileButton) {
			if (list.getSelectedIndex() != -1) {
				String str = list.getSelectedValue();
				clientSocketThread.sendString("DELETE_FILE" + "--" + str);
			}*/
		}
		else if (e.getSource() == uploadFileButton) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				String filePath = fileToSave.getPath();
				clientSocketThread.sendFile(filePath);
			}
		}
	}

	@Override
	public void updateListFile(File[] listFile) {
		// TODO Auto-generated method stub
		//list.setListData(listFile);
		//browser.files = listFile;
		//browser.run();
	}

	@Override
	public void setProgress(int n) {
		// TODO Auto-generated method stub
		jb.setValue(n);
	}

	@Override
	public void showDialog(String str, String type) {
		if (type.equals("ERROR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.ERROR_MESSAGE);
		else if (type.equals("INFOR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void chooserFileToSave(DataFile dataFile) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String filePath = fileToSave.getPath();
			try {
				dataFile.saveFile(filePath);
				JOptionPane.showMessageDialog(null, "File Saved");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e);
			}
		}

	}

}
