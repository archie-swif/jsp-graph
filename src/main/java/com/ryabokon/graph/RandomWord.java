package com.ryabokon.graph;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomWord {

    private static List<String> words;

    public static String getWord() {
        if (words == null || words.isEmpty()) {
            try (Stream<String> stream = Files.lines(Paths.get(RandomWord.class.getClassLoader().getResource("shuffled.txt").toURI()))) {
                words = stream.collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return words.remove(0);
    }


}
