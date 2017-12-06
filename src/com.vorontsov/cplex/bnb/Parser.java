package com.vorontsov.cplex.bnb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class Parser {

    public static Graph graphFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return parseGraphFromStream(reader.lines());
        } catch (IOException e) {
            throw new IllegalStateException("Error", e);
        }
    }

    private static Graph parseGraphFromStream(Stream<String> lines) {
        Graph graph = new Graph();
        lines.forEach(edge -> {
            String[] line = edge.split(" ");
            if ("e".equals(line[0])) {
                graph.createEdge(parseInt(line[1]), parseInt(line[2]));
            }
        });

        return graph;
    }
}
