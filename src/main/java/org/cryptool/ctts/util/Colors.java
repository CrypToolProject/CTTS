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

import javafx.scene.paint.Color;
import org.cryptool.ctts.CTTSApplication;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Colors, their transcription values, and ordering/sorting.
 */
public class Colors {
    private static final String SECOND_COPY = "_SECOND_COPY";
    private static ArrayList<String> all = colorSet();
    private final Map<String, String> colorStringToText = new TreeMap<>();
    private Map<String, Double> f;
    private Map<String, Integer> ordering = new HashMap<>();
    private boolean changed = false;

    public Colors() {
        ArrayList<String> list = colorSet();
        for (int pos = 0; pos < list.size(); pos++) {
            String rgb = list.get(pos);
            colorStringToText.put(rgb, "");
            ordering.put(rgb, pos);
        }
        Random r = new Random();
        for (int z = 0; z < 1000; z++) {
            int i = r.nextInt(list.size() - 1);
            int j = i + 1 + r.nextInt(list.size() - i - 1);
            Color c1 = Color.valueOf(list.get(i));
            Color c2 = Color.valueOf(list.get(j));
            swap(c1, c2);
        }

    }

    public static ArrayList<String> all() {
        return all;
    }

    public static ArrayList<String> colorSetOld() {
        ArrayList<String> colorArrayList = new ArrayList<>();
        for (int r : new int[]{0, 255 / 3, 255 / 2, 2 * 255 / 3, (255 + 2 * 255 / 3) / 2, 255}) {
            for (int g : new int[]{0, 255 / 3, 255 / 2, 2 * 255 / 3, (255 + 2 * 255 / 3) / 2, 255}) {
                for (int b : new int[]{0, 255 / 3, 255 / 2, 2 * 255 / 3, 255}) {
                    if (r + g + b == 0) {
                        continue;
                    }
                    Color rgb = Color.rgb(r, g, b);
                    colorArrayList.add(rgb.toString());
                }
            }
        }
        return colorArrayList;
    }

    public static ArrayList<String> colorSet() {
        ArrayList<String> colorArrayList = new ArrayList<>();
        int[] base = {0, 255 / 3, 255 / 2, 2 * 255 / 3, (255 + 2 * 255 / 3) / 2, 255};
        int[] bExtended1 = {(255 - 2 * 255 / 3) / 2};
        int[] bExtended2 = {17 * 3, 34 * 3, 51 * 3, 68 * 3, 81 * 3};
        for (int[] bArray : new int[][]{base, bExtended1, bExtended2}) {
            for (int r : base) {
                for (int g : base) {
                    for (int b : bArray) {
                        if (r + g + b == 0) {
                            continue;
                        }
                        Color rgb = Color.rgb(r, g, b);
                        colorArrayList.add(rgb.toString());
                    }
                }
            }
        }
        TreeSet<String> set = new TreeSet<>(colorArrayList);
        if (set.size() != colorArrayList.size()) {
            throw new RuntimeException("There are duplicate base colors " + set.size() + " " + colorArrayList.size());
        }
        return colorArrayList;
    }

    public static Colors restore(String backupFilename) {
        Colors state = fromTextFile(backupFilename);
        if (state != null) {
            System.out.printf("Symbol types read from %s.txt\n", backupFilename);
        }
        if (state == null) {
            state = fromTextFile(backupFilename + SECOND_COPY);
            if (state != null) {
                System.out.printf("Symbol types read from %s.txt\n", backupFilename + SECOND_COPY);
            }
        }
        if (state != null) {
            int assigned = 0;
            int withIcon = 0;
            all = colorSet();
            boolean defaultSorting = false;
            for (String rgb : all) {
                if (!state.colorStringToText.containsKey(rgb)) {
                    state.colorStringToText.put(rgb, "");
                }
                if (!state.colorStringToText.get(rgb).isEmpty()) {
                    assigned++;
                }
                if (Icons.readIcon(Color.valueOf(rgb), true)) {
                    withIcon++;
                }
                if (state.ordering == null || !state.ordering.containsKey(rgb)) {
                    defaultSorting = true;
                }
            }
            if (defaultSorting) {
                state.defaultSorting();
            }
            System.out.printf("%d valid symbol types (%d assigned, %d with icon)\n", state.colorStringToText.keySet().size(), assigned, withIcon);
        } else {
            System.out.println("Could not restore symbol types");
        }
        return state;
    }

    private static Colors fromTextFile(String backupFilename) {
        Colors state = new Colors();

        String s = FileUtils.readTextFile(null, backupFilename + ".txt");
        if (s == null) {
            return null;
        }
        for (String line : s.split("\n")) {
            String[] partsSemi = line.split(";");
            if (partsSemi.length == 3) {
                state.colorStringToText.put(partsSemi[0], partsSemi[1]);
                if (!partsSemi[2].isEmpty() && !partsSemi[2].equals("null")) {
                    state.ordering.put(partsSemi[0], Integer.parseInt(partsSemi[2]));
                }
                continue;
            }

            String[] parts = line.split("[ ]+");
            if (parts.length == 1) {
                state.colorStringToText.put(parts[0], "");
            } else {
                state.colorStringToText.put(parts[0], parts[1]);
            }
        }
        return state;
    }

    public boolean changed() {
        return changed;
    }

    public boolean swap(Color c1, Color c2) {
        if (c1 == null || c2 == null || c1.equals(c2)) {
            return false;
        }
        int o1 = ordering.get(c1.toString());
        int o2 = ordering.get(c2.toString());
        ordering.put(c1.toString(), o2);
        ordering.put(c2.toString(), o1);
        changed = true;
        return true;
    }

    public boolean insert(Color from, Color to) {
        if (from == null || to == null || from.equals(to)) {
            return false;
        }
        int fromPosition = ordering.get(from.toString());
        int toPosition = ordering.get(to.toString());
        ArrayList<String> sorted = sortedColors();
        if (toPosition < fromPosition) {
            for (int p = toPosition; p < fromPosition; p++) {
                String c = sorted.get(p);
                ordering.put(c, p + 1);
            }
            ordering.put(from.toString(), toPosition);
        } else {
            for (int p = fromPosition + 1; p < toPosition; p++) {
                String c = sorted.get(p);
                ordering.put(c, p - 1);
            }
            ordering.put(from.toString(), toPosition - 1);
        }
        changed = true;
        return true;
    }

    public int size() {
        return all.size();
    }

    public ArrayList<String> sortedColors() {
        ArrayList<String> usedColors = new ArrayList<>(colorStringToText.keySet());
        usedColors.sort(Comparator.comparingInt(o -> ordering.getOrDefault(o, 0)));
        return usedColors;
    }

    public void defaultSorting() {
        f = TranscribedImage.rawFreq();
        ArrayList<String> sorted = new ArrayList<>(colorStringToText.keySet());
        sorted.sort(this::defaultComparator);
        if (ordering == null) {
            ordering = new TreeMap<>();
        }
        ordering.clear();
        for (int i = 0; i < sorted.size(); i++) {
            ordering.put(sorted.get(i), i);
        }
        changed = true;
    }

    public void sortByFrequency() {
        f = TranscribedImage.rawFreq();
        ArrayList<String> sorted = new ArrayList<>(colorStringToText.keySet());
        sorted.sort((o1, o2) -> {
            int freqCompare = Double.compare(f.getOrDefault(o2, 0.0), f.getOrDefault(o1, 0.0));
            if (freqCompare != 0) {
                return freqCompare;
            }
            return defaultComparator(o1, o2);
        });
        ordering.clear();
        for (int i = 0; i < sorted.size(); i++) {
            ordering.put(sorted.get(i), i);
        }
        changed = true;
    }

    public void sortByDecryption() {
        if (!CTTSApplication.key.isKeyAvailable()) {
            defaultSorting();
            return;
        }
        f = TranscribedImage.rawFreq();
        ArrayList<String> sorted = new ArrayList<>(colorStringToText.keySet());
        sorted.sort(this::compareByDecryption);
        ordering.clear();
        for (int i = 0; i < sorted.size(); i++) {
            ordering.put(sorted.get(i), i);
        }
        changed = true;
    }

    private int compareByDecryption(String o1, String o2) {
        String t1 = colorStringToText.get(o1);
        String t2 = colorStringToText.get(o2);
        String d1 = CTTSApplication.key.fromTranscription(t1);
        String d2 = CTTSApplication.key.fromTranscription(t2);
        if (d1 == null && d2 == null) {
            return defaultComparator(o1, o2);
        }
        if (d1 == null) {
            return 1;
        }
        if (d2 == null) {
            return -1;
        }
        int l1 = d1.length();
        int l2 = d2.length();
        if (l1 == 0 && l2 == 0) {
            return defaultComparator(o1, o2);
        }
        if (l1 == 0) {
            return 1;
        }
        if (l2 == 0) {
            return -1;
        }
        int singleLetter1 = Utils.singleLetterIndex(d1);
        int singleLetter2 = Utils.singleLetterIndex(d2);
        if (singleLetter1 != -1 && singleLetter2 == -1) {
            return -1;
        }
        if (singleLetter1 == -1 && singleLetter2 != -1) {
            return 1;
        }
        if (singleLetter1 != -1 && singleLetter2 != -1) {
            int integerCompare = Integer.compare(singleLetter1, singleLetter2);
            if (integerCompare != 0) {
                return integerCompare;
            }
        }
        int upper = d1.toUpperCase().compareTo(d2.toUpperCase());
        if (upper != 0) {
            return upper;
        }
        int normal = d1.compareTo(d2);
        if (normal != 0) {
            return normal;
        }

        int freqCompare = Double.compare(f.getOrDefault(o2, 0.0), f.getOrDefault(o1, 0.0));
        if (freqCompare != 0) {
            return freqCompare;
        }

        return t1.compareTo(t2);
    }

    public Set<String> keySet() {
        return colorStringToText.keySet();
    }

    public String getOrDefault(String colorString, String s) {
        return colorStringToText.getOrDefault(colorString, s);
    }

    public Collection<String> values() {
        return colorStringToText.values();
    }

    public void put(String colorString, String c) {
        colorStringToText.put(colorString, c);
        changed = true;
    }

    public boolean available() {
        return colorStringToText != null;
    }

    public void markAsChanged() {
        changed = true;
    }

    public int defaultComparator(String o1, String o2) {
        String text1 = colorStringToText.get(o1);
        String text2 = colorStringToText.get(o2);
        Color color1 = Color.valueOf(o1);
        Color color2 = Color.valueOf(o2);
        CompareFields c1 = CompareFields.get(text1);
        CompareFields c2 = CompareFields.get(text2);
        int mainTypesCompare = c1.mainType.compare(c2.mainType);
        if (mainTypesCompare != 0) {
            return mainTypesCompare;
        }
        int mainValues = 0;
        switch (c1.mainType) {
            case EMPTY:
                break;
            case NUMBER:
                mainValues = Integer.compare(c1.mainNumber, c2.mainNumber);
                break;
            case LETTERS:
            case SYMBOLS:
                mainValues = c1.mainString.compareTo(c2.mainString);
                break;
        }
        if (mainValues != 0) {
            return mainValues;
        }
        int serialTypesCompare = c1.serialType.compare(c2.serialType);
        if (serialTypesCompare != 0) {
            return serialTypesCompare;
        }
        int serialValuesCompare = 0;
        switch (c1.serialType) {
            case EMPTY:
                break;
            case NUMBER:
                serialValuesCompare = Integer.compare(c1.serialNumber, c2.serialNumber);
                break;
            case LETTERS:
            case SYMBOLS:
                serialValuesCompare = c1.serialString.compareTo(c2.serialString);
                break;
        }
        if (serialValuesCompare != 0) {
            return serialValuesCompare;
        }
        int freqCompare = Double.compare(f.getOrDefault(o2, 0.0), f.getOrDefault(o1, 0.0));
        if (freqCompare != 0) {
            return freqCompare;
        }

        return color1.toString().compareTo(color2.toString());
    }

    public Color get(int index) {
        return Color.valueOf(all.get(index));
    }

    public int indexOf(Color rgb) {
        return all.indexOf(rgb.toString());
    }

    public Color valueOf(String colorString) {
        if (!colorStringToText.containsKey(colorString)) {
            return null;
        }
        try {
            return Color.valueOf(colorString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String get(String colorString) {
        return colorStringToText.get(colorString);
    }

    public boolean contains(String colorString) {
        return colorStringToText.containsKey(colorString);
    }

    public void save(String backupFilename) {

        if (changed) {
            saveTextFile(backupFilename + ".txt");
            saveTextFile(backupFilename + SECOND_COPY + ".txt");
            changed = false;
        }
    }

    public void saveTextFile(String outFilename) {
        StringBuilder text = new StringBuilder();
        for (String colorString : colorSet()) {
            String transcriptionValue = get(colorString);
            text.append(colorString).append(';').append(transcriptionValue).append(';').append(ordering.get(colorString)).append('\n');
        }
        FileUtils.writeTextFile(null, outFilename, text.toString());
    }

    static class CompareFields {
        int mainNumber = -1;
        String mainString = "";
        Type mainType = Type.EMPTY;
        int serialNumber = -1;
        String serialString = "";
        Type serialType = Type.EMPTY;
        String keepS;

        static CompareFields get(String s) {
            CompareFields c = new CompareFields();
            c.keepS = s;
            if (s.isEmpty()) {
                return c;
            }
            Pattern p;
            Matcher m;
            // 02
            p = Pattern.compile("^([0-9]+)$");
            m = p.matcher(s);
            if (m.find()) {
                c.mainString = m.group(1);
                c.mainNumber = Integer.parseInt(c.mainString);
                c.mainType = Type.NUMBER;
                return c;
            }
            // Ba
            p = Pattern.compile("^([a-zA-Z]+)$");
            m = p.matcher(s);
            if (m.find()) {
                c.mainString = m.group(1);
                c.mainType = Type.LETTERS;
                return c;
            }
            // 03a
            p = Pattern.compile("^([0-9]+)(.+)$");
            m = p.matcher(s);
            if (m.find()) {
                c.mainString = m.group(1);
                c.mainNumber = Integer.parseInt(c.mainString);
                c.mainType = Type.NUMBER;
                c.serialString = m.group(2);
                c.serialType = c.serialString.matches("[a-zA-Z]+.*") ? Type.LETTERS : Type.SYMBOLS;
                return c;
            }
            // Ba23
            p = Pattern.compile("^([a-zA-Z]+)([0-9]+)$");
            m = p.matcher(s);
            if (m.find()) {
                c.mainString = m.group(1);
                c.mainType = Type.LETTERS;
                c.serialString = m.group(2);
                c.serialNumber = Integer.parseInt(c.serialString);
                c.serialType = Type.NUMBER;
                return c;
            }
            // Ba23
            p = Pattern.compile("^([a-zA-Z]+)(.+)$");
            m = p.matcher(s);
            if (m.find()) {
                // get the two groups we were looking for
                c.mainString = m.group(1);
                c.mainType = Type.LETTERS;
                c.serialString = m.group(2);
                c.serialType = Type.SYMBOLS;
                return c;
            }
            // _23
            p = Pattern.compile("^(.*)([0-9]+)$");
            m = p.matcher(s);
            if (m.find()) {
                // get the two groups we were looking for
                c.mainString = m.group(1);
                c.mainType = Type.SYMBOLS;
                c.serialString = m.group(2);
                c.serialNumber = Integer.parseInt(c.serialString);
                c.serialType = Type.NUMBER;
                return c;
            }
            // _a
            p = Pattern.compile("^(.*)([a-zA-Z]+)$");
            m = p.matcher(s);
            if (m.find()) {
                // get the two groups we were looking for
                c.mainString = m.group(1);
                c.mainType = Type.SYMBOLS;
                c.serialString = m.group(2);
                c.serialType = Type.LETTERS;
                return c;
            }
            c.mainString = s;
            c.mainType = Type.SYMBOLS;
            return c;
        }

        void print() {
            System.out.printf("%-10s Main: %-10s %-10s %5d Serial: %-10s %-10s %5d\n", keepS, mainType, mainString, mainNumber, serialType, serialString, serialNumber);
        }

        enum Type {
            NUMBER, LETTERS, SYMBOLS, EMPTY;

            int compare(Type t2) {
                if (t2 == this) {
                    return 0;
                }
                if (this == EMPTY) {
                    return 1;
                }
                if (t2 == EMPTY) {
                    return -1;
                }
                if (this == SYMBOLS) {
                    return 1;
                }
                if (t2 == SYMBOLS) {
                    return -1;
                }
                if (this == LETTERS) {
                    return 1;
                }
                if (t2 == LETTERS) {
                    return -1;
                }
                throw new RuntimeException("Cannot happen " + this + " vs " + t2);
            }
        }
    }
}
