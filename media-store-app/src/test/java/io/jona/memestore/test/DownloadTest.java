package io.jona.memestore.test;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DownloadTest {
    private static final Logger logger = LoggerFactory.getLogger(DownloadTest.class);
    public static void main(String[] args) {
        Path memesBeforeParsing = Paths.get("C:\\Users\\barri\\IdeaProjects\\padawan-learner\\meme-store\\src\\main\\resources\\memesToShare.txt");
        Path memesAfterParsing = Paths.get("C:\\Users\\barri\\IdeaProjects\\padawan-learner\\meme-store\\src\\main\\resources\\newMemesToShare.txt");
        try(BufferedReader reader = Files.newBufferedReader(memesBeforeParsing);
                BufferedWriter writer = Files.newBufferedWriter(memesAfterParsing, StandardOpenOption.TRUNCATE_EXISTING)) {
            String currentLine = null;
            while((currentLine = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("https?://.\\S*");
                Matcher matcher = pattern.matcher(currentLine);
                if (matcher.find()) {
                    System.out.println(matcher.group());
                    writer.write(matcher.group());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Got exception");
        }
    }

    public static class TagsController {

    }
}