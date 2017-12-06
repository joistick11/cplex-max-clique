package com.vorontsov.cplex.bnb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<Integer, Node> nodes;

    Graph() {
        this.nodes = new HashMap<>();
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    void createEdge(int index1, int index2) {
        Node node1 = getOrCreateNode(index1);
        Node node2 = getOrCreateNode(index2);

        node1.addNeighbour(node2);
        node2.addNeighbour(node1);
    }

    private Node getOrCreateNode(int index) {
        if (nodes.get(index) == null) {
            nodes.put(index, new Node(index));
        }

        return nodes.get(index);
    }

    public class Node {
        private int index;
        private List<Node> neighbours;

        public Node(int index) {
            this.index = index;
            this.neighbours = new LinkedList<>();
        }

        public void addNeighbour(Node neighbour) {
            neighbours.add(neighbour);
        }

        public int getIndex() {
            return index;
        }

        public List<Node> getNeighbours() {
            return neighbours;
        }

        @Override
        public String toString() {
            return String.valueOf(index);
        }
    }
}
