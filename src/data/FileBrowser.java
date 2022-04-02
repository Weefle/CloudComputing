package data;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class FileBrowser implements Runnable {

    public DefaultMutableTreeNode root;

    public DefaultTreeModel treeModel;

    public JTree tree;

    //public File[] files;

    public File fileRoot;

        @Override
    public void run() {

        /*File fileRoot = new File("C:/temp");
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);*/

            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);

            TreeSelectionListener treeSelectionListener = tse -> {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
                showChildren(node);
                //setFileDetails((File)node.getUserObject());
            };

            // show the file system roots.
            fileRoot = new File("C:/temp");
            //File[] roots = fileSystemView.getRoots();
            //for (File fileSystemRoot : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileRoot/*fileSystemRoot*/);
            root.add( node );
            //showChildren(node);
            //
            //File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            File[] files = fileRoot.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }

        tree = new JTree(treeModel);
        //tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
            tree.addTreeSelectionListener(treeSelectionListener);
            tree.setCellRenderer(new FileTreeCellRenderer());
            tree.expandRow(0);
            JScrollPane treeScroll = new JScrollPane(tree);
            tree.setVisibleRowCount(15);
            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(200,(int)preferredSize.getHeight());
            treeScroll.setPreferredSize( widePreferred );

        /*CreateChildNodes ccn =
                new CreateChildNodes(fileRoot, root);
        new Thread(ccn).start();*/
    }

    private void showChildren(final DefaultMutableTreeNode node) {
        tree.setEnabled(false);
        //progressBar.setVisible(true);
        //progressBar.setIndeterminate(true);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                //if (file.isDirectory()) {
                    File[] files = file.listFiles(); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
                            //if (child.isDirectory()) {
                                publish(child);
                           // }
                        }
                    }
                    //setTableData(files);
               // }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done() {
                //progressBar.setIndeterminate(false);
                //progressBar.setVisible(false);
                tree.setEnabled(true);
            }
        };
        worker.execute();
    }

    public class CreateChildNodes implements Runnable {

        private DefaultMutableTreeNode root;

        private File fileRoot;

        public CreateChildNodes(File fileRoot,
                                DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        @Override
        public void run() {
            /*while (true) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                createChildren(fileRoot, root);
            //}
        }



        private void createChildren(File fileRoot,
                                    DefaultMutableTreeNode node) {
            File[] files = fileRoot.listFiles();
            if (files == null) return;

            for (File file : files) {

                    DefaultMutableTreeNode childNode =
                            new DefaultMutableTreeNode(new FileNode(file));
                    node.add(childNode);
                    if (file.isDirectory()) {
                        createChildren(file, childNode);
                    }


            }
        }

    }

    public class FileNode {

        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

}