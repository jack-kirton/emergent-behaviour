emergent-behaviour
==================

A simulation inspired by natural processes in the hopes of creating emergent behavioural structures.

Basic Initialisation
--------------------

To begin, ensure you have all of the Java source code files in some directory. If you need the source code and the directory structure then 
you can use git and find the repository at "https://github.com/jack-kirton/emergent-behaviour". 
Then ensure that folders exists within this directory like so:

* ./sims/
* ./maps/
* ./settings/
* ./textures/plant.png

After this is completed, you must compile the code using the command: "javac *.java". This should compile all of the classes 
into the current directory.
Then, you must create the default settings file. To do this, simply run "java Settings". This 
will create "./settings/constants.prop" as the default settings file.


Generating Terrain
------------------

To create a new map, you must run the command:

java Terrain NAME X_SIZE Y_SIZE Z_SIZE VARIANCE

This will create a new map that is called NAME in the "./maps/" directory. This map will have a two-dimensional 
width and height of X_SIZE and Y_SIZE respectively. The Z_SIZE parameter indicates the total 
height that the world can be in the third-dimension. Finally, the VARIANCE parameter is a floating point value 
that is used in the generation process to define how rough the final landscape is, with the standard value being 1.0.

To alter the biome of the generated landscape is a little more complex. Within the Terrain class and the main 
function, a call is made to generateTerrain. The final argument in this call is what biome the landscape will be, 
and can be any Terrain.Type instance.


Executing the Program
---------------------

To execute the program, all that is necessary is to use the command "java Main". This will begin by asking for some data, 
which is self explanatory. After this data collection the GUI will be launched. To control the GUI, ensure the window has 
focus and use the controls:

    Move: Arrow keys
    Zoom: Page Up/Down keys


Data will also be printed to the console after each simulation about the fitness values of each population.
To exit the system, either close the GUI window or ensure the console window is in focus and use Ctrl+C to stop execution. 
The simulation is saved automatically after each generation so to resume a simulation just type its name when prompted at launch time.
