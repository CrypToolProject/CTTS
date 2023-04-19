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

package org.cryptool.ota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TranscribedImage {
    public static TranscribedImage[] transcribedImages;
    public static int currentImageIndex = 0;

    public Image image;
    public Image negative;
    ArrayList<Rectangle> positions;
    public String filename;

    double scaleValue = 1.0;
    double vValue = 0.0;
    double hValue = 0.0;

    double detailedScaleValue = 0.6;
    double detailedvValue = 0.0;
    double detailedhValue = 0.0;

    boolean changed = false;

    static Map<String, ArrayList<Rectangle>> symbolTypesCache = null;

    TranscribedImage(String filename) {
        this.filename = filename;
    }

    static TranscribedImage[] extractFromArgs(String[] args) {
        int count = 0;
        for (String arg : args) {
            if (ImageUtils.isSupportedFormat(arg)) {
                count++;
            }
        }
        TranscribedImage[] transcribedImages = new TranscribedImage[count];
        count = 0;
        for (String arg : args) {
            if (ImageUtils.isSupportedFormat(arg)) {
                transcribedImages[count] = new TranscribedImage(arg);
                transcribedImages[count].image = FileUtils.readImage(null, arg, false);
                if (transcribedImages[count].image == null) {
                    System.exit(-1);
                }
                String negativeFilename = arg.substring(0, arg.lastIndexOf(".")) + "_negative"
                        + arg.substring(arg.lastIndexOf("."));
                transcribedImages[count].negative = FileUtils.readImage(null, negativeFilename, true);
                if (transcribedImages[count].negative == null) {
                    WritableImage negative = ImageUtils.negative(transcribedImages[count].image);
                    transcribedImages[count].negative = negative;
                    FileUtils.writeImage(null, negativeFilename, negative);
                }

                transcribedImages[count].positions = Positions.restore(arg);
                if (transcribedImages[count].positions.isEmpty()) {
                    System.out.printf("Could not restore symbols for: %s\n", arg);
                }

                count++;
            }
        }
        return transcribedImages;
    }

    public static int size() {
        return transcribedImages.length;
    }

    public static TranscribedImage current() {
        return transcribedImages[currentImageIndex];
    }

    static TranscribedImage image(int imageIndex) {
        return transcribedImages[imageIndex];
    }

    static StringBuilder transcriptionTextFormat(int index) {
        StringBuilder lines = new StringBuilder();

        lines.append(String.format("# %s\n", transcribedImages[index].filename));
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            for (Rectangle r : lineOfSymbols) {
                lines.append(OTAApplication.colors.get(r.getFill().toString())).append(";");
            }
            lines.append("\n");
        }
        lines.append("\n");
        return lines;
    }

    static StringBuilder decryptionTextFormat(int index) {
        StringBuilder lines = new StringBuilder();

        lines.append(String.format("# %s\n", transcribedImages[index].filename));
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            for (String d : DetailedTranscriptionPane.decryptionSequence(lineOfSymbols)) {
                lines.append(d.toLowerCase(Locale.ROOT)).append(' ');
            }
            lines.append("\n");

        }
        lines.append("\n");
        return lines;
    }

    static void saveTranscriptionsDecryptionsPositions() {
        saveTranscriptions();
        if (OTAApplication.key.isKeyAvailable()) {
            saveDecryptions();
        }
        for (int i = 0; i < size(); i++) {
            if (TranscribedImage.image(i).changed) {
                Positions.save(i);
                TranscribedImage.image(i).changed = false;
            }
        }
    }

    public static boolean change() {
        for (int i = 0; i < size(); i++) {
            if (TranscribedImage.image(i).changed) {
                return true;
            }
        }
        return false;
    }

    private static void saveTranscriptions() {

        StringBuilder all = new StringBuilder();

        for (int index = 0; index < size(); index++) {

            StringBuilder lines = transcriptionTextFormat(index);
            FileUtils.writeTextFile("transcription", transcribedImages[index].filename, lines.toString());

            all.append(lines);
        }

        FileUtils.writeTextFile("transcription", "all", all.toString());

    }

    private static void saveDecryptions() {

        StringBuilder all = new StringBuilder();

        for (int index = 0; index < size(); index++) {

            StringBuilder lines = decryptionTextFormat(index);
            FileUtils.writeTextFile("decryption", transcribedImages[index].filename, lines.toString());

            all.append(lines);

        }

        FileUtils.writeTextFile("decryption", "all", all.toString());

        OTAApplication.key.covered();

    }

    static Map<String, Double> rawFreq() {

        Map<String, Double> freq = new HashMap<>();
        int total = 0;

        for (TranscribedImage transcribedImage : transcribedImages) {

            total += transcribedImage.positions.size();
            for (Rectangle r : transcribedImage.positions) {
                final String key = r.getFill().toString();
                freq.put(key, freq.getOrDefault(key, 0.0) + 1);
            }
        }

        int finalTotal = total;
        freq.replaceAll((n, v) -> freq.get(n) / finalTotal);
        return freq;
    }

    static Map<String, Double> freq(String start) {

        Map<String, Double> freq = new HashMap<>();
        int total = 0;

        for (TranscribedImage transcribedImage : transcribedImages) {
            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            if (start.contains(filename.substring(0, endStartIndex))) {
                total += transcribedImage.positions.size();
                for (Rectangle r : transcribedImage.positions) {
                    final String key = r.getFill().toString();
                    String name = OTAApplication.colors.get(key);
                    freq.put(name, freq.getOrDefault(name, 0.0) + 1);
                }
            }
        }

        int finalTotal = total;
        freq.replaceAll((n, v) -> freq.get(n) / finalTotal);
        return freq;
    }

    static void consistencyCheck(String start) {

        final String[] criteria = { "01|85", "13|65", "02|72", "20|41", "53|108", "22|131", "18|84" };

        for (int idx = 0; idx < size(); idx++) {

            TranscribedImage transcribedImage = transcribedImages[idx];
            ;
            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            if (start.contains(filename.substring(0, endStartIndex))) {
                System.out.println(filename);
                for (ArrayList<Rectangle> line : Alignment.linesOfSymbols(idx)) {
                    Map<String, Integer> counts = new TreeMap<>();
                    for (Rectangle r : line) {
                        final String key = r.getFill().toString();
                        String name = OTAApplication.colors.get(key);
                        counts.put(name, counts.getOrDefault(name, 0) + 1);
                    }

                    for (String c : criteria) {
                        String k1 = c.split("\\|")[0];
                        int c1 = counts.getOrDefault(k1, 0);
                        String k2 = c.split("\\|")[1];
                        int c2 = counts.getOrDefault(k2, 0);

                        if (c1 == 0 && c2 == 0) {
                            System.out.print("[   ] ");
                        } else if (c1 > c2) {
                            System.out.printf("[%3s] ", k1);
                        } else {
                            System.out.printf("[%3s] ", k2);
                        }

                    }
                    System.out.println();
                }
            }
        }

    }

    static double averageSymbolsPerLine(String start) {

        int total = 0;
        int totalLines = 0;
        for (int idx = 0; idx < TranscribedImage.size(); idx++) {
            TranscribedImage transcribedImage = TranscribedImage.image(idx);

            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            if (start.contains(filename.substring(0, endStartIndex))) {

                for (ArrayList<Rectangle> line : Alignment.linesOfSymbols(idx)) {
                    if (line.size() > 60) {
                        totalLines++;
                        total += line.size();
                    }
                }
            }
        }

        return 1.0 * total / totalLines;
    }

    static int totalSymbols(String start) {

        int total = 0;
        for (int idx = 0; idx < TranscribedImage.size(); idx++) {
            TranscribedImage transcribedImage = TranscribedImage.image(idx);

            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            if (start.contains(filename.substring(0, endStartIndex))) {

                for (ArrayList<Rectangle> line : Alignment.linesOfSymbols(idx)) {
                    total += line.size();
                }
            }
        }

        return total;
    }

    static int totalLines(String start) {

        int total = 0;
        for (int idx = 0; idx < TranscribedImage.size(); idx++) {
            TranscribedImage transcribedImage = TranscribedImage.image(idx);

            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            if (start.contains(filename.substring(0, endStartIndex))) {
                total += Alignment.linesOfSymbols(idx).size();
            }
        }

        return total;
    }

    public static Rectangle nextPosition(int index, Rectangle selected) {
        ArrayList<Rectangle> nodes = Alignment.sortedPositions(index);
        int selectedPos = nodes.indexOf(selected);
        Rectangle next = null;
        if (nodes.size() > 1) {
            if (selectedPos == -1) {
                next = nodes.get(0);
            } else {
                next = nodes.get((selectedPos + 1) % nodes.size());
            }
        }
        return next;
    }

    public static Rectangle previousPosition(int index, Rectangle selected) {
        ArrayList<Rectangle> nodes = Alignment.sortedPositions(index);
        int selectedPos = nodes.indexOf(selected);
        Rectangle next = null;
        if (nodes.size() > 1) {
            if (selectedPos == -1) {
                next = nodes.get(0);
            } else {
                next = nodes.get((selectedPos - 1 + nodes.size()) % nodes.size());
            }
        }
        return next;
    }

    public static int totalSymbols() {
        int total = 0;
        for (TranscribedImage transcribedImage : transcribedImages) {
            total += transcribedImage.positions.size();
        }
        return total;
    }

    public static Rectangle idToRectangle(String id) {
        String[] values = id.split(":");
        if (values.length != 5) {
            return null;
        }
        int idx = Integer.parseInt(values[0]);
        double x = Double.parseDouble(values[1]);
        double y = Double.parseDouble(values[2]);
        double w = Double.parseDouble(values[3]);
        double h = Double.parseDouble(values[4]);

        for (Rectangle r : transcribedImages[idx].positions) {
            if (r.getLayoutX() == x && r.getLayoutY() == y && r.getWidth() == w && r.getHeight() == h) {
                return r;
            }
        }
        return null;
    }

    public static int rectangleToIndex(Rectangle nr) {

        double x = nr.getLayoutX();
        double y = nr.getLayoutY();
        double w = nr.getWidth();
        double h = nr.getHeight();

        for (int idx = 0; idx < transcribedImages.length; idx++) {
            for (Rectangle r : transcribedImages[idx].positions) {
                if (r.getLayoutX() == x && r.getLayoutY() == y && r.getWidth() == w && r.getHeight() == h) {
                    return idx;
                }
            }
        }
        return -1;
    }

    public static int idToIndex(String id) {
        String[] values = id.split(":");
        if (values.length != 5) {
            return -1;
        }

        return Integer.parseInt(values[0]);
    }

    private static void buildSymbolTypesCacheIfNeeded() {
        if (symbolTypesCache != null) {
            return;
        }
        symbolTypesCache = new HashMap<>();
        for (int index = 0; index < transcribedImages.length; index++) {
            for (Rectangle nr : TranscribedImage.image(index).positions) {
                String colorString = nr.getFill().toString();
                nr.setId(TranscribedImage.rectangleToId(index, nr));
                ArrayList<Rectangle> list = symbolTypesCache.getOrDefault(colorString, new ArrayList<>());
                if (list.isEmpty()) {
                    symbolTypesCache.put(colorString, list);
                }
                list.add(nr);
            }
        }
    }

    public static Rectangle first(int index, Color color) {

        for (Rectangle nr : TranscribedImage.image(index).positions) {
            if (nr.getFill().equals(color)) {
                nr.setId(TranscribedImage.rectangleToId(index, nr));
                return nr;
            }
        }
        return null;
    }

    public static Rectangle first(int index) {

        for (Rectangle nr : TranscribedImage.image(index).positions) {
            nr.setId(TranscribedImage.rectangleToId(index, nr));
            return nr;
        }
        return null;
    }

    public static Rectangle first(Color color) {

        buildSymbolTypesCacheIfNeeded();
        String colorString = color.toString();
        ArrayList<Rectangle> list = symbolTypesCache.get(colorString);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);

    }

    public static ArrayList<Rectangle> symbolsOfType(Color color) {
        buildSymbolTypesCacheIfNeeded();
        return symbolTypesCache.getOrDefault(color.toString(), new ArrayList<>());
    }

    public static String rectangleToId(int idx, Rectangle r) {
        if (r == null) {
            return null;
        }
        return idx + ":" + r.getLayoutX() + ":" + r.getLayoutY() + ":" + r.getWidth() + ":" + r.getHeight();

    }

    public ArrayList<Rectangle> positions() {
        return positions;
    }

    public void add(Rectangle nr) {
        positions.add(nr);
        symbolTypesCache = null;
        changed = true;
    }

    public void remove(Rectangle selected) {
        positions.remove(selected);
        symbolTypesCache = null;
        changed = true;
    }

    public void replaceAll(ArrayList<Rectangle> filtered) {
        positions.clear();
        positions.addAll(filtered);
    }

    public static void changeColor(String id, Rectangle r, Color color) {
        int index = idToIndex(id);
        r.setFill(color);
        symbolTypesCache = null;
        image(index).changed = true;
    }

    public static void resizeOrMove(Rectangle r, double x, double y, double w, double h) {
        r.setLayoutX(x);
        r.setLayoutY(y);
        r.setWidth(w);
        r.setHeight(h);
        symbolTypesCache = null;
        image(TranscribedImage.currentImageIndex).changed = true;
    }
}
