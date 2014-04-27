import java.io.Serializable;

//Layer of neurons in network
public class NeuronLayer implements Serializable
{
    public int numNeurons;
    public Neuron[] neurons;
   
    public NeuronLayer()
    {
        //Necessary for serialisation
    }
   
    public NeuronLayer(int numNeurons, int numInputsPerNeuron)
    {
        this.numNeurons = numNeurons;
        neurons = new Neuron[numNeurons];
        
        //Create all neurons in the layer
        for(int i = 0; i < numNeurons; i++)
        {
            neurons[i] = new Neuron(numInputsPerNeuron);
        }
    }
}
