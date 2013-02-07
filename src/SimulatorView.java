import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import sun.audio.*;
import java.io.*;
import java.applet.*;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location 
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2011.07.31
 */
public class SimulatorView extends JFrame
{
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for objects that have no defined color.
    private static final Color UNKNOWN_COLOR = Color.gray;

    private final String STEP_PREFIX = "Step: ";
    private final String POPULATION_PREFIX = "Population: ";
    private JLabel stepLabel, population;
    private FieldView fieldView;
    private JPanel stepbutton;
    private JButton button;
    private JButton buttonsteps;
    private Simulator simulator;
    private JMenuBar menu;
    private TextField text1;
    private TextField text2;
    private TextField text3;
    private TextField text4;
    private JTextField aantalstappen;
    // A map for storing colors for participants in the simulation
    private Map<Class, Color> colors;
    // A statistics object computing and storing simulation information
    private FieldStats stats;
    

    /**
     * Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width.
     */
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<Class, Color>();

        setTitle("Vossen en Konijnen");
        stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
        population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);
        stepbutton = new JPanel();
        
        stepbutton.getPreferredSize();
        stepbutton.setLayout(new GridLayout(0,2));
        
        button = new JButton("1 Stap");
        button.addActionListener(new Onestep());
        stepbutton.add(button);
        
        buttonsteps = new JButton("100 Stappen");
        buttonsteps.addActionListener(new Hundredstep());      
        stepbutton.add(buttonsteps);
        
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        
        stepbutton.add(new JLabel("Voortplantings leeftijd"));
        text1 = new TextField("");
        stepbutton.add(text1);
        
        stepbutton.add(new JLabel("Maximale leeftijd"));
        text2 = new TextField("");
        stepbutton.add(text2);
        
        stepbutton.add(new JLabel("Voortplantings kans"));
        text3 = new TextField();
        stepbutton.add(text3);
        
        stepbutton.add(new JLabel("Maximaal aantal nakomelingen"));
        text4 = new TextField();
        stepbutton.add(text4);
        
        stepbutton.add(new JLabel("Het dier dat u wilt aanpassen"));
        stepbutton.add(new JLabel(""));
        
        JButton buttonrabbit = new JButton("Konijn");
        buttonrabbit.addActionListener(new Rabbitsettings());
        stepbutton.add(buttonrabbit);
        
        JButton buttonfox = new JButton("Vos");
        buttonfox.addActionListener(new Foxsettings());
        stepbutton.add(buttonfox);
        
        JButton buttonbeer = new JButton("Beer");
        buttonbeer.addActionListener(new Beersettings());
        stepbutton.add(buttonbeer);
        
        JButton buttonjager = new JButton("Jager");
        buttonjager.addActionListener(new Jagersettings());
        stepbutton.add(buttonjager);
        
        setLocation(100, 50);
        
        stepbutton.add(new JLabel());
        stepbutton.add(new JLabel());
        
        JButton buttonrelease = new JButton("Vrij Laten");
        buttonrelease.addActionListener(new Release());
        stepbutton.add(buttonrelease);
        
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel("Aantal stappen"));
        
        aantalstappen = new JTextField();
        stepbutton.add(aantalstappen);

        stepbutton.add(new JLabel(""));
        
        JButton aantalsteps = new JButton("Simuleer");
        aantalsteps.addActionListener(new AantalStappen());
        stepbutton.add(aantalsteps);
        
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        JButton ziekte = new JButton("Ziekte");
        ziekte.addActionListener(new Ziekte());
        stepbutton.add(ziekte);
        
        JButton boom = new JButton("Oerknal");
        boom.addActionListener(new Oerknal());
        stepbutton.add(boom);
        
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        stepbutton.add(new JLabel(""));
        
        JButton reloader = new JButton("Reload");
        reloader.addActionListener(new Reloader());
        stepbutton.add(reloader);
     
        fieldView = new FieldView(height, width);

        Container contents = getContentPane();
        contents.add(stepLabel, BorderLayout.NORTH);
        contents.add(stepbutton, BorderLayout.WEST);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(population, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }
    
    /**
     * Define a color to be used for a given class of animal.
     * @param animalClass The animal's Class object.
     * @param color The color to be used for the given class.
     */
    public void setColor(Class animalClass, Color color)
    {
        colors.put(animalClass, color);
    }

    /**
     * @return The color to be used for a given class of animal.
     */
    private Color getColor(Class animalClass)
    {
        Color col = colors.get(animalClass);
        if(col == null) {
            // no color defined for this class
            return UNKNOWN_COLOR;
        }
        else {
            return col;
        }
    }

    /**
     * Show the current status of the field.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed.
     */
    public void showStatus(int step, Field field)
    {
        if(!isVisible()) {
            setVisible(true);
        }
            
        stepLabel.setText(STEP_PREFIX + step);
        stats.reset();
        
        fieldView.preparePaint();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Object animal = field.getObjectAt(row, col);
                if(animal != null) {
                    stats.incrementCount(animal.getClass());
                    fieldView.drawMark(col, row, getColor(animal.getClass()));
                }
                else {
                    fieldView.drawMark(col, row, EMPTY_COLOR);
                }
            }
        }
        stats.countFinished();

        population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field));
        fieldView.repaint();
    }

    /**
     * Determine whether the simulation should continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        return stats.isViable(field);
    }
    
    /**
     * Provide a graphical view of a rectangular field. This is 
     * a nested class (a class defined inside a class) which
     * defines a custom component for the user interface. This
     * component displays the field.
     * This is rather advanced GUI stuff - you can ignore this 
     * for your project if you like.
     */
    private class FieldView extends JPanel
    {
        private final int GRID_VIEW_SCALING_FACTOR = 6;

        private int gridWidth, gridHeight;
        private int xScale, yScale;
        Dimension size;
        private Graphics g;
        private Image fieldImage;

        /**
         * Create a new FieldView component.
         */
        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        /**
         * Tell the GUI manager how big we would like to be.
         */
        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                                 gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        /**
         * Prepare for a new round of painting. Since the component
         * may be resized, compute the scaling factor again.
         */
        public void preparePaint()
        {
            if(! size.equals(getSize())) {  // if the size has changed...
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) {
                    xScale = GRID_VIEW_SCALING_FACTOR;
                }
                yScale = size.height / gridHeight;
                if(yScale < 1) {
                    yScale = GRID_VIEW_SCALING_FACTOR;
                }
            }
        }
        
        /**
         * Paint on grid location on this field in a given color.
         */
        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale-1, yScale-1);
        }

        /**
         * The field view component needs to be redisplayed. Copy the
         * internal image to screen.
         */
        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    // Rescale the previous image.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
    public class Hundredstep implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
        	Simulator.simulate(100);
		}

	}

	public class Onestep implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
        	Simulator.simulateOneStep();

		}

	}
	
	public class Reloader implements ActionListener {
		public void actionPerformed(ActionEvent e){
	        FieldStats.ziekteisfalse();
			Simulator.reload();
			Simulator.simulateOneStep();
		}
	}
	
	public class Oerknal implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Simulator.nuke();
	        Simulator.simulateOneStep();
	        	      
	        JFrame framelol = new JFrame("And it's gone");
	        framelol.setLayout(new BorderLayout(10,10));
	         
	         JLabel picLabel = new JLabel(new ImageIcon("strawberry.jpg"));
	         add( picLabel );
	         
	         //begin audio
	         try {
	                 AudioInputStream stream =
	                          AudioSystem.getAudioInputStream(
	                            new File("boom.wav"));
	         
	                DataLine.Info info =
	                          new DataLine.Info(Clip.class,
	                                  stream.getFormat());
	                Clip clip = (Clip) AudioSystem.getLine(info);
	         
	                clip.open(stream);
	                clip.start();
	         
	              } catch (Exception event) {
	                   event.printStackTrace();
	              }
	         //einde audio
	         
	        framelol.setSize(600, 600);
	        framelol.add(picLabel, BorderLayout.CENTER);
	        framelol.pack();
	        framelol.setVisible(true);
	    }
	}
	
	public class Ziekte implements ActionListener {
		private JFrame ziekteu;
		private JTextField ziekteveld;
		private JTextField doodkans;
		private JTextField aantalsteps;
		public void actionPerformed(ActionEvent e) {
			ziekteu = new JFrame("Ziekte muhahaha");
			ziekteu.setLayout(new BorderLayout());
			
			JPanel ziekte = new JPanel();
			ziekte.setLayout(new GridLayout(0,1));
			
			ziekte.add(new JLabel("Voer uw ziekte in"));
			ziekte.add(new JLabel("Naam"));
			ziekteveld = new JTextField();
			ziekte.add(ziekteveld);
			ziekte.add(new JLabel("Kans op ziekte"));			
			doodkans = new JTextField();
			ziekte.add(doodkans);
			ziekte.add(new JLabel("Tot hoeveel aantal steps"));
			aantalsteps = new JTextField();
			ziekte.add(aantalsteps);
			JButton ziektebutton = new JButton("Verspreid");
			ziektebutton.addActionListener(new Ziektemaker());
			ziekte.add(ziektebutton);
			ziekteu.add(ziekte, BorderLayout.CENTER);
			
			ziekteu.pack();
			ziekteu.setVisible(true);
		}
		
		public class Ziektemaker implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				Simulator.murderdabitches(Double.parseDouble(doodkans.getText()), ziekteveld.getText(), Integer.parseInt(aantalsteps.getText()));
				Simulator.simulateOneStep();
				ziekteu.dispose();
			}
		}
	}
	
	public class Rabbitsettings implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(text1.getText().length() == 0 && text2.getText().length() == 0 && text3.getText().length() == 0 && text4.getText().length() == 0){
			}else{

			if(text1.getText().length() > 0){
				Rabbit.setBreedingAge(Integer.parseInt(text1.getText()));				
			}
			if(text2.getText().length() > 0){
	        	Rabbit.setMaxAge(Integer.parseInt(text2.getText()));				
			}
			if(text3.getText().length() > 0){
	        	Rabbit.setBreedingProbability(Double.parseDouble(text3.getText()));				
			}
			if(text4.getText().length() > 0){
				Rabbit.setMaxLitterSize(Integer.parseInt(text4.getText()));				
			}
        	
        	text1.setText("");
        	text2.setText("");
        	text3.setText("");
        	text4.setText("");
        	
        	JFrame frame2 = new JFrame("Message");
        	frame2.setTitle("Message");
        	frame2.setLayout(new BorderLayout());
        	JLabel labelgelukt = new JLabel("de aanpassing is volbracht");
        	frame2.add(labelgelukt, BorderLayout.CENTER);
        	
        	frame2.pack();
        	frame2.setVisible(true);
			}
		}
	}
	public class Foxsettings implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(text1.getText().length() == 0 && text2.getText().length() == 0 && text3.getText().length() == 0 && text4.getText().length() == 0){
			}else{

			if(text1.getText().length() > 0){
				Fox.setBreedingAge(Integer.parseInt(text1.getText()));				
			}
			if(text2.getText().length() > 0){
	        	Fox.setMaxAge(Integer.parseInt(text2.getText()));				
			}
			if(text3.getText().length() > 0){
	        	Fox.setBreedingProbability(Double.parseDouble(text3.getText()));				
			}
			if(text4.getText().length() > 0){
	        	Fox.setMaxLitterSize(Integer.parseInt(text4.getText()));				
			}
        	
        	text1.setText("");
        	text2.setText("");
        	text3.setText("");
        	text4.setText("");
        	
        	JFrame frame2 = new JFrame("Message");
        	frame2.setTitle("Message");
        	frame2.setLayout(new BorderLayout());
        	JLabel labelgelukt = new JLabel("de aanpassing is volbracht");
        	frame2.add(labelgelukt, BorderLayout.CENTER);
        	
        	frame2.pack();
        	frame2.setVisible(true);
			}
		}
	}
	public class Beersettings implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(text1.getText().length() == 0 && text2.getText().length() == 0 && text3.getText().length() == 0 && text4.getText().length() == 0){
			}else{

			if(text1.getText().length() > 0){
				Beer.setBreedingAge(Integer.parseInt(text1.getText()));				
			}
			if(text2.getText().length() > 0){
	        	Beer.setMaxAge(Integer.parseInt(text2.getText()));				
			}
			if(text3.getText().length() > 0){
	        	Beer.setBreedingProbability(Double.parseDouble(text3.getText()));				
			}
			if(text4.getText().length() > 0){
	        	Beer.setMaxLitterSize(Integer.parseInt(text4.getText()));				
			}
     
        	text1.setText("");
        	text2.setText("");
        	text3.setText("");
        	text4.setText("");
        	
        	JFrame frame2 = new JFrame("Message");
        	frame2.setTitle("Message");
        	frame2.setLayout(new BorderLayout());
        	JLabel labelgelukt = new JLabel("de aanpassing is volbracht");
        	frame2.add(labelgelukt, BorderLayout.CENTER);
        	
        	frame2.pack();
        	frame2.setVisible(true);
			}
		}
	}
	public class Jagersettings implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(text1.getText().length() == 0 && text2.getText().length() == 0 && text3.getText().length() == 0 && text4.getText().length() == 0){
			}else{

			if(text1.getText().length() > 0){
				Jager.setBreedingAge(Integer.parseInt(text1.getText()));				
			}
			if(text2.getText().length() > 0){
	        	Jager.setMaxAge(Integer.parseInt(text2.getText()));				
			}
			if(text3.getText().length() > 0){
	        	Jager.setBreedingProbability(Double.parseDouble(text3.getText()));				
			}
			if(text4.getText().length() > 0){
	        	Jager.setMaxLitterSize(Integer.parseInt(text4.getText()));				
			}
        	
        	text1.setText("");
        	text2.setText("");
        	text3.setText("");
        	text4.setText("");
        	
        	JFrame frame2 = new JFrame("Message");
        	frame2.setTitle("Message");
        	frame2.setLayout(new BorderLayout());
        	JLabel labelgelukt = new JLabel("de aanpassing is volbracht");
        	frame2.add(labelgelukt, BorderLayout.CENTER);
        	
        	frame2.pack();
        	frame2.setVisible(true);
			}
		}
	}
	
	public class AantalStappen implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(Integer.parseInt(aantalstappen.getText()) > 0);
			Simulator.simulate(Integer.parseInt(aantalstappen.getText()));
		}
	}
	
	public class Release implements ActionListener {
		private JRadioButton fox;
		private JRadioButton rabbit;
		private JRadioButton jager;
		private JRadioButton beer;
		private JButton button;
		private JTextField amount;
		private JLabel label;
		private String test;
		private JFrame frame3;
		public void actionPerformed(ActionEvent e){
			frame3 = new JFrame();
			
			JPanel center = new JPanel();
			center.setLayout(new GridLayout(0,1));
			JLabel label4 = new JLabel("Kies het dier dat u vrij wilt laten");
			center.add(label4);
			fox = new JRadioButton("Vos");
			center.add(fox);
			jager = new JRadioButton("Jager");
			center.add(jager);
			rabbit = new JRadioButton("Konijn");
			center.add(rabbit);
			beer = new JRadioButton("Beer");
			center.add(beer);
			JLabel label3 = new JLabel("");
			center.add(label3);
			JLabel label2 = new JLabel("Aantal dieren dat u wilt vrij laten");
			center.add(label2);
			amount = new JTextField();
			center.add(amount);
			label = new JLabel("");
			center.add(label);
			button = new JButton("Vrij laten");
			button.addActionListener(new ReleaseAnimal());
			center.add(button);
			

			frame3.add(center);
			frame3.pack();
			frame3.setVisible(true);
		}
		
		public class ReleaseAnimal implements ActionListener {
			public void actionPerformed(ActionEvent e){
			           if(fox.isSelected()){
			        	   Simulator.RealeaseNewAnimals(Integer.parseInt(amount.getText()), 'f');
			           }
			           if(rabbit.isSelected()){
			        	   Simulator.RealeaseNewAnimals(Integer.parseInt(amount.getText()), 'r');			        	   
			           }
			           if(jager.isSelected()){
			        	   Simulator.RealeaseNewAnimals(Integer.parseInt(amount.getText()), 'j');			        	   
			           }
			           if(beer.isSelected()){
			        	   Simulator.RealeaseNewAnimals(Integer.parseInt(amount.getText()), 'b');			        	   
			           }
			           Simulator.simulateOneStep();
		        	   frame3.dispose();
			}
		}
	
		
	}
}
