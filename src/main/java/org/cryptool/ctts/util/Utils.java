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

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static int screenWidth = -1;
    public static int screenHeight = -1;
    static long start;

    public static int adjust(int z) {
        return Math.min(z * screenWidth / 2194, z * screenHeight / 1234);
    }

    public static double adjust(double z) {
        return Math.min(z * screenWidth / 2194, z * screenHeight / 1234);
    }

    public static int extractNumber(String s) {
        Pattern p = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*");
        Matcher m = p.matcher(s);

        if (m.find()) {
            // get the two groups we were looking for
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    private static double highestXPixelShown(ScrollPane scrollPane) {
        double hmin = scrollPane.getHmin();
        double hmax = scrollPane.getHmax();
        double hvalue = scrollPane.getHvalue();
        double contentWidth = scrollPane.getContent().getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double lowestXPixelShown =
                Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);
        double highestXPixelShown = lowestXPixelShown + viewportWidth;
        return highestXPixelShown;
    }

    private static double highestYPixelShown(ScrollPane scrollPane) {
        double vmin = scrollPane.getVmin();
        double vmax = scrollPane.getVmax();
        double vvalue = scrollPane.getVvalue();
        double contentHeight = scrollPane.getContent().getLayoutBounds().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double lowestYPixelShown =
                Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);
        double highestYPixelShown = lowestYPixelShown + viewportHeight;
        return highestYPixelShown;
    }

    private static double lowestXPixelShown(ScrollPane scrollPane) {
        double hmin = scrollPane.getHmin();
        double hmax = scrollPane.getHmax();
        double hvalue = scrollPane.getHvalue();
        double contentWidth = scrollPane.getContent().getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double lowestXPixelShown =
                Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);
        return lowestXPixelShown;
    }

    private static double lowestYPixelShown(ScrollPane scrollPane) {
        double vmin = scrollPane.getVmin();
        double vmax = scrollPane.getVmax();
        double vvalue = scrollPane.getVvalue();
        double contentHeight = scrollPane.getContent().getLayoutBounds().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double lowestYPixelShown =
                Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);
        return lowestYPixelShown;
    }

    public static void adjustHorizontalScrollBar(ScrollPane scrollPane, Node reference, double scaleValue, double increment) {
        double lowestXPixelShown = lowestXPixelShown(scrollPane);
        double highestXPixelShown = highestXPixelShown(scrollPane);
        final double x = reference.getLayoutX() * scaleValue;
        final double w = reference.getBoundsInParent().getWidth() * scaleValue;
        double xMargin = w;
        while (x - xMargin < lowestXPixelShown) {
            final double newVvalue = scrollPane.getHvalue() - increment;
            if (newVvalue < 0.0) {
                break;
            }
            scrollPane.setHvalue(newVvalue);
            lowestXPixelShown = lowestXPixelShown(scrollPane);
        }
        while (x + w + xMargin > highestXPixelShown) {
            final double newVvalue = scrollPane.getHvalue() + increment;
            if (newVvalue > 1.0) {
                break;
            }
            scrollPane.setHvalue(newVvalue);
            highestXPixelShown = highestXPixelShown(scrollPane);
        }
    }

    public static void adjustVerticalScrollBar(ScrollPane scrollPane, Node reference, double scaleValue, double increment) {
        final double y = reference.getLayoutY() * scaleValue;
        final double h = reference.getBoundsInParent().getHeight() * scaleValue;
        double lowestYPixelShown = lowestYPixelShown(scrollPane);
        double highestYPixelShown = highestYPixelShown(scrollPane);
        double yMargin = h;
        if (y > lowestYPixelShown && y + h < highestYPixelShown) {
            return;
        }
        while (y - yMargin < lowestYPixelShown) {
            final double newVvalue = scrollPane.getVvalue() - increment;
            if (newVvalue < 0.0) {
                break;
            }
            scrollPane.setVvalue(newVvalue);
            lowestYPixelShown = lowestYPixelShown(scrollPane);
        }
        while (y + h + yMargin > highestYPixelShown) {
            final double newVvalue = scrollPane.getVvalue() + increment;
            if (newVvalue > 1.0) {
                break;
            }
            scrollPane.setVvalue(newVvalue);
            highestYPixelShown = highestYPixelShown(scrollPane);
        }
    }

    public static double[] setAndGetSize(Text helper, Font font, double wrappingWidth) {
        helper.setFont(font);
        // Note that the wrapping width needs to be set to zero before
        // getting the text's real preferred width.
        helper.setWrappingWidth(0);
        helper.setLineSpacing(0);
        double w = Math.min(helper.prefWidth(-1), wrappingWidth);
        helper.setWrappingWidth((int) Math.ceil(w));
        double textWidth = Math.ceil(helper.getLayoutBounds().getWidth());
        double textHeight = Math.ceil(helper.getLayoutBounds().getHeight());
        return new double[]{textWidth, textHeight};
    }

    public static boolean isSingleLetter(String c) {
        return singleLetterIndex(c) != -1;
    }

    public static int singleLetterIndex(String c) {
        int penalty = 0;
        if (c.length() == 2) {
            char c1 = c.charAt(0);
            char c2 = c.charAt(1);
            if (c1 != c2 || isDigit(c1) || isDigit(c2)) {
                return -1;
            }
            c = c.substring(0, 1);
            penalty = 2;
        }
        if (c.length() != 1) {
            return -1;
        }
        final String UPPER = "AÀÁÃÅĄÄÂΆBCÇČDĎEĚÊÈÉĘËFGHIĮÎÌÍÏJKLMNŇŃÑOÐÖØÒÓÔŐÕΘǪPQRŘŔSŠSTŤUÜÚŰÙÛŮVWXYZÝŻŽŹ";
        final String LOWER = "aàáãåąäâάbcçčdďeěêèéęëfghiįîìíïjklmnňńñoðöøòóôőõθǫpqrřŕsšßtťuüúűùûůvwxyzýżžź";
        int u = UPPER.indexOf(c);
        if (u != -1) {
            u = 4 * u;
        }
        int l = LOWER.indexOf(c);
        if (l != -1) {
            l = 4 * l + 1;
        }
        return Math.max(u, l) + penalty;
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static void recursiveSetId(Node node, String id) {
        if (node == null) {
            return;
        }
        node.setId(id);
        if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                recursiveSetId(child, id);
            }
        }
    }

    static void start() {
        start = System.nanoTime();
    }

    static void stop(String s) {
        System.out.printf("Timer for: %s %,d micro\n", s, (System.nanoTime() - start) / 1000);
        start();
    }
}
