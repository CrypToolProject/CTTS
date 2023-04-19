package sample;

import javafx.scene.SnapshotParameters;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.Locale;
import java.util.Random;

public class ImageUtils {

    public static boolean isSupportedFormat(String f) {
        f = f.toLowerCase(Locale.ROOT);
        return f.endsWith(".jpg") || f.endsWith(".png") || f.endsWith(".bmp");
    }

    public static String removeImageFormat(String f) {
        if (isSupportedFormat(f)) {
            return f.substring(0, f.length() - 4);
        }
        for (String format : new String[] {".jpg", ".png", ".bmp"}) {
            if (f.contains(format)) {
                int pos = f.lastIndexOf(format);
                return f.substring(0, pos) + f.substring(pos + 4);
            }
        }
        return f;
    }
    public static String replaceImageFormat(String f, String s) {
        if (isSupportedFormat(f)) {
            return f.substring(0, f.length() - 4) + s;
        }
        for (String format : new String[] {".jpg", ".png", ".bmp"}) {
            if (f.contains(format)) {
                int pos = f.lastIndexOf(format);
                return f.substring(0, pos) + s + f.substring(pos + 4);
            }
        }
        return f;
    }

    public static WritableImage negative(Image imageIn) {
        WritableImage image = new WritableImage((int) imageIn.getWidth(), (int) imageIn.getHeight());
        PixelReader reader = imageIn.getPixelReader();
        PixelWriter writer = image.getPixelWriter();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = reader.getColor(i, j);
                writer.setColor(i, j, color.invert());
            }
        }
        return image;
    }
    public static boolean hasTransparency(Image imageIn) {
        PixelReader reader = imageIn.getPixelReader();
        for (int x = 0; x < imageIn.getWidth(); x++) {
            for (int y = 0; y < imageIn.getHeight(); y++) {
                Color color = reader.getColor(x, y);
                if (color.equals(Color.TRANSPARENT)) {
                   return true;
                }
            }
        }
        return false;
    }
    public static WritableImage removeTransparentMargins(WritableImage imageIn) {

        int leftMargin = 0;
        int rightMargin = 0;
        int topMargin = -1;
        int bottomMargin = -1;

        final int width = (int) imageIn.getWidth();
        final int height = (int) imageIn.getHeight();

        PixelReader reader = imageIn.getPixelReader();
        for (int x = 0; x < width; x++) {
            boolean allTransparent = true;
            for (int y = 0; y < height; y++) {
                Color color = reader.getColor(x, y);
                if (!color.equals(Color.TRANSPARENT)) {
                    leftMargin = x;
                    allTransparent = false;
                    break;
                }
            }
            if (!allTransparent) {
                break;
            }
        }
        for (int x = width - 1; x >= 0; x--) {
            boolean allTransparent = true;
            for (int y = 0; y < height; y++) {
                Color color = reader.getColor(x, y);
                if (!color.equals(Color.TRANSPARENT)) {
                    rightMargin = width - x;
                    allTransparent = false;
                    break;
                }
            }
            if (!allTransparent) {
                break;
            }
        }

        for (int y = 0; y < height; y++) {
            boolean allTransparent = true;
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                if (!color.equals(Color.TRANSPARENT)) {
                    topMargin = y;
                    allTransparent = false;
                    break;
                }
            }
            if (!allTransparent) {
                break;
            }
        }
        for (int y = height - 1; y >= 0; y--) {
            boolean allTransparent = true;
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                if (!color.equals(Color.TRANSPARENT)) {
                    bottomMargin = height - y;
                    allTransparent = false;
                    break;
                }
            }
            if (!allTransparent) {
                break;
            }
        }


        if (topMargin + rightMargin + leftMargin + bottomMargin == 0) {
            return imageIn;
        }
        WritableImage image = new WritableImage(width - leftMargin - rightMargin, height - topMargin - bottomMargin);
        PixelWriter writer = image.getPixelWriter();
        for (int x = leftMargin; x < width - rightMargin; x++) {
            for (int y = topMargin; y < height - bottomMargin; y++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x - leftMargin, y - topMargin, color);
            }
        }
        return image;
    }

    public static WritableImage blackAndWhiteRandom(Image imageIn) {

        Random r = new Random();

        WritableImage image = new WritableImage((int) imageIn.getWidth(), (int) imageIn.getHeight());

        PixelReader reader = imageIn.getPixelReader();
        PixelWriter writer = image.getPixelWriter();
        double centerX = (0.25 + r.nextFloat() * 0.5) * image.getWidth();
        double centerY = (0.25 + r.nextFloat() * 0.5) * image.getHeight();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color col = reader.getColor(i, j);
                double grayscale = 0.6 * Math.sqrt(Math.pow((i - centerX)/image.getWidth(), 2) + Math.pow((j - centerY)/image.getHeight(), 2));
                boolean transparent = col.equals(Color.TRANSPARENT);
                if (!transparent && r.nextFloat() < 0.05) {
                    transparent = true;
                }
                if (transparent) {
                    writer.setColor(i, j, Color.TRANSPARENT);
                } else {
                    writer.setColor(i, j, new Color(grayscale, grayscale, grayscale, 1.0));
                }
            }
        }


        return image;


    }
    public static WritableImage blackAndWhite(Image imageIn) {

        final double MAX_GRAY_SCALE = 0.60;
        final double MIN_GRAY_SCALE = 0.4;
        final int BLUR_RADIUS = 3;
        final double CONTRAST = 0.5;

        WritableImage image = new WritableImage((int) imageIn.getWidth(), (int) imageIn.getHeight());

        PixelReader reader = imageIn.getPixelReader();
        PixelWriter writer = image.getPixelWriter();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color col = reader.getColor(i, j);
                double grayscale = col.getRed() * 0.3 + col.getGreen() * 0.59 + col.getBlue() * 0.11;
                if (grayscale > MAX_GRAY_SCALE) {
                    writer.setColor(i, j, Color.TRANSPARENT);
                    continue;
                }

                if (grayscale < MIN_GRAY_SCALE) {
                    grayscale = 0.0;
                }
                writer.setColor(i, j, new Color(grayscale, grayscale, grayscale, 1.0));
            }
        }


        //Instantiating the GaussianBlur class
        GaussianBlur gaussianBlur = new GaussianBlur();

        //Setting the radius to apply the Gaussian Blur effect
        gaussianBlur.setRadius(BLUR_RADIUS);
        ImageView iv = new ImageView(imageIn);
        iv.setEffect(gaussianBlur);
        image =  iv.snapshot(new SnapshotParameters(), null);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(CONTRAST);
        iv.setEffect(colorAdjust);

        PixelReader reader2 = image.getPixelReader();
        WritableImage image2 = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter writer2 = image2.getPixelWriter();
        for (int i = 0; i < image2.getWidth(); i++) {
            for (int j = 0; j < image2.getHeight(); j++) {
                Color col = reader2.getColor(i, j);
                double grayscale = col.getRed() * 0.3 + col.getGreen() * 0.59 + col.getBlue() * 0.11;
                if (grayscale > MAX_GRAY_SCALE) {
                    writer2.setColor(i, j, Color.TRANSPARENT);
                    continue;
                } else {
                    grayscale = 0.0;
                }
                writer2.setColor(i, j, new Color(grayscale, grayscale, grayscale, 1.0));
            }
        }
        return image2;


    }

    public static WritableImage negativeAround(int x, int y, int w, int h, int marginX, int marginY, Image imageIn) {


        WritableImage image = new WritableImage((int) imageIn.getWidth(), (int) imageIn.getHeight());

        PixelReader reader = imageIn.getPixelReader();
        PixelWriter writer = image.getPixelWriter();
        for (int i = Math.max(x, 0); i < Math.max(Math.min(x + w, imageIn.getWidth()), 0); i++) {
            for (int j = Math.max(y, 0); j < Math.max(Math.min(y + h, imageIn.getHeight()), 0); j++) {
                Color color = reader.getColor(i, j);
                if (i >= x + marginX && i < x + w - marginX && j >= y + marginY && j < y + h - marginY) {
                    writer.setColor(i, j, color);
                } else {
                    writer.setColor(i, j, color.invert());
                }
            }
        }
        return image;
    }

    static WritableImage gaussianBlur(Image image2, double blurRadius) {
        GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.setRadius(blurRadius);
        ImageView iv = new ImageView(image2);
        iv.setEffect(gaussianBlur);
        WritableImage image =  iv.snapshot(new SnapshotParameters(), null);
        return removeMargin(image, (int) blurRadius /2 );
    }
    static WritableImage motionBlur(Image image2, double blurRadius) {
        MotionBlur motionBlur = new MotionBlur();
        motionBlur.setRadius(blurRadius);
        ImageView iv = new ImageView(image2);
        iv.setEffect(motionBlur);
        WritableImage image =  iv.snapshot(new SnapshotParameters(), null);
        return removeMargin(image, (int) blurRadius /2 );
    }

    static WritableImage contrast(Image imageIn, double contrast) {
        ImageView iv = new ImageView(imageIn);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(contrast);
        iv.setEffect(colorAdjust);
        return iv.snapshot(new SnapshotParameters(), null);
    }

    static WritableImage binarize(Image imageIn, double maxGrayScale) {
        PixelReader reader = imageIn.getPixelReader();
        final int width = (int) imageIn.getWidth();
        final int height = (int) imageIn.getHeight();

        WritableImage image2 = new WritableImage(width, height);
        PixelWriter writer2 = image2.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height ; j++) {
                Color col = reader.getColor(i, j);
                double grayscale = col.getRed() * 0.3 + col.getGreen() * 0.59 + col.getBlue() * 0.11;
                if (grayscale >  1 - maxGrayScale) {
                    col = Color.TRANSPARENT;
                } else {
                    col = Color.BLACK;
                }
                writer2.setColor(i, j, col);
            }
        }
        return image2;
    }

    private static WritableImage removeMargin(Image image, int margin){
        PixelReader reader2 = image.getPixelReader();
        final int width = (int) image.getWidth();
        final int height = (int) image.getHeight();

        WritableImage image2 = new WritableImage(width - 2 * margin, height - 2 * margin);
        PixelWriter writer2 = image2.getPixelWriter();
        for (int i = margin; i < width - margin; i++) {
            for (int j = margin; j < height - margin; j++) {
                Color col = reader2.getColor(i, j);
                writer2.setColor(i - margin, j - margin, col);
            }
        }

        return image2;
    }
}
