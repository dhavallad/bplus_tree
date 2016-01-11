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
		} else if (noOfpointers(currentNode) < 2) {
			System.out.println("Underflow LeafNode has FEW POINTERS/VALUES.");
			
			Node<K> parent = findParent(currentNode);
			// int siblingIndex = 0;
			// if (siblingNode.equals((parent).child(key))) {
			// siblingNode = parent.child(1);
			// siblingIndex = currentIndex + 1;
			// System.out.println("Current Node index(" + currentIndex + ")-" +
			// currentNode.ToString()
			// + " Sibling Node index(" + siblingIndex + ")-" +
			// siblingNode.ToString());
			// } else {
			// System.out.println("Current Node index(" + currentIndex + ")-" +
			// currentNode.ToString()
			// + " Sibling Node index(" + siblingIndex + ")-" +
			// siblingNode.ToString());
			// }
			//			System.out.println("Parent Node" + parent.ToString() + parent.ToPointers());
			//			System.out.println("Current Node" + currentNode.ToString() + currentNode.ToPointers());


			// Find index position of current node in parent node.
			int currentPointer = 0;
			int siblingPointer = 0;

			// Get the position of N in parent.
			for (int k = 0; k < parent.pointers.length; k++) {
				if (parent.pointers[k] == currentNode) {
					currentPointer = k;
				}
			}

			System.out.println("Current Node Pointer Index" + currentPointer);
			
			K keyValue;
			int k_pos=0;

			if (currentPointer == 0) {
				keyValue = parent.keys[currentPointer];
				siblingPointer = currentPointer + 1;
				k_pos = currentPointer;
				//				siblingNode = (Node<K>) parent.pointers[currentPointer + 1];
				// siblingNode = ((NonLeafNode<K>) parent).child(currentPointer
				// + 1);
				//				keyValue = parent.keys[currentIndex];
			} else {
				siblingPointer = currentPointer -1;
				k_pos = currentPointer;//siblingPointer
				keyValue = parent.keys[siblingPointer];
				//				siblingNode = (Node<K>) parent.pointers[currentPointer - 1];
				//				keyValue = parent.keys[currentPointer - 1];
			}
			Node<K> siblingNode = (Node<K>)parent.pointers[siblingPointer];

			// entries in N and N' can fit in a single node, and then coalesce
			// nodes
			if ((currentNode.numberOfKeys + siblingNode.numberOfKeys) < degree) {
				// if N is a predecessor of N' swap_variables (N,N')
				
				System.out.println("Merege call.");
				if (currentPointer<siblingPointer) {
					
					NonLeafNode<K> temp = new NonLeafNode<K>(degree);
					temp.numberOfKeys = 0;
					
					// Temp <-- N

					for(i=0; i<N.numberOfKeys; i++){
						temp.keys[i] = N.keys[i];
						temp.numberOfKeys++;
					}

					for(i=0; i<degree; i++)
					temp.pointers[i] = N.pointers[i];



					// N <-- N'
					N.numberOfKeys=0;

					for(i=0; i<n_.numberOfKeys; i++){
						N.keys[i] = n_.keys[i];
						N.numberOfKeys++;
					}

					for(i=0; i<degree; i++)
					N.pointers[i] = n_.pointers[i];
					
					// N' <-- Temp
					n_.numberOfKeys=0;

					for(i=0; i<temp.numberOfKeys; i++){
						n_.keys[i] = temp.keys[i];
						n_.numberOfKeys++;
					}

					for(i=0; i<degree; i++)
					n_.pointers[i] = temp.pointers[i];

					temp.clear(); // Delete temp, as its not needed
					
					
					
					Node<K> temp = currentNode;
					System.out.println("In Merge C/S/T"+currentNode.ToString()+siblingNode.ToString()+temp.ToString());
					currentNode = siblingNode;
					siblingNode = temp;
					//temp.clear();
					System.out.println("In Merge C/S/T"+currentNode.ToString()+siblingNode.ToString()+temp.ToString());
				}

				if (!currentNode.isLeafNode()) {
					System.out.println("INSIDE MERGE CURRENT NODE IS NO LEAFNODE IF CONDITION.");
					siblingNode.keys[siblingNode.numberOfKeys++] = key;
					for (i = 0; i < siblingNode.numberOfKeys; i++) {/////////// change
						siblingNode.pointers[siblingNode.numberOfKeys] = currentNode.pointers[i];
						siblingNode.keys[siblingNode.numberOfKeys++] = currentNode.keys[i];
					}
					if(currentNode.pointers[currentNode.numberOfKeys]!=null)
					siblingNode.pointers[siblingNode.numberOfKeys] = currentNode.pointers[currentNode.numberOfKeys];
				} else {
					System.out.println("INSIDE MERGE CURRENT NODE IS NO LEAFNODE ELSE CONDITION.");
					
					// N is leaf node, append all (Ki, Pi) pairs in N to N' ;
					for (i = 0; i < currentNode.numberOfKeys; i++) {
						siblingNode.pointers[siblingNode.numberOfKeys] = currentNode.pointers[i];
						siblingNode.keys[siblingNode.numberOfKeys++] = currentNode.keys[i];
					}
					// update the number of keys in NPrime and set N'.Pn = N.Pn
					siblingNode.pointers[degree - 1] = currentNode.pointers[degree - 1];
					System.out.println("In C/S"+currentNode.ToString()+siblingNode.ToString()+parent.ToString()+parent.ToPointers());
				}
				if(parent.numberOfKeys == 1)
				parent.pointers[currentPointer] = null;
				System.out.println("AFFF"+currentNode.ToString()+siblingNode.ToString()+parent.ToString()+parent.ToPointers()+keyValue);
				delete_key(parent, keyValue, value);
				currentNode.clear();
				
			} else {
				// Redistribution
				if (siblingPointer<currentPointer) {
					if (!currentNode.isLeafNode()) {
						int mPos = 0; 
						
						for(i=0;i<siblingNode.pointers.length;i++){
							if(siblingNode.pointers[i]!=null){
								mPos++;
							}
						}
						
						Object m = siblingNode.pointers[mPos];
						Object tempKm = siblingNode.keys[mPos - 1];
						siblingNode.keys[mPos-1]= null;
						siblingNode.numberOfKeys--;
						siblingNode.keys[mPos]= null;
						currentNode.insert(keyValue, m, 0);
						parent = findParent(currentNode);
						parent.keys[k_pos] = parent.keys[mPos-1];
						//						K M = (K) siblingNode.pointers[mPos];
						//						K tempP = siblingNode.keys[mPos - 1];
						// siblingNode.keys[mPos-1]=null;
						//						siblingNode.numberOfKeys--; // remove (N'.Km-1,N'.Pm)
						// from N'
						// siblingNode.keys[mPos]=null;
						//						currentNode.insert(keyValue, M, 0);
						// parent = findParent(currentNode);
						//						parent.keys[currentPointer] = tempP;
						// parent.keys[currentPointer] = NPrimeKMMinus ;
						// //replace K' in parent(N) by N'.Km-1
						//						keyValue = parent.keys[currentPointer];
					} else {
						int mPos = 0;
						
						K lastkey = currentNode.keys[currentNode.numberOfKeys];
						
						for(i=0;i<siblingNode.pointers.length;i++){
							if(siblingNode.pointers[i]!=null){
								mPos++;
							}
						}
						
						Object tempKm = siblingNode.keys[mPos]; // Storing this for further use before removal

						Object m = siblingNode.pointers[mPos];

						siblingNode.pointers[mPos] = null;
						siblingNode.keys[mPos] = null;
						siblingNode.numberOfKeys--;

						currentNode.insert(lastkey,m, 0);

						//replace K' in parent(N) by N'.Km
						parent =  findParent(currentNode);
						parent.keys[k_pos] = parent.keys[mPos];
						
						
						
						
						//						int mPos = currentNode.numberOfKeys - 1;
						//						K NPrimePointerM = (K) currentNode.pointers[mPos];
						//						K NPrimeKM = currentNode.keys[mPos];
						//						siblingNode.numberOfKeys--; // remove (N'.Km-1,N'.Pm)
						//													// from N'
						//						siblingNode.insert(NPrimeKM, NPrimePointerM, 0); 
						//						parent.keys[currentPointer] = NPrimeKM; 
					}
					//					keyValue = parent.keys[currentIndex];
				} else {
					if (!currentNode.isLeafNode()) {
						int mPos= 0;

						for(i=0; i<siblingNode.pointers.length; i++){
							if(siblingNode.pointers[i]!=null){
								mPos++;
								//break;
							}
						}

						Object m = siblingNode.pointers[mPos];
						Object tempKm = siblingNode.keys[mPos-1];  // Storing this for further use before removal
						siblingNode.keys[mPos-1] = null;
						siblingNode.numberOfKeys--;
						siblingNode.pointers[mPos] = null;
						currentNode.insert(keyValue, m, 0);				
						parent =  findParent(currentNode);
						parent.keys[k_pos] = parent.keys[mPos-1];					
						
						
						//						int m = currentNode.numberOfKeys;
						//						K NPointerM = (K) currentNode.pointers[m];
						//						K NKMMinus = currentNode.keys[m - 1];
						//						currentNode.numberOfKeys--; 
						//						siblingNode.insert(NPointerM, keyValue, 0);
						//						parent.keys[currentIndex] = NKMMinus; 
						//						keyValue = parent.keys[currentIndex];

					} else {
						
						int mPos = 0; // Position of last pointer
						K lastkey = currentNode.keys[currentNode.numberOfKeys]; // Last key

						for(i=0; i<siblingNode.pointers.length; i++){
							if(siblingNode.pointers[i]!=null){
								mPos++;
								//break;
							}
						}

						Object tempKm = siblingNode.keys[mPos];

						Object m = siblingNode.pointers[mPos];

						siblingNode.pointers[mPos] = null;
						siblingNode.keys[mPos] = null;
						siblingNode.numberOfKeys--;

						currentNode.insert(lastkey, m, 0);
						parent =  findParent(currentNode);
						parent.keys[k_pos] = parent.keys[mPos];
						

						//						int m = currentNode.numberOfKeys - 1; 
						//						K NPointerM = (K) currentNode.pointers[m];
						//						K NKM = currentNode.keys[m];
						//						currentNode.numberOfKeys--;
						//						siblingNode.insert(NKM, NPointerM, 0);
						//						parent.keys[currentIndex] = NKM; 
						//						keyValue = parent.keys[currentIndex];

					}
				}
			}

		}
		
	}
	
	private int noOfpointers(Node p) {
		int count = 0;
		for (int j = 0; j < p.pointers.length; j++) {
			if (p.pointers[j] != null)
			count++;
		}
		return count;

	}

	private void shift(int index, Node n) {
		int i = 0;
		// Shifting keys/ pointers to the left. Suppose I'm deleting |10|20|, 10
		// here, I need to shift 20 to the left, else it'll have null value
		if (index != n.numberOfKeys - 1) {
			for (i = index; i < n.numberOfKeys - 1; i++) {
				n.keys[i] = n.keys[i + 1];
				n.pointers[i] = n.pointers[i + 1];
			}
			n.pointers[i] = n.pointers[i + 1];
			n.pointers[i + 1] = null; // After shifting, we get 1 last pointer
			// which is not gonna be used. Hence
			// null.
			n.keys[n.numberOfKeys - 1] = null; // This key is shifted, left,
			// hence null now.
		}
		n.numberOfKeys--; // After deletion, decrease the no. of Keys for the
		// node.

	}
	
	
	
}

