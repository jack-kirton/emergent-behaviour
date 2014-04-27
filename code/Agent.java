import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

public class Agent implements Serializable
{
   public static final int NET_INPUTS = (int)Settings.getValue("NET_INPUTS");
   public static final int NET_OUTPUTS = (int)Settings.getValue("NET_OUTPUTS");
   public static final int NEURONS_PER_LAYER = (int)Settings.getValue("NEURONS_PER_LAYER");
   public static final int HIDDEN_LAYERS = (int)Settings.getValue("HIDDEN_LAYERS");
   public static final int NUM_WEIGHTS = (int)Settings.getValue("NUM_WEIGHTS");
   
   public static int totalDead = 0;

   public int species;
   public Color color;
   
   public double[] position;
   public double[] velocity;
   
   public double energy;
   public double hunger;
   public double thirst;
   public boolean dead = false;
   public boolean edible = false;
   
   public Genome genes = null;
   public NeuralNet net = null;
   
   public Agent()
   {
      //Necessary for serialisation
   }
   
   public Agent(int species, Genome g, double[] position)
   {
      this.species = species;
   
      this.position = position;
      this.velocity = new double[2];
      this.energy = 15;
      this.hunger = 100;
      this.thirst = 100;
      
      genes = g;
      net = new NeuralNet(NET_INPUTS, NET_OUTPUTS, HIDDEN_LAYERS, NEURONS_PER_LAYER);
      net.setWeights(genes.weights);
      
      color = Agent.setColor(species);
   }
   
   public void updateAI(Simulation sim, Senses senses)
   {
      if(dead) return;
      genes.live();
      //Evaluate senses into net inputs
      //Process net
      //Actuate movement
      //Auto-eat
      updateHormones();
      ArrayList<Double> inputs = sensesToInputs(sim, senses);
      ArrayList<Double> outputs = net.update(inputs);
      
      double x = outputs.get(0);
      double y = outputs.get(1);
      double mag = outputs.get(2);
      velocity[0] = (x/Math.sqrt(x*x + y*y))*mag;
      velocity[1] = (y/Math.sqrt(x*x + y*y))*mag;
   }
   
   public void updateMovement(int xBound, int yBound)
   {
      if(dead) return;
      
      position[0] += velocity[0];
      position[1] += velocity[1];
      
      //Catch erroneous movement
      if(position[0] > xBound) position[0] = (double)xBound - 1;
      if(position[0] < 0) position[0] = 0;
      if(position[1] > yBound) position[1] = (double)yBound - 1;
      if(position[1] < 0) position[1] = 0;
   }
   
   public static final double INTERACTION_DISTANCE = 3; //Maximum distance which fighting/eating can take place
   
   private void updateAction(Simulation sim, Agent a) //Implements auto-eat and fight behaviour
   {
      double[] temp = new double[2];
      temp[0] = a.position[0] - this.position[0];
      temp[1] = a.position[1] - this.position[1];
      
      if(Math.sqrt(temp[0]*temp[0] + temp[1]*temp[1]) < INTERACTION_DISTANCE)
      {
         if(!a.dead)
         {
            //Fighting
            genes.fought();
            a.energy--;
         }
         else if(a.dead && a.edible)//If its dead
         {
            //Eating code
            double energyGain = sim.eatObject(this, a);
            genes.ate(energyGain, hunger);
            hunger = (hunger - energyGain < 0) ? 0 : hunger - energyGain;
         }
      }
   }
   
   //Perform automated actions
   private void updateAction(Simulation sim, TerrainBlock t)
   {
      double[] temp = new double[2];
      temp[0] = (double)t.position[0] - this.position[0];
      temp[1] = (double)t.position[1] - this.position[1];
      
      if(Math.sqrt(temp[0]*temp[0] + temp[1]*temp[1]) < INTERACTION_DISTANCE)
      {
         if(t.dynamicObject == Terrain.Type.PLANT)
         {
            //Eat code
            double energyGain = sim.eatObject(this, t);
            genes.ate(energyGain, hunger);
            hunger = (hunger - energyGain < 0) ? 0 : hunger - energyGain;
         }
         if(t.type == Terrain.Type.WATER)
         {
            //Drink code
            genes.drank();
            thirst = (thirst - 25 < 0) ? 0 : thirst - 25;
         }
      }
   }
   
   //This function evaluates the objects in the environment that the agent is able sense 
   //and converts them into a vector that the neural network can use as inputs
   private ArrayList<Double> sensesToInputs(Simulation sim, Senses senses)
   {
      //Food vector
      //Drink vector
      //Friend vector
      //Friend travel vector
      //Foe vector
      
      //Energy
      //Thirst
      
      //Initially set the vectors as too far away to discover
      double[] food = {100000, 100000};
      double[] drink = {100000, 100000};
      double[] friend = {100000, 100000};
      double[] friendTravel = {100000, 100000};
      double[] foe = {100000, 100000};
      
      for(TerrainBlock t : senses.getTerrain())
      {
         //Find nearest water source from senses
         if(t.type == Terrain.Type.WATER)
         {
            double[] temp = new double[2];
            temp[0] = (double)t.position[0] - this.position[0];
            temp[1] = (double)t.position[1] - this.position[1];
            
            if(Math.sqrt(drink[0]*drink[0] + drink[1]*drink[1]) > Math.sqrt(temp[0]*temp[0] + temp[1]*temp[1]))
            {
               drink = temp;
            }
            
            updateAction(sim, t);
         }
         
         //Find nearest edible vegetation source from senses
         if(t.dynamicObject == Terrain.Type.PLANT)
         {
            double[] temp = new double[2];
            temp[0] = (double)t.position[0] - this.position[0];
            temp[1] = (double)t.position[1] - this.position[1];
            
            if(Math.sqrt(food[0]*food[0] + food[1]*food[1]) > Math.sqrt(temp[0]*temp[0] + temp[1]*temp[1]))
            {
               food = temp;
            }
            
            updateAction(sim, t);
         }
      }
      
      for(Agent a : senses.getAgents())
      {
         if(this.species == a.species) //Same species
         {
            //Same species average position vector
            if(a.dead) continue;
            
            double[] temp = new double[2];
            temp[0] = a.position[0] - this.position[0];
            temp[1] = a.position[1] - this.position[1];
            
            if(friend[0] == 100000 && friend[1] == 100000)
            {
               friend = temp;
            }
            else
            {
               friend[0] = (friend[0] + temp[0])/2;
               friend[1] = (friend[1] + temp[1])/2;
            }
            
            //Same species average travel vector
            temp[0] += a.velocity[0];
            temp[1] += a.velocity[1];
            
            if(friendTravel[0] == 100000 && friendTravel[1] == 100000)
            {
               friendTravel = temp;
            }
            else
            {
               friendTravel[0] = (friendTravel[0] + temp[0])/2;
               friendTravel[1] = (friendTravel[1] + temp[1])/2;
            }
         }
         
         else //Other species
         {
            double[] temp = new double[2];
            temp[0] = a.position[0] - this.position[0];
            temp[1] = a.position[1] - this.position[1];
            
            if(Math.sqrt(foe[0]*foe[0] + foe[1]*foe[1]) > Math.sqrt(temp[0]*temp[0] + temp[1]*temp[1]))
            {
               foe = temp;
            }
            
            updateAction(sim, a);
         }
         
      }
      
      double[] zero = {0, 0};
      
      //If any remain uninitialised, set them to 0
      food = (food[0] == 100000) ? zero : food;
      drink = (drink[0] == 100000) ? zero : drink;
      friend = (friend[0] == 100000) ? zero : friend;
      friendTravel = (friendTravel[0] == 100000) ? zero : friendTravel;
      foe = (foe[0] == 100000) ? zero : foe;
      
      //Build the input for the network
      ArrayList<Double> inputs = new ArrayList<Double>(NET_INPUTS);
      inputs.add(hunger);
      inputs.add(thirst);
      inputs.add(food[0]);
      inputs.add(food[1]);
      inputs.add(drink[0]);
      inputs.add(drink[1]);
      inputs.add(friend[0]);
      inputs.add(friend[1]);
      inputs.add(friendTravel[0]);
      inputs.add(friendTravel[1]);
      inputs.add(foe[0]);
      inputs.add(foe[1]);
      
      return inputs;
   }
   
   private void printList(List a)
   {
      System.out.print("(");
      for(Object i : a)
      {
         System.out.print(i.toString() + ", ");
      }
      System.out.println(")");
   }
   
   //The maximum values that an agent can withstand before dying
   public static final double MAX_HUNGER = 1000;
   public static final double MAX_THIRST = 800; //Not used
   
   private void updateHormones()
   {
        hunger++;
        //thirst++;
        
        energy = (energy + 0.25 > 15) ? 15 : energy + 0.25;
        
        if(hunger > MAX_HUNGER) 
        {
            dead = true;
            totalDead++;
        }
        //if(thirst > MAX_THIRST) dead = true;
        
        //If energy is depleted (from fighting)
        if(energy < 0) 
        {
            dead = true;
            totalDead++;
        }
        
        edible = dead;
    }
        
        
   
   public void drawAgent(Graphics2D g) //Draws the agent to screen
   {
       if(dead && !edible) return;
       
       Polygon marker = new Polygon(); //Create a chevron
       marker.addPoint((int)position[0], (int)position[1]);
       marker.addPoint((int)position[0] - 3, (int)position[1] - 5);
       marker.addPoint((int)position[0], (int)position[1] - 3);
       marker.addPoint((int)position[0] + 3, (int)position[1] - 5);
       
       AffineTransform tran = g.getTransform(); //Keep the original transform to reset it
       Color b = (dead) ? color : Color.BLACK;
       g.setPaint(b);
       g.rotate(Math.atan2(velocity[1], velocity[0]) - Math.PI/2, position[0], position[1]); //Point the chevron in the direction of travel
       g.draw(marker);
       //Fill it the right color
       Color c = (dead) ? Color.BLACK : color;
       g.setPaint(c);
       g.fill(marker);

       g.setTransform(tran);
   }
   
    //Constants that transform species ID to a colour
    public static Color setColor(int species)
    {
        switch(species)
        {
            case 0: return Color.RED;
            case 1: return Color.BLUE.brighter();
            case 2: return Color.CYAN;
            case 3: return Color.MAGENTA;
            case 4: return Color.ORANGE;
            case 5: return Color.PINK;
            case 6: return Color.YELLOW;
            case 7: return Color.LIGHT_GRAY;
            default: return null;
        }
    }
    
    //Used to output colour name of species for logging
    public static String colorName(int species)
    {
        switch(species)
        {
            case 0: return "Red";
            case 1: return "Blue";
            case 2: return "Cyan";
            case 3: return "Magenta";
            case 4: return "Orange";
            case 5: return "Pink";
            case 6: return "Yellow";
            case 7: return "Gray";
            default: return "Unknown";
        }
    }
}


