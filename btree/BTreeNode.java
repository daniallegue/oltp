// Node class from the BTree

/**
 * Implements a Node from a B-Tree
 */
 public class BTreeNode {

    int[] keys;
    BTreeNode[] children;
    int t; // Minimum Degree
    boolean isLeaf;
    int n; // Number of keys

    /**
     * @param t: int Minimum degree
     * @param isLeaf: boolean Is the node a leaf
     */
    public BTreeNode(int t, boolean isLeaf){
        this.t = t;
        this.isLeaf = isLeaf;
        this.keys = new int[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.n = 0;
    }

    /**
     * Helper method to traverse the tree
     */
    public void traverse(){
        int i;
        for(i = 0; i < this.n; i++){
            if(!this.isLeaf){
                children[i].traverse();
            }
            System.out.print(keys[i] + " ");
        }

        // Subtree of last child
        if(!this.isLeaf){
            children[i].traverse();
        }
    }


    /**
     * @param key to search for
     * @return BTreeNode node
     */
    public BTreeNode search(int key){
        int i = 0;
        while (i < n && key > keys[i]){
            i++;
        }

        if(i < n && keys[i] == key){
            return this;
        }

        if (isLeaf){
            return null;
        }

        return children[i].search(key);
    }

    /**
     * Inserts a new key into a non-full BTreeNode
     * @param key to insert
     */
    public void insertNonFull(int key){
        int i = n - 1;

        if(isLeaf){
            while(i >= 0 && keys[i] > key){
                keys[i + 1] = keys[i];
                i--;
            }

            keys[i+1] = key;
            n = n + 1;
        }
        else {
            while(i >= 0 && keys[i] > key){
                i--;
            }

            if( children[i + 1].n == 2 * t - 1){
                // i.e. full
                splitChild(i + 1, children[i + 1]);

                if(keys[i + 1] > key){
                    i++;
                }
            }

            children[i + 1].insertNonFull(key);
        }
    }

    /**
     * @param i index to split
     * @param y BTreeNode
     */
    public void splitChild(int i, BTreeNode y){
        BTreeNode z = new BTreeNode(y.t, y.isLeaf);
        z.n = t - 1;

        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
        }

        if(!y.isLeaf){
            for(int j = 0; j < t; j++){
                //Offset children by t
                z.children[j] = y.children[j + t];
            }
        }

        y.n = t - 1;

        for(int j = n; j >= i + 1; j--){
            children[j + 1] = children[j];
        }

        children[i + 1] = z;

        for(int j = n - 1; j >= i; j--){
            keys[j + 1] = keys[j];
        }

        keys[i] = y.keys[t - 1];
        n = n + 1;
    }

    /**
     * Deletes a key from the subtree rooted with this node
     * @param key the key to be deleted
     */
    public void delete(int key) {
        int idx = findKey(key);

        // Case 1: We find the key in this node
        if(idx < n && keys[idx] == key){
            if(isLeaf){
                removeFromLeaf(idx);
            }
            else {
                removeFromNonLeaf(idx);
            }
        }
        else {
            if(isLeaf){
                // The key is not in the tree
                System.out.println("The key " + key + " does not exist in the tree.");
                return;
            }

            boolean flag = (idx == n);
            if(children[idx].n < t) {
                fill(idx);
            }

            // If the last child has been merged, recurse on the (idx-1)th child
            if (flag && idx > n) {
                children[idx - 1].delete(key);
            } else {
                children[idx].delete(key);
            }
        }
    }

    /**
     * Finds the first index in keys where keys[idx] >= key
     * @param key the key to find
     * @return the index
     */
    private int findKey(int key) {
        int idx = 0;
        while(idx < n && keys[idx] < key) {
            ++idx;
        }

        return idx;
    }

    /**
     * Removes the key present in idx-th position in this leaf node
     * @param idx the index of the key to remove
     */
    private void removeFromLeaf(int idx) {
        // Move all keys from one position to the left
        for(int i = idx + 1; i < n; i++){
            keys[i - 1] = keys[i];
        }

        n--;
    }

    /**
     * Removes the key present in idx-th position in this non-leaf node
     * @param idx the index of the key to remove
     */
    private void removeFromNonLeaf(int idx) {
        int key = keys[idx];

        if(children[idx].n >= t) {
            int pred = getPredecessor(idx);
            keys[idx] = pred;
            children[idx].delete(pred);
        }

        else if(children[idx + 1].n >= t) {
            int succ = getSuccessor(idx);
            keys[idx] = succ;
            children[idx + 1].delete(succ);
        }

        else {
            merge(idx);
            children[idx].delete(key);
        }
    }

    /**
     * Gets the predecessor of keys[idx]
     * @param idx the index of the key
     * @return the predecessor key
     */
    private int getPredecessor(int idx) {
        BTreeNode current = children[idx];
        while (!current.isLeaf){
            current = current.children[current.n];
        }

        return current.keys[current.n - 1];
    }


    /**
     * Gets the successor of keys[idx]
     * @param idx the index of the key
     * @return the successor key
     */
    private int getSuccessor(int idx){
        BTreeNode current = children[idx + 1];
        while (!current.isLeaf){
            current = current.children[0];
        }

        return current.keys[0];
    }


    /**
     * Fills up the child node children[idx] which has less than t-1 keys
     * @param idx the index of the child node
     */
    private void fill(int idx) {
        if(idx != 0 && children[idx - 1].n >= t){
            borrowFromPrev(idx);
        }
        else if(idx != n && children[idx + 1].n >= t){
            borrowFromNext(idx);
        } else {
            if(idx != n){
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    private void borrowFromPrev(int idx){
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx - 1];

        // The last key from children[idx-1] goes up to the parent and
        // key[idx-1] from parent is inserted as the first key in children[idx]
        // Thus, the sibling loses one key and child gains one key

        for(int i = child.n - 1; i >= 0; --i){
            child.keys[i + 1] = child.keys[i];
        }

        if(!child.isLeaf) {
            for(int i = child.n; i >= 0; --i){
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = keys[idx - 1];

        if(!child.isLeaf) {
            child.children[0] = sibling.children[sibling.n];
        }

        keys[idx - 1] = sibling.keys[sibling.n - 1];

        child.n += 1;
        sibling.n -= 1;
    }

    /**
     * Borrows a key from children[idx+1] and inserts it into children[idx]
     * @param idx the index of the child node
     */
    private void borrowFromNext(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        // keys[idx] is inserted as the last key in children[idx]
        child.keys[child.n] = keys[idx];

        // If child is not a leaf, append the first child of sibling to children[idx]
        if (!child.isLeaf) {
            child.children[child.n + 1] = sibling.children[0];
        }

        // The first key from sibling is inserted into keys[idx]
        keys[idx] = sibling.keys[0];

        // Move all keys in sibling one step to the left
        for (int i = 1; i < sibling.n; ++i) {
            sibling.keys[i - 1] = sibling.keys[i];
        }

        // Move the child pointers in sibling one step to the left
        if (!sibling.isLeaf) {
            for (int i = 1; i <= sibling.n; ++i) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.n += 1;
        sibling.n -= 1;
    }

    /**
     * Merges children[idx] with children[idx+1] and moves a key from this node down
     * @param idx the index of the child node
     */
    private void merge(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        child.keys[t - 1] = keys[idx];

        // Copying the keys from sibling to child
        for (int i = 0; i < sibling.n; ++i) {
            child.keys[i + t] = sibling.keys[i];
        }

        // Copying the child pointers from sibling to child
        if (!child.isLeaf) {
            for (int i = 0; i <= sibling.n; ++i) {
                child.children[i + t] = sibling.children[i];
            }
        }

        // Moving all keys after idx in the current node one step to the left
        for (int i = idx + 1; i < n; ++i) {
            keys[i - 1] = keys[i];
        }

        for (int i = idx + 2; i <= n; ++i) {
            children[i - 1] = children[i];
        }

        child.n += sibling.n + 1;
        n--;
    }


}