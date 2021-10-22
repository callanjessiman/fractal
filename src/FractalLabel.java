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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/* class FractalLabel:
 * - extends JLabel to act as Component containing the rendered image
 *     - contains as fields the BufferedImage and the array from which it is rendered
 *         - contains methods to populate the array by calling FractalCalculator methods, and to render the array into a image
 *             - contains objects to multithread these calculations
 *             - contains an abort() method to halt these calculations
 *         - contains fields describing the current fractal calculation and framing
 *         - contains methods for transforming between image space (pixel indices) and fractal space (2D Cartesian coordinates)
 *     - contains listeners to change the framing when the JLabel is interacted with
 * - contains static fields/methods for converting floating-point fractal values into colours
 */

/* TODO:
 * - add reuse of previous calculations where possible
 * - try exploiting the set's connectedness to avoid calculating values that escape
 * - add option to use less threads
 * - add antialiasing (MSAA, but possibly also over/undersampling)
 *     - try adaptive antialiasing, where sampling goes until a certain precision is reached
 * - figure out how to do coloring properly by examining how iteration counts change from one repeated pattern to the next
 * - add customizable and encapsulated coloring
 *     - add color map customization
 *     - add scaling customization
 */

public class FractalLabel extends JLabel {
	// auto-generated Component ID
	private static final long serialVersionUID = 6834230068761778027L;
	
	// antialiasing strategies
	static final int ANTIALIAS_NONE = 0;
	static final int ANTIALIAS_RANDOM = 1;
	
	// miscellaneous static fields
	static int UPDATE_DELAY = 100;		// time to wait after last GUI input before updating
	static int N_RUNNABLES = 4000;		// efficiency seems to plateau between 1k and 10k Runnables (for 1MP of fractal mostly reaching maxiter = 1000)
	static int PROGRESS_DELAY = 100;	// time between progress updates
	
	// static fields for default colour scheme
	static double logScalingA = 168, logScalingB = 0.05, logScalingC = 19;	// log-scaling parameters (n -> A*log(B*(n + C)))
	static int[][] defaultGradient = {										// cyclic colour gradient (RGB)
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
	};

	// fractal math parameters
	int maxIter;		// maximum number of iterations to attempt
	double escapeRad;	// radius from origin beyond which a point is considered to have escaped
	
	// framing parameters in fractal space
	double centerX;		// x or real coordinate of center of view
	double centerY;		// y or imaginary coordinate of center of view
	double width;		// width of view
	double rotation;	// rotation angle (CCW camera rotation, or CW fractal rotation) (rad)

	// fractal data and objects to show image
	int widthPixels;		// pixel size of fractal to calculate
	int heightPixels;
	double[][] fractal;		// array to store floating-point fractal values calculated for a grid of points
	BufferedImage image;	// image to display the fractal; same size as and calculated from fractal array
	ImageIcon icon;			// icon for displaying the image
	
	// to track calculation progress
	int[] progressCounter;	// number of points calculated (in array for call-by-reference)
	Timer progressTimer;	// timer to report calculation progress
	FractalGUI gui;			// reference back to GUI (for progress bar)
	
	// objects to handle multithreaded calculation
	Timer updateTimer;							// Timer to call fractal update at a delay after last user input
	ThreadFactory fractalThreadFactory;			// ThreadFactory to create threads for fractal calculation
	SwingWorker<Object, Object> updateWorker;	// Worker to execute updates in a separate, stoppable process
	ExecutorService fractalThreadPool;			// ThreadPool to handle parallel calculation of fractal values
	
	// antialiasing
	int antialiasingType;
	Number antialiasingFactor;
	Point2D.Double[][] randomAntialiasPoints;
	// TODO: when using antialising, change
	//     - fractal array size
	//     - points passed to runnable
	//     - image drawing
	
	// constructor: initialize fractal/framing parameters, JLabel, calculation objects, and listeners
	FractalLabel(FractalGUI g){// TODO: clean up style
		// set alignment and background so resizing looks nice
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setBackground(Color.BLACK);
		setOpaque(true);
		
		// add Components
		icon = new ImageIcon();
		setIcon(icon);
		gui = g;
		
		// initialize math parameters
		maxIter = 256;
		escapeRad = 420.69;
		antialiasingType = ANTIALIAS_RANDOM;
		antialiasingFactor = 4;
		
		// initialize framing parameters
		centerX = -0.69420;
		centerY = 0;
		width = 5;
		rotation = 0;
		
		// initialize objects to support calculation
		fractalThreadFactory = new ThreadFactory(){
			public Thread newThread(Runnable r){
				Thread t = new Thread(r);
				t.setPriority(Thread.MIN_PRIORITY);// priority is 1-10
				return t;
			}
		};
		
		updateTimer = new Timer(UPDATE_DELAY, new ActionListener() {
			// after a delay, abort any previous update and start a new update
			public void actionPerformed(ActionEvent e) {
				updateWorker = new SwingWorker<Object, Object>(){
			        protected Object doInBackground(){
						updateFractal();
						updateImage();
						return null;
			        }
			    };
				updateWorker.execute();
			}
		});
		updateTimer.setRepeats(false);
		
		loadRandomAntialiasPoints();
		
		// initialize progress reporting
		progressCounter = new int[1];
		
		progressTimer = new Timer(PROGRESS_DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reportProgress();
			}
		});

		// add listeners
		addComponentListener(new ComponentAdapter() {
			// on window resize, start update timer
			public void componentResized(ComponentEvent e) {
				System.out.println(String.format("FractalLabel resized (%s, %s)", getWidth(), getHeight()));
				abort();
				updateTimer.restart();
			}
		});
		
		addMouseListener(new MouseAdapter() {
			// on click, center frame on clicked point and start update timer
			public void mouseClicked(MouseEvent e) {
				Point2D.Double newCenter = getFractalXY(e.getPoint());
				centerX = newCenter.getX();
				centerY = newCenter.getY();
				System.out.println(String.format("FractalLabel center changed (%s, %s)", centerX, centerY));
				abort();
				updateTimer.restart();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			// on mouse scroll, zoom accordingly and start update timer
			public void mouseWheelMoved(MouseWheelEvent e) {
				width *= Math.pow(2, e.getWheelRotation());
				System.out.println(String.format("FractalLabel width changed (%s)", width));
				abort();
				updateTimer.restart();
			}
		});
	}
	
	// load list of random points for antialiasing
	// TODO: this should handle exceptions better
	void loadRandomAntialiasPoints() {
		try {
			List<String> fileLines = Files.readAllLines(Paths.get("assets/randompoints/points.txt"), StandardCharsets.UTF_8);
			randomAntialiasPoints = new Point2D.Double[fileLines.size()][];
			for(int i = 0; i < fileLines.size(); i++) {
				String[] pointStrings = fileLines.get(i).split(" ");
				randomAntialiasPoints[i] = new Point2D.Double[pointStrings.length];
				for(int j = 0; j < pointStrings.length; j++) {
					String[] coordinateStrings = pointStrings[j].split(",");
					randomAntialiasPoints[i][j] = new Point2D.Double(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
				}
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	// recreate fractal array to match current window size and fractal/framing parameters, then calculate its contents
	void updateFractal() {
		System.out.println("Updating fractal...");
		long startTime = System.currentTimeMillis();
		progressCounter[0] = 0;
		
		// initialize fractalThreadPool
		int nThreads = Runtime.getRuntime().availableProcessors(); // efficiency seems to increase with nThreads until nThreads = [CPU nThreads], and slowly decrease above that
		fractalThreadPool = Executors.newFixedThreadPool(nThreads, fractalThreadFactory);
		
		// redeclare fractal array with size based on antialiasing type
		widthPixels = getWidth();
		heightPixels = getHeight();
		switch (antialiasingType) {
			case ANTIALIAS_NONE:
				fractal = new double[widthPixels][heightPixels];
				break;
			case ANTIALIAS_RANDOM:
				fractal = new double[widthPixels*(int)antialiasingFactor][heightPixels]; // should react if factor isn't an integer > 1
				break;
			default:
				System.err.println(String.format("Warning: antialiasingType %s not understood; using ANTIALIAS_NONE"));
				antialiasingType = ANTIALIAS_NONE;
				fractal = new double[widthPixels][heightPixels];
				break;
		}

		Point2D.Double zeros = getFractalXY(new Point(0, 0));
		Point2D.Double ones = getFractalXY(new Point(1, 1));
		Point2D.Double twos = getFractalXY(new Point(2, 2));
		if(zeros.x == ones.x || zeros.y == ones.y || ones.x == twos.x || ones.y == twos.y) {
			System.out.println("Warning: double precision insufficient");
		}
		
		for(int i=0; i<fractal.length; i++) {
			for(int j=0; j<fractal[0].length; j++) {
				fractal[i][j] = FractalCalculator.NOT_CALCULATED;
			}
		}
		
		// make a list of indices in fractal array that require calculation
		ArrayList<Point> allIndices = new ArrayList<Point>();
		for(int i=0; i<fractal.length; i++) {
			for(int j=0; j<fractal[0].length; j++) {
				if(fractal[i][j] == FractalCalculator.NOT_CALCULATED) {
					allIndices.add(new Point(i, j));
				}
			}
		}
		
		gui.progressBar.setMaximum(allIndices.size());
		
		// start reporting progress
		reportProgress();
		progressTimer.start();
		
		// divide calculations evenly between Runnables
		int pointsPerRunnable = allIndices.size()/N_RUNNABLES;	// all Runnables calculate this many points
		int leftover = allIndices.size() %N_RUNNABLES;			// this many Runnables calculate one extra point
		for(int p = 0; p < N_RUNNABLES; p++) {
			// figure out which indices to calculate in this Runnable
			int start = pointsPerRunnable*p + Math.min(p, leftover);	// first index in allIndices for this Runnable
			int end = start + pointsPerRunnable;						// last index
			if(p < leftover) {
				end += 1;
			}
			
			// build and execute Runnable
			int[][] indices = new int[end - start][2];					// this Runnable's sub-array of allIndices
			Point2D.Double[] points = new Point2D.Double[end - start];	// this Runnable's points in fractal space
			for(int i1d = start; i1d < end; i1d++) {
				indices[i1d - start][0] = allIndices.get(i1d).x;
				indices[i1d - start][1] = allIndices.get(i1d).y;
				switch (antialiasingType) {
					case ANTIALIAS_NONE:
						points[i1d - start] = getFractalXY(new Point(indices[i1d - start][0], indices[i1d - start][1]));
						break;
					case ANTIALIAS_RANDOM:
						Point2D.Double pixelCenter = getFractalXY(new Point(indices[i1d - start][0]/(int)antialiasingFactor, indices[i1d - start][1]));
						double pixelSize = width/widthPixels;
						Point2D.Double antialiasOffsetFactor = randomAntialiasPoints[(int)antialiasingFactor - 2][indices[i1d - start][0]%(int)antialiasingFactor];
						points[i1d - start] = new Point2D.Double(pixelCenter.x + pixelSize*antialiasOffsetFactor.x, pixelCenter.y + pixelSize*antialiasOffsetFactor.y);
						break;
				}
			}
			fractalThreadPool.execute(new FractalCalculator(fractal, points, indices, maxIter, escapeRad, progressCounter));
		}
		
		// wait until calculation is finished
		if(!fractalThreadPool.isShutdown()) {
			fractalThreadPool.shutdown();								// stop accepting new Runnables
			try {
				fractalThreadPool.awaitTermination(1, TimeUnit.DAYS);	// wait for existing Runnables to finish running
			} catch (InterruptedException e) { System.out.println("Interrupted ThreadPool shutdown in FractalLabel.updateFractal()"); }
		}
		
		progressTimer.stop();
		progressCounter[0] = allIndices.size();
		reportProgress();
		System.out.println(String.format("Done updating fractal (%s threads, %s processes, %.2f seconds)", nThreads, N_RUNNABLES, 0.001*(System.currentTimeMillis() - startTime)));
	}

	// recreate fractal image to match fractal array
	void updateImage() {
		System.out.println("Updating image... ");
		
		// create new image and render the contents of the fractal array to it
		image = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_RGB);
		switch (antialiasingType) {
			case ANTIALIAS_NONE:
				for(int i = 0; i < image.getWidth(); i++) {
					for(int j = 0; j < image.getHeight(); j++) {
						image.setRGB(i, j, getIterationRGB(fractal[i][j], defaultGradient));
					}
				}
				break;
			case ANTIALIAS_RANDOM:
				for(int i = 0; i < image.getWidth(); i++) {
					for(int j = 0; j < image.getHeight(); j++) {
						Double[] pixelIterations = new Double[(int)antialiasingFactor];
						for(int k = 0; k < (int)antialiasingFactor; k++) {
							int ik = i*(int)antialiasingFactor + k;
							pixelIterations[k] = fractal[ik][j];
						}
						Arrays.sort(pixelIterations, FractalCalculator.ITERATION_COMPARATOR);
						image.setRGB(i, j, getIterationRGB(pixelIterations[pixelIterations.length/2], defaultGradient)); // median-ish
					}
				}
				break;
		}
		
		// display new image
		icon.setImage(image);
		updateUI();
		
		System.out.println("Done updating image");
	}
	
	// abort fractal calculation
	void abort() {
		// if updateWorker exists, cancel its execution
		if(updateWorker != null) {
			updateWorker.cancel(true);
		}
		
		// if fractalThreadPool exists, cancel its execution
		if(fractalThreadPool != null) {
			fractalThreadPool.shutdownNow();								// stop accepting new Runnables and halt existing ones
			try {
				fractalThreadPool.awaitTermination(1, TimeUnit.SECONDS);	// wait for existing Runnables to finish halting
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		progressTimer.stop();
	}
	
	// transform a point from image space to fractal space
	Point2D.Double getFractalXY(Point imageXY){
		double dx = (((double)imageXY.getX())/widthPixels - 0.5)*width;						// x-coordinate relative to center, ignoring rotation
		double dy = -(((double)imageXY.getY()) - 0.5*fractal[0].length)*width/widthPixels;	// y-coordinate relative to center, ignoring rotation
		return new Point2D.Double(
				centerX + dx*Math.cos(rotation) - dy*Math.sin(rotation),						// multiply by rotation matrix and add center offset
				centerY + dx*Math.sin(rotation) + dy*Math.cos(rotation)
		);
	}
	
	// transform a point from fractal space to image space
	Point getImageXY(Point2D.Double fractalXY){
		double dxt = fractalXY.getX() - centerX;					// temporary x
		double dy = fractalXY.getY() - centerY;						// subtract center offset
		double dx = dxt*Math.cos(rotation) + dy*Math.sin(rotation);	// multiply by inverse of rotation matrix
		dy = -dxt*Math.sin(rotation) + dy*Math.cos(rotation);		// can re-use double dy
		return new Point(
				(int)(widthPixels*(0.5 + dx/width) + 0.5),
				(int)(0.5*fractal[0].length - dy*widthPixels/width + 0.5)
		);
	}
	
	// convert fractal value to colour code
	static int getIterationRGB(double iterations, int[][] gradientRGB) {
		// return black if point didn't escape
		if(iterations == FractalCalculator.REACHED_MAXITER || iterations == FractalCalculator.IN_SET) {
			return Color.BLACK.getRGB();
		}
		
		// log-scale n for better contrast when zoomed in, and normalize to gradient length
		iterations = logScalingA*Math.log(logScalingB*(iterations + logScalingC))/gradientRGB.length;
		
		// interpolate colour between gradient values
		while(iterations < 0) {
			iterations += gradientRGB.length;			// bring n above zero to get positive modulo
		}
		int n1 = ((int)iterations) % gradientRGB.length;	// gradient index below n
		int n2 = ((int)iterations + 1) % gradientRGB.length;	// gradient index above n
		double w = iterations % 1;								// where n is between n1 and n2 (0 if n = n1, 1 if n = n2, linear between)
		
		int[] rgb = new int[3];						// interpolated {red, green, blue}
		for(int i=0; i<3; i++) {
			rgb[i] = (int)((1 - w)*gradientRGB[n1][i] + w*gradientRGB[n2][i]);
		}
		
		// create a Color object of the desired RGB values and return its RGB code
		return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
	}
	
	// update GUI to reflect calculation progress
	void reportProgress() {
		gui.progressBar.setValue(progressCounter[0]);
	}
}