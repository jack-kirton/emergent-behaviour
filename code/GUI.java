import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

//Simple GUI to allow for viewing simulation as it progresses

public class GUI extends JFrame implements KeyListener
{
    private Simulation sim;
    private Surface surface;

	public GUI(Simulation s)
	{
        this.sim = s;

		setTitle("Genetic Algorithms");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Set to draw simulation on main surface        
        surface = new Surface(sim);
        add(surface);
        
        setSize(800 + 2, 800 + 33);
        setResizable(false);
        setLocationRelativeTo(null);
		setVisible(true);

        addKeyListener(this);
	}

    public void keyPressed(KeyEvent e)
    {
        //Handles movement and zooming of surface
        switch(e.getKeyCode())
        {
            case KeyEvent.VK_UP:    surface.setPosY(surface.getPosY() - 5); break;
            case KeyEvent.VK_DOWN:  surface.setPosY(surface.getPosY() + 5); break;
            case KeyEvent.VK_LEFT:  surface.setPosX(surface.getPosX() - 5); break;
            case KeyEvent.VK_RIGHT: surface.setPosX(surface.getPosX() + 5); break;
            case KeyEvent.VK_PAGE_UP:  surface.setZoom(surface.getZoom() + 0.1f); break;
            case KeyEvent.VK_PAGE_DOWN: surface.setZoom(surface.getZoom() - 0.1f); break;
        }
    }

    public void keyReleased(KeyEvent e)
    {
        
    }

    public void keyTyped(KeyEvent e)
    {
        
    }

	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() 
            {
                Simulation sim = new Simulation(true, "gen_test", "gui-main");
                sim.init();
                
                GUI gui = new GUI(sim);
                sim.run();
            }
        });
	}
	
	class Surface extends JPanel implements ActionListener
	{
        private Timer timer; //Controls the repainting of the panel
        private Simulation sim;
        private float zoom;
        private int posX = 0;
        private int posY = 0;
        
        public Surface(Simulation s)
        {
            sim = s;
            initSurface();
            initTimer();
        }

        private void initSurface()
        {
            setBackground(Color.white);
            
            //TODO: Calculate correct mapping of initial zoom and map size when not equal to screen size

            int scale = (getWidth() > getHeight()) ? getHeight() : getWidth();
            //zoom = (float)sim.terrain.map.length/(float)scale;

            //zoom = 1.5f;

            setZoom((float)scale/(float)sim.terrain.map.length);
        }

        private void initTimer()
        {
            timer = new Timer(10, this);
            timer.setInitialDelay(10);
            timer.start(); 
        }

        public void setZoom(float z)
        { 
            zoom = z;
            zoom = (zoom < 1) ? 1 : zoom;
            zoom = (zoom > 20) ? 20 : zoom;
            //setPosX(posX);
            //setPosY(posY);
        }
        public float getZoom(){ return zoom; }

        public void setPosX(int x)
        { 
            posX = x;
            //posX = (posX < 0) ? 0 : posX;
            //posX = (posX > (int)(sim.terrain.map.length*zoom) - getWidth()) ? (int)(sim.terrain.map.length*zoom) - getWidth() : posX;
        }
        public int getPosX(){ return posX; }
        
        public void setPosY(int y)
        { 
            posY = y;
            //posY = (posY < 0) ? 0 : posY;
            //posY = (posY > (int)(sim.terrain.map[0].length*zoom) - getHeight()) ? (int)(sim.terrain.map[0].length*zoom) - getHeight() : posY;
        }
        public int getPosY(){ return posY; }

		private void doDrawing(Graphics g) 
		{

			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(new Color(150, 150, 150));

			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			rh.put(RenderingHints.KEY_RENDERING,
				  RenderingHints.VALUE_RENDER_QUALITY);

			g2d.setRenderingHints(rh);

			g2d.fillRect(30, 20, 50, 50);
			g2d.fillRect(120, 20, 90, 60);
			g2d.fillRoundRect(250, 20, 70, 60, 25, 25);

			g2d.fill(new Ellipse2D.Double(10, 100, 80, 100));
			g2d.fillArc(120, 130, 110, 100, 5, 150);
			g2d.fillOval(270, 130, 50, 50);   
	    } 

		@Override
		public void paintComponent(Graphics g) 
        {
			super.paintComponent(g);
            
			//doDrawing(g);

            Graphics2D g2d = (Graphics2D) g;

			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			rh.put(RenderingHints.KEY_RENDERING,
				  RenderingHints.VALUE_RENDER_QUALITY);

			g2d.setRenderingHints(rh);

            //g2d.setClip(view);
            g2d.scale(zoom, zoom);
            g2d.translate(-posX, -posY);

            sim.draw(g2d);
		}

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            repaint();
        }   

	}
}
