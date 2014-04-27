import java.io.Serializable;

//A single block of terrain in the environment
public class TerrainBlock implements Serializable
{
    public int[] position;
    public int height; //Height of block
    public Terrain.Type type; //The basic type (such as dirt)
    public Terrain.Type staticObject; //Surface object, not used
    public Terrain.Type dynamicObject; //Surface object like food

    public TerrainBlock(Terrain.Type type, int height)
    {
        this.type = type;
        this.height = height;
        this.staticObject = null;
        this.dynamicObject = null;
        this.position = new int[3];
    }

    public TerrainBlock(Terrain.Type type, Terrain.Type staticObject, int height)
    {
        this.type = type;
        this.height = height;
        this.staticObject = staticObject;
        this.dynamicObject = null;
        this.position = new int[3];
    }

    public TerrainBlock(Terrain.Type type, Terrain.Type staticObject, Terrain.Type dynamicObject, int height)
    {
        this.type = type;
        this.height = height;
        this.staticObject = staticObject;
        this.dynamicObject = dynamicObject;
        this.position = new int[3];
    }

    public void setDynamicObject(Terrain.Type dynamicObject)
    {
        this.dynamicObject = dynamicObject;
    }
    
    public void removeDynamicObject()
    {
        this.dynamicObject = null;
    }

    public void setPosition(int x, int y)
    {
        position[0] = x;
        position[1] = y;
        position[2] = height;
    }
    
}
