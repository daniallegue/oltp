package btree;

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
}