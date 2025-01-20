package btree;

import btree.BTreeNode;

public class BTree {
    BTreeNode root;
    int t;

    /**
     * @param t max depth
     */
    public BTree(int t) {
        this.root = null;
        this.t = t;
    }

    /**
     * Helper method to traverse the tree
     */
    public void traverse() {
        if(this.root != null){
            root.traverse();
        }
    }

    public BTreeNode search(int key) {
        if(root == null){
            return null;
        }
        else {
            return root.search(key);
        }
    }

    public void insert(int key){
        if(root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1){
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;
                s.splitChild(0, root);

                int i = 0;
                // Find key position
                if(s.keys[0] < key){
                    i++;
                }

                s.children[i].insertNonFull(key);
                root = s;
            } else {
                root.insertNonFull(key);
            }
        }
    }
}