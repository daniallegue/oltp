// Write Ahead Log class for persistence

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a Write-Ahead Log (WAL) for the B-Tree.
 */
public class WriteAheadLog implements Serializable, Closeable{
    private BufferedWriter writer;
    private String logPath;


    /**
     * Initializes WAL with the specified log file path
     *
     * @param path The path to the log file
     * @throws IOException If an I/O error arises
     */
    public WriteAheadLog(String path) throws IOException {
        this.logPath = path;
        File logFile = new File(logPath);

        logFile.getParentFile().mkdirs();
        this.writer = new BufferedWriter(new FileWriter(logFile, true)); //Append only
    }

    /**
     * Logs an insert operation.
     *
     * @param key The key to insert.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void logInsert(String key) throws IOException {
        // Use synchronized for thread safety
        writer.write("INSERT " + key);
        writer.newLine();
        writer.flush();
    }

    /**
     * Logs an delete operation.
     *
     * @param key The key to delete.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void logDelete(String key) throws IOException {
        // Use synchronized for thread safety
        writer.write("DELETE " + key);
        writer.newLine();
        writer.flush();
    }





    /**
     * Logs a put operation.
     *
     * @param key   The key to insert/update.
     * @param value The value associated with the key.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void logPut(String key, String value) throws IOException {
        writer.write("PUT " + key + " " + value);
        writer.newLine();
        writer.flush();
    }


    /**
     * Reads all log entries from the WAL.
     *
     * @return A list of log entries.
     * @throws IOException If an I/O error occurs.
     */
    public List<String> readLogs() throws IOException {
        List<String> logs = new ArrayList<>();
        Path path = Paths.get(logPath);

        if(!Files.exists(path)){
            return logs;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(logPath))) {
            String line;
            while((line = reader.readLine()) != null) {
                logs.add(line.trim());
            }
        }

        return logs;
    }

    /**
     * Closes the WAL writer.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * Clears the WAL by deleting the file.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void clear() throws IOException {
        close();
        Files.deleteIfExists(Paths.get(logPath));

        //Reopen writer
        this.writer = new BufferedWriter(new FileWriter(logPath, true));
    }
}
