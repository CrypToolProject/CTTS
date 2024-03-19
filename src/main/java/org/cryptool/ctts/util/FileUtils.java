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

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileUtils {
    public static String workingDirectory = ".";

    public static void writeImage(String dirName, String filename, Image image) {
        File file = fileToWrite(dirName, filename, false);
        if (file == null) {
            return;
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            System.out.printf("Could not save image: %s\n", file);
        }
    }

    public static Image readImage(String dirName, String filename, boolean silent) {
        File file = fileToRead(dirName, filename, silent);
        if (file == null) {
            return null;
        }
        try {
            InputStream stream = new FileInputStream(file);
            final Image image = new Image(stream);
            stream.close();
            return image;
        } catch (FileNotFoundException e) {
            if (!silent) {
                System.out.printf("Could not read image: %s\n", file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readTextFile(String dirName, String filename) {
        File file = fileToRead(dirName, textFilename(filename), false);
        return readTextFile(file);
    }

    public static String readTextFile(File file) {
        if (file == null) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            if (fis.read(data) != -1) {
                fis.close();
                String s8 = new String(data, StandardCharsets.UTF_8);
                String s1 = new String(data, StandardCharsets.ISO_8859_1);
                int[] c1 = new int[256];
                int[] c8 = new int[256];
                for (byte c : s1.getBytes()) {
                    c1[c + 128]++;
                }
                for (byte c : s8.getBytes()) {
                    c8[c + 128]++;
                }
                if (c1[128 - 125] + c1[128 - 62] > 0) {
                    return s8;
                }
                return s1;
            }
        } catch (FileNotFoundException e) {
            System.out.printf("File not found: %s\n", file);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String textFilename(String filename) {
        return filename.replaceAll("\\..*", "") + ".txt";
    }

    public static void writeTextFile(String dirName, String filename, String text) {
        File file = fileToWrite(dirName, textFilename(filename), false);
        if (file == null) {
            return;
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(text);
            fileWriter.flush();
            fileWriter.close();
            System.out.printf("Saved %s\n", file);
        } catch (IOException e) {
            System.out.printf("Failed save %s\n", file);
        }
    }

    public static boolean deleteFile(String dirName, String filename) {
        File file = fileToRead(dirName, filename, false);
        if (file == null) {
            return false;
        }
        if (file.delete()) {
            System.out.printf("Deleted %s\n", file);
            return true;
        } else {
            System.out.printf("Could not delete %s\n", file);
            return false;
        }
    }

    public static void snapshot(String dirName, String imageFileName, Pane node) {
        final int width = (int) node.getWidth() + 20;
        final int height = (int) node.getHeight() + 20;
        final int maxHeight = 5000;
        final int margin = 500;
        if (height < maxHeight) {
            final String filename = imageFileName.replaceAll("\\..*", "") + ".png";
            File file = fileToWrite(dirName, filename, false);
            if (file == null) {
                return;
            }
            try {
                WritableImage writableImage = new WritableImage(width, height);
                node.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
                System.out.printf("Saved %s\n", file);
            } catch (Exception ex) {
                System.out.printf("Failed to save %s\n", file);
            }
        } else {
            for (int y = 0; y < height; y += maxHeight) {
                final String filename = imageFileName.replaceAll("\\..*", "") + "_" + y / maxHeight + ".png";
                File file = fileToWrite(dirName, filename, false);
                if (file == null) {
                    return;
                }
                try {
                    int effectiveHeight = Math.min(maxHeight, height - y) + margin;
                    WritableImage writableImage = new WritableImage(width, effectiveHeight);
                    SnapshotParameters p = new SnapshotParameters();
                    p.setViewport(new Rectangle2D(0, y, width, effectiveHeight));
                    node.snapshot(p, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                    System.out.printf("Saved %s\n", file);
                } catch (Exception ex) {
                    System.out.printf("Failed to save %s\n", file);
                }
            }
        }
    }

    static boolean isAbsolutePath(String filepath) {
        return filepath.startsWith("/") || filepath.startsWith("\\") || filepath.matches("[A-Z]:[/\\\\].*");
    }

    public static File fileToRead(String dirName, String filename, boolean silent) {
        if (filename == null) {
            return null;
        }
        if (isAbsolutePath(filename)) {
            return new File(filename);
        }
        if (dirName == null) {
            return new File(workingDirectory, filename);
        }
        File directory = new File(workingDirectory, dirName);
        if (!directory.exists()) {
            if (!silent) {
                System.out.printf("Directory does not exist: %s\n", dirName);
            }
            return null;
        }
        return new File(directory, filename);
    }

    public static File fileToWrite(String dirName, String filename, boolean silent) {
        if (filename == null) {
            return null;
        }
        if (isAbsolutePath(filename)) {
            throw new RuntimeException("Cannot save to absolute path");
        }
        if (dirName == null) {
            return new File(workingDirectory, filename);
        }
        File directory = new File(workingDirectory, dirName);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                if (!silent) {
                    System.out.printf("Failed to create directory %s\n", dirName);
                }
                return null;
            }
        }
        return new File(directory, filename);
    }

    public static String keyFileInCurrentDirectory() {
        File dir = new File(workingDirectory);
        for (File entry : Objects.requireNonNull(dir.listFiles())) {
            if (entry.isFile()) {
                String filename = entry.getName();
                if (filename.equalsIgnoreCase("key.txt")) {
                    return filename;
                }
            }
        }
        return null;
    }

    public static String currentDirectoryFullpathString() {
        File f = new File(workingDirectory);
        String fs = f.getAbsolutePath();
        if (fs.endsWith("\\.") || fs.endsWith("/.")) {
            fs = fs.substring(0, fs.length() - 2);
        }
        return fs;
    }

    public static String currentDirectoryString() {
        Set<String> collections = new TreeSet<String>(List.of(new String[]{"BNF", "BNE", "TNA", "KHA", "YALE", "ASV", "ARA", "OSH", "NAH", "NLS"}));
        File f = new File(workingDirectory);
        String fs = f.getAbsolutePath();
        if (fs.endsWith("\\.") || fs.endsWith("/.")) {
            fs = fs.substring(0, fs.length() - 2);
        }
        String[] parts = fs.split("[\\\\/]+");
        if (parts.length == 0) {
            return "";
        }
        String s = parts[parts.length - 1];
        for (int i = parts.length - 2; i >= 0; i--) {
            String p = parts[i].toUpperCase(Locale.ROOT);
            if (collections.contains(p)) {
                s = p + " " + s;
            }
        }
        return s;
    }

    public static ArrayList<String> imageFilesInCurrentDirectory() {
        File dir = new File(workingDirectory);
        ArrayList<String> filenameArray = new ArrayList<>();

        for (File entry : Objects.requireNonNull(dir.listFiles())) {
            if (entry.isFile()) {
                String filename = entry.getName();
                if (ImageUtils.isSupportedFormat(filename) && !filename.contains("negative")) {
                    filenameArray.add(filename);
                }
            }
        }

        filenameArray.sort((o1, o2) -> {
            int i1 = Utils.extractNumber(o1);
            int i2 = Utils.extractNumber(o2);
            if (i1 != -1 && i2 != -1) {
                return Integer.compare(i1, i2);
            }
            return o1.compareTo(o2);
        });

        return filenameArray;
    }

    public static ArrayList<String> textFilesInDirectory(String directory) {
        File dir = new File(workingDirectory, directory);

        ArrayList<String> filenameArray = new ArrayList<>();

        final File[] files = dir.listFiles();
        if (files == null) {
            return filenameArray;
        }
        for (File entry : files) {
            if (entry.isFile()) {
                String filename = entry.getName();
                if (filename.endsWith(".txt") && !filename.equals("edits.txt")) {
                    filenameArray.add(filename);
                }
            }
        }

        filenameArray.sort((o1, o2) -> {
            int i1 = Utils.extractNumber(o1);
            int i2 = Utils.extractNumber(o2);
            if (i1 != -1 && i2 != -1) {
                return Integer.compare(i1, i2);
            }
            return o1.compareTo(o2);
        });

        return filenameArray;
    }

    public static StringBuilder readFileAZSpaceOnly(String filename) {

        final String RAW_PLAINTEXT_LETTERS = "abcdefghijklmnopqrstuvwxyzàáãåάąäâªªçčðďλěêèéęëįîìíïłňńñöøòóôőõθº°ǫφþřŕš§ťüúűùûů×ýżžź";
        final String PLAINTEXT_LETTERS_MAP = "abcdefghijklmnopqrstuvwxyzaaaaaaaaaaccdddeeeeeeiiiiilnnnooooooooooopprrsstuuuuuuxyzzz";

        try {
            StringBuilder sb = new StringBuilder();
            FileReader fileReader = new FileReader(filename);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            boolean wasSpace = true;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.toLowerCase(Locale.ROOT);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    int index = RAW_PLAINTEXT_LETTERS.indexOf(c);
                    if (index != -1) {
                        sb.append(PLAINTEXT_LETTERS_MAP.charAt(index));
                        wasSpace = false;
                    } else if (c == 'ß') {
                        sb.append("ss");
                        wasSpace = false;
                    } else if (!wasSpace) {
                        wasSpace = true;
                        sb.append(" ");
                    }
                }
            }

            bufferedReader.close();
            return sb;
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filename + "'");
            return null;
        } catch (IOException ex) {
            System.out.println("Error reading file '" + filename + "'");
            return null;
        }
    }

    public static String readResourceFile(String filename) {

        // for static access, uses the class name directly
        InputStream is = Utils.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            System.out.println("Could not open resource file: " + filename);
            System.exit(0);
        }
        try {
            byte[] b = new byte[1_000_000];
            int read = is.read(b);
            String s = new String(b);
            is.close();
            return s;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);

            return null;
        }

    }
}
