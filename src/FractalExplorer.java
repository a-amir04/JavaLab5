import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;


// FractalExplorer отслеживает несколько важных полей для состояния программ
public class FractalExplorer {
    // Размер экрана
    private final int displaySize;
    // Ссылка JImageDisplay, для обновления отображения в разных методах в процессе вычисления фрактала.
    private JImageDisplay imageDisplay;
    // Объект FractalGenerator. Будет использоваться ссылка на базовый
    // класс для отображения других видов фракталов в будущем.
    private FractalGenerator fractalGenerator;
    // Объект Rectangle2D.Double, указывающий диапазона комплексной
    // плоскости, которая выводится на экран.
    private Rectangle2D.Double range;
    private JComboBox<FractalGenerator> comboBox;

    /*
    Конструктор, который принимает значение размера отображения в качестве аргумента,
    затем сохраняет это значение в соответствующем поле, а также инициализирует
    объекты диапазона и фрактального генератора.
    */
    private FractalExplorer(int displaySize) {
        this.displaySize = displaySize;
        this.fractalGenerator = new Mandelbrot();
        this.range = new Rectangle2D.Double(0, 0, 0, 0);
        fractalGenerator.getInitialRange(this.range);
    }

    public static void main(String[] args) {
    FractalExplorer fractalExplorer = new FractalExplorer(600);
    fractalExplorer.setGUI();
    fractalExplorer.drawFractal();
    }

    /*
    Метод setGUI(), которыйинициализирует
    графическийинтерфейс Swing: JFrame, содержащийобъектJimageDisplay, и
    кнопкудлясбросаотображения, атакжесохранения.
    */
    public void setGUI() {
        JFrame frame = new JFrame("Fractal Generator");
        JButton buttonReset = new JButton("Reset");
        JButton buttonSave = new JButton("Save image");
        JPanel jPanel_1 = new JPanel();
        JPanel jPanel_2 = new JPanel();
        JLabel label = new JLabel("Fractal:");

        imageDisplay= new JImageDisplay(displaySize, displaySize);
        imageDisplay.addMouseListener(new MouseListener());

        // Выпадающий список.
        comboBox= new JComboBox<>();
        comboBox.addItem(new Mandelbrot());
        comboBox.addItem(new Tricorn());
        comboBox.addItem(new BurningShip());
        comboBox.addActionListener(new ActionHandler());

        // Кнопка reset.
        buttonReset.setActionCommand("Reset");
        buttonReset.addActionListener(new ActionHandler());

        // Кнопка сохранить.
        buttonSave.setActionCommand("Save");
        buttonSave.addActionListener(new ActionHandler());

        jPanel_1.add(label, BorderLayout.CENTER);
        jPanel_1.add(comboBox, BorderLayout.CENTER);
        jPanel_2.add(buttonReset, BorderLayout.CENTER);
        jPanel_2.add(buttonSave, BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(imageDisplay, BorderLayout.CENTER);
        frame.add(jPanel_1, BorderLayout.NORTH);
        frame.add(jPanel_2, BorderLayout.SOUTH);

        // Закрытия окна поумолчанию.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
        Данные операции правильно разметят содержимое окна, сделают его
        видимыми затем запретят изменение размеров окна:
        */
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        }

        // Отрисовка фрактала в JImageDisplay.
    private void drawFractal() {
        for (int x = 0; x <displaySize; x++) {
            for (int y = 0; y <displaySize; y++) {
                int counter = fractalGenerator.numIterations(FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x),
                FractalGenerator.getCoord(range.y, range.y+ range.width, displaySize, y));
            if (counter == -1) {
                imageDisplay.drawPixel(x, y, 0);
            }
            else {
                float hue = 0.7f + (float) counter / 200f;
                int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                imageDisplay.drawPixel(x, y, rgbColor);
            }
            }
        }
        imageDisplay.repaint();
    }

    // Обработчик кнопок.
    public class ActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Reset")) {
                // Перерисовка фрактала.
                fractalGenerator.getInitialRange(range);
                drawFractal();
            }
            else if (e.getActionCommand().equals("Save")) {
                // Сохранение.
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("PNG Images", "png");
                fileChooser.setFileFilter(fileFilter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int t = fileChooser.showSaveDialog(imageDisplay);
                if (t == JFileChooser.APPROVE_OPTION) {
                    try {
                        ImageIO.write(imageDisplay.getImage(), "png", fileChooser.getSelectedFile());
                    } catch (NullPointerException | IOException ee) {
                        JOptionPane.showMessageDialog(imageDisplay, ee.getMessage(), "Cannot save image", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            else {
                fractalGenerator= (FractalGenerator) comboBox.getSelectedItem();
                range = new Rectangle2D.Double(0,0,0,0);
                fractalGenerator.getInitialRange(range);
                drawFractal();
            }
        }
    }
    // Внутренний класс для обработки событий java.awt.event.MouseListener с дисплея.
    public class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            double x = FractalGenerator.getCoord(range.x, range.x+ range.width, displaySize, e.getX());
            double y = FractalGenerator.getCoord(range.y, range.y+ range.width, displaySize, e.getY());
            fractalGenerator.recenterAndZoomRange(range, x, y, 0.5);
            drawFractal();
        }
    }
}
