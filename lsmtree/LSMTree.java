// Implements the LSMTree from a list of SSTables


import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * Represents the LSM Tree
 */
public class LSMTree {
    private final MemTable memTable;
    private final WriteAheadLog wal;
    private final List<SSTable> sstables;
    private final String sstableDirectory;
    private final int memTableSize;

    /**
     * Initializes the LSM Tree.
     *
     * @param memTableSize      The maximum number of entries in the MemTable before flushing.
     * @param walFilePath       The path to the WAL file.
     * @param sstableDirectory  The directory to store SSTables.
     * @throws IOException If an I/O error occurs.
     */
    public LSMTree(int memTableSize, String walFilePath, String sstableDirectory) throws IOException {
        this.memTable = new MemTable();
        this.wal = new WriteAheadLog(walFilePath);
        this.sstables = new ArrayList<>();
        this.sstableDirectory = sstableDirectory;
        this.memTableSize = memTableSize;
        recover(); // Recover from WAL
    }

    /**
     * Inserts or updates a key-value pair in the LSM Tree.
     *
     * @param key   The key to insert/update.
     * @param value The value associated with the key.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void put(String key, String value) throws IOException {
        wal.logPut(key, value);
        memTable.put(key, value);
        if (memTable.isFull(memTableSize)) {
            flushMemTable();
        }
    }

    /**
     * Deletes a key from the LSM Tree.
     *
     * @param key The key to delete.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void delete(String key) throws IOException {
        wal.logDelete(key);
        memTable.delete(key);
        if (memTable.isFull(memTableSize)) {
            flushMemTable();
        }
    }


    /**
     * Searches for a key in the LSM Tree.
     *
     * @param key The key to search for.
     * @return The associated value, or null if not found or deleted.
     * @throws IOException If an I/O error occurs.
     */
    public synchronized String search(String key) throws IOException {
        TreeMap<String, String> memData = memTable.getTable();
        if(memData.containsKey(key)) {
            return memData.get(key);
        }

        // Search in SSTables in reverse order (newest first)
        for (int i = sstables.size() - 1; i >= 0; i--) {
            SSTable sstable = sstables.get(i);
            String value = sstable.search(key);
            if (value != null) {
                return value;
            }
        }

        // Key not found
        return null;
    }


    /**
     * Flushes the MemTable to disk as an SSTable.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void flushMemTable() throws IOException {
        SSTable sstable = memTable.flush(sstableDirectory);
        sstables.add(sstable);
        wal.clear(); // Clear the WAL
    }

    /**
     * Compacts SSTables to reclaim space.
     *
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void compact() throws IOException {
        if (sstables.size() <= 1) {
            return; // Nothing to compact
        }

        // For simplicity, compact all SSTables into one
        List<SSTable> sstablesToCompact = new ArrayList<>(sstables);
        SSTable merged = Compaction.compact(sstablesToCompact, sstableDirectory);
        sstables.clear();
        sstables.add(merged);

        // Delete old SSTables from disk
        for (SSTable sstable : sstablesToCompact) {
            Files.deleteIfExists(sstable.getFilePath());
        }
    }

    /**
     * Recovers the LSM Tree state by replaying the WAL.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void recover() throws IOException {
        List<String> logs = wal.readLogs();
        for (String log : logs) {
            if (log.startsWith("PUT")) {
                String[] parts = log.split(" ", 3);
                if (parts.length == 3) {
                    String key = parts[1];
                    String value = parts[2];
                    memTable.put(key, value);
                }
            } else if (log.startsWith("DELETE")) {
                String[] parts = log.split(" ", 2);
                if (parts.length == 2) {
                    String key = parts[1];
                    memTable.delete(key);
                }
            }
        }
    }

    /**
     * Closes the LSM Tree, ensuring all resources are properly released.
     *
     * @throws IOException If an I/O error occurs.
     */
    public synchronized void close() throws IOException {
        flushMemTable(); // Flush any remaining data
        wal.close();
    }


}
