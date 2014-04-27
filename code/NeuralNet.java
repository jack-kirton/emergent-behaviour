import java.util.ArrayList;
import java.io.Serializable;


public class NeuralNet implements Serializable
{
   static final double BIAS = (int)Settings.getValue("BIAS");
   static final double ACTIVATION = (int)Settings.getValue("ACTIVATION");

   int numInputs;
   int numOutputs;
   int numHiddenLyrs;
   int numNeuronsPerHiddenLyr;
   NeuronLayer[] layers;
   
   public NeuralNet()
   {
        //Necessary for serialisation
        setNet(Agent.NET_INPUTS, Agent.NET_OUTPUTS, Agent.HIDDEN_LAYERS, Agent.NEURONS_PER_LAYER);
   }
   
   public NeuralNet(int inputs, int outputs, int hiddenLyrs, int neuronsPerHiddenLyr)
   {
      setNet(inputs, outputs, hiddenLyrs, neuronsPerHiddenLyr);
      
      createNet();
   }
   
   private void setNet(int inputs, int outputs, int hiddenLyrs, int neuronsPerHiddenLyr)
   {
      numInputs = inputs;
      numOutputs = outputs;
      numHiddenLyrs = hiddenLyrs;
      numNeuronsPerHiddenLyr = neuronsPerHiddenLyr;
   }   
   
   //Builds the network
   private void createNet()
   {
      if(numHiddenLyrs > 0)
      {
         layers = new NeuronLayer[numHiddenLyrs+2];
         layers[0] = new NeuronLayer(numNeuronsPerHiddenLyr, numInputs); //Input layer
      
         for(int i = 1; i < numHiddenLyrs+1; i++)
         {
            layers[i] = new NeuronLayer(numNeuronsPerHiddenLyr, numNeuronsPerHiddenLyr); //Hidden layers
         }
         
         layers[layers.length - 1] = new NeuronLayer(numOutputs, numNeuronsPerHiddenLyr); //Output layer
      }
      else
      {
         layers = new NeuronLayer[1];
         layers[0] = new NeuronLayer(numOutputs, numInputs);
      }
   }
   
   //Return all of the weights from the network (for GA)
   public ArrayList<Double> getWeights()
   {
      ArrayList<Double> weights = new ArrayList<Double>();
      
      for(int i = 0; i < layers.length; i++)
      {
         for(int j = 0; j < layers[i].neurons.length; j++)
         {
            for(int k = 0; k < layers[i].neurons[j].weights.length; k++)
            {
               weights.add(new Double(layers[i].neurons[j].weights[k]));
            }
         }
      }
      
      return weights;
   }
   
   //Sets every weight in network (from GA)
   public void setWeights(ArrayList<Double> weights)
   {
      int counter = 0;
      
      for(int i = 0; i < layers.length; i++)
      {
         for(int j = 0; j < layers[i].neurons.length; j++)
         {
            for(int k = 0; k < layers[i].neurons[j].weights.length; k++)
            {
               layers[i].neurons[j].weights[k] = weights.get(counter).doubleValue();
               counter++;
            }
         }
      }
   }
   
   //Returns the sum of every weight in network
   public double getTotalWeights()
   {
      double weights = 0;
      
      for(int i = 0; i < layers.length; i++)
      {
         for(int j = 0; j < layers[i].neurons.length; j++)
         {
            for(int k = 0; k < layers[i].neurons[j].weights.length; k++)
            {
               weights += layers[i].neurons[j].weights[k];
            }
         }
      }
      
      return weights;
   }
   
   //Sigmoid activation function between 1 and -1
   public double sigmoid(double netInput, double response)
   {
      return (( 1 / ( 1 + Math.exp(-netInput / response)))-0.5)*2;
   }
   
   //Process inputs to outputs
   public ArrayList<Double> update(ArrayList<Double> in)
   {
      if(in.size() != numInputs) return null;
      
      ArrayList<Double> inputs = in;
      ArrayList<Double> outputs = new ArrayList<Double>();
      int weightIndex = 0;
      
      //Process every layer
      for(int i = 0; i < layers.length; i++)
      { 
         if( i > 0 )
         {
            //If it is a hidden layer, the old outputs are the new inputs
            inputs = new ArrayList<Double>(outputs);
         }
         
         outputs.clear();
         weightIndex = 0;
         
         for(int j = 0; j < layers[i].numNeurons; j++)
         {
            double netInput = 0.0;
            
            int numInputs = layers[i].neurons[j].numInputs;
            
            //Sum inputs of current neuron
            for(int k = 0; k < numInputs - 1; k++)
            {
               netInput += layers[i].neurons[j].weights[k] * inputs.get(k).doubleValue();
            }
            
            //Add the activation value of the neuron * BIAS (default -1) to get overall net input
            netInput += layers[i].neurons[j].weights[numInputs - 1] * BIAS;
            
            //Add output of activation function to outputs
            outputs.add(sigmoid(netInput, ACTIVATION));
         }
      }
      
      return outputs;
   }

}
