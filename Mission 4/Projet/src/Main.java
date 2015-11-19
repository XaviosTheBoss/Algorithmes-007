import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Main {
	public static void main(String[] args) {
    	MapInterface<String, Integer> map = new HashMap(3);
    	map.put("coucou", 2);
    	map.put("hihi", 4);
    	map.put("lolo", 6);
    	System.out.println("size : "+map.size());
    	System.out.println("coucou : "+map.get("coucou"));
    	System.out.println("hihi : "+map.get("hihi"));
    	System.out.println("lolo : "+map.get("lolo"));
    	PlagiarismInterface p = new Plagiarism("corpus",10);
    	
    	Set<Map.Entry<String, Integer>> entry = p.detect("document.txt");
    	Iterator<Entry<String, Integer>> it = entry.iterator();
    	while (it.hasNext()) {
    		Entry<String, Integer> tmp = it.next();
    		System.out.println(tmp.getKey()+" "+tmp.getValue());
    	}
    }
}
