import java.awt.geom.Point2D;

/* class FractalCalculator:
 * - contains static methods for the calculation of the Mandelbrot fractal
 *     - has static fields for special-case return values
 * - implements Runnable to perform parallelized execution of these static methods
 *     - constructor sets fields containing general parameters of the calculation, a list of points at which to evaluate the fractal,
 *       a reference to an array in which to save the results, and the indices in which to save the result for each point
 *     - run() performs the calculation when the Runnable is executed
 */

/* TODO:
 * - figure out how Mandelbrot smoothing actually works, and try to fix it so it conserves integer iteration numbers
 * - improve maximum resolution through arbitrary-precision BigDecimal and/or perturbation theory (see Wikipedia)
 * - try out periodicity-checking (see Wikipedia)
 */

public class FractalCalculator implements Runnable {
	
	// return values for special cases in fractal calculation
	static double NOT_CALCULATED = -1;	// placeholder indicating the value of a pixel has not yet been calculated
	static double REACHED_MAXITER = -2;	// special return value for points that did not escape before reaching the maximum iteration
	static double IN_SET = -3;			// special return value for points analytically known to never escape
	
	// general calculation parameters
	int maxIter;	// maximum number of fractal iterations to calculate before giving up
	double escape;	// radius at which a point is considered to have "escaped" from the set
	
	// calculation input/output
	Point2D.Double[] points;	// a list of points at which to calculate the fractal
	double[][] fractal;			// reference to an array in which to save the results of calculation
	int[][] indices;			// indices in fractal at which to save the result for each point

	// constructor: assign values to fields and check that points and indices match
	FractalCalculator(double[][] f, Point2D.Double[] p, int[][] i, int m, double e) {
		// throw exception if the number of points doesn't match the number of indices
		if(i.length != p.length) {
			throw new IllegalArgumentException("Illegal arguments passed to FractalCalculator constructor: i.length != p.length");
		}
		
		// throw exception if the entries in indices aren't 2D
		if(i[0].length != 2) {
			throw new IllegalArgumentException("Illegal arguments passed to FractalCalculator constructor: i[0].length != 2");
		}
		
		fractal = f;
		indices = i;
		points = p;
		maxIter = m;
		escape = e;
	}
	
	// for each point, evaluate the fractal and save the result
	public void run() {
		for(int i=0; i<points.length; i++) {
			fractal[indices[i][0]][indices[i][1]] = MandelbrotPoint(points[i], maxIter, escape);
		}
	}

	// evaluate the Mandelbrot fractal at a point
	static double MandelbrotPoint(Point2D.Double z0, int maxIter, double escapeRad) {
		double r0 = z0.getX();
		double i0 = z0.getY();
		
		// return special value if z0 is known to be in the set
		if(mandelTest(r0, i0)) {
			return IN_SET;
		}
		
		int n = 0;
		double r = 0;
		double i = 0;
		
		while(true){
			// iterate z_{n+1} = z_n^2 + z0 until z_n escapes or n reaches maxIter
			double rt = r;
			r = rt*rt - i*i + r0;
			i = 2*rt*i + i0;
			n++;
			
			// if z_n has escaped, return number of iterations
			if(r*r + i*i >= escapeRad) {
				// to interpolate between integer iteration numbers, consider how far z escaped
				// should probably look into exactly how this smoothing works; it doesn't seem to conserve integer iteration numbers
				return n + 1 - Math.log(0.5*Math.log(r*r + i*i)/Math.log(2))/Math.log(2);
			}
			
			// return special value if n reached maxIter without escaping
			if(n >= maxIter) {
				return REACHED_MAXITER;
			}
		}
	}
	
	// check if a point is in the simple period-1 or 2 regions of the Mandelbrot set
	static boolean mandelTest(double r, double i){
		double rPlus = r + 1;
		double rMinus = r - 0.25;
		double iSquared = i*i;
		double q = rMinus*rMinus + iSquared;
		return q*(q + rMinus) < 0.25*iSquared || rPlus*rPlus + iSquared < 0.0625;
	}
}