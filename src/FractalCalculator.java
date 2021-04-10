import java.awt.geom.Point2D;

// class that has static methods to calculate fractals and implements Runnable for mutithreaded execution (maybe should switch to Callable?)

/* todos:
 * - add specific return values for points that don't escape/won't escape/etc
 */

public class FractalCalculator implements Runnable{
	
	// a list of points at which to calculate the fractal
	Point2D.Double[] points;
	
	// an array in which to save the results of the calculation, and an array of indices at which to save the result for each point
	double[][] fractal;
	int[][] indices;
	
	// general parameters
	int maxIter;
	double escape;

	FractalCalculator(double[][] f, int[][] i, Point2D.Double[] p, int m, double e) {
		fractal = f;
		indices = i;
		points = p;
		maxIter = m;
		escape = e;
	}
	
	// calculate and save the value of the Mandelbrot escape function at each point
	public void run() {
		for(int i=0; i<points.length; i++) {
			fractal[indices[i][0]][indices[i][1]] = MandelbrotPoint(points[i], maxIter, escape);
		}
	}

	// function to calculate the value of the Mandelbrot fractal at a point
	static double MandelbrotPoint(Point2D.Double z0, int maxIter, double escapeRad) {
		double r0 = z0.getX();
		double i0 = z0.getY();
		int n = 0;
		
		// return max if z0 is known to be in the set
		if(mandelTest(r0, i0)) {
			return maxIter;
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
	static boolean mandelTest(double r, double i){
		double rMinus = r - 0.25;
		double rPlus = r + 1;
		double iSquared = i*i;
		double q = rMinus*rMinus + iSquared;
		return q*(q + rMinus) < 0.25*iSquared || rPlus*rPlus + iSquared < 0.0625;
	}
}