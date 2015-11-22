
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;


public final class Decompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java AdaptiveHuffmanDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, decompress
		String inputFile = args[0];
		File outputFile = new File(args[1]);
		
		InputBitStream in = new InputBitStream(inputFile);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			decompress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	static void decompress(InputBitStream in, OutputStream out) throws IOException {
		int[] initFreqs = new int[257];
		Arrays.fill(initFreqs, 1);
		
		FrequencyTable freqTable = new FrequencyTable(initFreqs);
		HuffmanDecoder dec = new HuffmanDecoder(in);
		dec.codeTree = freqTable.buildCodeTree();
		int count = 0;
		while (true) {
			int symbol = dec.read();
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
			
			freqTable.increment(symbol);
			count++;
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
				dec.codeTree = freqTable.buildCodeTree();
			if (count % 262144 == 0)  // Reset frequency table
				freqTable = new FrequencyTable(initFreqs);
		}
	}
	
	
	private static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}
	
}

class HuffmanDecoder {
	
	private InputBitStream input;
	
	// Must be initialized before calling read().
	// The code tree can be changed after each symbol decoded, as long as the encoder and decoder have the same code tree at the same time.
	public CodeTree codeTree;
	
	
	
	public HuffmanDecoder(InputBitStream in) {
		if (in == null)
			throw new NullPointerException("Argument is null");
		input = in;
	}
	
	
	
	public int read() throws IOException {
		if (codeTree == null)
			throw new NullPointerException("Code tree is null");
		
		InternalNode currentNode = codeTree.root;
		while (true) {
			boolean temp = input.readBoolean();
			Node nextNode;
			if      (temp == false) nextNode = currentNode.leftChild;
			else if (temp == true) nextNode = currentNode.rightChild;
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