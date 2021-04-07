import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

// simple class to act as the top-level Component in the GUI

/*todos:
 * - add a better control panel for pan/zoom/rotate
 * 		- especially a progress bar
 * - add image saving
 * - add precise framing (place a frame in view, then use it to set view params)
 * - add an icon
 */

/* old files:
 *     - C:\Users\Callan\Documents\Code\Java\OldWorkspace\FractalGenerator\src (started a re-write, didn't get very far)
 *         - FractalGUI.java		: top-level Component; everything is re-implemented here
 *         - FractalLabel.java		: contains label/image; mostly re-implemented here, but also has antialiasing and multithreading
 *         - FractalGenerator.java	: multithreaded Mandelbrot calculator; mostly re-implemented here, but also has multithreading
 *         - FractalColorModel.java	: gradient and scaling; everything is re-implemented here
 *         
 *     - D:\Callan\Backup dump\Documents\Programming\Java\workspace\MandelbrotGenerator\src (older and more complete version)
 *         - FractalGUI.java		: ?
 *         - FractalLabel.java		: ?
 *         - FractalRGB.java		: ?
 *         - Complex.java			: ?
 *         - ImageRow.java			: ?
 *         - PixelGenerator.java	: ?
 *         - FourierTransform.java	: ?
 *         - FractalSTL.java		: ?
 *         
 *     - D:\Callan\Backup dump\Documents\School\Past\Nepean\Programming\mandelbrot.txt (OG grade 12 version, 100 lines of C++)
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
		
		// add components
		setLayout(new BorderLayout());
		
		label = new FractalLabel();
		add(label, BorderLayout.CENTER);
		
		controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		JTextField placeholderText = new JTextField("Placeholder");
		placeholderText.setEditable(false);
		controlPanel.add(placeholderText, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.EAST);
		
		// initialize components (before showing frame)
		
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