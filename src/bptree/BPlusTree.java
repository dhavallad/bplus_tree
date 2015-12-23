package bptree;

/**
 * The {@code BPlusTree} class implements B+-trees. Each {@code BPlusTree} stores its elements in the main memory (not
 * on disks) for simplicity.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <K>
 *            the type of keys
 * @param <V>
 *            the type of values
 */
public class BPlusTree<K extends Comparable<K>, V> {

	/**
	 * The maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	protected int degree;

	/**
	 * The root node of this {@code BPlusTree}.
	 */
	protected Node<K> root;

	/**
	 * Constructs a {@code BPlusTree}.
	 * 
	 * @param degree
	 *            the maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	public BPlusTree(int degree) {
		this.degree = degree;
	}

	/**
	 * Copy-constructs a {@code BPlusTree}.
	 * 
	 * @param tree
	 *            another {@code BPlusTree} to copy from.
	 */
	@SuppressWarnings("unchecked")
	public BPlusTree(BPlusTree<K, V> tree) {
		this.degree = tree.degree;
		if (tree.root instanceof LeafNode)
			this.root = new LeafNode<K, V>((LeafNode<K, V>) tree.root);
		else
			this.root = new NonLeafNode<K>((NonLeafNode<K>) tree.root);
	}

	/**
	 * Returns the degree of this {@code BPlusTree}.
	 * 
	 * @return the degree of this {@code BPlusTree}.
	 */
	public int degree() {
		return degree;
	}

	/**
	 * Returns the root {@code Node} of this {@code BPlusTree}.
	 * 
	 * @return the root {@code Node} of this {@code BPlusTree}.
	 */
	public Node<K> root() {
		return root;
	}

	/**
	 * Finds the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 * 
	 * @param key
	 *            the search key.
	 * @return the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 */
	@SuppressWarnings("unchecked")
	public LeafNode<K, V> find(K key) {
		Node<K> c = root;
		while (c instanceof NonLeafNode) {
			c = ((NonLeafNode<K>) c).child(key);
		}
		return (LeafNode<K, V>) c;
	}

	/**
	 * Finds the parent {@code Node} of the specified {@code Node}.
	 * 
	 * @param node
	 *            a {@code Node}.
	 * @return the parent {@code Node} of the specified {@code Node}; {@code null} if the parent cannot be found.
	 */
	public NonLeafNode<K> findParent(Node<K> node) {
		Node<K> p = root;
		while (p != null) {
			K key = node.firstKey();
			Node<K> c = ((NonLeafNode<K>) p).child(key);
			if (c == node) { // if found the parent of the node.
				return (NonLeafNode<K>) p;
			}
			p = c;
		}
		return null;
	}

	/**
	 * Inserts the specified key and the value into this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param value
	 *            the value to insert.
	 */
	public void insert(K key, V value) {
		LeafNode<K, V> leaf; // the leaf node where insertion will occur
		if (root == null) { // if the root is null
			leaf = new LeafNode<K, V>(degree);
			root = leaf;
		} else { // if root is not null
			leaf = find(key);
		}
		if (leaf.hasRoom()) { // if the leaf node has room for the new entry
			leaf.insert(key, value);
		} else { // if split is required
			LeafNode<K, V> t = new LeafNode<K, V>(degree + 1); // create a temporary leaf node
			t.copy(leaf, 0, leaf.numberOfKeys());// copy everything to the temporary node
			t.insert(key, value); // insert the key and value to the temporary node
			LeafNode<K, V> nLeaf = new LeafNode<K, V>(degree); // create a new leaf node
			nLeaf.setSuccessor(leaf.successor()); // chaining
			leaf.clear(); // clear the leaf node
			leaf.setSuccessor(nLeaf); // chaining from leaf to nLeaf
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			leaf.copy(t, 0, m); // put the first half into leaf
			nLeaf.copy(t, m, t.numberOfKeys()); // put the second half to nLeaf
			insertInParent(leaf, nLeaf.firstKey(), nLeaf); // use the first key of nLeaf as the separator.
		}
	}

	/**
	 * Inserts pointers to the specified {@code Node}s into an appropriate parent {@code Node}.
	 * 
	 * @param n
	 *            a {@code Node}.
	 * @param key
	 *            the key that splits the {@code Node}s
	 * @param nn
	 *            a new {@code Node}.
	 */
	void insertInParent(Node<K> n, K key, Node<K> nn) {
		if (n == root) { // if the root was split
			root = new NonLeafNode<K>(degree); // create a new node
			root.insert(key, n, 0); // make the new root point to the nodes.
			root.pointers[1] = nn;
			return;
		}
		NonLeafNode<K> p = findParent(n);
		if (p.hasRoom()) {
			p.insertAfter(key, nn, n); // insert key and nn right after n
		} else { // if split is required
			NonLeafNode<K> t = new NonLeafNode<K>(degree + 1); // crate a temporary node
			t.copy(p, 0, p.numberOfKeys()); // copy everything of p to the temporary node
			t.insertAfter(key, nn, n); // insert key and nn after n
			p.clear(); // clear p
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			p.copy(t, 0, m - 1);
			NonLeafNode<K> np = new NonLeafNode<K>(degree); // create a new node
			np.copy(t, m, t.numberOfKeys()); // put the second half to np
			insertInParent(p, t.keys[m - 1], np); // use the middle key as the separator
		}
	}

	/**
	 * Deletes the specified key and the value from this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to delete.
	 * @param value
	 *            the value to delete.
	 */
	public void delete(K key, V value) {
// please implement the body of this method so that we can remove
		// key-value pairs from the tree (refer to page
		// 498 in the text book).
		Node<K> leaf = find(key);
		// if there is no element in the B + tree.

		if (root == null) {
			System.out.println("This B+ tree is empty !!");
			return;
		} else if (leaf.findIndexGE(key) < 0) {
			System.out.println("This B+ tree doesn't contain value.");
			return;
		}
		// find the leaf node L that could contain the (key,value) pair
		/*
		 * if (l.findIndexL(key) < 0) { System.out.println(
		 * "This B+ tree doesn't contain value."); return; }
		 */
		// Delete entry
		delete_key(leaf, key, value);
	}
	
public void delete_key(Node<K> currentNode, K key, V value) {

		System.out.println("Before Shift and del cURRE"+currentNode.ToString()+currentNode.ToPointers());
		int i = 0;
		int currentIndex = 0;

		// Delete of Key
		for (i = 0; i < currentNode.numberOfKeys; i++) 
			if (currentNode.keys[i] == key) { // Key found in the node.
				currentNode.keys[i] = null; // Delete the key
				
				currentIndex = i; // Storing the Index of deletion, to be used
			}					// for shifting
			
		shift(currentIndex,currentNode);
		System.out.println("After Shift and del cURRE"+currentNode.ToString()+currentNode.ToPointers());

		// N has only one child & only one child.
		if (currentNode == root && noOfpointers(root) == 1) {
			System.out.println(" N has only one child & only one child. Current Node/pointer"+currentNode.ToString()+currentNode.ToPointers()+root.ToString());
			
//			root.copy(currentNode, 0, currentNode.numberOfKeys);
			
			for (i = 0; i < currentNode.pointers.length; i++) {
				if (currentNode.pointers[i] != null) // Checking for child. This will be the new root node.
					root = (Node<K>)currentNode.pointers[i];
			}
			
			
			System.out.println(" N has only one B4 clear. Current Node/pointer"+currentNode.ToString()+currentNode.ToPointers()+root.ToString());
			currentNode.clear();
			System.out.println(" N has only one ATR clear one child. Current Nodepointers"+currentNode.ToString()+currentNode.ToPointers()+root.ToString());
			// Few Pointers
		} else if(noOfpointers(currentNode) < 2) {
			System.out.println("Underflow LeafNode has FEW POINTERS/VALUES.");
			
			Node<K> parent = findParent(currentNode);
//			System.out.println("Parent Node" + parent.ToString() + parent.ToPointers());
//			System.out.println("Current Node" + currentNode.ToString() + currentNode.ToPointers());


			// Find index position of current node in parent node.
			int currentPointer = 0;
			int siblingPointer = 0;
			
		}
		
	}

}
