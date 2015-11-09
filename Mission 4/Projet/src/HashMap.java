
public class HashMap implements MapInterface<String, Integer> {

    private int N;           /*nombre de paires de key-value dans la table*/
    private int M;           /*taille de la table*/
    private String[] keys;   /*les clés*/
    private Integer[] vals;  /*les valeurs*/
    
    /*Initialise une table vide de taille du double de 'capacity'*/
    public HashMap(int capacity) {
        this.M = 2*capacity;
        this.keys = (String[])new String[M];
        this.vals = (Integer[])new Integer[M];
    }
    
	@Override
	public Integer get(String key) {
		for (int i = hashCode(key); keys[i] != null; i = (i + 1) % M) 
            if (keys[i].equals(key))
                return vals[i];
        return null;
	}
	
	@Override
	public Integer get(String key, int hashCode) {
		for (int i = hashCode; keys[i] != null; i = (i + 1) % M) 
            if (keys[i].equals(key))
                return vals[i];
        return null;
	}
	
	@Override
	public void put(String key, Integer value) {
        int i;
        for (i = hashCode(key); keys[i] != null; i = (i + 1) % M) {
            if (keys[i].equals(key)) {
                vals[i] = value;
                return;
            }
        }
        keys[i] = key;
        vals[i] = value;
        N++;
	}
	
	@Override
	public void put(String key, Integer value, int hashCode) {
		int i;
        for (i = hashCode; keys[i] != null; i = (i + 1) % M) {
            if (keys[i].equals(key)) {
                vals[i] = value;
                return;
            }
        }
        keys[i] = key;
        vals[i] = value;
        N++;
	}
	
	@Override
	public int size() {
		return N;
	}
	
	@Override
	public int hashCode(String key) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int incrementalHashCode(int keyLength, int lastKeyChar, int lastHash, int lastChar) {
		// TODO Auto-generated method stub
		return 0;
	}

}
