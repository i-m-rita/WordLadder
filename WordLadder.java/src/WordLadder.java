import java.io.*;
import java.util.*;

public class WordLadder {

    static class Node {
        String word;
        List<Node> neighbors = new ArrayList<>();
        int distance = Integer.MAX_VALUE;
        List<Node> predecessors = new ArrayList<>();

        Node(String word) {
            this.word = word;
        }
    }

    static class Graph {
        Map<String, Node> map = new HashMap<>();

        /**
         * Use putIfAbsent to check if the node is absent or not.
         * If the node is absent, then create the node.
         * @param word
         * @return
         */
        Node createNode(String word) {
            map.putIfAbsent(word, new Node(word));
            return map.get(word);
        }

        /**
         * Adds an edge from one word to another.
         * @param w1
         * @param w2
         */
        void addEdge(String w1, String w2) {
            Node v1 = createNode(w1);
            Node v2 = createNode(w2);
            v1.neighbors.add(v2);
        }

        /**
         * Replace the word in the node we created.
         * @param word
         * @return
         */
        Node getNode(String word) {
            return map.get(word);
        }
    }


    /**
     * Use BufferReader to read the list of words from text file of dictionary given in the program
     * @param filename
     * @return
     * @throws IOException
     */
    static List<String> readFile(String filename) throws IOException {
        List<String> words = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            words.add(line.trim().toLowerCase());
        }
        br.close();
        return words;
    }

    /**
     * Builds a graph of words by using a bunch of "buckets" where words in the same bucket are connected in the graph.
     * Use computeIfAbsent to check if the node is absent or not.
     * @param wordList
     * @return
     */
    static Graph buildGraph(List<String> wordList) {
        Map<String, List<String>> buckets = new HashMap<>();
        Graph graph = new Graph();

        for (String word : wordList) {
            for (int i = 0; i < word.length(); i++) {
                String bucket = word.substring(0, i) + "_" + word.substring(i + 1);
                buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(word);
            }
        }

        for (List<String> group : buckets.values()) {
            for (String word1 : group) {
                for (String word2 : group) {
                    if (!word1.equals(word2)) {
                        graph.addEdge(word1, word2);
                    }
                }
            }
        }

        return graph;
    }

    /**
     * Use bfs to find all shortest paths from the start word to the target word in the given word graph.
     * It then tracks all shortest paths using a predecessor list for each node and then backtracks from the target node to fix up all possible minimal paths.
     * @param graph
     * @param startString
     * @param targetString
     * @return
     */
    static List<List<String>> bfsAllShortestPaths(Graph graph, String startString, String targetString) {
        for (Node v : graph.map.values()) {
            v.distance = Integer.MAX_VALUE;
            v.predecessors.clear();
        }

        Node start = graph.getNode(startString);
        Node end = graph.getNode(targetString);
        start.distance = 0;

        Queue<Node> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Node neighbor : current.neighbors) {
                if (neighbor.distance == Integer.MAX_VALUE) {
                    neighbor.distance = current.distance + 1;
                    neighbor.predecessors.add(current);
                    queue.add(neighbor);
                } else if (neighbor.distance == current.distance + 1) {
                    neighbor.predecessors.add(current);  // Another shortest path
                }
            }
        }

        if (end.distance == Integer.MAX_VALUE) return new ArrayList<>();

        List<List<String>> allPaths = new ArrayList<>();
        LinkedList<String> currentPath = new LinkedList<>();
        backTrack(end, startString, currentPath, allPaths);
        return allPaths;
    }

    /**
     * This method works backward from the target word to the start word and builds all the shortest paths.
     * @param current
     * @param startWord
     * @param path
     * @param allPaths
     */
    static void backTrack(Node current, String startWord, LinkedList<String> path, List<List<String>> allPaths) {
        path.addFirst(current.word);
        if (current.word.equals(startWord)) {
            allPaths.add(new ArrayList<>(path));
        } else {
            for (Node pred : current.predecessors) {
                backTrack(pred, startWord, path, allPaths);
            }
        }
        path.removeFirst();
    }

    /**
     * Print the required result as the same as the expected output given in the instruction.
     * @param start
     * @param target
     * @param paths
     */
    static void printResult(String start, String target, List<List<String>> paths) {
        int steps = paths.get(0).size() - 1;
        System.out.println(start + " --> " + target);
        System.out.println("the minimum number of the steps: " + steps);
        System.out.println("the number of solutions with the minimal steps: " + paths.size());
        for (List<String> path : paths) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i)).append(" (").append(i).append(")");
                if (i < path.size() - 1) sb.append(", ");
            }
            System.out.println("[" + sb.toString() + "]");
        }
    }

    /**
     * If there is no solution, print as given in the expected output.
     * @param start
     * @param end
     */
    static void printNoSolution(String start, String end) {
        System.out.println(start + " --> " + end);
        System.out.println("There is no solution in this case");
    }

    /**
     * Main method to run the program and print out all the results.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java WordLadder word.ladder.txt start_string target_string");
            return;
        }

        String fileName = args[0];
        String start = args[1].toLowerCase(); // Read the start string
        String target = args[2].toLowerCase(); // Read the target string

        List<String> words = readFile(fileName);
        Graph graph = buildGraph(words);

        if (!graph.map.containsKey(start) || !graph.map.containsKey(target)) {
            printNoSolution(start, target);
            return;
        }

        List<List<String>> paths = bfsAllShortestPaths(graph, start, target);

        if (paths.isEmpty()) {
            printNoSolution(start, target);
        } else {
            printResult(start, target, paths);
        }
    }
}
