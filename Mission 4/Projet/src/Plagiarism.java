import java.io.*;
import java.util.*;

public class Plagiarism implements PlagiarismInterface {
    private int w;
    private File[] files;
    private int fLength;

     public Plagiarism(String folderpath, int w) {
        File folder = new File(folderpath);
        this.files = folder.listFiles();
        this.fLength = files.length;
        this.w = w;
    }

    /** @pre : 'doc' is the path to a text file
	 * @post : searches for plagiarized sentences of at least 'w' characters between 'doc' and all text files in 'corpus'. 
	 * returns a set of (document name, position) for each plagiarized sentence found in a corpus file
	 * ('position' is the position of the first character of that sentence in the corpus file, starting at 0) */
	public Set<Map.Entry<String, Integer>> detect(String doc){
	    Set<Map.Entry<String, Integer>> set = new HashSet<Map.Entry<String, Integer>>(); // + créer hashmap avec M la taille du fichier
        HashMap map = new HashMap(this.fLength);
        return set;
    }

}
