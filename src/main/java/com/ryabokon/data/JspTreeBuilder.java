package com.ryabokon.data;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class JspTreeBuilder {

    public void findNodes() throws IOException {

        Set<String> nodes = new HashSet<>();


        Pattern pattern = Pattern.compile("([\\-/a-zA-Z])*\\w+\\.jsp");

        Files.walk(Paths.get("/Users/admin/Git/ncgit/gb/src/main/webapp/WEB-INF/jsp"), FileVisitOption.FOLLOW_LINKS)
                .parallel()
                .filter(path -> !Files.isDirectory(path))
                .peek(path -> nodes.add(path.getFileName().toString()))
                .flatMap(path -> {
                    try {
                        return Files.lines(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(s -> s.contains(".jsp\""))
                //.filter(s -> s.contains(".."))
                //.map(s -> pattern.matcher(s).group(0))
                .peek(System.out::println);

        System.out.println(nodes);
    }

    public static void main(String[] args) throws IOException {
        JspTreeBuilder tb = new JspTreeBuilder();
        tb.findNodes();
    }
}
