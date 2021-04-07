import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

// class which is Component showing the rendered image, and which also contains the view parameters and methods for interaction

/* todos:
 * - add keyboard control (requires learning the focus subsystem)
 * - encapsulate the calculation of pixel values
 * - add customizable coloring
 *     - add color map customization
 *     - add scaling customization
 * - multithread the calculation of pixel values
 * - add antialiasing
 * - add abort capability (on button-press, new update call, or close)
 */

public class FractalLabel extends JLabel {
	private static final long serialVersionUID = 6834230068761778027L;

	// fractal math parameters
	int maxIter;
	double escapeRad;
	
	// view parameters
	double centerX;
	double centerY;
	double width;
	double rotation;

	// fractal data and objects to show image
	double[][] fractal;
	BufferedImage image;
	ImageIcon icon;
	
	// timer to call fractal update at a delay after last action
	Timer updateTimer;
	
	// pretty colors
	static int[][] defaultGradient = {
		{4, 4, 73},
		{0, 7, 100},
		{12, 44, 138},
		{24, 82, 177},
		{57, 125, 209},
		{134, 181, 229},
		{211, 236, 248},
		{241, 233, 191},
		{248, 201, 95},
		{255, 170, 0},
		{204, 128, 4},
		{153, 87, 0},
		{106, 52, 3},
		{66, 30, 15},
		{25, 7, 26},
	};
	
	// color log scaling parameters
	static double logScalingA = 6.36, logScalingB = 0.05363;
	
	FractalLabel(){
		// set alignment and background (to achieve desired behaviour during resize)
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setBackground(Color.BLACK);
		setOpaque(true);
		
		// add components
		icon = new ImageIcon();
		setIcon(icon);
		
		//initialize fractal parameters
		maxIter = 1000;
		escapeRad = 420.69;
		
		//initialize view parameters
		centerX = -0.69;
		centerY = 0;
		width = 5;
		rotation = 0;

		//add listeners
		addComponentListener(new ComponentAdapter() {
			// on window resize, start update timer
			public void componentResized(ComponentEvent e) {
				System.out.println(String.format("FractalLabel resized (%s, %s)", getWidth(), getHeight()));
				updateTimer.restart();
			}
		});
		
		addMouseListener(new MouseAdapter() {
			// on click, center clicked point and start update timer
			public void mouseClicked(MouseEvent e) {
				Point2D.Double newCenter = getFractalXY(e.getPoint());
				centerX = newCenter.getX();
				centerY = newCenter.getY();
				System.out.println(String.format("FractalLabel center changed (%s, %s)", centerX, centerY));
				updateTimer.restart();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			// on mouse scroll, zoom accordingly and start update timer
			public void mouseWheelMoved(MouseWheelEvent e) {
				width *= Math.pow(2, e.getWheelRotation());
				System.out.println(String.format("FractalLabel width changed (%s)", width));
				updateTimer.restart();
			}
		});
		
		updateTimer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// at a delay after the last call to update timer, update fractal
				System.out.print("Updating fractal... ");
				updateFractal();
				System.out.print("done\nUpdating image... ");
				updateImage();
				System.out.println("done");
			}
		});
		updateTimer.setRepeats(false);
	}
	
	// recreate fractal array to match current window size and fractal and view parameters
	void updateFractal() {
		if(fractal == null || fractal.length != getWidth() || fractal[0].length != getHeight()) {
			fractal = new double[getWidth()][getHeight()];
		}
		for(int imageX = 0; imageX < fractal.length; imageX++) {
			for(int imageY = 0; imageY < fractal[0].length; imageY++) {
				fractal[imageX][imageY] = smoothedMandelbrotIterations(getFractalXY(new Point(imageX, imageY)));
			}
		}
	}

	// recreate fractal image to match fractal array
	void updateImage() {		
		image = new BufferedImage(fractal.length, fractal[0].length, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < fractal.length; i++) {
			for(int j = 0; j < fractal[0].length; j++) {
				image.setRGB(i, j, defaultColorRGB(fractal[i][j]));
			}
		}
		
		icon.setImage(image);
		updateUI();
	}
	
	// convert a point in image space to fractal space
	Point2D.Double getFractalXY(Point imageXY){
		double dx = (((double)imageXY.getX())/fractal.length - 0.5)*width;
		double dy = -(((double)imageXY.getY()) - 0.5*fractal[0].length)*width/fractal.length;
		return new Point2D.Double(
				centerX + dx*Math.cos(rotation) - dy*Math.sin(rotation),
				centerY + dx*Math.sin(rotation) + dy*Math.cos(rotation)
		);
	}
	
	// convert a point from fractal space to image space
	Point getImageXY(Point2D.Double fractalXY){
		double dxt = fractalXY.getX() - centerX;
		double dy = fractalXY.getY() - centerY;
		double dx = dxt*Math.cos(rotation) + dy*Math.sin(rotation);
		dy = -dxt*Math.sin(rotation) + dy*Math.cos(rotation);
		return new Point(
				(int)(fractal.length*(0.5 + dx/width) + 0.5),
				(int)(0.5*fractal[0].length - dy*fractal.length/width + 0.5)
		);
	}
	
	// calculate the smoothed number of iterations for escape for a point in the Mandelbrot fractal
	double smoothedMandelbrotIterations(Point2D.Double z0) {
		double r0 = z0.getX();
		double i0 = z0.getY();
		
		// initialize iteration number, set to max if z0 is known to be in the set
		int n = 0;
		if(mandelTest(r0, i0)) {
			n = maxIter;
		}
		
		// iterate z_{n+1} = z_n^2 + z0
		double r = 0;
		double i = 0;
		while(n < maxIter && r*r + i*i < escapeRad){
			double rt = r;
			r = rt*rt - i*i + r0;
			i = 2*rt*i + i0;
			n++;
		}
		
		// return scaled iteration number
		if(n < maxIter) {
			// scale based on how far the point escaped on final iteration
			return n + 1 - Math.log(0.5*Math.log(r*r + i*i)/Math.log(2))/Math.log(2);
		}
		else {
			return n;
		}		
	}
	
	// check if a point is in the simple period-1, 2 regions of the Mandelbrot set
	boolean mandelTest(double r, double i){
		double rMinus = r - 0.25;
		double rPlus = r + 1;
		double iSquared = i*i;
		double q = rMinus*rMinus + iSquared;
		return q*(q + rMinus) < 0.25*iSquared || rPlus*rPlus + iSquared < 0.0625;
	}
	
	// convert iteration number to color RGB code
	int defaultColorRGB(double n) {
		// escaped: black
		if(n == maxIter) {
			return Color.BLACK.getRGB();
		}
		
		// log scaling for better coloring when zooming in
		n = logScalingA*Math.log(logScalingB*n + 1);// should probably change to A*log(B*(n + 1)) + C
		
		// interpolate color based on scaled n
		int l = defaultGradient.length;
		int n1 = ((int)n)%l;
		int n2 = ((int)n+1)%l;
		double w = n%1;
		
		int[] rgb = new int[3];
		for(int i=0; i<3; i++)
			rgb[i] = (int)((1-w)*defaultGradient[n1][i] + w*defaultGradient[n2][i]);
		
		return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
	}
}