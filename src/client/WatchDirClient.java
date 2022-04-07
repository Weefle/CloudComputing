package client;

import server.WatchDir;

import java.nio.file.*;

import static client.ClientFrame.clientSocketThread;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDirClient implements Runnable {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchDir.createWatchKey(dir, watcher, trace, keys);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDirClient(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();

            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");

        // enable trace after initial registration
        this.trace = true;
    }

    @Override
    public void run() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                        if(new File(child.toString()).isDirectory()){
                            clientSocketThread.sendString("CREATE_FOLDER" + child);

                        }else{
                            clientSocketThread.sendFile(child.toString());
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }else if(kind == ENTRY_DELETE){
                    clientSocketThread.sendString("DELETE_FILE" + child);
                }/*else if(kind == ENTRY_MODIFY){
                    if(new File(child.toString()).isDirectory()){
                        clientSocketThread.sendString("CREATE_FOLDER" + child);

                    }else {
                        clientSocketThread.sendFile(child.toString());
                    }
                }*/
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
