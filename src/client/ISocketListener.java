package client;

import data.DataFile;

import java.io.File;

public interface ISocketListener {

	void updateListFile(File[] listFile);

	void setProgress(int n);

	void showDialog(String str, String type);

	void chooserFileToSave(DataFile dataFile);

}
