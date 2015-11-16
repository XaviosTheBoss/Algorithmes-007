
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
    	PlagiarismInterface p = new Plagiarism("corpus",2);
    	p.detect("document.txt");
    }
}
