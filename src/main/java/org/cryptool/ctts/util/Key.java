/*
    Copyright 2023 George Lasry & CrypTool 2 Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cryptool.ctts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cryptool.ctts.CTTSApplication;

public class Key {
    private final Map<String, String> key = new TreeMap<>();
    private final ArrayList<String> metadata = new ArrayList<>();
    private String keyFilename = null;
    private boolean changed = false;
    private boolean available = false;

    public String getKeyFilename() {
        return keyFilename;
    }

    public boolean isKeyAvailable() {
        return available;
    }

    public boolean fromTranscriptionAvailable(String transcription) {
        if (!isKeyAvailable()) {
            return false;
        }

        return key.containsKey(transcription);

    }

    public String fromTranscriptionOrDefault(String transcription, String defaultValue) {
        if (!isKeyAvailable()) {
            return null;
        }
        return key.getOrDefault(transcription, defaultValue);
    }

    public String fromTranscription(String transcription) {
        if (!isKeyAvailable()) {
            return null;
        }
        return key.get(transcription);
    }

    public boolean fromColorStringAvailable(String colorString) {
        if (!isKeyAvailable()) {
            return false;
        }
        String transcription = CTTSApplication.colors.get(colorString);
        if (transcription == null) {
            return false;
        }
        return key.containsKey(transcription);

    }

    public String fromColorString(String colorString) {
        if (!isKeyAvailable()) {
            return null;
        }
        String transcription = CTTSApplication.colors.get(colorString);
        if (transcription == null) {
            return null;
        }
        return key.get(transcription);
    }

    public void covered() {
        if (isKeyAvailable()) {
            Set<String> covered = new TreeSet<>(CTTSApplication.colors.values());
            for (String c : key.keySet()) {
                if (!covered.contains(c)) {
                    System.out.printf("Key mapping not used: %s to %s\n", c, key.get(c));
                }
            }
        }

    }

    public static Key readFromFile(String filename) {
        String keyS = FileUtils.readTextFile(null, filename);
        Key key = new Key();
        key.keyFilename = filename;
        if (keyS == null) {
            System.out.printf("Key file not found: %s\n", filename);
            System.exit(1);
        }

        key.parse(filename, new StringBuilder(keyS));
        System.out.printf("Read %s - %d homophones\n", key.keyFilename, key.keySet().size());
        return key;
    }

    public void parse(String legend, StringBuilder keyS) {

        String startsString = "#KEY\n" +
                "#CATALOG\n" +
                "#IMAGE\n" +
                "#LANGUAGE\n" +
                "#TRANSCRIBER\n" +
                "#DATE\n" +
                "#TRANSCRIPTION\n" +
                "#STATUS\n" +
                "#ORIGIN\n" +
                "Score:\n" +
                "<COMMENT\n" +
                "#<\n" +
                "<NOTE";
        key.clear();

        String[] starts = startsString.split("\n");

        for (String line : keyS.toString().split("[\r\n]+")) {
            // System.out.println(line);
            if (line.isEmpty()) {
                continue;
            }
            boolean ignore = false;
            for (String start : starts) {
                if (line.startsWith(start)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                if (metadata != null) {
                    metadata.add(line);
                }
                continue;
            }
            final String[] lineParts = line.split("[ ]+-[ ]+");
            if (lineParts.length != 2) {
                System.out.printf("Invalid key entry - %s:\n%s\n", legend, line);
                System.exit(1);
            }
            String homophonesPart = lineParts[0];
            String plain = lineParts[1];
            final String[] homophones = homophonesPart.split("\\|");
            if (homophones.length == 0) {
                System.out.printf("Invalid key entry -  %s:\n%s\n", legend, line);
                System.exit(1);
            }
            // System.out.println(homophones.length);
            for (String h : homophones) {
                // System.out.printf("%-10s => %s\n", h, plain);
                key.put(h, plain);
            }
        }
        available = true;
    }

    public void replace(Key newKey, String metadata) {
        key.clear();
        key.putAll(newKey.key);
        this.metadata.clear();
        this.metadata.add(metadata);
        if (keyFilename == null || keyFilename.isEmpty()) {
            keyFilename = "key.txt";
        }
        available = true;
        markAsChanged();
    }

    public void markAsChanged() {
        changed = true;
    }

    public boolean changed() {
        return changed;
    }

    public void saveKey() {

        if (keyFilename == null || !changed) {
            return;
        }

        StringBuilder f = toStringBuilder();

        FileUtils.writeTextFile(null, keyFilename, f.toString());

        changed = false;
    }

    public StringBuilder toStringBuilder() {
        Map<String, String> nomenclature = new HashMap<>();

        Map<String, String> homophones = new TreeMap<>();
        for (String c : key.keySet()) {
            String p = key.get(c);
            if (CTTSApplication.colors.values().contains(c)) {
                String h = homophones.getOrDefault(p, "");
                if (!h.isEmpty()) {
                    h += "|";
                }
                h += c;
                homophones.put(p, h);
            } else {
                if (c.matches("\\[[0-9]+:[0-9]+]")) {
                    nomenclature.put(c, p);
                } else {
                    System.out.printf("No color has transcription value: %s (mapped to decryption: %s)\n", c, p);
                }
            }
        }
        StringBuilder f = new StringBuilder();
        for (String line : metadata) {
            f.append(line).append("\n");
        }

        for (String p : homophones.keySet()) {

            final String h = homophones.get(p);
            if (!h.startsWith("_")) {
                f.append(h).append(" - ").append(p).append("\n");
            }
        }
        for (String p : homophones.keySet()) {

            final String h = homophones.get(p);
            if (h.startsWith("_")) {
                f.append(h).append(" - ").append(p).append("\n");
            }
        }

        ArrayList<String> sorted = new ArrayList<>(nomenclature.keySet());
        sorted.sort((o1, o2) -> {
            int a1 = Integer.parseInt(o1.substring(1, o1.indexOf(":")));
            int a2 = Integer.parseInt(o2.substring(1, o2.indexOf(":")));
            int b1 = Integer.parseInt(o1.substring(o1.indexOf(":") + 1, o1.length() - 1));
            int b2 = Integer.parseInt(o2.substring(o2.indexOf(":") + 1, o2.length() - 1));
            return b1 * 1000 + a1 - (b2 * 1000 + a2);
        });
        for (String c : sorted) {
            f.append(c).append(" - ").append(nomenclature.get(c)).append("\n");
        }
        System.out.printf("Saved %s - %d homophones %d plaintext elements\n", keyFilename, key.size(),
                homophones.size());
        return f;
    }

    public static boolean lockedC(String c) {
        return c.toLowerCase().startsWith("_");
    }

    public boolean lockedHomophoneP(String c) {
        if (lockedC(c)) {
            return false;
        }
        String p = fromTranscription(c);

        if (p == null || p.isEmpty()) {
            return false;
        }

        char first = p.charAt(0);

        boolean reservedP = p.startsWith("_") || p.startsWith("[");

        if (!reservedP && p.length() == 1 && first >= 'A' && first <= 'Z') {
            return true;
        }
        return false;
    }

    public boolean lockedOtherP(String c) {
        if (lockedC(c)) {
            return false;
        }
        String p = fromTranscription(c);

        if (p == null || p.isEmpty()) {
            return false;
        }

        char first = p.charAt(0);

        boolean reservedP = p.startsWith("_") || p.startsWith("[");

        if (reservedP || (p.length() > 1 && first >= 'A' && first <= 'Z')) {
            return true;
        }
        return false;
    }

    public boolean isDelete(String c) {

        String p = fromTranscription(c);

        if (p == null || p.isEmpty()) {
            return false;
        }

        return p.equals("[-]");

    }

    public boolean isRepeat(String c) {

        String p = fromTranscription(c);

        if (p == null || p.isEmpty()) {
            return false;
        }

        return p.equals("[x2]");

    }

    public boolean lockedP(String c) {
        if (lockedC(c)) {
            return false;
        }
        return lockedHomophoneP(c) | lockedOtherP(c);

    }

    public String get(String c) {
        return key.get(c);
    }

    public Set<String> keySet() {
        return key.keySet();
    }

    public void put(String c, String newText) {
        key.put(c, newText);
        markAsChanged();
    }

    public void remove(String c) {
        key.remove(c);
        markAsChanged();
    }

    public Collection<String> values() {
        return key.values();
    }
}