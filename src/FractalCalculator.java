import java.awt.geom.Point2D;

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

	// placeholder function to calculate the value of the Mandelbrot escape function at a point
	static double MandelbrotPoint(Point2D.Double z, int maxiter, double escape) {
		return 0;// should return smoothed iterations, and possibly specific values to indicate un-escaped points and so on
	}
}