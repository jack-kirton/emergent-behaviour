import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Simulation implements Serializable
{
    //Contains agents
    //Updates agents
    //Contains some terrain map

    //Should be easy to reset all agents and restarts
    //Handles food sources
    //Evaluation of each agent
    //Save agents to disk after each round
    int timeLimit;
    int timeStep; //Wait time between AI update loops
    
    public String simName;
    
    List<Agent> agents;
    public Terrain terrain;
    public ArrayList<GeneticAlgorithm> gas;
    
    public Simulation()
    {
        //ONLY FOR LOADING FROM A FILE
        //Necessary for serialisation
        
        timeLimit = 5000;
        timeStep = 10;
    }

    public Simulation(boolean gui, String map, String sim)
    {
        terrain = new Terrain(gui);
        terrain.loadMap(map);
        
        //Initialises 4 species of agents (their GAs), with 10 agents in each
        gas = new ArrayList<GeneticAlgorithm>();
        for(int i = 0; i < 4; i++)
        {
            gas.add(new GeneticAlgorithm(10, GeneticAlgorithm.MUTATION_RATE, GeneticAlgorithm.CROSSOVER_RATE, Agent.NUM_WEIGHTS));
        }

        timeLimit = 5000;
        timeStep = 10;
        
        simName = sim;
    }

    public void init() //Prepare simulation to start
    {
         agents = new ArrayList<Agent>();
         for(int i = 0; i < gas.size(); i++)
         {
            for(Genome g : gas.get(i).population)
            {
                agents.add(new Agent(i, g, randomAgentPosition()));
            }
        }
         
        terrain.createTerrainImage();
    }
    
    public static final int TOTAL_PLANTS = (int)Settings.getValue("TOTAL_PLANTS");

    //Main loop of whole system
    public void run()
    {
        while(true)
        {
            //Reset death count
            Agent.totalDead = 0;
            
            //Reset edible vegetation
            for(int i = 0; i < terrain.map.length; i++)
            {
                for(int j = 0; j < terrain.map[i].length; j++)
                {
                    terrain.map[i][j].dynamicObject = null;
                }
            }
            
            terrain.plants = 0; //Updated within terrain class when addPlant is called
            
            //Adds edible vegetation to map
            for(int i = terrain.plants; i < TOTAL_PLANTS; i++)
            {
                terrain.addPlant((int)(Math.random()*(double)terrain.map.length), (int)(Math.random()*(double)terrain.map[0].length));
            }
            
            System.out.println("#Simulation: Executing generation " + gas.get(0).generation + " simulation...");
            
            //Begin simulation
            for(int i = 0; i < timeLimit; i++)
            {
                //Update each agent
                for(int j = 0; j < agents.size(); j++)
                {
                    Agent a = agents.get(j);
                    a.updateAI(this, getSenses(agents.get(j)));
                    a.updateMovement(getXBound(), getYBound());
                    
                    if(terrain.map[(int)a.position[0]][(int)a.position[1]].type == Terrain.Type.WATER)
                    {
                        a.position[0] -= a.velocity[0];
                        a.position[1] -= a.velocity[1];
                        
                        a.position[0] = (a.position[0] <  0) ? 0 : a.position[0];
                        a.position[0] = (a.position[0] > getXBound() - 1) ? getXBound() - 1 : a.position[0];
                        a.position[1] = (a.position[1] <  0) ? 0 : a.position[1];
                        a.position[1] = (a.position[1] > getYBound() - 1) ? getYBound() - 1 : a.position[1];
                    }
                }

                if(terrain.plants < TOTAL_PLANTS) terrain.addPlant((int)(Math.random()*(double)terrain.map.length), (int)(Math.random()*(double)terrain.map[0].length));

                try 
                {
                    Thread.sleep(timeStep);
                } 
                catch(InterruptedException ex) 
                {
                    Thread.currentThread().interrupt();
                }
                
                //If every agent is dead, end simulation
                if(agents.size() == Agent.totalDead) break;
                
            }
            
            System.out.println("#Simulation: Finished generation " + gas.get(0).generation + " simulation.");
            
            //Performs GA for each species
            runGA();
            
            for(int i = 0; i < gas.size(); i++)
            {
                System.out.println("Species " + Agent.colorName(i) + "\n------------------------");
                System.out.println("Best fitness: " + gas.get(i).getBestFitness());
                System.out.println("Worst fitness: " + gas.get(i).getWorstFitness());
                System.out.println("Average fitness: " + gas.get(i).getAverageFitness());
                System.out.println("");
            }
            
            //Saves simulation to file after each generation
            Simulation.saveSimulation(this);
        }
    }
    
    //Runs the genetic algorithms for each species
    private void runGA()
    {
        ArrayList<ArrayList<Genome>> sorted = new ArrayList<ArrayList<Genome>>();
        
        for(int i = 0; i < gas.size(); i++)
        {
            sorted.add(new ArrayList<Genome>());
        }
        
        for(Agent a : agents)
        {
            sorted.get(a.species).add(a.genes);
        }
        
        agents = new ArrayList<Agent>();
        
        for(int i = 0; i < gas.size(); i++)
        {
            ArrayList<Genome> species = gas.get(i).breed(sorted.get(i));
            
            for(Genome g : species)
            {
                agents.add(new Agent(i, g, randomAgentPosition()));
            } 
        }
    }
    
    //Returns random map position that is valid to start on
    private double[] randomAgentPosition()
    {
        double[] pos = new double[3];
        do
        {
            pos[0] = Math.random()*(double)getXBound();
            pos[1] = Math.random()*(double)getYBound();
        }
        while(terrain.map[(int)pos[0]][(int)pos[1]].type == Terrain.Type.WATER);
        
        return pos;
    }

    //Draws terrain then agents on top
    public void draw(Graphics2D g)
    {
        terrain.drawTerrain(g);

        for(int i = 0; i < agents.size(); i++)
        {
            agents.get(i).drawAgent(g);
        }
    }



    public int getXBound()
    {
        return terrain.map.length;
    }

    public int getYBound()
    {
        return terrain.map[0].length;
    }

    public static final int SENSE_RANGE = 50;
    public static final int SIGHT_RANGE = 250;
    public static final double SIGHT_ANGLE = Math.PI/1.5;
    
    //Builds a senses object for a given agent
    public Senses getSenses(Agent agent)
    {
        List<TerrainBlock> visibleTerrain = new ArrayList<TerrainBlock>();
        for(int x = (int)agent.position[0] - 50; x < agent.position[1] + 50; x++)
        {
            for(int y = (int)agent.position[1] - 50; y < agent.position[1] + 50; y++)
            {
                if(x < 0 || x > getXBound() - 1) continue;
                if(y < 0 || y > getYBound() - 1) continue;
                visibleTerrain.add(terrain.map[x][y]);
            } 
        }

        List<Agent> visibleAgents = new ArrayList<Agent>();
        for(Agent a : agents)
        {
            if((a.position[0] < agent.position[0] + 50) && (a.position[0] > agent.position[0] - 50) && (a.position[1] < agent.position[1] + 50) && (a.position[1] > agent.position[1] - 50)) visibleAgents.add(a);
        }
        return new Senses(visibleTerrain, visibleAgents);
    }

    public static final double PLANT_PERSISTENCE = Settings.getValue("PLANT_PERSISTENCE");
    public static final double MEAT_PERSISTENCE = Settings.getValue("MEAT_PERSISTENCE");
    
    public static final double PLANT_MAX_ENERGY = Settings.getValue("PLANT_MAX_ENERGY");
    public static final double MEAT_MAX_ENERGY = Settings.getValue("MEAT_MAX_ENERGY");

    //Code to auto-eat object called by agent
    public double eatObject(Agent agent, TerrainBlock target)
    {
        TerrainBlock t = target;
        if(t.dynamicObject == Terrain.Type.PLANT)
        {
            t.dynamicObject = (Math.random() < PLANT_PERSISTENCE) ? t.dynamicObject : null;
            return Math.random()*PLANT_MAX_ENERGY;
        }
        else return 0;
    }

    //Code to auto-eat object with target as agent
    public double eatObject(Agent agent, Agent target)
    {
        if(target.dead && target.edible)
        {
            target.edible = (Math.random() < MEAT_PERSISTENCE);
            return Math.random()*MEAT_MAX_ENERGY;
        }

        return 0;
    }
    
    //Save simulation to file
    public static void saveSimulation(Simulation s)
    {
        try
        {
            File file = new File("./sims/" + s.simName + ".sim");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(s);
            System.out.println("#Simulation: Saved");
            out.close();
        }
        catch(IOException e)
        {
            System.err.println("#Simulation: Unable to save or cleanup sim file.");
            e.printStackTrace();
        }
    }

}
