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

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cryptool.ctts.CTTSApplication;
import org.cryptool.ctts.gui.DetailedTranscriptionPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TranscribedImage {
    public static TranscribedImage[] transcribedImages;
    public static int currentImageIndex = 0;
    static Map<String, ArrayList<Rectangle>> symbolTypesCache = null;
    public Image image;
    public Image negative;
    public String filename;
    public double scaleValue = 1.0;
    public double vValue = 0.0;
    public double hValue = 0.0;
    public double detailedScaleValue = 0.6;
    public double detailedvValue = 0.0;
    public double detailedhValue = 0.0;
    ArrayList<Rectangle> positions;
    boolean changed = false;

    TranscribedImage(String filename) {
        this.filename = filename;
    }

    public static void createNegativesIfNeed() {
        for (TranscribedImage t : transcribedImages) {
            if (t.negative == null) {
                t.negative = ImageUtils.negative(ImageUtils.blackAndWhite(t.image));
                String negativeFilename = t.filename.substring(0, t.filename.lastIndexOf(".")) + "_negative" + t.filename.substring(t.filename.lastIndexOf("."));
                FileUtils.writeImage(null, negativeFilename, t.negative);
                System.out.printf("Created %s\n", negativeFilename);
            }
        }
    }

    public static TranscribedImage[] extractFromArgs(String[] args) {
        int count_ = 0;
        for (String arg : args) {
            if (ImageUtils.isSupportedFormat(arg)) {
                count_++;
            }
        }
        TranscribedImage[] transcribedImages = new TranscribedImage[count_];
        count_ = 0;
        Runnables r = new Runnables();
        for (String arg : args) {
            if (ImageUtils.isSupportedFormat(arg)) {
                int count = count_;
                r.addRunnable(() -> {
                    transcribedImages[count] = new TranscribedImage(arg);
                    transcribedImages[count].image = FileUtils.readImage(null, arg, false);
                    if (transcribedImages[count].image == null) {
                        System.exit(-1);
                    }
                    String negativeFilename = arg.substring(0, arg.lastIndexOf(".")) + "_negative" + arg.substring(arg.lastIndexOf("."));
                    transcribedImages[count].negative = FileUtils.readImage(null, negativeFilename, true);
                    transcribedImages[count].positions = Positions.restore(arg);
                    if (transcribedImages[count].positions.isEmpty()) {
                        System.out.printf("Could not restore symbols for: %s\n", arg);
                    }
                });
                count_++;
            }
        }
        r.run(count_);
        return transcribedImages;
    }

    public static int size() {
        return transcribedImages.length;
    }

    public static TranscribedImage current() {
        return transcribedImages[currentImageIndex];
    }

    public static TranscribedImage image(int imageIndex) {
        return transcribedImages[imageIndex];
    }

    static StringBuilder transcriptionTextFormat(int index) {
        StringBuilder lines = new StringBuilder();

        lines.append(String.format("# %s\n", transcribedImages[index].filename));
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            for (Rectangle r : lineOfSymbols) {
                lines.append(CTTSApplication.colors.get(r.getFill().toString())).append(";");
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

    public static void saveTranscriptionsDecryptionsPositions() {
        Utils.start();
        saveTranscriptions();
        Utils.stop("Save Transcriptions");
        if (CTTSApplication.key.isKeyAvailable()) {
            saveDecryptions();
            Utils.stop("Save Decryption");
        }
        for (int i = 0; i < size(); i++) {
            if (TranscribedImage.image(i).changed) {
                Positions.save(i);
                TranscribedImage.image(i).changed = false;
            }
        }
        Utils.stop("Save Positions");
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
        if (CTTSApplication.catalog != null && !CTTSApplication.catalog.isEmpty()) {
            String[] parts = CTTSApplication.catalog.split("_");
            String prefix = "#CIPHERTEXT\n" + "#CATALOG NAME: ";
            if (parts.length < 2 || parts.length > 3) {
                System.out.printf("Invalid catalog (-c): %s\n", CTTSApplication.catalog);
                return;
            }
            if (parts.length == 2) {
                prefix += CTTSApplication.catalog + "/" + parts[1] + "\n";
            } else {
                prefix += parts[0] + "_" + parts[1] + "/" + parts[2] + "\n";
            }
            FileUtils.writeTextFile("../../transcription", parts[1], prefix + all);
        }
    }

    private static void saveDecryptions() {
        StringBuilder all = new StringBuilder();
        for (int index = 0; index < size(); index++) {
            StringBuilder lines = decryptionTextFormat(index);
            FileUtils.writeTextFile("decryption", transcribedImages[index].filename, lines.toString());
            all.append(lines);
        }
        FileUtils.writeTextFile("decryption", "all", all.toString());
        CTTSApplication.key.covered();
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

    public static Map<String, Double> freq(String start) {
        Map<String, Double> freq = new HashMap<>();
        int total = 0;
        for (TranscribedImage transcribedImage : transcribedImages) {
            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            String startOfFilename = filename.substring(0, endStartIndex);
            if (start.toLowerCase().equalsIgnoreCase(startOfFilename)) {
                total += transcribedImage.positions.size();
                for (Rectangle r : transcribedImage.positions) {
                    final String key = r.getFill().toString();
                    String name = CTTSApplication.colors.get(key);
                    freq.put(name, freq.getOrDefault(name, 0.0) + 1);
                }
            }
        }
        int finalTotal = total;
        freq.replaceAll((n, v) -> freq.get(n) / finalTotal);
        return freq;
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
            String startOfFilename = filename.substring(0, endStartIndex);
            if (start.equalsIgnoreCase(startOfFilename)) {
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

    public static int totalSymbols(String start) {
        int total = 0;
        for (int idx = 0; idx < TranscribedImage.size(); idx++) {
            TranscribedImage transcribedImage = TranscribedImage.image(idx);
            final String filename = transcribedImage.filename;
            int endStartIndex = filename.indexOf(".");
            if (filename.charAt(endStartIndex - 1) < '0' || filename.charAt(endStartIndex - 1) > '9') {
                endStartIndex--;
            }
            String startOfFilename = filename.substring(0, endStartIndex);
            if (start.equalsIgnoreCase(startOfFilename)) {
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
            String startOfFilename = filename.substring(0, endStartIndex);
            if (start.equalsIgnoreCase(startOfFilename)) {
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
}
