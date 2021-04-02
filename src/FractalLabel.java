import javax.swing.JLabel;

public class FractalLabel extends JLabel {
	private static final long serialVersionUID = 6834230068761778027L;

}

/*Old version:

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

@SuppressWarnings("serial")

public class FractalLabel extends JLabel {

	int maxiter;
	int escape;
	
	double centerX;
	double centerY;
	double widthX;
	int antialias;

	double[][] fractal;
	BufferedImage image;
	
	ImageIcon icon;
	Timer updateTimer;
	Timer progressTimer;
	
	ExecutorService threadPool;
	
	FractalColorModel colorModel;
	
	FractalLabel(){
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setBackground(Color.BLACK);
		setOpaque(true);
		
		maxiter = 10000;
		escape = 42069;
		
		centerX = -0.55;
		centerY = 0;
		widthX = 5;
		antialias = 1;
		
		icon = new ImageIcon();
		setIcon(icon);
		
		colorModel = new FractalColorModel();
		
		updateTimer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFractal();
				updateImage();
			}
		});
		updateTimer.setRepeats(false);
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				updateTimer.restart();
			}
		});
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				centerX = getXCoordImage(e.getPoint().x);
				centerY = getYCoordImage(e.getPoint().y);
				updateTimer.restart();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				widthX *= Math.pow(2, e.getWheelRotation());
				updateTimer.restart();
			}
		});
	}
	
	void updateFractal() {
		System.out.println(centerX + "\t" + centerY + "\t" + widthX);
		fractal = new double[getWidth()*antialias][getHeight()*antialias];
		int[][] dest = new int[fractal.length*fractal[0].length][2];
		for(int i=0; i<fractal.length; i++) {
			for(int j=0; j<fractal[0].length; j++) {
				fractal[i][j] = -1;
				dest[i + j*fractal.length][0] = i;
				dest[i + j*fractal.length][1] = j;
			}
		}
		
		int cores = Runtime.getRuntime().availableProcessors();
		
		threadPool = Executors.newFixedThreadPool(cores, new ThreadFactory(){
			public Thread newThread(Runnable r){
				Thread t = new Thread(r);
				t.setPriority(Thread.MIN_PRIORITY);
				return t;
			}
		});
		
		int nRemain = dest.length;
		while(nRemain > 0) {
			int[][] subDest = new int[Math.min(nRemain, 1000)][2];
			for(int i=0; i<subDest.length; i++)
				for(int j=0; j<2; j++)
					subDest[i][j] = dest[i][j];
		}//do 2 for loops
		
		FractalGenerator generator = new FractalGenerator(this, dest);
		generator.run();		
	}
	
	void updateImage() {
		long t = System.currentTimeMillis();
		
		if(antialias == 1) {
			image = new BufferedImage(fractal.length, fractal[0].length, BufferedImage.TYPE_INT_RGB);
			for(int i=0; i<fractal.length; i++) {
				for(int j=0; j<fractal[0].length; j++) {
					image.setRGB(i, j, colorModel.defaultColor(fractal[i][j]));
				}
			}
		}
		else {
			BufferedImage bigImage = new BufferedImage(fractal.length, fractal[0].length, BufferedImage.TYPE_INT_RGB);
			for(int i=0; i<fractal.length; i++) {
				for(int j=0; j<fractal[0].length; j++) {
					bigImage.setRGB(i, j, colorModel.defaultColor(fractal[i][j]));
				}
			}
			
			image = new BufferedImage(fractal.length/antialias, fractal[0].length/antialias, BufferedImage.TYPE_INT_RGB);
			image.getGraphics().drawImage(bigImage.getScaledInstance(fractal.length/antialias, fractal[0].length/antialias, BufferedImage.SCALE_SMOOTH), 0, 0, null);
			
			//image = new BufferedImage(fractal.length/antialias, fractal[0].length/antialias, BufferedImage.TYPE_INT_RGB);
			/*
			image = new BufferedImage(fractal.length/antialias, fractal[0].length/antialias, BufferedImage.TYPE_INT_RGB);
			for(int x=0; x<image.getWidth(); x++) {
				for(int y=0; y<image.getHeight(); y++) {
					double[] values = new double[antialias*antialias];
					for(int dx = 0; dx < antialias; dx++) {
						for(int dy = 0; dy < antialias; dy++) {
							values[dx + dy*antialias] = fractal[x*antialias + dx][y*antialias + dy];
						}
					}
					Arrays.sort(values);
					double value = 0;
					for(int i=0; i<values.length/2; i++)
						value += values[i];
					
					image.setRGB(x, y, colorModel.defaultColor(value*2/values.length));					
				}
			}star/
		}
		
		icon.setImage(image);
		updateUI();
		System.out.println(System.currentTimeMillis() - t);
	}

	double getXCoordFractal(double n){
		return centerX + widthX*(n/(getWidth()*antialias) - 0.5);
	}
	
	double getYCoordFractal(double n){
		return centerY + widthX*(0.5*getHeight()/getWidth() - n/(getWidth()*antialias));
	}

	double getXCoordImage(double n){
		return centerX + widthX*(n/(getWidth()) - 0.5);
	}
	
	double getYCoordImage(double n){
		return centerY + widthX*(0.5*getHeight()/getWidth() - n/getWidth());
	}
}

*/

/*Even older version:

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class FractalLabel extends JLabel{
	private static final long serialVersionUID = 6834230068761778027L;
	
	protected boolean isBusy;
	protected BufferedImage image, fullImage;
	protected double[][] imageIters;
	private FractalGUI gui;
	private ExecutorService threadPool;
	public FractalLabel(FractalGUI g){
		gui = g;
	}
	public void resize(){
		setSize(gui.fractal.getSize());
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		fullImage = new BufferedImage((int)(getWidth()*gui.scalingFactor), (int)(getHeight()*gui.scalingFactor), BufferedImage.TYPE_INT_RGB);
		setIcon(new ImageIcon(image));		
	}
	public void setIcon(){
		try{
			if(gui.scalingFactor!=1)image.getGraphics().drawImage(fullImage.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH), 0, 0, null);
			else image = fullImage;
			setIcon(new ImageIcon(image));
		}
		catch(Exception ex){ex.printStackTrace();}
	}
	public void refresh(){
		gui.progressBar.setValue(0);
		abort();
		gui.startTime = System.currentTimeMillis();
		setSize(gui.getWidth()-gui.controlPanel.getWidth(), gui.getHeight()-59);
		imageIters = new double[fullImage.getWidth()][fullImage.getHeight()];
		gui.apfloatPrecision = (ApfloatMath.log(gui.cartesianWidth.divide(new Apfloat(fullImage.getWidth(), gui.apfloatPrecision))).divide(ApfloatMath.log(new Apfloat(10, gui.apfloatPrecision))).negate().intValue()+2);
		int freeCores = Math.max(0, Math.min(gui.freeCores, Runtime.getRuntime().availableProcessors()-1));
		long start = System.currentTimeMillis();
		threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-freeCores, new ThreadFactory(){
			public Thread newThread(Runnable r){
				Thread t = new Thread(r);
				t.setPriority(Thread.MIN_PRIORITY);
				return t;
			}
		});
		for(Apfloat y = new Apfloat(fullImage.getHeight()/2, gui.apfloatPrecision);y.intValue()<=fullImage.getHeight();y=y.add(Apfloat.ONE)){
			Apfloat y2 = new Apfloat(fullImage.getHeight()).subtract(y);
			if(y2.intValue() != y.intValue() && y2.intValue() >=0){
				try{ threadPool.execute(new ImageRow(gui, y2)); }
				catch(RejectedExecutionException ex){};				
			}
			if(y.intValue()<fullImage.getHeight()){
				try{ threadPool.execute(new ImageRow(gui, y)); }
				catch(RejectedExecutionException ex){};				
			}
		}
		threadPool.shutdown();
		while(!threadPool.isTerminated());
		double lowestN = gui.maxIterations, highestN = 0;
		for(int x=0; x<imageIters.length; x++)for(int y=0; y<imageIters[x].length; y++){
			if(imageIters[x][y]>highestN)highestN = imageIters[x][y];
			if(imageIters[x][y]<lowestN)lowestN = imageIters[x][y];
		}
		for(int x=0; x<imageIters.length; x++)for(int y=0; y<imageIters[x].length; y++)imageIters[x][y] = (imageIters[x][y]-lowestN)/(highestN-lowestN);
		setIcon();
		System.out.println(String.format("%1.3f", (System.currentTimeMillis()-start)/1000.0)+"s ("+gui.centreX.doubleValue()+", "+gui.centreY.doubleValue()+"), "+gui.cartesianWidth.doubleValue()+", "+image.getWidth()+"x"+image.getHeight());
		gui.progressBar.setValue(100);
	}
	public void abort(){
		if(threadPool!=null)threadPool.shutdownNow();
		resize();
	}
}
//
//import java.awt.Point;
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//import javax.swing.ImageIcon;
//import javax.swing.JLabel;
//
//
//public class FractalLabel extends JLabel{
//	private static final long serialVersionUID = 2695444034606747255L;
//	
//	private int rows;
//	PixelGenerator pixelGenerator;
//	private BufferedImage image;
//	private FractalGUI gui;
//	public FractalLabel(int r, FractalGUI g){
//		super();
//		rows = r;
//		pixelGenerator = new PixelGenerator();
//		image = new BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
//		gui = g;
//	}
//	public void Refresh(int I, double cartesianWidth, double centreX, double centreY, String instructions, Point newCentre){
//		long start = System.currentTimeMillis();
//		gui.resetProgressBar();
//		ExecutorService exec=Executors.newFixedThreadPool(rows);
//		ArrayList<Future<BufferedImage>>results = new ArrayList<Future<BufferedImage>>(); 
//		if(instructions=="Resize"){
//			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
//		}
//		else if(instructions=="Pan"){	
//			for(int c1=0;c1<rows;c1++)results.add(exec.submit(new ImageRow(getWidth(), getHeight()/rows+1, I, cartesianWidth, centreX, centreY+cartesianWidth*getHeight()/getWidth()*(0.5-(double)c1/rows), instructions, newCentre, c1+1, pixelGenerator, image.getSubimage(0, c1*image.getHeight()/rows, image.getWidth(), Math.min(image.getHeight()/rows+1, image.getHeight()-c1*image.getHeight()/rows)), gui)));
//			for(int c1=0;c1<rows;c1++)try{
//				image.getGraphics().drawImage(results.get(c1).get(),0,c1*getHeight()/rows,null);				
//			} 
//			catch (InterruptedException|ExecutionException|IndexOutOfBoundsException e){}
//		}
//		else{
//			for(int c1=0;c1<rows;c1++)results.add(exec.submit(new ImageRow(getWidth(), getHeight()/rows+1, I, cartesianWidth, centreX, centreY+cartesianWidth*getHeight()/getWidth()*(0.5-(double)c1/rows), instructions, newCentre, c1+1, pixelGenerator, image.getSubimage(0, c1*image.getHeight()/rows, image.getWidth(), Math.min(image.getHeight()/rows+1, image.getHeight()-c1*image.getHeight()/rows)), gui)));
//			for(int c1=0;c1<rows;c1++)try{
//				image.getGraphics().drawImage(results.get(c1).get(),0,c1*getHeight()/rows,null);				
//			} 
//			catch (InterruptedException|ExecutionException|IndexOutOfBoundsException e){}
//		}
//		setIcon(new ImageIcon(image));
//		System.out.println("Rendered in "+(System.currentTimeMillis()-start)+"ms ("+instructions+"), C = "+centreX+"+"+centreY+"i");
//	}
//	public BufferedImage getImage(){return image;}
//}

*/