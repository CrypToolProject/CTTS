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
import org.cryptool.ctts.CTTSApplication;

import java.util.Map;
import java.util.TreeMap;

public class Icons {
    public final static String ICONS_DIR_NAME = "icons";
    private static final Map<String, Image> colorStringToImage = new TreeMap<>();

    public static void saveIcon(Color rgb, Image imageIn) {

        final String filename = rgb + ".png";
        FileUtils.writeImage(ICONS_DIR_NAME, filename, imageIn);
        Image image = FileUtils.readImage(ICONS_DIR_NAME, filename, false);
        if (image != null) {
            colorStringToImage.put(rgb.toString(), image);
        }
        CTTSApplication.colors.markAsChanged();
    }

    public static boolean readIcon(Color rgb, boolean silent) {
        final String filepath = rgb + ".png";
        return readIcon(rgb, filepath, silent);
    }

    public static boolean readIcon(Color rgb, String filepath, boolean silent) {
        Image image = FileUtils.readImage(ICONS_DIR_NAME, filepath, silent);
        if (image != null) {
            if (ImageUtils.hasTransparency(image)) {
                Icons.colorStringToImage.put(rgb.toString(), image);
            } else {
                Icons.colorStringToImage.put(rgb.toString(), ImageUtils.blackAndWhiteBinary(image));
            }
            if (!silent) {
                System.out.printf("Read %s\n", filepath);
            }
            return true;
        } else {
            if (!silent) {
                System.out.printf("Could not read %s\n", filepath);
            }
            return false;
        }
    }

    public static boolean deleteIcon(Color rgb) {

        if (FileUtils.deleteFile(ICONS_DIR_NAME, rgb + ".png")) {
            colorStringToImage.remove(rgb.toString());
            CTTSApplication.colors.markAsChanged();
            return true;
        } else {
            return false;
        }

    }

    public static Image get(String toString) {
        return colorStringToImage.get(toString);
    }

    public static Image getOrDefault(String colorString, Image o) {
        return colorStringToImage.getOrDefault(colorString, o);
    }
}
