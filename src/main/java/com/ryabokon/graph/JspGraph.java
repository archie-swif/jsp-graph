package com.ryabokon.graph;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JspGraph {

    Pattern pattern = Pattern.compile("\\w+\\.jsp");
    private MultiGraph graph = new MultiGraph("JSP");
    private List<Node> nodes = new ArrayList<>();

    public JspGraph() throws InterruptedException, IOException {

        setUpDisplay();
        createNodes();
        addLinks();

    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new JspGraph();
    }

    private void setUpDisplay() {

        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        Viewer view = graph.display(false);
        Layout layout = new SpringBox();
        layout.setQuality(0); //set to 0 if it lags
        layout.setStabilizationLimit(1.0);
        //layout.setForce(0.5);
        view.enableAutoLayout(layout);
    }

    private void createNodes() throws InterruptedException, IOException {
        Files.walk(Paths.get("/Users/admin/Git/ncgit/gb/src/main/webapp/WEB-INF/jsp"), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    Node n = graph.addNode(path.getFileName().toString());
                    n.addAttribute("ui.style", "size: 3px;");
                    //n.addAttribute("ui.label", path.getFileName().toString());
                });
    }

    public void addLinks() throws IOException {
        Files.walk(Paths.get("/Users/admin/Git/ncgit/gb/src/main/webapp/WEB-INF/jsp"), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> !path.getFileName().toString().equals("content.jsp"))
                .forEach(path -> {
                    try {

                        String color = getRandomColorStyle();
                        //Node parent = graph.getNode(path.getFileName().toString());

                        Files.lines(path)
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
                                .filter(s -> !s.equals("tagLibs.jsp"))
                                .filter(s -> !s.equals("localization.jsp"))
                                .filter(s -> !s.equals("locale.jsp"))
                                .filter(s -> !s.equals("rowStyle.jsp"))
                                .forEach(jspName ->
                                {
                                    Node child = graph.getNode(jspName);
                                    if (child == null) {
                                        child = graph.addNode(jspName);
                                        child.addAttribute("ui.style", "size: 3px;");
                                    }
                                    child.addAttribute("ui.style", color);
                                    //child.addAttribute("ui.label", path.getFileName().toString());
                                    graph.addEdge(RandomStringUtils.random(8), path.getFileName().toString(), jspName);
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }



                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


    }

    private String getRandomColorStyle() {
        float hue = RandomUtils.nextFloat(0.0f, 1.0f);
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
        return String.format("size: 5px; fill-color: #%02x%02x%02x;", color.getRed(), color.getGreen(), color.getBlue());
    }

    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        return res != 0 ? res : 1; // Special fix to preserve items with equal values
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }


}
