package com.vorontsov.cplex.bnb;

import ilog.concert.IloException;

public class App {
    public static void main(String[] args) throws IloException {
        String filePath = "/home/netcracker.com/mavo0215/pers-projects/cplex-max-clique/test-graph";

        Graph graph = Parser.graphFromFile(filePath);
        BnBCplex bnBCplex = new BnBCplex(graph);

        System.out.println(graph);
    }
}
