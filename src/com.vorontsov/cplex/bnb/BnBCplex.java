package com.vorontsov.cplex.bnb;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class BnBCplex {
    private IloCplex cplex;
    private Graph graph;
    private List<Graph.Node> maxClique;
    private Map<Graph.Node, IloNumVar> vars;

    BnBCplex(Graph graph) throws IloException {
        this.graph = graph;
        this.maxClique = new LinkedList<>();
        this.cplex = new IloCplex();
        this.vars = new HashMap<>();

        initialize();
    }

    private void initialize() throws IloException {
        // Variables
        initializeVars();

        // objective function -> max
        initializeObjFunc();

        // add obvious constraints on nodes which are not connected
        addPrimitiveConstraints();
        
        addIndependentSetsConstraints();
    }

    private void addIndependentSetsConstraints() {
        Map<Integer, Set<Graph.Node>> independentSets = getIndependentSets(new LinkedList<>(graph.getNodes().values()));
        independentSets.forEach((key, set) -> {
            try {
                IloNumExpr iloNumExpr = cplex.numExpr();
                for (Graph.Node node : set) {
                    iloNumExpr = cplex.sum(iloNumExpr, vars.get(node.getIndex()));
                }

                cplex.addLe(iloNumExpr, 1);
            } catch (IloException e) {
                e.printStackTrace();
            }
        });
    }

    private void addPrimitiveConstraints() throws IloException {
        for (Graph.Node node : graph.getNodes().values()) {
            for (int anotherNodeIndex = node.getIndex(); anotherNodeIndex < graph.getNodes().size(); anotherNodeIndex++) {
                if (!node.getNeighbours().contains(graph.getNodes().get(anotherNodeIndex))) {
                    cplex.addLe(cplex.sum(vars.get(node.getIndex()), vars.get(anotherNodeIndex)), 1);
                }
            }
        }
    }

    private void initializeObjFunc() throws IloException {
        IloLinearNumExpr func = cplex.linearNumExpr();
        vars.values().forEach(x -> {
            try {
                func.addTerm(1, x);
            } catch (IloException e) {
                e.printStackTrace();
            }
        });
        cplex.addMaximize(func);
    }

    private void initializeVars() {
        graph.getNodes().forEach((index, node) -> {
            try {
                vars.put(node, cplex.numVar(0, 1, String.valueOf(node.getIndex())));
            } catch (IloException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Graph.Node> findMaxClique() {
        return null;
    }

    private static Map<Integer, Set<Graph.Node>> getIndependentSets(List<Graph.Node> nodes) {
        // It is better for us to have small color for nodes with a little number of neighbours
        // In this case using |Q|+|R| > |Qmax| we will reject nodes with big number of neighbours
        nodes.sort(Comparator.<Graph.Node>comparingInt(elem -> elem.getNeighbours().size()).reversed());
        int maxColor = 0;
        // contains sets with vertexes of the same color. Key - color number, value - set of nodes of this color
        Map<Integer, Set<Graph.Node>> colorsSets = new HashMap<>();
        Map<Integer, Integer> colors = new HashMap<>();

        for (Graph.Node node : nodes) {
            int k = 1;

            while (true) {
                // Get all nodes of current K color
                Set<Graph.Node> nodesOfCurrentColor = colorsSets.get(k) != null ?
                        new HashSet<>(colorsSets.get(k)) : new HashSet<>();

                // And try to find neighbours with this color
                nodesOfCurrentColor.retainAll(node.getNeighbours());

                // if none - great, current K is suitable for coloring current node
                if (nodesOfCurrentColor.isEmpty()) {
                    break;
                }
                // Otherwise  - continue cycle
                k++;
            }

            if (k > maxColor) {
                maxColor = k;
                // New color, so create a new set for nodes
                colorsSets.put(k, new HashSet<>());
            }
            colorsSets.get(k).add(node);
            colors.put(node.getIndex(), k);
        }

        return colorsSets;
    }
}
