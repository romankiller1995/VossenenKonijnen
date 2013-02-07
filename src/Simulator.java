import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2011.07.31
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.04;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;  
    // the probability that a beer will be created in any given grid position.
    private static final double BEER_CREATION_PROBABILITY = 0.03;
    // the probability that a jager will be created in any given grid position.
    private static final double JAGER_CREATION_PROBABILITY = 0.02;
    
    private static String ziekte;
    
    private static int counter;

    // List of animals in the field.
    private static List<Animal> animals;
    // The current state of the field.
    private static Field field;
    // The current step of the simulation.
    private static int step;
    // A graphical view of the simulation.
    private static SimulatorView view;
    
    public static void main(String args[]){
        new Simulator(); 
       }    
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        animals = new ArrayList<Animal>();
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Rabbit.class, Color.orange);
        view.setColor(Fox.class, Color.blue);
        view.setColor(Beer.class, Color.red);
        view.setColor(Jager.class, Color.green);
        
        // Setup a valid starting point.
        reset();
    }
    
    public static void murderdabitches(double kans, String ziektenaam, int steps){
    	Random random = new Random();
    	for(int x=0; x < steps; x++)
    	{
	    	for(int i=0; i < animals.size(); i++){
		    	double randomcijfer = random.nextDouble();
		    	if(randomcijfer <= kans){
		    		Animal animal = animals.get(i);
		    		animal.setDead();
		    		animals.remove(i);
		    		i--;
		    		counter ++;
		    	}else{
		    	}
	    	}
		    simulateOneStep();
	    	
    	}
    	FieldStats.ziekteistrue();
    	setZiekte(ziektenaam);
    }
    
    
    public static int getAantalZiek(){
    	return counter;
    }
    
    public static void setZiekte(String naam){
    	ziekte = naam;
    }
    
    public static void removeZiekte(){
    	ziekte = null;
    }
    
    public static String getZiekte(){
    	return ziekte;
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(4000);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public static void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and rabbit.
     */
    public static void simulateOneStep()
    {
        step++;

        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<Animal>();        
        // Let all rabbits act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            animal.act(newAnimals);
            if(! animal.isAlive()) {
                it.remove();
            }
        }
               
        // Add the newly born foxes and rabbits to the main lists.
        animals.addAll(newAnimals);

        view.showStatus(step, field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        animals.clear();
        populate();
        
        // Show the starting state in the view.
        view.showStatus(step, field);
    }
    
    public static void nuke(){
    	animals.clear();
    	field.clear();
    }
    
    public static void RealeaseNewAnimals(int amount, char soort){
        Random rand = Randomizer.getRandom();
        for(int i=0; i < amount; i++){ 
	                if(soort == 'f') {
	                	Location location = new Location(rand.nextInt(80), rand.nextInt(120));
	                    Fox fox = new Fox(true, field, location);
	                    animals.add(fox);
	                }
	                else if(soort == 'r') {
	                    Location location = new Location(rand.nextInt(80), rand.nextInt(120));
	                    Rabbit rabbit = new Rabbit(true, field, location);
	                    animals.add(rabbit);
	                }
	                else if(soort == 'b') {
	                    Location location = new Location(rand.nextInt(80), rand.nextInt(120));
	                    Beer beer = new Beer(true, field, location);
	                    animals.add(beer);
	                }
	                else if(soort == 'j') {
	                    Location location = new Location(rand.nextInt(80), rand.nextInt(120));
	                    Jager jager = new Jager(true, field, location);
	                    animals.add(jager);
	                }
        }
    }
    
    public static int getSteps(){
    	return step;
    }
    
    public static Field getField(){
    	return field;
    }
    
    public static void reload(){
    	populate();
    }
	        
      
    /**
     * Randomly populate the field with foxes and rabbits.
     */
    private static void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        animals.clear();
        Simulator.removeZiekte();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Fox fox = new Fox(true, field, location);
                    animals.add(fox);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Rabbit rabbit = new Rabbit(true, field, location);
                    animals.add(rabbit);
                }
                else if(rand.nextDouble() <= BEER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Beer beer = new Beer(true, field, location);
                    animals.add(beer);
                }
                else if(rand.nextDouble() <= JAGER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Jager jager = new Jager(true, field, location);
                    animals.add(jager);
                }
                // else leave the location empty.
            }
        }
    }
}
