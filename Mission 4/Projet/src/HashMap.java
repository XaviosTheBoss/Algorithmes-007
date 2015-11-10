
public class HashMap implements MapInterface<String, Integer> {

    private int N;           /*nombre de paires de key-value dans la table*/
    private int M;           /*taille de la table*/
    private String[] keys;   /*les clefs*/
    private Integer[] vals;  /*les valeurs*/
    
    /*Initialise une table vide de taille du double de 'capacity'*/
    public HashMap(int capacity) {
        int size = 2*capacity;
        for(int i = 2; i<Math.sqrt(3*capacity); i++){
            if((size % i) == 0){
                size++;
                i = 2;
            }
        }
        this.M = size;
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
        int hash = 0;
        int R = 10;
        int size = key.length();
        int j;
        for(j = 0; j < size; j++){
            hash = (R*hash + key.charAt(j)) % M;
        }
        return hash;
    }
    
    @Override
    /*
     * Pour rappel, cette fonction calcul le hash du mot décallé d'un caractère vers la droite
     * sur base du hash précédent. Par exemple, elle calcul le hash pour le mot "arginale" si 
     * vous lui donner la valeur du hash du mot "marginal" en troisième argument, la valeur 
     * numérique du premier caractère de marginal (à savoir 'm') en deuxième argument, la 
     * longueur des mots en cours (à savoir 8) en premier argument et la valeur numérique du
     * caractère "ajouté" en dernier argument.
     * 
     * @pre : 
     * keyLength = longueur utilisée pour la calcul du hash précédent et qui va être utilisée
     * pour le calcul du hash en cours.
     * 
     * lastKeyChar = valeur numérique du premier caractère du mot dont le lastHash est le hash.
     * 
     * lastChar = dernière lettre du nouveau mot dont on cherche le hash.
     * 
     * @post : retourne la valeur du hash du mot "décallé" d'un caractère dans le texte.
     * 
     */
    public int incrementalHashCode(int keyLength, int lastKeyChar, int lastHash, int lastChar) {

        int R = 10;
        
        int quick_next_string_hash = (lastHash + M - (int)(Math.pow(R,keyLength-1))*lastKeyChar % M ) % M;
        
        quick_next_string_hash = (quick_next_string_hash*R + lastChar) % M; 
        
        return quick_next_string_hash;
    }

}
