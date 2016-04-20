import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class Peer {
	private Peer[] fingerTable;
	private int n;
	private long id;
	private Peer predecessor;
	private Peer successor;
	private Chord parent;
	private Multimap<Long, String> itemList;

	public Peer(long id, int n) {
		this.n = n;
		this.id = id;
		fingerTable = new Peer[n];
		itemList = new Multimap<Long, String>();
	}

	/* Required functions */

	public void addPeer(long id) {
		Peer newPeer = new Peer(id, n);
		newPeer.join(parent);
		System.out.println(String.format("PEER %d ADDED", id));
	}

	public boolean removePeer(long id) {
		boolean terminated = false;
		Peer removedPeer = findSuccessor(id, true);
		System.out.println();
		// if no peer with that id exists
		if (removedPeer.id != id) {
			return false;
		}

		// if we remove the referenced peer
		if (removedPeer == parent.getReferencedPeer()) {
			parent.setReferencedPeer(removedPeer.successor);
		}

		// if the removed node is the only node in the system
		if (removedPeer.successor == removedPeer) {
			System.out.println("Last of the peers removed. CHORD terminated");
			terminated = true;
		} else {
			removedPeer.predecessor.successor = removedPeer.successor;
			removedPeer.successor.predecessor = removedPeer.predecessor;

			// transfer key to the successor
			for (Entry<Long, Set<String>> item : removedPeer.itemList.getItems()) {
				removedPeer.successor.itemList.addBulkKeyItems(item.getKey(), item.getValue());
			}
			removedPeer.updateOtherFingerTables(removedPeer, false);
		}
		// clean up
		removedPeer.predecessor = null;
		removedPeer.successor = null;
		removedPeer.fingerTable = null;
		removedPeer.itemList = null;
		System.out.println(String.format("PEER %d REMOVED", removedPeer.id));

		if (terminated)
			System.exit(0);
		return true;
	}

	public void insert(String key) {
		long keyId = hash(key);
		Peer peer = findSuccessor(keyId, true);
		System.out.println();
		peer.addNewKey(key);
		System.out.println(String.format("INSERTED %s(key=%d) AT %d", key, keyId, peer.id));
	}

	public void delete(String key) {
		long keyId = hash(key);
		Peer peer = findSuccessor(keyId, true);
		System.out.println();
		boolean deleted = peer.deleteKey(key);
		if (deleted)
			System.out.println(String.format("REMOVED %s(key=%d) FROM %d", key, keyId, peer.id));
		else
			System.out.println("KEY NOT FOUND");
	}

	public Peer findKey(String key) {
		long keyId = hash(key);
		Peer successor = findSuccessor(keyId, true);
		System.out.println();
		boolean found = successor.keyExists(key);
		if (found)
			System.out.println(String.format("FOUND KEY %s (key=%d) AT %d", key, keyId, successor.id));
		else
			System.out.println("KEY NOT FOUND");
		return successor;
	}

	private long hash(String input) {
		int key = 0;
		int length = input.length();
		for (int i = 0; i < length; i++) {
			key = ((key << 5) + key) ^ input.charAt(i);
		}
		long chordSize = powerOfTwo(n);
		key = (int) modPowerOfTwo(key, n);
		if (key < 0) {
			key += chordSize;
		}
		return key;
	}

	public void print(long id) {
		Peer foundPeer = findSuccessor(id, true);
		System.out.println();
		System.out.println("DATA AT INDEX NODE " + foundPeer.id);
		for (Entry<Long, Set<String>> itemBucket : foundPeer.itemList.getItems()) {
			for (String item : itemBucket.getValue()) {
				System.out.println(item);
			}
		}
		System.out.println("FINGER TABLE OF NODE " + foundPeer.id);
		for (int i = 0; i < foundPeer.fingerTable.length; i++)
			System.out.print(foundPeer.fingerTable[i].id + " ");
		System.out.println();
	}

	/* Helper functions */

	private long range(long id1, long id2) {
		if (id1 < id2) {
			id1 += powerOfTwo(n);
		}
		return (id1 - id2);
	}

	private long shift(long id, long step) {
		return modPowerOfTwo(id + step, n);
	}

	private void updateOtherFingerTables(Peer node, boolean insert) {
		for (int i = 0; i < fingerTable.length; i++) {
			// start with updating the predecessor of the new node
			Peer currentNode = node.predecessor;
			// starting Node
			Peer initialNode = node.predecessor;
			while (true) {
				long powerOfTwo = powerOfTwo(i);
				if (range(initialNode.id, currentNode.id) >= powerOfTwo)
					break;
				long shiftId = shift(currentNode.id, powerOfTwo);
				if (inRange(shiftId, initialNode.id, node.id, true)) {
					//only when shiftId is within that range (not equal to the boundary)
					if (shiftId > initialNode.id)
					{
						//we are inserting
						if (insert)
						{
								currentNode.fingerTable[i] = node;
						}
						//we are deleting
						else
						{
							if (shiftId > initialNode.id)
								currentNode.fingerTable[i] = node.successor;
						}
					}
					
				}
				// check the previous peer
				currentNode = currentNode.predecessor;
				// has completed one cycle
				if (currentNode == initialNode || currentNode == node)
					break;
			}
		}

	}

	public long getId() {
		return id;
	}

	private boolean deleteKey(String key) {
		return itemList.removeKeyItem(hash(key), key);
	}

	private boolean keyExists(String key) {
		return itemList.findKeyItem(hash(key), key);
	}

	private void addNewKey(String key) {
		itemList.addKeyItem(hash(key), key);
	}

	private Peer findSuccessor(long keyId, boolean tracking) {
		if (tracking)
			System.out.print(id);
		// if KeyId is equal to this node id
		if (keyId == id)
			return this;
		// keyId is equal to predecessor id
		if (keyId == predecessor.id) {
			// forward to the predecessor
			if (tracking)
				System.out.print(">" + predecessor.id);
			return predecessor;
		}
		if (inRange(keyId, predecessor.getId(), id, false)) {
			// forward to the current node
			if (tracking)
				System.out.print(">" + id);
			return this;
		}
		if (inRange(keyId, id, successor.getId(), false)) {
			// forward to the successor
			if (tracking)
				System.out.print(">" + successor.id);
			return successor;
		}
		// look up the finger table to find the closest preceding node
		int i = 0;
		for (i = 0; i < fingerTable.length - 1; i++) {
			if (inRange(keyId, fingerTable[i].id, fingerTable[i + 1].id, true)) {
				// the successor is more close to the keyId
				if (fingerTable[i + 1].id == keyId)
					i++;
				break;
			}
		}

		// forward the request to another node
		if (tracking)
			System.out.print(">");
		return fingerTable[i].findSuccessor(keyId, tracking);
	}

	public void join(Chord chord) {
		this.parent = chord;
		Peer referencePeer = chord.getReferencedPeer();
		// first node in the chord
		if (referencePeer == this) {
			successor = this;
			predecessor = this;
			for (int i = 0; i < n; i++)
				fingerTable[i] = this;
		} else {
			successor = referencePeer.findSuccessor(id, true);
			System.out.println();
			// a peer with that id already exists in the system
			if (successor.id == id) {
				return;
			}
			predecessor = successor.predecessor;
			successor.predecessor.successor = this;
			successor.predecessor = this;

			// update its finger table
			for (int i = 0; i < n; i++) {
				long keyId = shift(id, powerOfTwo(i));
				if (inRange(keyId, predecessor.id, id, false)) {
					if (keyId == predecessor.id)
						fingerTable[i] = predecessor;
					else
						fingerTable[i] = this;
				} else if (inRange(keyId, id, successor.id, false)) {
					fingerTable[i] = successor;
				} else {
					fingerTable[i] = successor.findSuccessor(keyId, false);
				}

			}

			// update the finger table of existing nodes
			updateOtherFingerTables(this, true);
			Iterator<Entry<Long, Set<String>>> it = successor.itemList.getItems().iterator();
			while (it.hasNext()) {
				Entry<Long, Set<String>> itemBucket = it.next();
				if (inRange(itemBucket.getKey(), predecessor.id, id, false)) {
					it.remove();
					itemList.addBulkKeyItems(itemBucket.getKey(), itemBucket.getValue());
				}
			}
		}
	}

	private boolean inRange(long keyId, long start, long end, boolean strict) {
		if (!strict) {
			// counted as one cycle
			if (start == end)
				return true;
		}
		if (keyId == end || keyId == start)
			return true;
		if (start > end) {
			end += powerOfTwo(n);
			if (keyId < start)
				keyId += powerOfTwo(n);
		}
		return keyId > start && keyId <= end;
	}

	// compute a power of 2
	private static long powerOfTwo(int exponent) {
		return (1 << exponent);
	}

	// compute modulo of a number with a power of 2
	private static long modPowerOfTwo(long number, int exponent) {
		return number & (powerOfTwo(exponent) - 1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node id: " + id + "\n");
		builder.append("Finger table: \n");
		for (int i = 0; i < fingerTable.length; i++) {
			builder.append("Entry ");
			builder.append(i);
			builder.append(": ");
			builder.append(fingerTable[i].id);
			builder.append("\n");
		}
		builder.append("Predecessor: ");
		builder.append(predecessor.id);
		builder.append("\n");
		builder.append("Successor: ");
		builder.append(successor.id);
		builder.append("\n");
		return builder.toString();
	}

}
