package com.vorontsov.cplex.bnb;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.IloCplex;

public class BnBCplex {
    private IloCplex cplex;
    private Graph graph;
    private List<Graph.Node> maxClique;
    private Map<Integer, IloNumVar> vars;

    BnBCplex(Graph graph) throws IloException {
        this.graph = graph;
        this.maxClique = new LinkedList<>();
        this.cplex = new IloCplex();
        this.vars = new HashMap<>();

        initialize();
    }

    private void initialize() throws IloException {
        cplex.setOut(null);

        // Variables
        initializeVars();

        // objective function -> max
        initializeObjFunc();

        // add obvious constraints on nodes which are not connected
        addPrimitiveConstraints();

        // add constraints based on independent sets
        addIndependentSetsConstraints();
    }

    private void addIndependentSetsConstraints() {
        Map<Integer, Set<Graph.Node>> independentSets = getIndependentSets(new LinkedList<>(graph.getNodes().values()));
        independentSets.values().stream().filter(s -> s.size() > 1)
                .forEach((set) -> {
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
            for (int anotherNodeIndex = node.getIndex() + 1; anotherNodeIndex <= graph.getNodes().size(); anotherNodeIndex++) {
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
                vars.put(node.getIndex(), cplex.numVar(0, 1, String.valueOf(node.getIndex()) + "n"));
            } catch (IloException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Graph.Node> findMaxClique() throws IloException {
        findCliqueInternal();

        return maxClique;
    }

    private void findCliqueInternal() throws IloException {
        if (cplex.solve()) {
            // this branch won't give us better result than existing one
            if (maxClique.size() > Math.floor(cplex.getObjValue())) {
                return;
            }

            double[] varsValues = cplex.getValues(vars.values().toArray(new IloNumVar[vars.size()]));
            int firstFractalIndex = -1;
            List<Graph.Node> possibleMaxClique = new LinkedList<>();
            for (int d = 0; d < varsValues.length; d++) {
                // if fractional var is found - doing branching basing on it
                if (varsValues[d] % 1 != 0.0) {
                    firstFractalIndex = d;
                    break;
                }

                // until we found fractal value of some var - it is potentially a clique
                if (varsValues[d] == 1.0) {
                    possibleMaxClique.add(graph.getNodes().get(d + 1));
                }
            }

            // it is an integer solution
            // if possible max clique is bigger then previous one - we found new max clique
            if (firstFractalIndex == -1) {
                if (maxClique.size() < possibleMaxClique.size()) {
                    maxClique = possibleMaxClique;
                }
            } else {
                // otherwise doing branching
                IloRange newBranchConstraint = cplex.addGe(vars.get(firstFractalIndex + 1), 1);
                findCliqueInternal();
                cplex.remove(newBranchConstraint);

                newBranchConstraint = cplex.addLe(vars.get(firstFractalIndex + 1), 0);
                findCliqueInternal();
                cplex.remove(newBranchConstraint);
            }
        }
    }

    private static Map<Integer, Set<Graph.Node>> getIndependentSets(List<Graph.Node> nodes) {
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

    public List<Graph.Node> getMaxClique() {
        return maxClique;
    }
}
