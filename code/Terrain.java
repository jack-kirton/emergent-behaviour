import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

//Class that contains all details of the environment for simulation

public class Terrain implements Serializable
{
    //Types of possible terrain or objects
    public static enum Type
    {
        AIR(0), //Not used
        LIGHT_DIRT(1),
        MEDIUM_DIRT(2),
        HEAVY_DIRT(3),
        ROCK(4),
        WATER(5),
        DYNAMIC_WATER(6), //Not used

        SHORT_GRASS(100), //Surface stuff
        MEDIUM_GRASS(101),
        LONG_GRASS(102),
        TREE(103),
        THIN_BRUSH(104),
        MEDIUM_BRUSH(105),
        THICK_BRUSH(106),

        PLANT(200); //Dynamic object

        private static Map<Integer, Type> types = new TreeMap<Integer, Type>();
        private int id;
        
        //Associate type with integer id
        Type(int id)
        {
            this.id = id;
        }

        static
        {
            for(int i = 0; i < values().length; i++)
            {
                types.put(values()[i].id, values()[i]);
            }
        }
        
        public int getID()
        {
            return id;
        }

        public static Type fromID(int id)
        {
            return types.get(id);
        }
        
    }



    private final String mapDirectory = "./maps/";
    private final String mapImageDir = mapDirectory + "images/";
    private boolean gui;

    public TerrainBlock[][] map;
    transient public BufferedImage display;
    transient public BufferedImage plantImage;
    transient public BufferedImage treeImage; //Not used
    public int seaLevel;
    public int plants = 0; //Amount of plants on terrain

    public Terrain()
    {
        gui = true;
    }

    public Terrain(boolean gui)
    {   
        this.gui = gui;
    }

    //Load a map from a file
    public void loadMap(String name)
    {
        //Get buffered reader of file
        //Set map size
        //Fill in values to 
        
        BufferedReader br = null;

        try
        {
            br = new BufferedReader(new FileReader(mapDirectory + name + ".map"));
        }
        catch(IOException e)
        {
            System.err.println("Could not read file: " + mapDirectory + name + ".map" + "\nNow exiting...");
            try
            {
                if(br != null) br.close();
            }
            catch(Exception ex){}
            System.exit(-1);
        }
        
        try
        {
            //Get initial details, size and sea level
            String[] init = br.readLine().split(",");
            map = new TerrainBlock[Integer.parseInt(init[0])][Integer.parseInt(init[1])];
            seaLevel = Integer.parseInt(init[2]);
            
            //Read values from file and create terrain blocks at correct index
            for(int x = 0; x < map.length; x++)
            {
                for(int y = 0; y < map[x].length; y++)
                {
                    String line = br.readLine();
                    String[] values = line.split(",");
                    //Stored as:
                    //Type_ID, height   OR
                    //Type_ID, Static_ID, height
                    map[x][y] = (values.length == 2) ? new TerrainBlock(Type.fromID(Integer.parseInt(values[0])), Integer.parseInt(values[1])) : new TerrainBlock(Type.fromID(Integer.parseInt(values[0])), Type.fromID(Integer.parseInt(values[1])), Integer.parseInt(values[2]));
                    map[x][y].setPosition(x, y);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("Now exiting...");
            System.exit(-1);
        }
                
    }

    //Saves map to file
    public void saveMap(String name)
    {
        BufferedWriter bw = null;

        try
        {
            bw = new BufferedWriter(new FileWriter(mapDirectory + name + ".map"));
            bw.write(map.length + "," + map[0].length + ",0");
            bw.newLine();

            for(int x = 0; x < map.length; x++)
            {
                for(int y = 0; y < map[x].length; y++)
                {
                    String line = "";
                    line = line.concat(map[x][y].type.id + ",");
                    line = (map[x][y].staticObject != null) ? line.concat(map[x][y].staticObject.id + ",") : line;
                    line = line.concat(Integer.toString(map[x][y].height));

                    bw.write(line);
                    bw.newLine();
                }
            }
            createTerrainImage();
            saveMapImage(name);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("#Terrain: Unable to save map.\nNow exiting...");
            System.exit(-1);
        }
        finally
        {
            try
            {
                if(bw != null) bw.close();
            }
            catch(Exception ex){}
        }
    }

    //Saves the image of the map to a file
    public void saveMapImage(String name)
    {
        File outputfile = new File(mapImageDir + name + ".png");
        try
        {
            ImageIO.write(display, "png", outputfile);
        }
        catch(IOException e)
        {
            System.err.println("#Terrain: Could not save map image.");
        }
    }

    //Generate a new map
    public void generateTerrain(int x, int y, int z, double variance, Type biome)
    {
        map = new TerrainBlock[x][y];

        HeightMapGenerator gen = new HeightMapGenerator();
        gen.setSize(x, y);
        gen.setVariance(variance);

        //Create some smoothed noise
        double[][] heights = gen.generate();

        double highest = heights[0][0];
        double lowest = heights[0][0];

        for(int xi = 0; xi < heights.length; xi++)
        {
            for(int yi = 0; yi < heights[xi].length; yi++)
            {
                if(heights[xi][yi] > highest) highest = heights[xi][yi];
                if(heights[xi][yi] < lowest) lowest = heights[xi][yi];
            }
        }
        

        for(int xi = 0; xi < heights.length; xi ++)
        {
            for(int yi = 0; yi < heights[xi].length; yi++)
            {
                heights[xi][yi] -= lowest;
                if(heights[xi][yi] > highest) highest = heights[xi][yi];
            }
        }

        //Set initial biome type for entire map
        for(int xi = 0; xi < heights.length; xi++)
        {
            for(int yi = 0; yi < heights[xi].length; yi++)
            {
                int heightIndex = (int)((heights[xi][yi]/highest) * z);
                map[xi][yi] = new TerrainBlock(biome, heightIndex);
            }
        }

        //Create terrain types
        for(int xi = 0; xi < heights.length; xi++)
        {
            for(int yi = 0; yi < heights[xi].length; yi++)
            {
                int heightIndex = (int)((heights[xi][yi]/highest) * z);
                System.out.println(heightIndex);

                Type type = setBlockType(xi, yi);
                if (type == null) type = biome;

                map[xi][yi] = new TerrainBlock(type, heightIndex);
            }
        }

        //Settle water
        for(int xi = 0; xi < heights.length; xi++)
        {
            for(int yi = 0; yi < heights[xi].length; yi++)
            {
                int heightIndex = (int)((heights[xi][yi]/highest) * z);
                if(heightIndex < z/5.0) map[xi][yi] = new TerrainBlock(Type.WATER, heightIndex);
                else continue;
            }
        }
    }

    private final float TERRAIN_PROB = 0.2f; //Probability of choosing a surrounding block
    
    //Returns a type for a given position
    private Type setBlockType(int x, int y)
    {
        if(x == 0 || y == 0) return null;
        else if(x == map.length-1 || y == map[0].length-1) return null;

        for(int xi = x-1; xi <= x+1; xi++)
        {
            for(int yi = y-1; yi <= y+1; yi++)
            {
                if (map[xi][yi] == null) continue;
                if (Math.random() < TERRAIN_PROB) return map[xi][yi].type;
            }
        }

        //If not choosing neighbour, choose using this structure
        double p = Math.random();

        if(p < 0.05) return Type.SHORT_GRASS;
        else if(p < 0.1) return Type.MEDIUM_GRASS;
        else if(p < 0.15) return Type.LONG_GRASS;
        else if(p < 0.2) return Type.THIN_BRUSH;
        else if(p < 0.25) return Type.MEDIUM_BRUSH;
        else if(p < 0.3) return Type.THICK_BRUSH;
        else if(p < 0.32) return Type.ROCK;
        else return setBlockType(x, y);
                
    }



    private final int PIXEL_DENSITY = 10;
    public void createTerrainImage() //Creates the terrain image to be drawn
    {
        display = new BufferedImage(map.length*PIXEL_DENSITY, map[0].length*PIXEL_DENSITY, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < map.length; x++)
        {
            for(int y = 0; y < map[0].length; y++)
            {
                Color newColor = Color.BLACK;
                
                switch (map[x][y].type) //Assigns a colour to a given type
                {
                    case AIR:              break; //Not used
                    case LIGHT_DIRT:       newColor = new Color(71, 35, 6); break;
                    case MEDIUM_DIRT:      newColor = new Color(71, 35, 6); break;
                    case HEAVY_DIRT:       newColor = new Color(71, 35, 6); break;
                    case ROCK:             newColor = Color.gray; break;
                    case WATER:            newColor = Color.blue; break;
                    case DYNAMIC_WATER:    newColor = Color.blue; break;
                    case SHORT_GRASS:      newColor = Color.green; break;
                    case MEDIUM_GRASS:     newColor = Color.green.darker(); break;
                    case LONG_GRASS:       newColor = Color.green.darker().darker(); break;
                    case TREE:             newColor = Color.green.darker(); break; //Not used
                    case THIN_BRUSH:       newColor = (new Color(71, 35, 6)).brighter(); break;
                    case MEDIUM_BRUSH:     newColor = (new Color(71, 35, 6)).brighter(); break;
                    case THICK_BRUSH:      newColor = (new Color(71, 35, 6)).brighter(); break;
                }
                if(map[x][y].height % 50 < 2) newColor = Color.white; //Adds contour lines
                
                //Sets the colour in the actual image
                for(int xi = x*PIXEL_DENSITY; xi < x*PIXEL_DENSITY+PIXEL_DENSITY; xi++)
                {
                    for(int yi = y*PIXEL_DENSITY; yi < y*PIXEL_DENSITY+PIXEL_DENSITY; yi++)
                    {
                        display.setRGB(xi, yi, newColor.getRGB());
                    }
                }
            }
        }

        plantImage = null;
        treeImage = null; //Not used

        try
        {
            plantImage = ImageIO.read(new File("./textures/plant.png"));
        }
        catch(IOException e)
        {
            System.err.println("#Terrain: Failed to load dynamic object sprites.\nNow exiting...");
            System.exit(-1);
        }

                        
    }
    
    //Draws the image to surface
    public void drawTerrain(Graphics2D g)
    {
        //Draws basic terrain image
        g.drawImage(display, 0, 0, map.length, map[0].length,null);

        for(int x = 0; x < map.length; x++)
        {
            for(int y = 0; y < map[x].length; y++)
            {
                //Draw dynamic objects
                if(map[x][y].dynamicObject == Type.PLANT)
                {
                    g.drawImage(plantImage, map[x][y].position[0], map[x][y].position[1], 1, 1, null);
                }
            }
        }
    }

    //Add a plant to position if legal
    public void addPlant(int x, int y)
    {
        if(map[x][y].type == Type.WATER || map[x][y].type == Type.ROCK) return;
        map[x][y].dynamicObject = Type.PLANT;
        plants++;
    }

    //Removes a plant from position
    public void removePlant(int x, int y)
    {
        map[x][y].dynamicObject = null;
        plants--;
    }

    //Generates new terrain with arguments: NAME X Y Z VARIANCE
    public static void main(String[] args)
    {
        Terrain t = new Terrain(true);
        t.generateTerrain(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Float.parseFloat(args[4]), Type.SHORT_GRASS); //Biome is set to short grass
        t.saveMap(args[0]);
    } 
}
