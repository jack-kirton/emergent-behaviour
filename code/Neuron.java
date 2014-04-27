import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;

//Single neuron in neural network
public class Neuron implements Serializable
{
    public int numInputs;
    public double[] weights;
   
    public Neuron()
    {
        //Necessary for serialisation
    }
   
    public Neuron(int numIn)
    {
        numInputs = numIn + 1; //Add 1 for the bias(threshold)
        weights = new double[numInputs];
      
        //Initially set weights to random values between 1 and -1
        Random rand = new Random();
        for(int i = 0; i < numInputs; i++)
        {
            weights[i] = rand.nextFloat() - rand.nextFloat();
        }
    }
}
   
