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
		/*Verification des arguments*/
		if (args.length < 2) {
			System.err.println("Le programme a besoin de deux fichiers en arguments");
			System.exit(1);
		}

		/*Compression*/
		File inputFile = new File(args[0]);
		InputStream input = new BufferedInputStream(new FileInputStream(inputFile));
		OutputBitStream output = new OutputBitStream(args[1]);
		try {
			compress(input, output);
		} finally {
			output.close();
			input.close();
		}
	}

	static void compress(InputStream input, OutputBitStream output) throws IOException {
		
		int[] initTable = new int[257];
		Arrays.fill(initTable, 1);
		/*Initialisation de la table de frequence*/
		FrequencyTable freqTable = new FrequencyTable(initTable);
		HuffmanEncoder encoder = new HuffmanEncoder(output);
		encoder.codeTree = freqTable.buildCodeTree();
		int count = 0;

		while (true) {
			int b = input.read();
			if (b == -1)
				break;
			encoder.write(b);
			freqTable.increment(b);
			count++;
			/*Mise a jour du code tree*/
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  
				encoder.codeTree = freqTable.buildCodeTree();
			/*Reinitialisation de la table de frequence*/
			if (count % 262144 == 0)  
				freqTable = new FrequencyTable(initTable);
		}
		/*Fin du fichier*/
		encoder.write(256);
	}

	private static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}

}

class HuffmanEncoder {
	
	public static final boolean BIT_1 = true;
	public static final boolean BIT_0 = false;
	private OutputBitStream output;
	public CodeTree codeTree;

	public HuffmanEncoder(OutputBitStream out) {
		if (out == null)
			throw new NullPointerException("L'argument est null");
		output = out;
	}

	public void write(int symbol) throws IOException {
		if (codeTree == null)
			throw new NullPointerException("Le code tree est null");
		List<Integer> bits = codeTree.getCode(symbol);
		for (Integer b : bits) {
			boolean bool = b != 0 ? BIT_1 : BIT_0;
			output.write(bool);
		}	
	}

}

class FrequencyTable {
	
	private int[] frequencies;

	public FrequencyTable(int[] freqs) {
		if (freqs == null)
			throw new NullPointerException("L'argument est null");
		if (freqs.length < 2)
			throw new IllegalArgumentException("La table a au moins besoin de 2 symboles");
		/*Copie de securite*/
		frequencies = freqs.clone();
		for (int x : frequencies) {
			if (x < 0)
				throw new IllegalArgumentException("Frequence negative");
		}
	}

	public int getSizeTable() {
		return frequencies.length;
	}

	public int getFrequency(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbole hors de la table");
		return frequencies[symbol];
	}

	public void setFrequency(int symbol, int freq) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbole hors de la table");
		frequencies[symbol] = freq;
	}

	public void increment(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbole hors de la table");
		frequencies[symbol]++;
	}

	/*Renvoie un code tree qui est optimal pour ces frequences*/
	public CodeTree buildCodeTree() {
		Queue<NodeWithFrequency> queue = new PriorityQueue<NodeWithFrequency>();
		/*Ajoute des noeuds enfants pour les symboles avec des frequences non nulle*/
		for (int i = 0; i < frequencies.length; i++) {
			if (frequencies[i] > 0)
				queue.add(new NodeWithFrequency(new Leaf(i), i, frequencies[i]));
		}
		/*Insertion avec des symboles de frequence nulle tant que la queue n'a pas au minimum 2 objets*/
		for (int i = 0; i < frequencies.length && queue.size() < 2; i++) {
			if (i >= frequencies.length || frequencies[i] == 0)
				queue.add(new NodeWithFrequency(new Leaf(i), i, 0));
		}
		if (queue.size() < 2)
			throw new AssertionError();
		/*Attacher 2 noeuds ensemble avec la plus basse frequence (a plusieurs reprises)*/
		while (queue.size() > 1) {
			NodeWithFrequency nodeFreq1 = queue.remove();
			NodeWithFrequency nodeFreq2 = queue.remove();
			queue.add(new NodeWithFrequency(
					new InternalNode(nodeFreq1.node, nodeFreq2.node),
					Math.min(nodeFreq1.lowestSymbol, nodeFreq2.lowestSymbol),
					nodeFreq1.frequency + nodeFreq2.frequency));
		}
		return new CodeTree((InternalNode)queue.remove().node, frequencies.length);
	}

	private static class NodeWithFrequency implements Comparable<NodeWithFrequency> {
		
		public final Node node;
		public final int lowestSymbol;
		public final long frequency;

		public NodeWithFrequency(Node node, int lowestSymbol, long freq) {
			this.node = node;
			this.lowestSymbol = lowestSymbol;
			this.frequency = freq;
		}

		public int compareTo(NodeWithFrequency nodeFreq) {
			if (frequency < nodeFreq.frequency)
				return -1;
			else if (frequency > nodeFreq.frequency)
				return 1;
			else if (lowestSymbol < nodeFreq.lowestSymbol)
				return -1;
			else if (lowestSymbol > nodeFreq.lowestSymbol)
				return 1;
			else
				return 0;
		}

	}

}

class CodeTree {
	
	public final InternalNode root;
	/*Stocke le code pour chaque symbole, ou null si le symbole n'a pas de code*/
	private List<List<Integer>> codes;

	/*Chaque symbole dans l'arbre 'root' doit etre strictement inferieure a 'symbolLimit'*/
	public CodeTree(InternalNode root, int symbolLimit) {
		if (root == null)
			throw new NullPointerException("L'argument est null");
		this.root = root;
		codes = new ArrayList<List<Integer>>();
		for (int i = 0; i < symbolLimit; i++)
			codes.add(null);
		buildCodeList(root, new ArrayList<Integer>());
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
				throw new IllegalArgumentException("Le symbole depasse la limite");
			if (codes.get(leaf.symbol) != null)
				throw new IllegalArgumentException("Le symbole a plus qu'un code");
			codes.set(leaf.symbol, new ArrayList<Integer>(prefix));
		} else {
			throw new AssertionError("Type du noeud illegal");
		}
	}

	public List<Integer> getCode(int symbol) {
		if (symbol < 0)
			throw new IllegalArgumentException("Symbole illegal");
		else if (codes.get(symbol) == null)
			throw new IllegalArgumentException("Pas de code pour le symbole");
		else
			return codes.get(symbol);
	}

}

class Node {

	Node() {}

}

class InternalNode extends Node {
	
	public final Node leftChild;
	public final Node rightChild; 

	public InternalNode(Node leftChild, Node rightChild) {
		if (leftChild == null || rightChild == null)
			throw new NullPointerException("L'argument est null");
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

}

class Leaf extends Node {
	
	public final int symbol;

	public Leaf(int symbol) {
		if (symbol < 0)
			throw new IllegalArgumentException("Valeur symbole illegale");
		this.symbol = symbol;
	}

}

