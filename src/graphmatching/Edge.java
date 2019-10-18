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
class Edge implements Comparable<Edge>{
    Node first, second;

    Edge(Node first, Node second) {
            this.first = first;
            this.second = second;
        
    }
    
    Node getOther(Node node) {
        if (first.equals(node)) {
            return second;
        }
        return first;
    }

    double dist() {
        double xDiff = first.x - second.x;
        double yDiff = first.y - second.y;
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge otherEdge = (Edge) other;
            return (first == otherEdge.first && second == otherEdge.second) ||
				   (first == otherEdge.second && second == otherEdge.first);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.first.id;
        hash = 29 * hash + this.second.id;
        return hash;
    }

    @Override
    public String toString() {
        return "{" + first.id + "," + second.id + "}";
    }

    @Override
    public int compareTo(Edge e){
        if(dist() == e.dist()) return 0;
        return dist() < e.dist() ? -1 : 1;
    }
}
