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

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
/*
    Algorithm to obtain lines of symbols.
 */
public class Alignment {
    public static ArrayList<ArrayList<Rectangle>> linesOfSymbols(int index) {
        final ArrayList<Rectangle> rectangles = TranscribedImage.image(index).positions();
        return linesOfSymbols(rectangles);
    }
    public static ArrayList<Rectangle> sortedPositions(int index) {

        ArrayList<ArrayList<Rectangle>> linesOfSymbols = linesOfSymbols(index);
        ArrayList<Rectangle> sorted = new ArrayList<>();
        for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
            sorted.addAll(lineOfSymbols);
        }

        return sorted;
    }

    private static ArrayList<ArrayList<Rectangle>> linesOfSymbols(ArrayList<Rectangle> rectangles) {

        // Sort everything from left to right.
        rectangles.sort(Comparator.comparingDouble(Node::getLayoutX));

        // Each line will be populated and sorted from left to right, but the lines are vertically sorted only after
        // they are populated.
        ArrayList<ArrayList<Rectangle>> linesOfSymbols = new ArrayList<>();

        // Process rectangles from left to right.
        for (Rectangle p : rectangles) {

            // Find the existing line with which the current rectangle best overlaps.
            double bestVerticalOverlap = 0.0;
            ArrayList<Rectangle> bestLineOfSymbols = null;
            for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {

                Rectangle lastInLine = lineOfSymbols.get(lineOfSymbols.size() - 1);
                double verticalOverlap = verticalOverlapRatio(p, lastInLine);

                // For robustness, also look at the rectangle before the last one, in case the last one is an outlier.
                if (lineOfSymbols.size() >= 2) {
                    Rectangle penultimateInLine = lineOfSymbols.get(lineOfSymbols.size() - 2);
                    verticalOverlap = Math.max(verticalOverlapRatio(p, penultimateInLine), verticalOverlap);

                }

                // Save it if better
                if (verticalOverlap > bestVerticalOverlap) {
                    bestVerticalOverlap = verticalOverlap;
                    bestLineOfSymbols = lineOfSymbols;
                }

            }
            // We want a minimal vertical overlap of 40%. Otherwise, create a new line.
            if (bestVerticalOverlap < 0.4) {
                ArrayList<Rectangle> lineOfSymbols = new ArrayList<>();
                lineOfSymbols.add(p);
                linesOfSymbols.add(lineOfSymbols);
            } else {
                bestLineOfSymbols.add(p);
            }
        }
        // Sort the lines
        linesOfSymbols.sort(new Comparator<>() {
            private double averageY(ArrayList<Rectangle> o) {
                double sum = 0.0;
                for (Rectangle r : o) {
                    sum += r.getLayoutY() + r.getHeight() / 2.0;
                }
                return sum / o.size();
            }

            @Override
            public int compare(ArrayList<Rectangle> o1, ArrayList<Rectangle> o2) {
                return Double.compare(averageY(o1), averageY(o2));
            }
        });

        return linesOfSymbols;
    }

    private static double verticalOverlapRatio(Rectangle r1, Rectangle r2) {
        // for simplicity, make sure r1 is higher than r2.
        if (r1.getLayoutY() > r2.getLayoutY()) {
            Rectangle keep = r1;
            r1 = r2;
            r2 = keep;
        }

        double overlap = Math.min(Math.max(0, r1.getHeight() + r1.getLayoutY() - r2.getLayoutY()), r2.getHeight());

        return overlap / Math.min(r1.getHeight(), r2.getHeight());
    }



}
