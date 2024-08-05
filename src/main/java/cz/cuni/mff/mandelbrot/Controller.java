package cz.cuni.mff.mandelbrot;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

/**
 * Controller class handles the GUI.
 * It controls every input from user from GUI. The following methods
 * are called in fxml file.
 *
 * @author Filip Cizmar
 * @version 1.0
 * @see Initializable
 *
 */
public class Controller implements Initializable {

    /**
     * Initializes the controller.
     * Sets the default set size according to size of application window,
     * the text fields in GUI and sliders range and their listeners.
     * Fills the canvas with the set with initial parameters.
     *
     * @param url initial param
     * @param rb  initial param
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resetCanvas();
        Global.height = (int) canvas.getHeight();
        Global.width = (int) canvas.getWidth();

        Set.setConstruct();
        textFieldIterations.setText(String.valueOf(Global.getIterations()));
        textFieldZoom.setText(String.valueOf(Global.getZoom()));

        RSlider.setValue(100);
        RSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ControllerCode.RSlider_val = RSlider.getValue() / 100;
            fillCanvas();
        });

        GSlider.setValue(100);
        GSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ControllerCode.GSlider_val = GSlider.getValue() / 100;
            fillCanvas();
        });

        BSlider.setValue(100);
        BSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ControllerCode.BSlider_val = BSlider.getValue() / 100;
            fillCanvas();
        });

        ZSlider.setValue(0);
        ZSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(0);
            nf.setGroupingUsed(false);
            long newZoom = ControllerCode.zoomBySlider(ZSlider.getValue());
            textFieldZoom.setText(nf.format(newZoom));
            textFieldIterations.setText(String.valueOf(Global.countIterations(newZoom)));
        });

        fillCanvas();
    }

    /**
     * Handles any change in iteration text field.
     * Set flag used in {@link #buttonRedrawOnAction()} that let it know if new
     * base iterations value is needed.
     * If input cannot by parse to integer nothing happens.
     */
    @FXML
    public void textFieldIterationsKeyTyped() {
        try {
            iterationsChanged = true;
        } catch (Exception e) {
        }
    }

    /**
     * Handles any change in zoom text field.
     * Recompute the set according to given value.
     * If input cannot by parse to integer nothing happens.
     */
    @FXML
    public void textFieldZoomKeyTyped() {
        try {
            long zoom = Long.parseLong(textFieldZoom.getText());
            if (zoom < 1)
                return;
            textFieldIterations.setText(String.valueOf(Global.countIterations(zoom)));
            ZSlider.setValue(ControllerCode.sliderByZoom(zoom));
            textFieldZoom.positionCaret(textFieldZoom.getText().length());
        } catch (Exception e) {
        }
    }

    /**
     * Provides translation of the set.
     * The set is not recomputed on each drag event.
     *
     * @param mouseEvent provides position of mouse on drag event
     */
    @FXML
    public void canvasMouseDragged(MouseEvent mouseEvent) {
        resetCanvas();
        ControllerCode.actualTranslationX += ControllerCode.mouse_lastX - (int) mouseEvent.getX();
        ControllerCode.actualTranslationY += ControllerCode.mouse_lastY - (int) mouseEvent.getY();
        fillDraggedCanvas();
        ControllerCode.upDateMouse(mouseEvent);
    }

    /**
     * Reset actual position of mouse.
     *
     * @param mouseEvent provides position of mouse on drag event
     */
    @FXML
    public void canvasOnMousePressed(MouseEvent mouseEvent) {
        ControllerCode.upDateMouse(mouseEvent);
    }

    /**
     * Recompute the set according to actual translation.
     */
    @FXML
    public void canvasOnMouseRelease() {
        ControllerCode.addToAbsoluteTranslationAndNullActual();
        Set.setConstruct();
        fillCanvas();
    }

    /**
     * Provides zooming on scroll.
     * If scroll event occurs stop computing of the new set, sets zoom variable
     * according to actual zoom and start computation of the new set.
     *
     * @param scrollEvent provides actual value of scroll on scroll event
     */
    @FXML
    public void canvasOnScroll(ScrollEvent scrollEvent) {
        Set.killThreads();
        ControllerCode.actualZoom += scrollEvent.getDeltaY();
        resetCanvas();
        textFieldZoom.setText(String.valueOf(Global.getZoom()));
        ControllerCode.zoom();
        textFieldIterations.setText(String.valueOf(Global.countIterations()));
        Set.setConstruct();
        ZSlider.setValue(ControllerCode.sliderByZoom(Global.getZoom()));
        fillDraggedCanvas();
    }

    /**
     * Recompute the set on button press.
     * It takes values from text fields.
     */
    @FXML
    public void buttonRedrawOnAction() {
        long zoom = Long.parseLong(textFieldZoom.getText().replaceAll("\\s+", ""));
        if (zoom < 1)
            zoom = 1;
        Global.setSize(zoom);

        if (iterationsChanged) {
            int it = Integer.parseInt(textFieldIterations.getText());
            Global.setIterations(it);
        }

        Global.pointX = Double.parseDouble(textFieldRe.getText());
        Global.pointY = Double.parseDouble(textFieldIm.getText());

        Set.setConstruct();
        fillCanvas();
    }

    /**
     * Fills each pixel of canvas with appropriate color.
     * And sets right value of center of the set to the text fields.
     */
    private void fillCanvas() {
        textFieldRe.setText(String.valueOf(Global.pointX));
        textFieldIm.setText(String.valueOf(Global.pointY));

        for (int i = 0; i < Global.width; ++i) {
            for (int j = 0; j < Global.height; ++j) {
                double realValue = Set.getValue(i, j);
                ControllerCode.valueToColor(realValue);
                canvas.getGraphicsContext2D().getPixelWriter().setColor(i, j, ControllerCode.valueToColor(realValue));
            }
        }
        iterationsChanged = false;
    }

    /**
     * Fills canvas with translation.
     */
    private void fillDraggedCanvas() {
        for (int i = 0; i < Global.width; ++i) {
            for (int j = 0; j < Global.height; ++j) {
                double realValue = Set.getValueError(i + ControllerCode.actualTranslationX,
                        j + ControllerCode.actualTranslationY);
                if (realValue == -1)
                    continue;

                ControllerCode.valueToColor(realValue);
                canvas.getGraphicsContext2D().getPixelWriter().setColor(i, j, ControllerCode.valueToColor(realValue));
            }
        }
    }

    /**
     * Fills canvas with white color.
     */
    private void resetCanvas() {
        canvas.getGraphicsContext2D().setFill(Color.rgb(253, 253, 253));
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private boolean iterationsChanged = false;

    @FXML
    private Canvas canvas;

    @FXML
    private TextField textFieldRe;

    @FXML
    private TextField textFieldIm;

    @FXML
    private TextField textFieldIterations;

    @FXML
    private TextField textFieldZoom;

    @FXML
    private Slider RSlider;

    @FXML
    private Slider GSlider;

    @FXML
    private Slider BSlider;

    @FXML
    private Slider ZSlider;
}
