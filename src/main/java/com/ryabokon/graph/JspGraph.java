package com.ryabokon.graph;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JspGraph {

    //TODO type in the path
    final String ROOT_PATH = "path_to_jsp_folder";
    final String PATH_TO_JSPS = ROOT_PATH + "/WEB-INF/jsp";
    Pattern pattern = Pattern.compile("([\\./\\-a-zA-Z\\d])*\\w+\\.jsp");
    Graph graph = new MultiGraph("JSP");

    public JspGraph() throws InterruptedException, IOException {
        setUpDisplay();
        createNodes();
        markNodes();
        addLinks();
        cleanupNodes();
        addLables();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new JspGraph();
    }

    private void setUpDisplay() {

        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        Viewer viewer = graph.display(false);
        Layout layout = new SpringBox();
        layout.setQuality(0); //set to 0 if it lags
        //layout.setStabilizationLimit(1);
        layout.setForce(1.5);
        viewer.enableAutoLayout(layout);
    }

    private void createNodes() throws InterruptedException, IOException {
        Files.walk(Paths.get(PATH_TO_JSPS), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> addNode(path));
    }

    private void addLinks() throws IOException {
        Files.walk(Paths.get(PATH_TO_JSPS), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> linkIncludedJsps(path));
    }


    private void markNodes() throws IOException {

        graph.getNodeSet().stream()
                //.filter(n -> n.getOutDegree() != 0 && n.getOutDegree() == 0) //Parent nodes
                .filter(n -> n.getId().contains("/subMerchant/"))
                .forEach(node -> node.addAttribute("ui.style", "fill-color: red; text-color: red;"));
    }

    //Delete nodes with no links
    private void cleanupNodes() throws IOException {
        graph.getNodeSet().stream()
                .filter(n -> n.getDegree() == 0)
                .map(n -> n.getId())
                .collect(Collectors.toList()).stream()
                .forEach(id -> removeNode(id));
    }

    private void addLables() {
        graph.getNodeSet().stream()
                .forEach(n -> n.addAttribute("ui.label", n.getId().substring(PATH_TO_JSPS.length())));
    }

    private void linkIncludedJsps(Path path) {
        Node parent = graph.getNode(path.toString());

        getLinesStream(path)
                .filter(line -> line.contains(".jsp\""))
                .flatMap(line ->
                {
                    List<String> includes = new ArrayList<>();
                    Matcher m = pattern.matcher(line);
                    while (m.find()) {
                        includes.add(m.group(0));
                    }
                    return includes.stream();
                })
                .filter(jsp -> !jsp.contains("tagLibs.jsp")) //Everything includes tagLibs.jsp
                .filter(jsp -> !jsp.contains("localization.jsp"))
                .filter(jsp -> !jsp.contains("locale.jsp"))
                .filter(jsp -> !jsp.contains("rowStyle.jsp"))
                .map(jsp -> normalizeJspPath(path, jsp)) //Get full path by include path
                .filter(jsp -> !(graph.getNode(jsp) == null)) //Hide jsps that are included but missing in fs
                .map(jsp -> (Node) graph.getNode(jsp))
                .forEach(child -> addLink(parent, child));

    }

    private Stream<String> getLinesStream(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Get full jsp path by include path
    private String normalizeJspPath(Path path, String includeJsp) {
        if (includeJsp.startsWith("/")) {
            return ROOT_PATH + includeJsp;
        }
        return path.getParent().resolve(includeJsp).normalize().toString();
    }

    private void addLink(Node parent, Node child) {
        graph.addEdge(RandomStringUtils.random(8), parent, child, true)
                .addAttribute("ui.style", "size: 1; arrow-shape: none;");
        waitForAnimation();
    }

    private void addNode(Path path) {
        Node n = graph.addNode(path.toString());
        n.addAttribute("ui.style", "size: 6; text-size: 6;");
        waitForAnimation(0);
    }

    private void removeNode(String id) {
        graph.removeNode(id);
        waitForAnimation();
    }

    //If you like to observe
    private void waitForAnimation(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForAnimation() {
        waitForAnimation(10);
    }

    private String getRandomColorStyle() {
        float hue = RandomUtils.nextFloat(0.0f, 1.0f);
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
        return String.format("fill-color: #%02x%02x%02x;", color.getRed(), color.getGreen(), color.getBlue());
    }
}
