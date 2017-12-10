package com.vorontsov.cplex.bnb;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import ilog.concert.IloException;

import java.util.List;
import java.util.concurrent.*;

import static java.lang.Integer.parseInt;

public class App {
    public static void main(String[] args) throws IloException {
        if (args.length == 0) {
            throw new RuntimeException("No file provided!");
        }

        String file = args[0];

        CliqueFindingJob cliqueFindingJob = new CliqueFindingJob(file);
        SimpleTimeLimiter simpleTimeLimiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());

        int timeLimit = args.length > 1 ? parseInt(args[1]) : 3600;

        try {
            long start = System.currentTimeMillis();
            List<Graph.Node> clique = simpleTimeLimiter.callWithTimeout(cliqueFindingJob, timeLimit, TimeUnit.SECONDS);
            System.out.println((System.currentTimeMillis() - start) / 1000.0 + " " + clique.size() + " "
                    + clique);
        } catch (TimeoutException e) {
            System.out.println(cliqueFindingJob.algorithm.getMaxClique().size() + " "
                    + cliqueFindingJob.algorithm.getMaxClique() + " timeout!");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static class CliqueFindingJob implements Callable<List<Graph.Node>> {
        public BnBCplex algorithm;
        private String filePath;

        CliqueFindingJob(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public List<Graph.Node> call() throws Exception {
            Graph graph = Parser.graphFromFile(filePath);
            algorithm = new BnBCplex(graph);
            return algorithm.findMaxClique();
        }
    }
}
