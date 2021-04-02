import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FractalGUI extends JFrame {
	private static final long serialVersionUID = -7891287854194431851L;
	
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
		
		// set frame visible
		setVisible(true);
		
		// apply initial update of components
		label.updateFractal();
		label.updateImage();
	}

	public static void main(String[] args) {
		new FractalGUI();
	}
}
/* Old version:

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")

public class FractalGUI extends JFrame {
	
	FractalLabel fractalLabel;
	
	FractalGUI(){
		super("fractal");
		setSize(400, 400);
		setLocation(80, 60);
		
		fractalLabel = new FractalLabel();
		
		add(fractalLabel, BorderLayout.CENTER);
		
		setVisible(true);
		fractalLabel.updateFractal();
		fractalLabel.updateImage();
	}

	public static void main(String[] args) {
		new FractalGUI();
	}
}

*/

/*Even older version:

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class FractalGUI extends JFrame{
	private static final long serialVersionUID = -6362963259469668702L;
	
	public double temp[] = {400*Math.pow(2.0, -13.0/12), 400*Math.pow(2.0, -12.0/12), 400*Math.pow(2.0, -11.0/12), 400*Math.pow(2.0, -10.0/12), 400*Math.pow(2.0, -9.0/12), 400*Math.pow(2.0, -8.0/12), 400*Math.pow(2.0, -7.0/12), 400*Math.pow(2.0, -7.0/12), 400*Math.pow(2.0, -6.0/12), 400*Math.pow(2.0, -5.0/12), 400*Math.pow(2.0, -4.0/12), 400*Math.pow(2.0, -3.0/12), 400*Math.pow(2.0, -2.0/12), 400*Math.pow(2.0, -1.0/12), 400*Math.pow(2.0, 0.0/12), 400*Math.pow(2.0, 1.0/12), 400*Math.pow(2.0, 2.0/12), 400*Math.pow(2.0, 3.0/12), 400*Math.pow(2.0, -13.0/12), 400*Math.pow(2.0, 4.0/12), 400*Math.pow(2.0, 5.0/12), 400*Math.pow(2.0, 6.0/12), 400*Math.pow(2.0, 7.0/12), 400*Math.pow(2.0, 8.0/12), 400*Math.pow(2.0, 9.0/12), 400*Math.pow(2.0, 10.0/12), 400*Math.pow(2.0, 11.0/12), 400*Math.pow(2.0, 12.0/12)};
	
	protected int maxIterations, escapeRadius, freeCores, apfloatPrecision;
	protected long timeOfChange, startTime;
	protected double scalingFactor;
	protected double[] refreshArgs;
	protected Apfloat centreX, centreY, cartesianWidth, multibrotExponent;
	protected String fractalSet, refreshType;
	protected Timer refreshTimer;
	protected JMenuBar menuBar;
	protected JMenu fileMenu, fractalMenu, helpMenu;
	protected JMenuItem saveImage, render3d;
	protected JPanel controlPanel, spinnerPanel;
	protected JProgressBar progressBar;
	protected JSpinner iterationSpinner, zoomSpinner, antialiasSpinner;
	protected FractalLabel fractal;
	protected PixelGenerator pixelGenerator;
	FourierTransform ft;
	public FractalGUI(){
		super("Fractal");
		setInitialValues("Newton");
		addMenuBar();
		addComponents();
		addListeners();
		setVisible(true);
		setImageSize(480, 270);
		scalingFactor = 1.5;
		
		
//		for(int c1=2;c1<360;c1++)if(!new File("ImageOutput/music/render"+c1+".png").exists()){
//			pixelGenerator.setSinusoidalNewtonFunction(ft, c1/50.0, 200, 1600, Math.pow(2, 2.0/12.0), true);
//			fractal.refresh();
//			try {
//				ImageIO.write(fractal.image, "png", new File("ImageOutput/music/render"+c1+".png"));
//			}catch (Exception e){e.printStackTrace();}
//		}
	}
	private void setInitialValues(String fs){
		fractalSet = fs;
		escapeRadius = (int)Math.pow(2, 32);
		freeCores = 4;//5 free uses 70% cpu, <5 uses 100%
		apfloatPrecision = 20;
		timeOfChange = startTime = 0;
		scalingFactor = 1;
		refreshType = "complete";
		centreX = new Apfloat("0", Apfloat.INFINITE);
		centreY = new Apfloat("0", Apfloat.INFINITE);
		cartesianWidth = new Apfloat("4", Apfloat.INFINITE);
		multibrotExponent = new Apfloat("2", Apfloat.INFINITE);
		pixelGenerator = new PixelGenerator(this);
		if(fractalSet=="Multibrot"||fractalSet=="BurningShip"){
			maxIterations = 1000;
			if(centreY.doubleValue()==0)centreY = new Apfloat("0.0001", Apfloat.INFINITE);
			if(centreX.doubleValue()==0)centreX = new Apfloat("-0.75", Apfloat.INFINITE);
		}
		else if(fractalSet=="Newton"){
			maxIterations = 200;
			cartesianWidth = new Apfloat(Math.pow(2, -11.5), Apfloat.INFINITE);
			ft = new FourierTransform(30, 1);
			ft.setFile();
			ft.getSpectrum();
			while(!ft.hasSpectrum);
			pixelGenerator.setSinusoidalNewtonFunction(ft, 0.25, 200, 1600, Math.pow(2, 1.0/12.0), true);
		}
	}
	private void addMenuBar(){
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		saveImage = new JMenuItem("Save current image");
		saveImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				saveImage();
		}});
		fileMenu.add(saveImage);
		render3d = new JMenuItem("Render 3D fractal");
		render3d.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				try{
					FractalSTL.generateSTL("C:/Users/Callan/Documents/3D/Fractal/Fractal.stl", fractal);
					System.out.println("Model saved");
				}catch(Exception e){e.printStackTrace();}
		}});
		fileMenu.add(render3d);
		menuBar.add(fileMenu);
		
		fractalMenu = new JMenu("Fractal");
		
		menuBar.add(fractalMenu);		
		
		helpMenu = new JMenu("Help");
		
		menuBar.add(helpMenu);		
	}
	private void addComponents(){
		setLayout(new BorderLayout());
		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(280, getHeight()));
		controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        add(controlPanel, BorderLayout.LINE_END);
		
		spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 5, 5);
		controlPanel.add(spinnerPanel, c);c.gridy = 0;

		c.gridx = 0;
		c.gridy = 0;
		spinnerPanel.add(new JLabel("Iterative Depth: "), c);
		c.gridx = 1;
		iterationSpinner=new JSpinner(new SpinnerNumberModel(maxIterations, 1.0, Math.pow(2.0,32.0), Math.pow(10, Math.max(1, (int)Math.log10(maxIterations-1)))));
		iterationSpinner.setPreferredSize(new Dimension(125, 20));
		spinnerPanel.add(iterationSpinner, c);

		c.gridx = 0;
		c.gridy = 1;
		spinnerPanel.add(new JLabel("Relative Zoom: "), c);
		zoomSpinner=new JSpinner(new SpinnerNumberModel(3-Math.log(cartesianWidth.doubleValue())/Math.log(2), 0.0, Math.pow(2.0,32.0), 1.0));
		zoomSpinner.setPreferredSize(new Dimension(125, 20));
		c.gridx = 1;
		spinnerPanel.add(zoomSpinner, c);

		c.gridx = 0;
		c.gridy = 2;
		spinnerPanel.add(new JLabel("Antialiasing: "), c);
		antialiasSpinner=new JSpinner(new SpinnerNumberModel(1, 1.0, 8, 0.5));
		antialiasSpinner.setPreferredSize(new Dimension(125, 20));
		c.gridx = 1;
		spinnerPanel.add(antialiasSpinner, c);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
        c.gridx = 0;
        c.gridy = 1;
        controlPanel.add(progressBar, c);

		fractal = new FractalLabel(this);
		add(fractal, BorderLayout.CENTER);	
		
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/12, Toolkit.getDefaultToolkit().getScreenSize().height/12);
		setSize(720, 480);
		setMinimumSize(new Dimension(336, 56));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	private void addListeners(){		
		addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				refreshType = "complete";
				refreshArgs = new double[0]; 
				fractal.resize();
				delayedRefresh();
			}
		});
		addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e) {
		        fractal.abort();
		    }
		});
		addMouseWheelListener(new MouseWheelListener(){
			public void mouseWheelMoved(MouseWheelEvent e){
				zoomSpinner.setValue(((Double)(zoomSpinner.getValue())).doubleValue()-e.getWheelRotation());
			}
		});
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				Point mousePosition = fractal.getMousePosition();
				if(mousePosition!=null){
					centreX = ApfloatMath.sum(centreX, ApfloatMath.product(cartesianWidth, new Apfloat(mousePosition.getX()/fractal.getWidth()-0.5)));
					centreY = centreY.subtract((cartesianWidth.multiply(new Apfloat(mousePosition.getY()/fractal.getWidth()-0.5*fractal.getHeight()/fractal.getWidth()))));
					refreshType = "complete";
					refreshArgs = new double[0]; 
					refresh();
				}
			}
		});
		zoomSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0){
				cartesianWidth = ApfloatMath.pow(new Apfloat("2", apfloatPrecision), new Apfloat(3-(double)zoomSpinner.getValue(), apfloatPrecision));
				refreshType = "complete";
				refreshArgs = new double[0]; 
				delayedRefresh();
			}			
		});
		iterationSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0){
				if((int)(double)iterationSpinner.getValue()*10==11*maxIterations)maxIterations*=2;
				else maxIterations = (int)(double)iterationSpinner.getValue();
				iterationSpinner.setModel(new SpinnerNumberModel(maxIterations, 1.0, Math.pow(2.0,32.0), Math.pow(10, Math.max(1, (int)Math.log10(maxIterations-1)))));
				refreshType = "complete";
				refreshArgs = new double[0]; 
				delayedRefresh();
			}			
		});
		antialiasSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0){
				scalingFactor = (double)antialiasSpinner.getValue();
				refreshType = "complete";
				refreshArgs = new double[0]; 
				fractal.resize();
				delayedRefresh();
			}			
		});
		refreshTimer = new Timer(50, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(timeOfChange>System.currentTimeMillis()){
					refreshTimer.stop();
					refresh();
				}
			}
		});
	}
	private void setImageSize(int w, int h){
		setSize(w+getWidth()-fractal.getWidth(), h+getHeight()-fractal.getHeight());
	}
	private void refresh(){
		new SwingWorker<Object, Object>(){
	        protected Object doInBackground(){
	        	apfloatPrecision = (int)(-Math.log(ApfloatMath.product(new Apfloat(2.5e-21), cartesianWidth).doubleValue()));
	        	centreX = new Apfloat(centreX.toString(), apfloatPrecision);
	        	centreY = new Apfloat(centreY.toString(), apfloatPrecision);
	        	cartesianWidth = new Apfloat(cartesianWidth.toString(), apfloatPrecision);
				fractal.refresh();
	            return null;
	        }
	    }.execute();		
	}
	private void delayedRefresh(){
		fractal.abort();
		timeOfChange = System.currentTimeMillis()+500;
		refreshTimer.restart();
	}
	void saveImage(){
		JFileChooser fileChooser = new JFileChooser("./");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jpg", new String[]{"jpg"}));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("png", new String[]{"png"}));
        fileChooser.showSaveDialog(this);
        try{
        	String fileName = fileChooser.getSelectedFile().getAbsolutePath();
        	if(!fileName.endsWith(fileChooser.getFileFilter().getDescription()))fileName+="."+fileChooser.getFileFilter().getDescription();
        	File file = new File(fileName);
	        System.out.println(file.getAbsoluteFile());
	        ImageIO.write(fractal.image, file.getName().substring(file.getName().indexOf(".")+1), file);
       }catch(Exception e){e.printStackTrace();}
	}
	public static void main(String args[]){
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ex){ex.printStackTrace();}
		new FractalGUI();
	}
}
//
//import java.awt.Dimension;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.Point;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JProgressBar;
//import javax.swing.JSpinner;
//import javax.swing.SpinnerNumberModel;
//import javax.swing.Timer;
//import javax.swing.UIManager;
//import javax.swing.UnsupportedLookAndFeelException;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//
//public class FractalGUI extends JFrame{
//	private static final long serialVersionUID = -8262093812177424337L;
//	
//	private int rows = 100;
//	private int iterations;
//	private long timeOfResize, timeOfMouseWheel;
//	private double centreX, centreY, cartesianWidth, magnification;
//	private FractalLabel fractalLabel;
//	private JButton smoothButton;
//	private JPanel controlPanel, spinnerPanel;
//	private JSpinner iterationSpinner, zoomSpinner;
//	private JProgressBar progressBar;
//	private Timer resizeTimer, mouseWheelTimer;
//	public FractalGUI(){
//		super("Fractal");
//		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
//		catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e1){}
//		iterations = 400;
//		magnification = 1;
//		cartesianWidth = 4;
//		centreX = -0.75;
//		centreY = 0;
//		
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setLocation(90,60);
//		setSize(1200, 600);
//		setMinimumSize(new Dimension(800,400));
//		setLayout(new GridBagLayout());
//		GridBagConstraints c = new GridBagConstraints();
//		
//		c.gridx = 0;
//		c.gridy = 0;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.fill = GridBagConstraints.BOTH;
//		fractalLabel = new FractalLabel(rows, this);
//		resizeTimer=new Timer(100,new ActionListener(){
//			public void actionPerformed(ActionEvent e){
//				if(timeOfResize+100<System.currentTimeMillis()){
//					fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Timer", null);
//					resizeTimer.stop();
//				}				
//			}			
//		});		
//		fractalLabel.addComponentListener(new ComponentAdapter(){
//			public void componentResized(ComponentEvent e){
//				fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Resize", null);
//				resizeTimer.restart();
//				timeOfResize = System.currentTimeMillis();		
//			}
//		});
//		add(fractalLabel, c);
//		
//		controlPanel = new JPanel();
//		controlPanel.setLayout(new GridBagLayout());
//		c.gridx = 1;
//		c.gridy = 0;
//		c.weightx = 2;
//		c.anchor = GridBagConstraints.NORTH;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		add(controlPanel);
//		
//		spinnerPanel = new JPanel();
//		spinnerPanel.setLayout(new GridBagLayout());
//		c.gridx = 0;
//		c.gridy = 0;
//		c.insets = new Insets(5, 5, 5, 5);
//		controlPanel.add(spinnerPanel, c);
//		
//		c.gridy = 0;
//		spinnerPanel.add(new JLabel("Iterative Depth: "), c);
//		
//		c.gridx = 1;
//		iterationSpinner=new JSpinner(new SpinnerNumberModel(iterations, 1.0, Math.pow(2.0,32.0),25.0));
//		iterationSpinner.addChangeListener(new ChangeListener(){
//			public void stateChanged(ChangeEvent e){
//				boolean deepen = false;
//				deepen = iterations<(double)iterationSpinner.getValue();
//				iterations = (int)(double)iterationSpinner.getValue();
//				if(deepen)fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Iterate In", null);
//				else fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Iterate Out", null);
//			}			
//		});
//		spinnerPanel.add(iterationSpinner, c);
//
//		c.gridx = 0;
//		c.gridy = 1;
//		spinnerPanel.add(new JLabel("Relative Zoom: "), c);
//		
//		c.gridx = 1;
//		zoomSpinner=new JSpinner(new SpinnerNumberModel(1.0, 0.0, Math.pow(2.0,32.0), 1.0));
//		zoomSpinner.addChangeListener(new ChangeListener(){
//			public void stateChanged(ChangeEvent e){
//				magnification = (double)zoomSpinner.getValue();
//				cartesianWidth = Math.pow(2, 3-magnification);
//				fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Zoom", null);
//			}			
//		});
//		spinnerPanel.add(zoomSpinner, c);
//		
//		smoothButton = new JButton("Smooth");
//		smoothButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e){
//				fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Smooth", null);
//			}
//		});
//		c.gridx = 0;
//		c.gridy = 2;
//		c.gridwidth = 1;
//		controlPanel.add(smoothButton, c);
//		
//		progressBar = new JProgressBar(0, rows);
//		c.gridx = 0;
//		c.gridy = 3;
//		c.gridwidth = 1;
//		controlPanel.add(progressBar, c);
//		
//		addMouseListener(new MouseListener(){
//			public void mouseClicked(MouseEvent e){}
//			public void mouseEntered(MouseEvent e){}
//			public void mouseExited(MouseEvent e){}
//			public void mousePressed(MouseEvent e){
//				Point mousePosition = fractalLabel.getMousePosition();
//				if(mousePosition!=null){
//					centreX += mousePosition.getX()*cartesianWidth/fractalLabel.getWidth()-cartesianWidth/2;
//					centreY -= mousePosition.getY()*cartesianWidth/fractalLabel.getWidth()-cartesianWidth*fractalLabel.getHeight()/fractalLabel.getWidth()/2;
//					Point newCentre = new Point((int)(mousePosition.getX()-fractalLabel.getWidth()/2), (int)(mousePosition.getY()-fractalLabel.getHeight()/2));
//					fractalLabel.Refresh(iterations, cartesianWidth, centreX, centreY, "Pan", newCentre);
//				}
//			}
//			public void mouseReleased(MouseEvent e){}
//		});
//		mouseWheelTimer=new Timer(500,new ActionListener(){
//			public void actionPerformed(ActionEvent e){
//				if(timeOfMouseWheel+500<System.currentTimeMillis()){
//					zoomSpinner.setValue(magnification);
//					resizeTimer.stop();
//				}				
//			}			
//		});		
//		addMouseWheelListener(new MouseWheelListener(){
//			public void mouseWheelMoved(MouseWheelEvent e){
//				magnification -= e.getWheelRotation();
//				mouseWheelTimer.stop();
//				mouseWheelTimer.start();
//				timeOfMouseWheel = System.currentTimeMillis();
//			}			
//		});
//		addComponentListener(new ComponentAdapter(){
//			public void componentResized(ComponentEvent e){
//				fractalLabel.setSize(getWidth()-400, getHeight());
//				controlPanel.setLocation(getWidth()-400, 0);
//				controlPanel.setSize(400, getHeight());
//			}
//		});
//	}
//	public void finishRow(){
//		progressBar.setValue(progressBar.getValue()+1);
//	}
//	public void resetProgressBar(){
//		progressBar.setValue(0);
//	}
//	public static void main(String[]args){		
//		FractalGUI gui = new FractalGUI();
//		gui.setVisible(true);
//	}
//}

*/