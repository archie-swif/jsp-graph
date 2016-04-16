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
import java.util.ArrayList;
import java.util.List;

public class JspGraph {

    private final int TOTAL_NODES = 512;
    private final int GROUPS = 8;
    private final int GROUP_SIZE = TOTAL_NODES / GROUPS;

    private MultiGraph graph = new MultiGraph("JSP");
    private List<Node> nodes = new ArrayList<>();

    public JspGraph() throws InterruptedException {
        setUpDisplay();
        createNodes();
        linkNodes();
    }

    private void setUpDisplay() {
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        Viewer view = graph.display(false);
        Layout layout = new SpringBox();
        layout.setQuality(2); //set to 0 if it lags
        layout.setStabilizationLimit(1.0);
        layout.setForce(0.5);
        view.enableAutoLayout(layout);
    }

    private void createNodes() throws InterruptedException {
        for (int i = 0; i < TOTAL_NODES; i++) {
            String nodeName = RandomWord.getWord()+".jsp";
            Node node = graph.addNode(nodeName);
            node.addAttribute("ui.label", nodeName);
            node.addAttribute("ui.size", 7);
            nodes.add(node);
        }
    }

    private void linkNodes() throws InterruptedException {
        for (int i = 0; i < GROUPS; i++) {
            String color = getRandomColorStyle();
            //Set # of links inside of a group
            for (int j = 0; j < GROUP_SIZE; j++) {
                Thread.sleep(20);
                Node nodeOne = nodes.get(RandomUtils.nextInt(GROUP_SIZE * i, GROUP_SIZE * (i + 1)));
                Node nodeTwo = nodes.get(RandomUtils.nextInt(GROUP_SIZE * i, GROUP_SIZE * (i + 1)));
                nodeOne.addAttribute("ui.style", color);
                nodeTwo.addAttribute("ui.style", color);
                graph.addEdge(RandomStringUtils.random(4), nodeOne, nodeTwo);
            }
        }

        //Set link between groups
        for (int j = 0; j < 24; j++) {
            Thread.sleep(20);
            Node nodeOne = nodes.get(RandomUtils.nextInt(0, TOTAL_NODES));
            Node nodeTwo = nodes.get(RandomUtils.nextInt(0, TOTAL_NODES));
            graph.addEdge(RandomStringUtils.random(4), nodeOne, nodeTwo);
        }
    }

    private String getRandomColorStyle() {
        float hue = RandomUtils.nextFloat(0.0f, 1.0f);
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
        return String.format("fill-color: #%02x%02x%02x;", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void main(String[] args) throws  InterruptedException {
        new JspGraph();
    }


}
