import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

// simple class to act as the top-level Component in the GUI

/*todos:
 * - add a better control panel for pan/zoom/rotate
 * 		- especially a progress bar
 * - add an icon
 * - add keyboard control (requires learning the focus subsystem)
 * - add image saving
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
 *         - FractalLabel.java		: contains label/image; mostly re-implemented here, but also has APFloat and multithreading
 *         - ImageRow.java			: Runnable row of pixel calculations; also updates progress bar and checks if double precision is sufficient
 *         - PixelGenerator.java	: fractal and color calculation; everything Mandelbrot is re-implemented here
 *         - Complex.java			: complex-number class I wrote for Multibrot 
 *         - FourierTransform.java	: fourier transform of audio file for Newton fractal
 *         - FractalRGB.java		: trivial class, forget what exactly it was for
 *         - FractalSTL.java		: generates an STL file from the contents of a FractalLabel, for 3D printing
 *         
 *     - D:\Callan\Backup dump\Documents\School\Past\Nepean\Programming\mandelbrot.txt (OG grade 12 version, 100 lines of C++)
 *         - very basic, doesn't have anything that isn't implemented here
 */

public class FractalGUI extends JFrame {
	private static final long serialVersionUID = -7891287854194431851L;
	
	// components
	FractalLabel label;
	JPanel controlPanel;

	FractalGUI(){
		// initialize JFrame
		super("fractal");
		setSize(400, 400);
		setLocation(80, 60);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){ e.printStackTrace(); }
		
		// add components
		setLayout(new BorderLayout());
		
		label = new FractalLabel();
		add(label, BorderLayout.CENTER);
		
		// controlPanel holds all of the Components for controlling the fractal; can be removed with setVisible()
		controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		JTextField placeholderText = new JTextField("Placeholder");
		placeholderText.setEditable(false);
		controlPanel.add(placeholderText, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.EAST);
		
		// initialize components (before showing frame)
		addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e) {
		        label.abort();
		    }
		});
		
		// show frame
		setVisible(true);
		
		// initialize components (after showing frame)
		label.updateFractal();
		label.updateImage();
	}

	public static void main(String[] args) {
		new FractalGUI();
	}
}