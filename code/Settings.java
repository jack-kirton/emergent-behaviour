import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.Properties;

//Used to import settings saved in external file "./settings/constants.prop"
public class Settings
{
    //Agent
    static final int NET_INPUTS = 12;
    static final int NET_OUTPUTS = 3;
    static final int NEURONS_PER_LAYER = 8;
    static final int HIDDEN_LAYERS = 2;
    static final int NUM_WEIGHTS = 419;
    
    //GA
    static final double MAX_PERTURBATION = 0.3; //Maximum amount a gene can mutate by
    static final int NUM_ELITE = 2; //Number of elites chosen
    static final int NUM_ELITE_COPIES = 5; //Number of each elite copies added before sampling
    static final double CROSSOVER_RATE = 0.7;
    static final double MUTATION_RATE = 0.1;
    
    //Neural Net
    static final double BIAS = -1;
    static final double ACTIVATION = 1;
    
    //Simulation
    static final int TOTAL_PLANTS = 1000;
    static final double PLANT_PERSISTENCE = 0.5;
    static final double MEAT_PERSISTENCE = 0.9;
    static final double PLANT_MAX_ENERGY = 25;
    static final double MEAT_MAX_ENERGY = 50;
    
    

    private static Properties p;
    
    //Load a settings file
    public static void load(String filename)
    {
        File file = new File("./settings/" + filename + ".prop");
        
        if(file.exists())
        {
            System.out.println("#Settings: Loading " + filename);
            p = new Properties();
            try
            {
                p.load(new FileInputStream(file));
            }
            catch(IOException e)
            {
                System.out.println("#Settings: Failed to load settings file, loading defaults instead.");
                p = Settings.getDefaults();
                return;
            }
        }
        else
        {
            //If file does not exist, loads the default values
            System.out.println("#Settings: Could not find file " + filename + ", loading defaults.");
            p = Settings.getDefaults();
            return;
        }
    }
    
    //Returns a requested property
    public static double getValue(String key)
    {
        if(p == null) load("constants");
        return Double.parseDouble(p.getProperty(key));
    }
    
    //Returns the default values for the system
    public static Properties getDefaults()
    {
        Properties pr = new Properties();
        pr.setProperty("NET_INPUTS", Integer.toString(NET_INPUTS));
        pr.setProperty("NET_OUTPUTS", Integer.toString(NET_OUTPUTS));
        pr.setProperty("NEURONS_PER_LAYER", Integer.toString(NEURONS_PER_LAYER));
        pr.setProperty("HIDDEN_LAYERS", Integer.toString(HIDDEN_LAYERS));
        pr.setProperty("NUM_WEIGHTS", Integer.toString(NUM_WEIGHTS));
        
        pr.setProperty("MAX_PERTURBATION", Double.toString(MAX_PERTURBATION));
        pr.setProperty("NUM_ELITE", Integer.toString(NUM_ELITE));
        pr.setProperty("NUM_ELITE_COPIES", Integer.toString(NUM_ELITE_COPIES));
        pr.setProperty("CROSSOVER_RATE", Double.toString(CROSSOVER_RATE));
        pr.setProperty("MUTATION_RATE", Double.toString(MUTATION_RATE));
        
        pr.setProperty("BIAS", Double.toString(BIAS));
        pr.setProperty("ACTIVATION", Double.toString(ACTIVATION));
        
        pr.setProperty("TOTAL_PLANTS", Integer.toString(TOTAL_PLANTS));
        pr.setProperty("PLANT_PERSISTENCE", Double.toString(PLANT_PERSISTENCE));
        pr.setProperty("MEAT_PERSISTENCE", Double.toString(MEAT_PERSISTENCE));
        pr.setProperty("PLANT_MAX_ENERGY", Double.toString(PLANT_MAX_ENERGY));
        pr.setProperty("MEAT_MAX_ENERGY", Double.toString(MEAT_MAX_ENERGY));
        
        return pr;
    }
    
    //Main function creates default settings file at "./settings/constants.prop"
    public static void main(String args[])
    {
        Properties pr = getDefaults();
        
        File file = new File("./settings/constants.prop");
        
        try
        {
            pr.store(new FileOutputStream(file), null);
        }
        catch(IOException e)
        {
            System.err.println("Failed to write properties file.");
            e.printStackTrace();
        }
    }
}
