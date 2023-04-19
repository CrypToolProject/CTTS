package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static sample.DetailedTranscriptionPane.ICON_SIZE;


public class SimulatedImage extends Popup {

    Pane mainPane;
    int index;
    private SimulatedImage(int index, boolean effects, boolean decryption, boolean edited) {
        this.index = index;
        setX(50);
        setY(50);

        Canvas canvas = new Canvas();

        mainPane = new Pane(canvas);

        Scale scale = new Scale(1.0, 1.0);
        mainPane.getTransforms().addAll(scale);

        ScrollPane scrollPane = new ScrollPane(new Group(mainPane));

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setMaxSize(2000, 1000);

        final Background globalBackground = new Background(new BackgroundFill(Color.rgb(240, 240, 255), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(globalBackground);

        if (effects) {
            Image image = new Image("old.jpg");
            BackgroundImage myBI = new BackgroundImage(image,
                    BackgroundRepeat.REPEAT,
                    BackgroundRepeat.REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
            mainPane.setBackground(new Background(myBI));
        }
        canvas.setWidth(1000);
        canvas.setHeight(500);

        VBox lines = drawLines(index, effects, decryption, edited);

        mainPane.getChildren().add(lines);
        getContent().addAll(scrollPane);



    }

    public static void simulatedImageSnapshot(int i, boolean effects, boolean decryption, boolean edited) {
        SimulatedImage p = new SimulatedImage(i, effects, decryption, edited);
        p.show(Main.myStage);
        p.snapshot(effects, decryption, edited);
        p.hide();
    }
    public static class SymbolStackPane extends StackPane {
        ImageView icon = new ImageView();
        Text pText = new Text();

        SymbolStackPane(boolean decryption, Key key){

            final Background globalBackground = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

            icon.setFitHeight(Utils.adjust(ICON_SIZE));
            icon.setPreserveRatio(true);

            StackPane psp = new StackPane(pText);
            psp.setMinHeight(Utils.adjust(20));
            psp.setBackground(globalBackground);

            StackPane.setMargin(pText, new Insets(Utils.adjust(0), 0, 0, 0));
            StackPane.setAlignment(pText, Pos.BOTTOM_CENTER);

            Region region = new Region();
            region.setMinHeight(Utils.adjust(3));
            VBox.setVgrow(region, Priority.ALWAYS);

            Region region2 = new Region();
            region2.setMinHeight(Utils.adjust(18));
            VBox.setVgrow(region2, Priority.ALWAYS);


            Pane iPane = new Pane(icon);


            VBox vBox = new VBox();

            vBox.getChildren().add(region2);
            vBox.getChildren().add(iPane);
            if (key.isKeyAvailable() && decryption) {
                vBox.getChildren().add(region);
                vBox.getChildren().add(psp);
            }


            StackPane.setAlignment(icon, Pos.CENTER);
            StackPane.setAlignment(pText, Pos.CENTER);

            getChildren().add(vBox);
            HBox.setMargin(this, new Insets(Utils.adjust(0), Utils.adjust(5), Utils.adjust(0), Utils.adjust(5)));

        }
        void update(Key key, List<String> decryptionSequence, int i, Rectangle r, boolean effects) {
            final Font pFont = Font.font("Verdana", FontWeight.BOLD,  Utils.adjust(24));
            final Font pFontSmall = Font.font("Verdana", FontWeight.BOLD,  Utils.adjust(16));
            pText.setFill(Color.RED);

            final String decryption = (decryptionSequence != null && decryptionSequence.size() > i) ? decryptionSequence.get(i) : "";

            Color color = (Color) r.getFill();

            final String colorString = color.toString();

            final Image image = Icons.getOrDefault(colorString, null);
            if (image != null) {
                if (effects) {
                    icon.setImage(ImageUtils.blackAndWhiteRandom(image));
                    Random d = new Random();
                    double height = icon.getFitHeight();
                    double width = height;
                    PerspectiveTransform trans = new PerspectiveTransform();
                    final double factor = 0.3;
                    trans.setUlx((factor / 2 - d.nextFloat() * factor) * width);
                    trans.setUly(0);
                    trans.setUrx(width + trans.getUlx());
                    trans.setUry(0);

                    trans.setLlx((factor / 2 - d.nextFloat() * factor) * width);
                    trans.setLly(height);

                    trans.setLrx(width - (factor / 2 - d.nextFloat() * factor) * width);
                    trans.setLry(height);

                    icon.setEffect(trans);

                    icon.setRotate(10 - 20 * d.nextFloat());

                    Scale scale = new Scale(1.1 - 0.2 * d.nextFloat(), 1.1 - 0.2 * d.nextFloat());
                    icon.getTransforms().addAll(scale);
                } else {
                    icon.setImage(image);
                }
            }

            String p = "";
            if (key.isKeyAvailable()) {
                p = decryption;
            }

            pText.setText(p.equals("_") ? "" : p);
            if (p.length() > 1) {
                pText.setFont(pFontSmall);
            } else {
                pText.setFont(pFont);
            }


        }

    }
    private static VBox drawLines(int index, boolean effects, boolean decryption, boolean edited) {
        VBox lines = new VBox();
        if (!edited) {
            ArrayList<Rectangle> allSymbols = new ArrayList<>();
            ArrayList<String> allDecryption = new ArrayList<>();
            ArrayList<ArrayList<Rectangle>> linesOfSymbols = Alignment.linesOfSymbols(index);
            for (ArrayList<Rectangle> lineOfSymbols : linesOfSymbols) {
                ArrayList<String> decryptionSequence = DetailedTranscriptionPane.decryptionSequence(lineOfSymbols);
                allSymbols.addAll(lineOfSymbols);
                allDecryption.addAll(decryptionSequence);
            }

            int size = 50;
            for (int z = 0; z < (allSymbols.size() + size - 1)/size; z++) {
                HBox line = symbolDisplayLine(Main.key, allSymbols.subList(z * size, Math.min(allSymbols.size(), (z + 1) * size)),
                        allDecryption.subList(z * size, Math.min(allSymbols.size(), (z + 1) * size)), effects, decryption);
                lines.getChildren().add(line);


            }
        } else {

            final Font pFont = Font.font("Verdana", FontWeight.BOLD, Utils.adjust(24));

            int l = 0;
            for (ArrayList<Rectangle> lineOfSymbols : Alignment.linesOfSymbols(index)) {
                ArrayList<String> decryptionSequence = DetailedTranscriptionPane.decryptionSequence(lineOfSymbols);
                HBox line = symbolDisplayLine(Main.key, lineOfSymbols, decryptionSequence, effects, decryption);
                lines.getChildren().add(line);
                if (decryption) {
                    String e = EditedRecord.get(TranscribedImage.image(index).filename, l);
                    if (e != null && !e.isEmpty()) {
                        Text t = new Text(e);
                        t.setFill(Color.BLUE);
                        t.setFont(pFont);
                        lines.getChildren().add(t);
                    }
                    l++;
                }
            }

        }
        return lines;
    }
    private static HBox symbolDisplayLine(Key key, List<Rectangle> lineOfSymbols, List<String> decryptionSequence, boolean effects, boolean decryption) {
        HBox line = new HBox();
        line.setSpacing(0);
        for (int i = 0; i < lineOfSymbols.size(); i++) {
            Rectangle r = lineOfSymbols.get(i);
            SymbolStackPane sp = new SymbolStackPane(decryption, key);
            sp.update(key, decryptionSequence, i, r, effects);
            line.getChildren().add(sp);
        }
        return line;
    }
    private void snapshot(boolean effects, boolean decryption, boolean edited) {
        int format = 1 + (edited ? 0 : 4) +(effects ? 0 : 2) + (decryption ? 0 : 1);
        FileUtils.snapshot("simulation", TranscribedImage.transcribedImages[index].filename.replaceAll("\\..*", "_format" + format), mainPane);

    }



}