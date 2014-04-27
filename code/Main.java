import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;

//Main class for beginning the system

public class Main
{
    Simulation sim;
    GUI gui;

    public static void main(String args[])
    {
        Main main = new Main();
        //String guiAns = "";
        String simName = "";
        String mapName = "";
        
        //Read information from command line
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            //System.out.println("GUI?");
            //guiAns = br.readLine();
            System.out.println("Simulation?");
            simName = br.readLine();
            System.out.println("Map?");
            mapName = br.readLine();
        }
        catch(IOException e)
        {
            System.err.println("#Main: Unable to read user responses.\nNow exiting...");
            System.exit(-1);
        }

        //GUI option not implemented
        boolean isGUI = true;
        //if(guiAns.contains("y")) isGUI = true;
        //else if(guiAns.contains("n")) isGUI = false;
        //else
        //{
        //    System.err.println("#Main: Incorrect responses.\nNow exiting...");
        //    System.exit(-1);
        //}
        
        //Load simulation if it exists, otherwise make a new simulation
        File file = new File("./sims/" + simName + ".sim");
        if(file.exists())
        {
            try
            {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                main.sim = (Simulation)in.readObject();
            }
            catch(Exception e)
            {
                System.err.println("#Main: Could not load simulation.\nNow exiting...");
                System.exit(-1);
            }
        }
        else
        {
            main.sim = new Simulation(isGUI, mapName, simName);
        }
        
        //Initialise simulation
        main.sim.init();
        
        //Initialise GUI
        main.gui = new GUI(main.sim);
        
        //Begin execution
        main.sim.run();

    }

    
}
