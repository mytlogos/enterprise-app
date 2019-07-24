package com.mytlogos.enterprise;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SimpleTest {
    public static void main(String[] args) {
        try (ZipFile file = new ZipFile("C:\\Users\\Dominik\\32.epub")) {
            String markerFile = "content.opf";
            Enumeration<? extends ZipEntry> entries = file.entries();

            List<String> chapterFiles = new ArrayList<>();
            String folder = null;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".xhtml")) {
                    chapterFiles.add(entry.getName());
                }
                int index = entry.getName().indexOf(markerFile);
                if (index > 0) {
                    folder = entry.getName().substring(0, index);
                    System.out.println(folder);
                }
            }
            if (folder == null) {
                return;
            }
            Map<Integer, String> episodeMap = new HashMap<>();

            for (String chapterFile : chapterFiles) {
                if (!chapterFile.startsWith(folder)) {
                    continue;
                }

                try (InputStream inputStream = file.getInputStream(file.getEntry(chapterFile))) {
                    byte[] buffer = new byte[128];
                    String readInput = "";
                    Pattern pattern = Pattern.compile("<body id=\"(\\d+)\">");
                    int read = inputStream.read(buffer);

                    while (read != -1) {
                        readInput += new String(buffer);
                        Matcher matcher = pattern.matcher(readInput);

                        if (matcher.find()) {
                            String group = matcher.group(1);
                            int episodeId = Integer.parseInt(group);
                            episodeMap.put(episodeId, chapterFile);
                            break;
                        }
                        read = inputStream.read(buffer);
                    }
                }
                if (!episodeMap.values().contains(chapterFile)) {
                    System.out.println("no id found for " + chapterFile);
                }
            }

            System.out.println(episodeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
