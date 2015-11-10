

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class HashMapTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */

/* Partial solution to the tests of the HashMap */

public class HashMapTests {

    @Test
    public void firstTest() {
        MapInterface map = new HashMap(19);
        map.put("a", 1);
        assertEquals(map.get("a"), new Integer(1));
    }

    @Test
    public void secondTest() {
        MapInterface map = new HashMap(19);
        assertEquals(map.size(), 0);
        map.put("a",1);
        assertEquals(map.size(), 1);
        map.put("b",2);
        assertEquals(map.size(), 2);
        map.put("c",3);
        assertEquals(map.size(), 3);
        assertEquals(map.get("a"), new Integer(1));
        assertEquals(map.size(), 3);
        assertEquals(map.get("b"), new Integer(2));
        assertEquals(map.size(), 3);
        assertEquals(map.get("c"), new Integer(3));
    }
    
    @Test
    public void hardcodeHashCodeTest() {
       MapInterface map = new HashMap(19);
       assertEquals(map.size(), 0); 
       map.put("a", 1, 3);
       map.put("b", 2, 3);
       
       assertEquals(map.get("b", 3), new Integer(2));
       assertEquals(map.get("a", 3), new Integer(1));
    }
    
    @Test
    public void incrementalHashCode() {
        MapInterface map1 = new HashMap(19);
        
        int hash1 = map1.hashCode("marginal");
        int hash2 = map1.hashCode("arginale");
        
        MapInterface map2 = new HashMap(19);
        
        int hash3 = map2.hashCode("marginal");
        char lastKeyCharChar = 'm';
        int lastKeyChar = java.lang.Character.getNumericValue(lastKeyCharChar);
        
        char lastCharChar = 'e';
        int lastChar = java.lang.Character.getNumericValue(lastCharChar);
        
        int hash4 = map2.incrementalHashCode( 8, lastKeyChar, hash3, lastChar);
        
        assertEquals(hash1, hash3);
        
        assertEquals(hash2, hash4);
        
    }
}

