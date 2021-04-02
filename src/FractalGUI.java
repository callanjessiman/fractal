import javax.swing.JFrame;

public class FractalGUI extends JFrame {
	private static final long serialVersionUID = -7891287854194431851L;

	FractalGUI(){
		// initialize JFrame
		super("fractal");
		setSize(400, 400);
		setLocation(80, 60);
		
		// add components
		
		// set frame visible
		setVisible(true);
		
		// apply initial update of components
	}

	public static void main(String[] args) {
		new FractalGUI();
	}
}
