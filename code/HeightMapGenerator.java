import java.util.Arrays;

//Class used to generate a smooted heightmap using the Diamond Square Algorithm

public class HeightMapGenerator 
{

    private int gensize;
    private int width;
    private int height;
    private double variance;

    public HeightMapGenerator()
    { 
        gensize = (int)Math.pow(2, 9) + 1;
        width = gensize;
        height = gensize;
        variance = 1;
    }
        
    //Sets the size of the map to be generated
    public void setSize(int width, int height)
    {       
        this.width = width;
        this.height = height;
                
        // gensize must be in the form 2^n + 1 and
        // also be greater or equal to both the width
        // and height
        double w = Math.ceil(Math.log(width)/Math.log(2));
        double h = Math.ceil(Math.log(height)/Math.log(2));
                
        if(w > h) gensize = (int)Math.pow(2, w) + 1;
        else gensize = (int)Math.pow(2, h) + 1;
    }

        
    //Can set size using gensize, not used
    public void setGenerationSize(int n)
    {
        gensize = (int)Math.pow(2, n) + 1;
        width = gensize;
        height = gensize;
    }
        
    //Sets the variance of generated heightmap, default is 1
    public void setVariance(double v)
    {
        variance = v;
    }
        

    //Generates heightmap
    public double[][] generate() 
    {

        double[][] map = new double[gensize][gensize];

        // Place initial seeds for corners
        map[0][0] = Math.random();
        map[0][map.length - 1] = Math.random();
        map[map.length - 1][0] = Math.random();
        map[map.length - 1][map.length - 1] = Math.random();

        map = generate(map);
        
        //Cut array down to correct size        
        if(width < gensize || height < gensize)
        {
            double[][] temp = new double[width][height];
                        
            for(int i = 0; i < temp.length; i++)
            {
                temp[i] = Arrays.copyOf(map[i], temp[i].length);
            }
                        
            map = temp;
                        
        }
                
        return map;
    }
        
    //Generates the heightmap given initial seeds for corners, above function uses this
    public double[][] generate(double[][] map)
    {
        map = map.clone();
        int step = map.length - 1;
        double v = variance;
                
        while(step > 1)
        {
            // SQUARE STEP
            for(int i = 0; i < map.length - 1; i += step)
            {
                for(int j = 0; j < map[i].length - 1; j += step)
                {
                    double average = (map[i][j] + map[i + step][j] + map[i][j + step] + map[i+step][j+step])/4;

                    if(map[i + step/2][j + step/2] == 0) // check if not pre-seeded
                    {
                        map[i + step/2][j + step/2] = average + randVariance(v);
                    }
                }       
            }

            // DIAMOND STEP
            for(int i = 0; i < map.length - 1; i += step)
            {
                for(int j = 0; j < map[i].length - 1; j += step)
                {

                    if(map[i + step/2][j] == 0) // check if not pre-seeded
                    {
                        map[i + step/2][j] = averageDiamond(map, i + step/2, j, step) + randVariance(v);
                    }

                    if(map[i][j + step/2] == 0)
                    {
                        map[i][j + step/2] = averageDiamond(map, i, j + step/2, step) + randVariance(v);
                    }

                    if(map[i + step][j + step/2] == 0)
                    {
                        map[i + step][j + step/2] = averageDiamond(map, i + step, j + step/2, step) + randVariance(v);
                    }

                    if(map[i + step/2][j + step] == 0)
                    {
                        map[i + step/2][j + step] = averageDiamond(map, i + step/2, j + step, step) + randVariance(v);
                    }
                }       
            }

            v /=2;
            step /= 2;
            
        }
                
        return map;
    }
    
    //Performs the diamond smoothing
    private double averageDiamond(double[][] map, int x, int y, int step)
    {
        int count = 0;
        double average = 0;

        if(x - step/2 >= 0)
        {
            count++;
            average += map[x - step/2][y];
        }

        if(x + step/2 < map.length)
        {
            count++;
            average += map[x + step/2][y];
        }

        if(y - step/2 >= 0)
        {
            count++;
            average += map[x][y - step/2];
        }

        if(y + step/2 < map.length)
        {
            count++;
            average += map[x][y + step/2];
        }

        return average/count;
    }

    //Creates a random between +-variance
    private double randVariance(double v)
    {
        return Math.random()*2*v - v;
    }

}
