import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


public final class Compress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java AdaptiveHuffmanCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, compress
		File inputFile = new File(args[0]);
		String outputFile = args[1];
		
		InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		OutputBitStream out = new OutputBitStream(outputFile);
		try {
			compress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	static void compress(InputStream in, OutputBitStream out) throws IOException {
		int[] initFreqs = new int[257];
		Arrays.fill(initFreqs, 1);
		
		FrequencyTable freqTable = new FrequencyTable(initFreqs);
		HuffmanEncoder enc = new HuffmanEncoder(out);
		enc.codeTree = freqTable.buildCodeTree();  // We don't need to make a canonical code since we don't transmit the code tree
		int count = 0;
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			enc.write(b);
			
			freqTable.increment(b);
			count++;
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
				enc.codeTree = freqTable.buildCodeTree();
			if (count % 262144 == 0)  // Reset frequency table
				freqTable = new FrequencyTable(initFreqs);
		}
		enc.write(256);  // EOF
	}
	
	
	private static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}
	
}

class FrequencyTable {
	
	private int[] frequencies;
	
	
	
	public FrequencyTable(int[] freqs) {
		if (freqs == null)
			throw new NullPointerException("Argument is null");
		if (freqs.length < 2)
			throw new IllegalArgumentException("At least 2 symbols needed");
		frequencies = freqs.clone();  // Defensive copy
		for (int x : frequencies) {
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
		}
	}
	
	
	
	public int getSymbolLimit() {
		return frequencies.length;
	}
	
	
	public int get(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		return frequencies[symbol];
	}
	
	
	public void set(int symbol, int freq) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		frequencies[symbol] = freq;
	}
	
	
	public void increment(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (frequencies[symbol] == Integer.MAX_VALUE)
			throw new RuntimeException("Arithmetic overflow");
		frequencies[symbol]++;
	}
	
	
	// Returns a string showing all the symbols and frequencies. The format is subject to change. Useful for debugging.
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < frequencies.length; i++)
			sb.append(String.format("%d\t%d%n", i, frequencies[i]));
		return sb.toString();
	}
	
	
	// Returns a code tree that is optimal for these frequencies. Always contains at least 2 symbols, to avoid degenerate trees.
	public CodeTree buildCodeTree() {
		// Note that if two nodes have the same frequency, then the tie is broken by which tree contains the lowest symbol. Thus the algorithm is not dependent on how the queue breaks ties.
		Queue<NodeWithFrequency> pqueue = new PriorityQueue<NodeWithFrequency>();
		
		// Add leaves for symbols with non-zero frequency
		for (int i = 0; i < frequencies.length; i++) {
			if (frequencies[i] > 0)
				pqueue.add(new NodeWithFrequency(new Leaf(i), i, frequencies[i]));
		}
		
		// Pad with zero-frequency symbols until queue has at least 2 items
		for (int i = 0; i < frequencies.length && pqueue.size() < 2; i++) {
			if (i >= frequencies.length || frequencies[i] == 0)
				pqueue.add(new NodeWithFrequency(new Leaf(i), i, 0));
		}
		if (pqueue.size() < 2)
			throw new AssertionError();
		
		// Repeatedly tie together two nodes with the lowest frequency
		while (pqueue.size() > 1) {
			NodeWithFrequency nf1 = pqueue.remove();
			NodeWithFrequency nf2 = pqueue.remove();
			pqueue.add(new NodeWithFrequency(
					new InternalNode(nf1.node, nf2.node),
					Math.min(nf1.lowestSymbol, nf2.lowestSymbol),
					nf1.frequency + nf2.frequency));
		}
		
		// Return the remaining node
		return new CodeTree((InternalNode)pqueue.remove().node, frequencies.length);
	}
	
	
	
	private static class NodeWithFrequency implements Comparable<NodeWithFrequency> {
		
		public final Node node;
		
		public final int lowestSymbol;
		
		public final long frequency;  // Using long prevents overflow
		
		
		public NodeWithFrequency(Node node, int lowestSymbol, long freq) {
			this.node = node;
			this.lowestSymbol = lowestSymbol;
			this.frequency = freq;
		}
		
		
		public int compareTo(NodeWithFrequency other) {
			if (frequency < other.frequency)
				return -1;
			else if (frequency > other.frequency)
				return 1;
			else if (lowestSymbol < other.lowestSymbol)
				return -1;
			else if (lowestSymbol > other.lowestSymbol)
				return 1;
			else
				return 0;
		}
		
	}
	
}

class CodeTree {
	
	public final InternalNode root;  // Not null
	
	// Stores the code for each symbol, or null if the symbol has no code.
	// For example, if symbol 5 has code 10011, then codes.get(5) is the list [1, 0, 0, 1, 1].
	private List<List<Integer>> codes;
	
	
	
	// Every symbol in the tree 'root' must be strictly less than 'symbolLimit'.
	public CodeTree(InternalNode root, int symbolLimit) {
		if (root == null)
			throw new NullPointerException("Argument is null");
		this.root = root;
		
		codes = new ArrayList<List<Integer>>();  // Initially all null
		for (int i = 0; i < symbolLimit; i++)
			codes.add(null);
		buildCodeList(root, new ArrayList<Integer>());  // Fills 'codes' with appropriate data
	}
	
	
	private void buildCodeList(Node node, List<Integer> prefix) {
		if (node instanceof InternalNode) {
			InternalNode internalNode = (InternalNode)node;
			
			prefix.add(0);
			buildCodeList(internalNode.leftChild , prefix);
			prefix.remove(prefix.size() - 1);
			
			prefix.add(1);
			buildCodeList(internalNode.rightChild, prefix);
			prefix.remove(prefix.size() - 1);
			
		} else if (node instanceof Leaf) {
			Leaf leaf = (Leaf)node;
			if (leaf.symbol >= codes.size())
				throw new IllegalArgumentException("Symbol exceeds symbol limit");
			if (codes.get(leaf.symbol) != null)
				throw new IllegalArgumentException("Symbol has more than one code");
			codes.set(leaf.symbol, new ArrayList<Integer>(prefix));
			
		} else {
			throw new AssertionError("Illegal node type");
		}
	}
	
	
	
	public List<Integer> getCode(int symbol) {
		if (symbol < 0)
			throw new IllegalArgumentException("Illegal symbol");
		else if (codes.get(symbol) == null)
			throw new IllegalArgumentException("No code for given symbol");
		else
			return codes.get(symbol);
	}
	
	
	// Returns a string showing all the codes in this tree. The format is subject to change. Useful for debugging.
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString("", root, sb);
		return sb.toString();
	}
	
	
	private static void toString(String prefix, Node node, StringBuilder sb) {
		if (node instanceof InternalNode) {
			InternalNode internalNode = (InternalNode)node;
			toString(prefix + "0", internalNode.leftChild , sb);
			toString(prefix + "1", internalNode.rightChild, sb);
		} else if (node instanceof Leaf) {
			sb.append(String.format("Code %s: Symbol %d%n", prefix, ((Leaf)node).symbol));
		} else {
			throw new AssertionError("Illegal node type");
		}
	}
	
}

class InternalNode extends Node {
	
	public final Node leftChild;  // Not null
	
	public final Node rightChild;  // Not null
	
	
	
	public InternalNode(Node leftChild, Node rightChild) {
		if (leftChild == null || rightChild == null)
			throw new NullPointerException("Argument is null");
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	
}

class Node {
	
	Node() {}  // Package-private to prevent accidental subclassing outside of this package
	
}

class Leaf extends Node {
	
	public final int symbol;
	
	
	
	public Leaf(int symbol) {
		if (symbol < 0)
			throw new IllegalArgumentException("Illegal symbol value");
		this.symbol = symbol;
	}
	
}

class HuffmanEncoder {
	
	public static final boolean BIT_1 = true;
	public static final boolean BIT_0 = false;
	private OutputBitStream output;
	
	// Must be initialized before calling write().
	// The code tree can be changed after each symbol encoded, as long as the encoder and decoder have the same code tree at the same time.
	public CodeTree codeTree;
	
	
	
	public HuffmanEncoder(OutputBitStream out) {
		if (out == null)
			throw new NullPointerException("Argument is null");
		output = out;
	}
	
	
	
	public void write(int symbol) throws IOException {
		if (codeTree == null)
			throw new NullPointerException("Code tree is null");
		
		List<Integer> bits = codeTree.getCode(symbol);
		for (Integer b : bits) {
			boolean bool = b!=0 ? BIT_1 : BIT_0;
			output.write(bool);
		}	
	}
	
}