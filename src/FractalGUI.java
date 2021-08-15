import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/* class FractalGUI:
 * - extends JFrame to act as the top-level Component in the GUI
 *     - creates and adds sub-Components
 * - contains main() for launching the GUI
 */

/* TODO:
 * - add a better control panel for pan/zoom/rotate/etc
 *     - improve layout
 * - add keyboard control (requires learning the focus subsystem)
 * - add image saving
 *     - try to add fractal information in image metadata (use ImageReader/ImageWriter)
 * - add precise framing (place a frame in view, then use it to set view params)
 */

/* old files:
 *     - C:\Users\Callan\Documents\Code\Java\OldWorkspace\FractalGenerator\src (started a re-write, didn't get very far)
 *         - FractalGUI.java		: top-level Component; everything is re-implemented here
 *         - FractalLabel.java		: contains label/image; mostly re-implemented here, but also has antialiasing
 *         - FractalGenerator.java	: multithreaded Mandelbrot calculator; everything is re-implemented here
 *         - FractalColorModel.java	: gradient and scaling; everything is re-implemented here
 *         
 *     - D:\Callan\Backup dump\Documents\Programming\Java\workspace\MandelbrotGenerator\src (older and more complete version)
 *         - FractalGUI.java		: top-level Component; mostly re-implemented here, but also has a good control panel and menu bar
 *         - FractalLabel.java		: contains label/image; mostly re-implemented here, but also has arbitrary precision
 *         - ImageRow.java			: Runnable row of pixel calculations; also updates progress bar and decides between double and arbitrary precision
 *         - PixelGenerator.java	: fractal and color calculation; everything Mandelbrot is re-implemented here, but has somewhat better color scaling
 *         - Complex.java			: complex-number class I wrote for Multibrot
 *         - FourierTransform.java	: Fourier transform of audio file for Newton fractal
 *         - FractalRGB.java		: trivial class, forget what exactly it was for
 *         - FractalSTL.java		: generates an STL file from the contents of a FractalLabel, for 3D printing
 *         
 *     - D:\Callan\Backup dump\Documents\School\Past\Nepean\Programming\mandelbrot.txt (OG grade 12 version, 100 lines of C++)
 *         - very basic, doesn't have anything that isn't implemented here
 */

public class FractalGUI extends JFrame {
	private static final long serialVersionUID = -7891287854194431851L; // auto-generated Component ID
	
	// Components
	FractalLabel label;			// label evaluates and displays the fractal and image, and stores and modifies the view parameters
	
	JPanel controlPanel;		// controlPanel should contain a progress bar and Components for modifying the view parameters (note: hide with setVisible())
	JSpinner iterationSpinner;	// increase/decrease max iterations
	JProgressBar progressBar;	// to track calculation progress

	// constructor: initialize the JFrame and its Components
	FractalGUI(){
		// initialize generic JFrame properties
		super("Fractal");
		setSize(520, 360);
		setLocation(80, 60);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			setIconImage(ImageIO.read(new File("images/icon.png")));
		} catch (IOException e) { e.printStackTrace(); }
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) { e.printStackTrace(); }
		
		// add Components
		setLayout(new BorderLayout());
		
		label = new FractalLabel(this);
		add(label, BorderLayout.CENTER);
		
		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(Box.createVerticalGlue());

		iterationSpinner = new JSpinner(new SpinnerNumberModel(4000, 1000, 100000, 1000));
		iterationSpinner.setPreferredSize(new Dimension(80, 20));
		iterationSpinner.setMaximumSize(new Dimension(80, 20));
		iterationSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0){
				label.maxIter = (int)iterationSpinner.getValue();
				label.updateTimer.restart();
			}			
		});
		controlPanel.add(iterationSpinner);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(80, 20));
		progressBar.setMaximumSize(new Dimension(80, 20));
		progressBar.setStringPainted(true);
		controlPanel.add(progressBar);
		
		controlPanel.add(Box.createVerticalGlue());
		
		add(controlPanel, BorderLayout.EAST);
		
		// initialize components (before showing frame)
		addWindowListener(new WindowAdapter(){
			// abort any calculations if the window is closed
		    public void windowClosing(WindowEvent e) {
		        label.abort();
		    }
		});
		
		// show the GUI
		setVisible(true);
		
		// initialize components (after showing frame)
		// currently, building the frame creates a ResizeEvent that does these automatically
		// label.updateFractal();
		// label.updateImage();
	}

	public static void main(String[] args) {
		new FractalGUI();
	}
}