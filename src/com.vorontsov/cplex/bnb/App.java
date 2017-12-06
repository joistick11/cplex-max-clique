package com.vorontsov.cplex.bnb;

public class App {
    public static void main(String[] args) {
        String filePath = "P:\\github\\cplex-max-clique\\test-graph";

        Graph graph = Parser.graphFromFile(filePath);
        System.out.println(graph);
    }
}
