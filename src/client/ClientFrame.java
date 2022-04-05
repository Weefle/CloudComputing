package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientFrame extends JFrame implements ActionListener, ISocketListener {
	JTextField ipInput, portInput;
	JButton connectButton, disconnectButton;
	public static ClientSocketThread clientSocketThread;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				new ClientFrame();
			} catch (Exception e) {
				e.printStackTrace();
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


		// Add event
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);


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
			clientSocketThread = new ClientSocketThread(this);
			Path dir = Paths.get("C:\\client");
			try {
				new Thread(new WatchDirClient(dir)).start();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			clientSocketThread.setSocket("127.0.0.1", 10);
		clientSocketThread.start();
		} else if (e.getSource() == disconnectButton) {
			clientSocketThread.closeSocket();
			showDialog("DISCONNECTED FROM SERVER", "INFOR");
		}
	}



	@Override
	public void showDialog(String str, String type) {
		if (type.equals("ERROR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.ERROR_MESSAGE);
		else if (type.equals("INFOR"))
			JOptionPane.showMessageDialog(this, str, type, JOptionPane.INFORMATION_MESSAGE);
	}



}
