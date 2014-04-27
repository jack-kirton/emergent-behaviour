import java.util.List;

//Senses data structure passed to agents
public class Senses
{
    private List<TerrainBlock> blocks;
    private List<Agent> agents;

    public Senses(List<TerrainBlock> blocks, List<Agent> agents)
    {
        this.blocks = blocks;
        this.agents = agents;
    }

    public List<TerrainBlock> getTerrain()
    {
        return blocks;
    }

    public List<Agent> getAgents()
    {
        return agents;
    }
}
