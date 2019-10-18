/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

/**
 *
 * @author Nil
 */
class Node {
    int id;
    double x;
	double y;

    Node(int id, double x2, double y2) {
        this.id = id;
        this.x = x2;
        this.y = y2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Node other = (Node) obj;
        if (id!=other.id || x!=other.x || y!=other.y) return false;
        return true;
    }

	@Override
	public String toString() {
		return id+"("+main.prettyStr(x)+","+main.prettyStr(y)+")";
	}
	
    
    
    
}
