/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author Jimmy
 */
@Serializable
public class BiMap<K extends Object, V extends Object> implements Cloneable {
    
    private HashMap<K, V> kMap = new HashMap<K, V>();
    private HashMap<V, K> vMap = new HashMap<V, K>();
    
    public BiMap() {
    }
    
    /**
     * Creates bidirectional mapping k<->v.
     * Removes any previous mappings k<->? and ?<->v.
     * 
     * @param k
     * @param v 
     */
    public void put(K k, V v) {
        
        // Assure consistency
        removeKey(k);
        removeValue(v);
        
        kMap.put(k, v);
        vMap.put(v, k);
    }
    
    /**
     * @param v
     * @return the key mapped to value v
     */
    public K getKey(V v) {
        return vMap.get(v);
    }
    /**
     * @param k
     * @return the value mapped to key k
     */
    public V getValue(K k) {
        return kMap.get(k);
    }
    
    /**
     * Removes mappings k->v and v->k
     * @param k
     * @return 
     */
    public V removeKey(K k) {
        V v = kMap.remove(k);
        vMap.remove(v);
        return v;
    }
    /**
     * Removes mappings v->k and k->v
     * @param k
     * @return 
     */
    public K removeValue(V v) {
        K k = vMap.remove(v);
        kMap.remove(k);
        return k;
    }
    
    /**
     * @return a list of all keys in the collection 
     */
    public List<K> getKeys() {
        // Return a copy, otherwise the values can be 
        // modified via the key set and cause inconsistencies
        return new ArrayList<K>(kMap.keySet());
        // return kMap.keySet();
    }
    
    /**
     * @return a list of all values in the collection
     */
    public List<V> getValues() {
        // Return a copy, otherwise the values can be 
        // modified via the key set and cause inconsistencies
        return new ArrayList<V>(vMap.keySet());
        // return vMap.keySet();
    }
    
    /**
     * Copies all keys into c
     * @param c 
     */
    public void getKeys(Collection<K> c) {
        c.addAll(kMap.keySet());
    }
    
    /**
     * Copies all values into c
     * @param c 
     */
    public void getValues(Collection<V> c) {
        c.addAll(vMap.keySet());
    }
    
    
    
    /************/
    /*   TEST   */
    public static void main(String[] args) {
        BiMap<String, Integer> b = new BiMap<String, Integer>();
        b.put("aha", 15);
        b.print();
        b.put("aha", 7);
        b.put("kött", 8);
        b.print();
        b.put("lingon", 7);
        b.print();
        b.put("", 5);
        b.put("skam", 54);
        b.print();
        b.put("", 6);
        b.put("", 54);
        b.print();
        b.removeKey("");
        b.removeValue(54);
        b.removeValue(7);
        
        b.put("Prutt", 67);
        b.put("stur", 673);
        b.print();
        
        b.getKeys().clear();
        b.print();
        
        b.getValues().clear();
        b.print();
    }
    
    public void print() {
        System.out.println("*Key, Value*");
        for (K k : getKeys()) {
            System.out.println(k + ", " + getValue(k));
        }
        System.out.println("*Value, Key*");
        for (V v : getValues()) {
            System.out.println(v + ", " + getKey(v));
        }
    }
    /************/
}
