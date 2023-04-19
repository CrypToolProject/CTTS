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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Positions implements Serializable {
    static final long serialVersionUID = 7394092863997515706L;
    private final ArrayList<String> positions = new ArrayList<>();
    public static String SECOND_COPY = "_SECOND_COPY";
    public static final String POSITIONS = "positions";

    private Positions() {

    }

    public static void save(int index) {
        String backupFilename = TranscribedImage.image(index).filename;

        backupFilename += "_" + POSITIONS;
        Positions record = new Positions();
        ArrayList<Rectangle> positions = Alignment.sortedPositions(index);
        for (Rectangle r : positions) {
            String s = "" + r.getLayoutX() + ":" + r.getLayoutY() + ":" + r.getWidth() + ":" + r.getHeight() + ":"
                    + r.getFill().toString();
            record.positions.add(s);
        }
        // record.save(backupFilename);
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
            // System.out.println(textFile);
            // return nodes;
            ArrayList<Rectangle> nodesOldColorSet = parsePositions(text, Colors.colorSetOld());
            final int assignedWithOld = positionsWithTranscriptionValues(nodesOldColorSet);
            final int assignedWithNew = positionsWithTranscriptionValues(nodes);

            if (assignedWithNew > 0 || assignedWithOld > 0) {
                if (assignedWithNew >= assignedWithOld) {

                    System.out.printf("Read %s/%s - %,d symbols (%,d assigned)\n", dir, textFile, nodes.size(),
                            assignedWithNew);
                    return nodes;
                }
                System.out.printf("Read %s/%s - %,d symbols (%,d assigned) using old colorset\n", dir, textFile,
                        nodesOldColorSet.size(), assignedWithOld);
                return nodesOldColorSet;
            } else if (nodes.size() > 0) {
                System.out.printf("Read %s/%s - %,d symbols (%,d assigned)\n", dir, textFile, nodes.size(),
                        assignedWithNew);
                return nodes;
            }
        }

        Positions record = readTwoCopies(backupFilename);
        if (record != null) {
            ArrayList<Rectangle> nodes = new ArrayList<>();
            for (String s : record.positions) {

                String[] values = s.split(":");
                if (values.length != 5) {
                    continue;
                }

                // System.out.println(s);

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);
                double w = Double.parseDouble(values[2]);
                double h = Double.parseDouble(values[3]);

                Color color = Color.valueOf(values[4].substring(2));

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

        return new ArrayList<>();
    }

    private static int positionsWithTranscriptionValues(ArrayList<Rectangle> positions) {
        int count = 0;
        for (Rectangle r : positions) {
            String c = OTAApplication.colors.getOrDefault(r.getFill().toString(), "");
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
                String line = String.format("%f %f %f %f %3d\n", r.getLayoutX(), r.getLayoutY(), r.getWidth(),
                        r.getHeight(),
                        OTAApplication.colors.indexOf((Color) r.getFill()));
                s.append(line);
            }
            s.append("\n");
        }
        FileUtils.writeTextFile(POSITIONS, outFilename, s.toString());
        FileUtils.writeTextFile(POSITIONS + SECOND_COPY, outFilename, s.toString());

    }

    private static Positions readTwoCopies(String backupFilename) {
        Positions state = read(backupFilename);
        if (state == null) {
            state = read(backupFilename + SECOND_COPY);
        }
        return state;
    } 

    private static Positions read(String backupFilename) {
        try {
            FileInputStream fileIn = new FileInputStream(backupFilename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Positions state = (Positions) in.readObject();
            in.close();
            fileIn.close();
            System.out.printf("Read %s - %d symbols\n", backupFilename, state.positions.size());

            return state;
        } catch (InvalidClassException e) {
            System.out.printf(
                    "Could not restore serialized state from %s - incompatible class - check serialVersionUID - currently: %d\n",
                    backupFilename, serialVersionUID);
            System.out.println(e);
            System.exit(0);
            return null;
        } catch (FileNotFoundException e) {
            System.out.printf("File not found: %s\n", backupFilename);
            return null;
        } catch (IOException | ClassNotFoundException i) {
            System.out.printf("Could not restore serialized state from %s\n", backupFilename);
            // i.printStackTrace();
            return null;
        }
    }

}
