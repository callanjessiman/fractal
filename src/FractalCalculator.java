import java.awt.geom.Point2D;

// class that has static methods to calculate fractals and implements Runnable for mutithreaded execution (maybe should switch to Callable?)

/* TODO:
 * - improve maximum resolution through arbitrary-precision BigDecimal and/or perturbation theory (see Wikipedia)
 * - try exploiting the set's connectedness
 * - try out the periodicity-checking algorithm from Wikipedia
 */

public class FractalCalculator implements Runnable{
	
	// a list of points at which to calculate the fractal
	Point2D.Double[] points;
	
	// an array in which to save the results of the calculation, and an array of indices at which to save the result for each point
	double[][] fractal;
	int[][] indices;
	
	// general calculation parameters
	int maxIter;
	double escape;
	
	// return values for special cases
	static double NOT_CALCULATED = -1;
	static double REACHED_MAXITER = -2;
	static double IN_SET = -3;

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

	// calculate the value of the Mandelbrot fractal at a point
	static double MandelbrotPoint(Point2D.Double z0, int maxIter, double escapeRad) {
		double r0 = z0.getX();
		double i0 = z0.getY();
		
		// return special value if z0 is known to be in the set
		if(mandelTest(r0, i0)) {
			return IN_SET;
		}
		
		// iterate z_{n+1} = z_n^2 + z0
		int n = 0;
		double r = 0;
		double i = 0;
		
		while(n < maxIter && r*r + i*i < escapeRad){
			double rt = r;
			r = rt*rt - i*i + r0;
			i = 2*rt*i + i0;
			n++;
		}
		
		// if escaped, return interpolated iteration number
		if(r*r + i*i >= escapeRad) {
			// should probably look into exactly how this works; it doesn't seem to conserve integer iteration numbers
			return n + 1 - Math.log(0.5*Math.log(r*r + i*i)/Math.log(2))/Math.log(2);
		}
		
		// return special value if we reached maxIter without escaping
		return REACHED_MAXITER;
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