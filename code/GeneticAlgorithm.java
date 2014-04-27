import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

import java.io.Serializable;

public class GeneticAlgorithm implements Serializable
{
   public static final double MAX_PERTURBATION = Settings.getValue("MAX_PERTURBATION"); //Maximum amount a gene can mutate by
   public static final int NUM_ELITE = (int)Settings.getValue("NUM_ELITE"); //Number of elites chosen
   public static final int NUM_ELITE_COPIES = (int)Settings.getValue("NUM_ELITE_COPIES"); //Number of each elite copies added before sampling
   public static final double CROSSOVER_RATE = Settings.getValue("CROSSOVER_RATE");
   public static final double MUTATION_RATE = Settings.getValue("MUTATION_RATE");

   public ArrayList<Genome> population;
   
   //size of population
	int popSize;
	
	//amount of weights per chromo
	int chromoLength;

	//total fitness of population
	double totalFitness;

	//best fitness this population
	double bestFitness;

	//average fitness
	double averageFitness;

	//worst
	double worstFitness;

	//keeps track of the best genome
	int fittestGenome;

	//probability that a chromosones bits will mutate.
	//Try figures around 0.05 to 0.3 ish
	double mutationRate;

	//probability of chromosones crossing over bits
	//0.7 is pretty good
	double crossoverRate;

	//generation counter
	int generation;
	
	public GeneticAlgorithm()
	{
	    //Necessary for serialisation
	}
   
   public GeneticAlgorithm(int pSize, double mutRate, double crossRate, int numWeights)
   {
      population = new ArrayList<Genome>();
   
      popSize = pSize;
      mutationRate = mutRate;
      crossoverRate = crossRate;
      chromoLength = numWeights;
      
      totalFitness = 0;
      bestFitness = 0;
      averageFitness = 0;
      worstFitness = 99999;
      fittestGenome = 0;
      generation = 1;
      
      for(int i = 0; i < popSize; i++)
      {
         Genome g = new Genome();
         Random r = new Random();
         
         for(int j = 0; j < chromoLength; j++)
         {
            g.weights.add(r.nextDouble() - r.nextDouble());
         }
         
         population.add(g);
      }
   }
   
   //Perform single point crossover on two agent chromosomes
   private Genome[] crossover(Genome dad, Genome mum)
   {
      Genome[] children = new Genome[2];
      Random r = new Random();
      
      if ((r.nextFloat() > crossoverRate) || (mum == dad)) 
	   {
		   children[0] = new Genome(mum.weights, 0);
		   children[1] = new Genome(dad.weights, 0);
   
		   return children;
	   }
	   else
	   {
	      children[0] = new Genome();
	      children[1] = new Genome();
      }
	   
	   //Single point crossover
	   int crossPoint = r.nextInt(chromoLength);
	   
	   for(int i = 0; i < crossPoint; i++)
	   {
	      children[0].weights.add(dad.weights.get(i));
	      children[1].weights.add(mum.weights.get(i));
	   }
	   
	   for(int i = crossPoint; i < chromoLength; i++)
	   {
	      children[0].weights.add(mum.weights.get(i));
	      children[1].weights.add(dad.weights.get(i));
	   }
	   
	   return children;
   }
   
   //Perform mutation on a list of values
   private ArrayList<Double> mutate(ArrayList<Double> chromo)
   {
      Random r = new Random();
      
      for(int i = 0; i < chromo.size(); i++)
      {
         if(r.nextFloat() < mutationRate)
         {
            chromo.set(i, new Double(chromo.get(i) + ((r.nextDouble() - r.nextDouble()) * MAX_PERTURBATION)));
         }
      }
      
      return chromo;
   }
   
   //Pick a Genome roulette wheel style
   private Genome chromoRoulette()
   {
      //generate a random number between 0 & total fitness count
      Random r = new Random();
	   double slice = r.nextDouble() * totalFitness;

	   //this will be set to the chosen chromosome
	   Genome chosen = null;
	
   	//go through the chromosones adding up the fitness so far
	   double currentFitness = 0.0;
	
   	for (int i = 0; i < popSize; i++)
   	{
   		currentFitness += population.get(i).fitness;
   		
   		//if the fitness so far > random number return the chromo at 
   		//this point
   		if (currentFitness >= slice)
   		{
   			chosen = population.get(i);
            break;
   		}
   	}
   
      return chosen;
   }
   
   //Add additional copies of best agent if elitism mechanism is used
   private void putNBest(int n, int numCopies)
   {
      while(n-- != 0)
      {
         for(int i = 0; i < numCopies; i++)
         {
            population.add(population.get((popSize - 1) - n)); //Assumes ordered from lowest to highest.
         }
      }
   }
   
   //Calculates general fitness statistics of the current population
   private void calculateFitnessStats()
   {
      totalFitness = 0;
      
      double currentHigh = 0;
      double currentLow = 99999;
      
      for(int i = 0; i < popSize; i++)
      {
         totalFitness += population.get(i).fitness; 
      
         if(population.get(i).fitness > currentHigh)
         {
            currentHigh = population.get(i).fitness;
            fittestGenome = i;
            bestFitness = currentHigh;
         }
         
         if(population.get(i).fitness < currentLow)
         {
            currentLow = population.get(i).fitness;
            worstFitness = currentLow;
         }
      }
      
      averageFitness = totalFitness/popSize;
   }
   
   //Resets fitness statistics to initial values
   private void reset()
   {
      totalFitness = 0;
      bestFitness = 0;
      worstFitness = 99999;
      averageFitness = 0;
   }
   

   private ArrayList<Genome> getChromosomes()
   {
      return population;
   }
   
   public double getAverageFitness()
   {
      return averageFitness;
   }
   
   public double getBestFitness()
   {
      return bestFitness;
   }
   
   public double getWorstFitness()
   {
      return worstFitness;
   }
   
   
   //Creates a new population from the old population
   public ArrayList<Genome> breed(ArrayList<Genome> oldPop)
   {
      population = oldPop;
      
      reset();
      
      //Sorts population based on fitness
      Collections.sort(population);
      calculateFitnessStats();
      ArrayList<Genome> newPop = new ArrayList<Genome>();
      
      //Adds elite copies if specified
      if((NUM_ELITE_COPIES * NUM_ELITE % 2) == 0)
      {
         putNBest(NUM_ELITE, NUM_ELITE_COPIES);
      }
      
      while(newPop.size() < popSize)
      {
         //Select two agents
         Genome dad = chromoRoulette();
         Genome mum = chromoRoulette();
         
         //Perform crossover
         Genome[] children = crossover(dad, mum);
         
         //Mutate children
         children[0].weights = mutate(children[0].weights);
         children[1].weights = mutate(children[1].weights);
         
         //Add children to population
         newPop.add(children[0]);
         newPop.add(children[1]);
      }
      
      population = newPop;
      
      generation++;
      
      return population;
      
   }
}








/* OLD VERSION
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//This will take in one species at a time and create the children of that species
public class GeneticAlgorithm
{
    Map<String, Integer> spec;
    int chromosomeLength;

    List<Agent> population;
    List<String> chromosomes;

    public GeneticAlgorithm()
    {
        //Need to set appropriate constants
        loadChromosomeSettings();
    }
    //Needs a way to get fitness values of population
    
    public void loadChromosomeSettings()
    {
        spec = new LinkedHashMap<String, Integer>();
        int cLength = 0;
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("./settings/attributes.txt"));
            
            do
            {
                String line = br.readLine();
                //System.out.println(line);
                if(line == null) break;
                else if (line.length() == 0) continue;
                else if (line.startsWith("//")) continue;
                
                String[] tuple = line.split(",");
                cLength += Integer.parseInt(tuple[1]);
                spec.put(tuple[0], Integer.valueOf(tuple[1]));
            }
            while(true);
    
            chromosomeLength = cLength;

        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.err.println("#GA: Failed to load chromosome specification.\nNow exiting...");
            System.exit(-1);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("#GA: Unknown error while loading chromosome specification.\nNow exiting...");
            System.exit(-1);
        }
    }

    public List<Agent> randomSpecies(int species, int amount)
    {
        List<Agent> agents = new ArrayList<Agent>();

        for(int a = 0; a < amount; a++)
        {
            String chromosome = "";
            for(int i = 0; i < chromosomeLength; i++)
            {
                if(Math.random() < 0.5) chromosome += "0";
                else chromosome += "1";
            }

            agents.add(new Agent(species, chromosome, spec));
        }

        return agents;
    }

    
    public void setPopulation(List<Agent> population)
    {
        this.population = population;
        chromosomes = new ArrayList<String>();

        for(Agent a : population)
        {
            chromosomes.add(a.encode(spec));
        }
    }

    //Crossover (need to select type that is fit for purpose)
    //Mutation (need to select type)
    //Selection
    //Replacement
}
*/
