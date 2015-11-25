
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;


public final class Decompress {
	
	public static void main(String[] args) throws IOException {
		/*Verification des arguments*/
		if (args.length < 2) {
			System.err.println("Le programme a besoin de deux fichiers en arguments");
			System.exit(1);
		}
		
		/*Decompression*/
		File outputFile = new File(args[1]);
		InputBitStream input = new InputBitStream(args[0]);
		OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			decompress(input, output);
		} finally {
			output.close();
			input.close();
		}
	}
	
	static void decompress(InputBitStream input, OutputStream output) throws IOException {
		int[] initTable = new int[257];
		Arrays.fill(initTable, 1);
		/*Initialisation de la table de frequence*/
		FrequencyTable freqTable = new FrequencyTable(initTable);
		HuffmanDecoder decoder = new HuffmanDecoder(input);
		decoder.codeTree = freqTable.buildCodeTree();
		int count = 0;
		
		while (true) {
			int symbol = decoder.read();
			/*Fin du fichier*/
			if (symbol == 256)
				break;
			output.write(symbol);
			freqTable.increment(symbol);
			count++;
			/*Mise a jour du code tree*/
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  
				decoder.codeTree = freqTable.buildCodeTree();
			/*Reinitialisation de la table de frequence*/
			if (count % 262144 == 0) 
				freqTable = new FrequencyTable(initTable);
		}
	}
	
	private static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}
	
}

class HuffmanDecoder {
	
	public static final boolean BIT_1 = true;
	public static final boolean BIT_0 = false;
	private InputBitStream input;
	public CodeTree codeTree;
	
	public HuffmanDecoder(InputBitStream in) {
		if (in == null)
			throw new NullPointerException("L'argument est null");
		input = in;
	}
	
	public int read() throws IOException {
		if (codeTree == null)
			throw new NullPointerException("Le code tree est null");
		InternalNode currentNode = codeTree.root;
		while (true) {
			boolean temp = input.readBoolean();
			Node nextNode;
			if (temp == BIT_0) nextNode = currentNode.leftChild;
			else if (temp == BIT_1) nextNode = currentNode.rightChild;
			else throw new AssertionError();
			
			if (nextNode instanceof Leaf)
				return ((Leaf)nextNode).symbol;
			else if (nextNode instanceof InternalNode)
				currentNode = (InternalNode)nextNode;
			else
				throw new AssertionError();
		}
	}
	
}
