/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Nil
 */
class LinkedSet<T> {
    ArrayList<T> elems;
	HashMap<T,Integer> positions;
    int size() {return elems.size();}

	T getAny() {
		return elems.get(elems.size()-1);
	}
	
	boolean contains(T item) {
		return positions.containsKey(item);
	}
	
	T extractAny() {
		T elem = elems.get(elems.size()-1);
		elems.remove(elems.size()-1);
		positions.remove(elem);
		return elem;
	}
	
	LinkedSet(Collection<T> setElems) {
		elems = new ArrayList<> (setElems);
		positions = new HashMap<> ();
		int i = 0;
		for (T elem : elems) {
			positions.put(elem, i);
			i++;
		}
	}
    
	void remove(T elem) {
		Integer pos = positions.get(elem);
		if (pos == null) return;
		positions.remove(elem);
		if (pos == elems.size()-1) {
			elems.remove(elems.size()-1);
		} else {
			T lastElem = elems.get(elems.size()-1);
			elems.set(pos, lastElem);
			elems.remove(elems.size()-1);
			positions.put(lastElem, pos);
		}
	}

	boolean isEmpty() {
		return elems.isEmpty();
	}

	@Override
	public String toString() {
		return "DelSet{" + "elems=" + elems + '}';
	}

};




