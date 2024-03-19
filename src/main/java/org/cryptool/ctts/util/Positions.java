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
import javafx.scene.shape.Rectangle;
import org.cryptool.ctts.CTTSApplication;

import java.util.ArrayList;

public class Positions {
    public static final String POSITIONS = "positions";
    public static String SECOND_COPY = "_SECOND_COPY";

    private Positions() {
    }

    public static void save(int index) {
        String backupFilename = TranscribedImage.image(index).filename;
        backupFilename += "_" + POSITIONS;
        String textFile = ImageUtils.removeImageFormat(backupFilename) + ".txt";
        savePositionsTextFile(textFile, index);
    }

    public static ArrayList<Rectangle> restore(String backupFilename) {
        backupFilename += "_" + POSITIONS;
        String textFile = ImageUtils.removeImageFormat(backupFilename) + ".txt";
        String dir = POSITIONS;
        String text = FileUtils.readTextFile(dir, textFile);
        if (text == null) {
            dir = POSITIONS + SECOND_COPY;
            text = FileUtils.readTextFile(dir, textFile);
        }
        if (text != null) {
            text = text.replaceAll(",", ".");
            ArrayList<Rectangle> nodes = parsePositions(text, Colors.colorSet());
            ArrayList<Rectangle> nodesOldColorSet = parsePositions(text, Colors.colorSetOld());
            final int assignedWithOld = positionsWithTranscriptionValues(nodesOldColorSet);
            final int assignedWithNew = positionsWithTranscriptionValues(nodes);
            if (assignedWithNew > 0 || assignedWithOld > 0) {
                if (assignedWithNew >= assignedWithOld) {
                    System.out.printf("Read %s/%s - %,d symbols (%,d assigned)\n", dir, textFile, nodes.size(), assignedWithNew);
                    return nodes;
                }
                System.out.printf("Read %s/%s - %,d symbols (%,d assigned) using old colorset\n", dir, textFile, nodesOldColorSet.size(), assignedWithOld);
                return nodesOldColorSet;
            } else if (nodes.size() > 0) {
                System.out.printf("Read %s/%s - %,d symbols (%,d assigned)\n", dir, textFile, nodes.size(), assignedWithNew);
                return nodes;
            }
        }
        return new ArrayList<>();
    }

    private static int positionsWithTranscriptionValues(ArrayList<Rectangle> positions) {
        int count = 0;
        for (Rectangle r : positions) {
            String c = CTTSApplication.colors.getOrDefault(r.getFill().toString(), "");
            if (c.length() > 0) {
                count++;
            }
        }
        return count;
    }

    private static ArrayList<Rectangle> parsePositions(String text, ArrayList<String> colorSet) {
        ArrayList<Rectangle> nodes = new ArrayList<>();
        for (String line : text.split("[\r\n]+")) {
            line = line.replaceAll(",", ".");
            String[] values = line.split("[ ]+");
            if (values.length != 5) {
                continue;
            }
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            double w = Double.parseDouble(values[2]);
            double h = Double.parseDouble(values[3]);
            int colorIndex = Integer.parseInt(values[4]);
            if (colorIndex >= colorSet.size()) {
                return new ArrayList<>();
            }
            Color color = Color.valueOf(colorSet.get(colorIndex));
            Rectangle nr = new Rectangle();
            nr.setLayoutX(x);
            nr.setLayoutY(y);
            nr.setWidth(w);
            nr.setHeight(h);
            nr.setFill(color);
            nodes.add(nr);
        }
        return nodes;
    }

    public static void savePositionsTextFile(String outFilename, int index) {
        StringBuilder s = new StringBuilder();
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            for (Rectangle r : lineOfSymbols) {
                String line = String.format("%f %f %f %f %3d\n", r.getLayoutX(), r.getLayoutY(), r.getWidth(), r.getHeight(),
                        CTTSApplication.colors.indexOf((Color) r.getFill()));
                s.append(line);
            }
            s.append("\n");
        }
        FileUtils.writeTextFile(POSITIONS, outFilename, s.toString());
        FileUtils.writeTextFile(POSITIONS + SECOND_COPY, outFilename, s.toString());
    }
}
