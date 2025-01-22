// This class ensures durability


import java.io.IOException;
import java.util.TreeMap;

/**
 * Implements the in-memory buffer for the LSMTree
 */
public class MemTable {

    private final TreeMap<String, String> table;

    /**
     * Initializes the MemTable
     */
    public MemTable() {
        this.table = new TreeMap<>();
    }

    /**
     * Inserts or updates a key-value pair in the MemTable
     *
     * @param key The key to insert/update
     * @param value The value associated with the key
     */
    public synchronized void put(String key, String value) {
        table.put(key, value);
    }

    /**
     * Deletes a key from the MemTable.
     *
     * @param key The key to delete.
     */
    public synchronized void delete(String key){
        table.put(key, null);
    }


    /**
     * Checks if the MemTable has reached max size.
     *
     * @param maxSize The maximum number of entries before flushing.
     * @return True if MemTable size >= maxSize, else False.
     */

    public synchronized boolean isFull(int maxSize) {
        return table.size() >= maxSize;
    }

    /**
     * Flushes the MemTable to create an SSTable.
     *
     * @param directory The directory to store the SSTable.
     * @return The created SSTable instance.
     * @throws IOException If an I/O error occurs during SSTable creation.
     */
    public synchronized SSTable flush(String directory) throws IOException {
        SSTable sslTable = SSTable.createFromMemTable(table, directory);
        table.clear();
        return sslTable;
    }

    /**
     * Returns the current state of the MemTable
     *
     * @return The TreeMap of the table
     */
    public synchronized TreeMap<String, String> getTable() {
        return new TreeMap<>(table);
    }
}

