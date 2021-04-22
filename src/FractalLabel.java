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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

// class which is the Component showing the rendered image, and which also contains the view parameters and methods for interaction

/* TODO:
 * - figure out how to make shutdown() and awaitTermination() (in updateFractal()) work properly
 * - add reuse of previous calculations where possible
 * - add antialiasing (MSAA, but possibly also over/undersampling)
 * - make coloring independent of gradient length (easy)
 * - figure out how to do coloring properly by examining how iteration counts change from one repeated pattern to the next
 * - add customizable and encapsulated coloring
 *     - add color map customization
 *     - add scaling customization
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
	
	// objects to handle multithreading
	ThreadFactory fractalThreadFactory;
	SwingWorker<Object, Object> updateWorker;
	ExecutorService fractalThreadPool;
	
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
	};
	
	// color log scaling parameters
	static double logScalingA = 6.4, logScalingB = 0.05;
	
	FractalLabel(){
		// set alignment and background so resizing looks nice
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setBackground(Color.BLACK);
		setOpaque(true);
		
		// add components
		icon = new ImageIcon();
		setIcon(icon);
		
		// initialize fractal parameters
		maxIter = 1000;
		escapeRad = 420.69;
		
		// initialize view parameters
		centerX = -0.69;
		centerY = 0;
		width = 5;
		rotation = 0;
		
		// set up multithreading
		fractalThreadFactory = new ThreadFactory(){
			public Thread newThread(Runnable r){
				Thread t = new Thread(r);
				t.setPriority(Thread.MIN_PRIORITY);// priority is 1-10
				return t;
			}
		};

		// add listeners
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
			// at a delay after the last call to update timer, abort previous update and update fractal
			public void actionPerformed(ActionEvent e) {
				abort();
				// create new SwingWorker to run update, so it runs in the background and can be aborted if necessary
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
	}
	
	// recreate fractal array to match current window size and fractal and view parameters
	void updateFractal() {
		System.out.println("Updating fractal...");
		long startTime = System.currentTimeMillis();
		
		// initialize ThreadPool
		// efficiency seems to increase with nThreads until nThreads = [CPU nThreads], and slowly decrease above that
		int nThreads = Runtime.getRuntime().availableProcessors();
		// number of processes to use
		// efficiency seems to plateau between 1k and 10k Runnables (for 1MP of fractal mostly reaching maxiter = 1000)
		int nRunnables = 4000;
		fractalThreadPool = Executors.newFixedThreadPool(nThreads, fractalThreadFactory);
		
		// redeclare fractal if necessary
		if(fractal == null || fractal.length != getWidth() || fractal[0].length != getHeight()) {
			fractal = new double[getWidth()][getHeight()];
		}
		
		// execute Runnable calculation
		int pixelsPerRunnable = fractal.length*fractal[0].length/nRunnables;
		int leftover = fractal.length*fractal[0].length - pixelsPerRunnable*nRunnables;
		for(int p = 0; p < nRunnables; p++) {
			int start = pixelsPerRunnable*p + Math.min(p, leftover);
			int end = start + pixelsPerRunnable;
			if(p < leftover) {
				end += 1;
			}
			Point2D.Double[] points = new Point2D.Double[end - start];
			int[][] indices = new int[end - start][2];
			for(int i1d=start; i1d < end; i1d++) {
				int imageX = i1d/fractal[0].length;
				int imageY = i1d%fractal[0].length;
				points[i1d - start] = getFractalXY(new Point(imageX, imageY));
				indices[i1d - start][0] = imageX;
				indices[i1d - start][1] = imageY;
			}
			fractalThreadPool.execute(new FractalCalculator(fractal, indices, points, maxIter, escapeRad));
		}
		
		// wait until all threads have completed, unless thread pool has been externally shut down
		// I don't think this works how I think it works
		if(!fractalThreadPool.isShutdown()) {
			fractalThreadPool.shutdown();
			try {
				fractalThreadPool.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) { System.out.println("Interrupted ThreadPool shutdown in FractalLabel.updateFractal()"); }
		}
		
		System.out.println(String.format("Done updating fractal (%s threads, %s processes, %.2f seconds)", nThreads, nRunnables, 0.001*(System.currentTimeMillis() - startTime)));
	}

	// recreate fractal image to match fractal array
	void updateImage() {
		System.out.println("Updating image... ");	
		
		// create new image and render the contents of the fractal array to it
		image = new BufferedImage(fractal.length, fractal[0].length, BufferedImage.TYPE_INT_RGB);				
		for(int i = 0; i < fractal.length; i++) {
			for(int j = 0; j < fractal[0].length; j++) {
				image.setRGB(i, j, defaultColorRGB(fractal[i][j]));
			}
		}
		
		// update GUI with new image
		icon.setImage(image);
		updateUI();
		
		System.out.println("Done updating image");
	}
	
	// abort fractal calculation
	void abort() {
		if(updateWorker != null && !(updateWorker.isCancelled() || updateWorker.isDone())) {
			updateWorker.cancel(true);
		}
		if(fractalThreadPool != null) {
			fractalThreadPool.shutdownNow();
			try {
				fractalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
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
	
	// convert iteration number to color RGB code
	int defaultColorRGB(double n) {
		// return black if point didn't escape
		if(n == FractalCalculator.REACHED_MAXITER || n == FractalCalculator.IN_SET) {
			return Color.BLACK.getRGB();
		}
		
		// log-scale n for better coloring when zooming in
		n = logScalingA*Math.log(logScalingB*n + 1);// should probably change to A*log(B*(n + 1)) + C
		
		// interpolate color based on scaled n
		int l = defaultGradient.length;
		while(n < 0) {
			n += l;
		}
		int n1 = ((int)n)%l;
		int n2 = ((int)n+1)%l;
		double w = n%1;
		
		int[] rgb = new int[3];
		for(int i=0; i<3; i++)
			rgb[i] = (int)((1-w)*defaultGradient[n1][i] + w*defaultGradient[n2][i]);
		
		return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
	}
}