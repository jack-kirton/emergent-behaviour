import java.util.ArrayList;
import java.io.Serializable;

//Class for containing the chromosome of an agent and adding up fitness during run of the simulation

public class Genome implements Comparable<Genome>, Serializable
{
   public ArrayList<Double> weights;
   public double fitness;
   
   public Genome()
   {
      weights = new ArrayList<Double>();
      fitness = 0.0;
   }
   
   public Genome(ArrayList<Double> w, double f)
   {
      weights = w;
      fitness = f;
   }
   
   //Needed to use the Collections.sort() function in the GA
   @Override
   public int compareTo(Genome other)
   {
      return (int)(this.fitness*1000 - other.fitness*1000); //Multiply 1000 to try and prevent any incorrect ordering due to casting
   }
   
   
   //METHODS FOR FITNESS CALCULATION
   //Experiment with these to alter fitness function
   
   public void live()
   {
      //fitness++;
   }
   
   public void died()
   {
      //fitness -= 1000;
   }
   
   public void ate(double energy, double hunger)
   {
      fitness += hunger/100;
   }
   
   public void drank()
   {
      //fitness++;
   }
   
   public void fought()
   {
      //fitness++;
   }
}
