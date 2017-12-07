package com.vorontsov.cplex.bnb;

import ilog.concert.IloException;

import java.util.List;

public class App {
    public static void main(String[] args) throws IloException {
        long start = System.currentTimeMillis();
        String filePath = "P:\\github\\cplex-max-clique\\graphs\\c-fat200-5.clq.txt";

        Graph graph = Parser.graphFromFile(filePath);
        BnBCplex bnBCplex = new BnBCplex(graph);
        List<Graph.Node> clique = bnBCplex.findMaxClique();
        System.out.println("Working time: " + (System.currentTimeMillis() - start) / 1000.0 + "s");
        System.out.println(clique);
        System.out.println(clique.size());

        System.out.println(graph);
    }
}
