import java.io.*;
import java.util.*;

public class Plagiarism implements PlagiarismInterface {
    private int w;
    private File[] files;

     public Plagiarism(String folderpath, int w) {
        File folder = new File(folderpath);
        this.files = folder.listFiles();
        this.w = w;
    }

    /** @pre : 'doc' is the path to a text file
     * @post : searches for plagiarized sentences of at least 'w' characters between 'doc' and all text files in 'corpus'. 
     * returns a set of (document name, position) for each plagiarized sentence found in a corpus file
     * ('position' is the position of the first character of that sentence in the corpus file, starting at 0) */
    public Set<Map.Entry<String, Integer>> detect(String doc){
        Set<Map.Entry<String, Integer>> set = new HashSet<Map.Entry<String, Integer>>();
        HashMap map = create(doc);
        String content = null;
        /*Lecture de chaque document du corpus*/
        for(int i = 0 ; i < files.length ; i++)
        {
            try 
            {
               content = new Scanner(files[i]).useDelimiter("\\Z").next();
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            }
            int previousHash = 0 ;
            for (int j = 0; j<content.length()-w+1 ; j++)
            {
                char previous;
                char end;
                if (j==0)
                {
                    String s = content.substring(j,j+w);
                    previousHash= map.hashCode(s);
                    if(map.get(s) != null) /*Match si pas null*/
                    {
                        MyEntry<String,Integer> entry = new MyEntry<String,Integer>(files[i].toString(),(Integer) j ) ;
                        set.add(entry);
                    }
                }
                else
                {
                    previous = content.charAt(j-1);
                    end = content.charAt(j+w-1);
                    String s = content.substring(j,j+w);
                    int h = map.incrementalHashCode(w, java.lang.Character.getNumericValue(previous), previousHash, java.lang.Character.getNumericValue(end));
                    if(map.get(s,h) != null) /*Match si pas null*/
                    {
                        MyEntry<String,Integer> entry = new MyEntry<String,Integer>(files[i].toString(),(Integer) j ) ;
                        set.add(entry);
                    }
                    previousHash = h ;
                }
            }
        }
        return set;
    }

    /** @pre: 'doc' est le chemin du fichier
     *  @post: retourne un hasmap de strings de longueur 'w' lus dans le fichier doc */
    public HashMap create(String doc) {
        String content = null;
        try {
            content = new Scanner(new File(doc)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        HashMap map = new HashMap(content.length());
        int previousHash = 0;
        for(int i = 0; i<content.length()-w+1 ; i ++)
        {
            char previous;
            char end;
            if (i==0)
            {
                String s = content.substring(i,i+w);
                map.put(s,-1);
                previousHash= map.hashCode(s);
            }
            else
            {
                previous = content.charAt(i-1);
                end = content.charAt(i+w-1);
                String s = content.substring(i,i+w);
                int h = map.incrementalHashCode(w, java.lang.Character.getNumericValue(previous), previousHash, java.lang.Character.getNumericValue(end));
                map.put(s,-1,h);
                previousHash = h ;
            }
        }
        return map;
    }
}

/*Classe Entry*/
class MyEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public MyEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V old = this.value;
        this.value = value;
		return old;
	}
}