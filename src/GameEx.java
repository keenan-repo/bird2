import java.awt.Graphics2D;

import javax.sound.sampled.*;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.util.*;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.File;
//import java.awt.geom.Line2D;




import javax.imageio.ImageIO;

import java.io.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

//import javax.swing.WindowConstants;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class GameEx extends JPanel {
	
	public VGTimerTask vgTask;
	public JFrame frame;
	public Rectangle screen, bird, wall, top, bot, left, right, cat ;
	public Rectangle bounds ; 
	public Rectangle[][] maps = new Rectangle[2][5];
	public int x_pos=550 , y_pos=100, spdU, spdD, spdR, spdL, Lvl=0, jump=5;
	public boolean[] keys = new boolean[256];
	public boolean keyL, keyR, keyU, keyD, swap, got_seed ;
	BufferedImage bird_img, seeds, wood, cat_img;

	public GameEx()  {
		
		KeyListener listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);
		screen   = new Rectangle(0, 0, 700, 500);
		bird     = new Rectangle(x_pos,  y_pos, 10, 10);
		cat      = new Rectangle(50,  410, 10, 10);
		bounds   = new Rectangle(50, 50, 600, 400);
		frame    = new JFrame("Bird Game");
		got_seed = false;
		
		// IT IS VERY IMPORTANT THAT THEY ARE THE SAME SIZE. FILL UNUSED SLOTS WITH EMPTY RECTANGLES
		// The maps array are the blocks in the level
		// Level 0
		maps[0][0]=new Rectangle(10,200,300,50);
		maps[0][1]=new Rectangle(350,300,50,50);
		maps[0][2]=new Rectangle(400,350,50,50);
		maps[0][3]=new Rectangle(0,0,0,0);
		maps[0][4]=new Rectangle(0,0,0,0);
		
		// Level 1
		maps[1][0]=new Rectangle(550,200,100,50); 
		maps[1][1]=new Rectangle(50,150,150,50);
		maps[1][2]=new Rectangle(320,260,50,50);
		maps[1][3]=new Rectangle(270,170,50,50);
		maps[1][4]=new Rectangle(0,0,0,0);

		vgTask = new VGTimerTask();	
			    
	    // Start the music and load the sprites
	    try {
	    	// TODO the convention in java for variables is lower camel case
	    	// i.e. birdImg
	    	bird_img = ImageIO.read(new File("sprite.png")); // Main character
	    	seeds    = ImageIO.read(new File("seeds.png"));  // Have to get these to beat the level
	    	wood     = ImageIO.read(new File("wood.png"));   // The image for the blocks
	    	cat_img  = ImageIO.read(new File("cat.png"));    // Bad guyi
	        // Open an audio input stream.
	    	File soundFile = new File("little.wav");
	    	AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
	        // Get a sound clip resource.
	        Clip clip = AudioSystem.getClip();
	        // Open audio clip and load samples from the audio input stream.
	        clip.open(audioIn);
	        clip.loop(clip.LOOP_CONTINUOUSLY);
	     } catch (UnsupportedAudioFileException e) {
	        e.printStackTrace();
	     } catch (IOException e) {
	        e.printStackTrace();
	     } catch (LineUnavailableException e) {
	        e.printStackTrace();
	     }
	    
	    //hit box. This should be turned into an array as well
		top= new Rectangle(getBirdX(),  getBirdY()-10, bird_img.getWidth(), 10);
		bot= new Rectangle(getBirdX(),  getBirdY()+bird_img.getHeight(), bird_img.getWidth(), 10);
		left= new Rectangle(getBirdX()-10,  getBirdY(), 10, bird_img.getHeight());
		right= new Rectangle(getBirdX()+bird_img.getWidth(),  getBirdY(), 10, bird_img.getHeight());
	}
	


	public void paint(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		//bounds = g.getClipBounds();

		g2d.clearRect(screen.x,  screen.y,  screen.width, screen.height);
		//g2d.fill(box);
		
		//bird is 50 wide by 48 height
		//this turns the bird. It's kinda broken right now and he spins everytime.
		//can't figure out how to fix it. Ideas?
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);

		if(swap && (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT])){
			tx.translate(-bird_img.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bird_img = op.filter(bird_img, null);
			swap=false;
	    } 
		
		//draw the bird
		g2d.drawImage(bird_img, getBirdX(),  getBirdY(), null);
		g2d.drawImage(cat_img, getCatX(),  getCatY(), null);

		//enable this to see the birds coordinates
		//System.out.println("X= " + getBirdX() + " Y= " + getBirdY() + " Lvl= " + Lvl);
		
		if (getBirdX()==50 && getBirdY()==150 && Lvl==0){
			Lvl=1;
			setBirdX(590);
			setBirdY(150);
		} 
		if (getBirdY()>400 && Lvl==1){
			Lvl=0;
			setBirdX(590);
			setBirdY(390);
		} 
		if(getBirdX()==600 && getBirdY()==150 && Lvl==1){
			Lvl=0;
			setBirdX(60);
			setBirdY(150);
		} 
		if(Lvl == 1 && !got_seed){
			g2d.drawImage(seeds, 50,  50, null);
			if (getBirdX()==50 && getBirdY() == 50){
				got_seed = true;
			}
		}
		
		//top		
		setTop(getBirdX(),  getBirdY()-10, bird_img.getWidth(), 10);
		
		//bottom
		setBot(getBirdX(),  getBirdY()+bird_img.getHeight(), bird_img.getWidth(), 10);
		
		//left
		setLeft(getBirdX()-10,  getBirdY(), 10, bird_img.getHeight());
		
		//right
		setRight(getBirdX()+bird_img.getWidth(),  getBirdY(), 10, bird_img.getHeight());
		
		/*g2d.draw(top);
		g2d.draw(bot);
		g2d.draw(left);
		g2d.draw(right);*/
		
		//box or any other stuff
		TexturePaint woodtp = new TexturePaint(wood, new Rectangle(0, 0, 50, 50));
		g2d.setPaint(woodtp);
		
		for(int i=0; i < maps[0].length ; i++){
			g2d.fill(maps[Lvl][i]);
		}

		g2d.draw(bounds);
		
		System.out.println("x = " + getBirdX() + " y = " + getBirdY());
		}

	
	 void look() {
		 //we need to initially say we're not hitting anything
		 //then check if we are and make the spd in that direction 0
		 spdU=30; spdD=10; spdR=10; spdL=10 ;
		 
		 for(int i=0 ; i < maps[1].length; i++){
			 check(maps[Lvl][i]);
		 }
		 
		//I needed a separate check for the maps bounds since we are always IN the screen
		 boundary(bounds);		
	 }

	
	public void processInput() {
		//this is my way of jumping. I didn't want an instant jump so it moves a bit up each run
		//after it hits the max, it will stop moving up until you hit up and you're touching something
		int x0=getBirdX();
		if(jump <= 4){
			setBirdY(getBirdY()-spdU);
			jump++;
		}
		
	    if(keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]){	    	
	    	//play the jump sound!
	    	playJump();
	    	//this is where we check to see if we're on the ground or a surface. THIS IS BROKEN NOT ALLOWING YOU TO JUMP ON THE NEXT BLOCK
	    	for(int i=0; i < maps[1].length; i++){
		    	if(bot.intersects(maps[Lvl][i])) {
					jump = 0;
		    	} else if (!bot.intersects(bounds)){
		    		jump = 0;
		    	}
	    	}
		    	    	
		}
	    keys[KeyEvent.VK_UP]=false;

	    if(keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]){
	        setBirdY(getBirdY()+spdD);
	    } else {
	    	//spdD=0;
	    }

	    if(keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]){
	        setBirdX(getBirdX()-spdL);
	        keyR=false;
	        keyL=true;
	    	
	    } else {
	    	spdL=0;
	    }

	    if(keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]){
	        setBirdX(getBirdX()+spdR);
	        keyR=true;
	        keyL=false;

	    } else {
	    	spdR=0;
	    }
	    setBirdY(getBirdY()+spdD);
	    int x1=getBirdX();
	    if (Math.abs(x1-x0) > 9){
	    	//playSteps();
	    }
	}
	
	public void processEnemy(){
		if (getCatX()<60){
			setCatX(getCatX()+10);
		} else if (getCatX()>200) {
			setCatX(getCatX()-10);
		}
	}
	
	//Below are where things are run
	//look() checks the collision boxes around the location
	//proccessInput() handles the input, move, jump etc
	//then we repaint the frame
	
	 class VGTimerTask extends TimerTask{
		 public void run() {
			 look();
			 processInput();
			 frame.repaint();
		 	}
		 
	 }
	
	public class MyKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
		    keys[e.getKeyCode()] = true;
		}

		public void keyReleased(KeyEvent e) {
		    keys[e.getKeyCode()] = false;
		    swap=true;
		}

		public void keyTyped(KeyEvent e) {
		}	
	}
	public void update(LineEvent le) {
	    LineEvent.Type type = le.getType();
	    if (type == LineEvent.Type.OPEN) {
	      System.out.println("OPEN");
	    } else if (type == LineEvent.Type.CLOSE) {
	      System.out.println("CLOSE");
	      System.exit(0);
	    } 
	  }
	

	public static void main(String[] args) {
		java.util.Timer vgTimer = new java.util.Timer();
		GameEx panel = new GameEx();

	
		//Set up the frame
	    panel.frame.setSize(panel.screen.width, panel.screen.height);
        panel.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

	    panel.frame.setContentPane(panel);     

        panel.frame.setVisible(true); 
		
		vgTimer.schedule(panel.vgTask, 0, 30);
		
	}
	
	int getBirdY(){
		return this.bird.y;
		}
	void setBirdY(int birdY){
		this.bird.y = birdY;
		}
	
	int getBirdX(){
		return this.bird.x;
	}
    void setBirdX(int birdX){
    	this.bird.x = birdX;
    	}
    
	int getCatY(){
		return this.cat.y;
		}
	void setCatY(int catY){
		this.cat.y = catY;
		}
	
	int getCatX(){
		return this.cat.x;
	}
    void setCatX(int catX){
    	this.cat.x = catX;
    	}
    
    void setTop(int x, int y, int w, int h){
    	this.top.x = x;
    	this.top.y = y;
    	this.top.width = w;
    	this.top.height = h;
    	}
    void setBot(int x, int y, int w, int h){
    	this.bot.x = x;
    	this.bot.y = y;
    	this.bot.width = w;
    	this.bot.height = h;
    	}
    void setRight(int x, int y, int w, int h){
    	this.right.x = x;
    	this.right.y = y;
    	this.right.width = w;
    	this.right.height = h;
    	}
    void setLeft(int x, int y, int w, int h){
    	this.left.x = x;
    	this.left.y = y;
    	this.left.width = w;
    	this.left.height = h;
    	}
    
	 void check(Rectangle r) {
		 //spdU=10; spdD=10; spdR=10; spdL=10 ;
			if (top.intersects(r)) {
				spdU=0;
			}  if(bot.intersects(r)) {
				spdD=0;
			}  if(right.intersects(r)) {
				spdR=0;			
			}  if(left.intersects(r)) {
				spdL=0;
			}
		 
	 }
	 void boundary(Rectangle r) {
		 //spdU=10; spdD=10; spdR=10; spdL=10 ;
			if (!top.intersects(r)) {
				spdU=0;
			}  if(!bot.intersects(r)) {
				spdD=0;
			}  if(!right.intersects(r)) {
				spdR=0;			
			}  if(!left.intersects(r)) {
				spdL=0;
			}
		 
	 }

	void playJump(){
		try {
	        // Open an audio input stream.
	    	File soundJump = new File("jump.wav");
	    	AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundJump);
	        // Get a sound clip resource.
	        Clip clip = AudioSystem.getClip();
	        // Open audio clip and load samples from the audio input stream.
	        clip.open(audioIn);
	        clip.start();
	     } catch (UnsupportedAudioFileException e) {
	        e.printStackTrace();
	     } catch (IOException e) {
	        e.printStackTrace();
	     } catch (LineUnavailableException e) {
	        e.printStackTrace();
	     }
		
	}
	void playSteps(){
		try {
	        // Open an audio input stream.
	    	File sound = new File("steps.wav");
	    	AudioInputStream audioIn = AudioSystem.getAudioInputStream(sound);
	        // Get a sound clip resource.
	        Clip clip = AudioSystem.getClip();
	        // Open audio clip and load samples from the audio input stream.
	        clip.open(audioIn);
	        clip.start();
	     } catch (UnsupportedAudioFileException e) {
	        e.printStackTrace();
	     } catch (IOException e) {
	        e.printStackTrace();
	     } catch (LineUnavailableException e) {
	        e.printStackTrace();
	     }
		
	}

}