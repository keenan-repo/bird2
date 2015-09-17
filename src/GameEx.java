import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.Dimension;
import java.awt.Canvas;
import java.io.File;
import java.io.IOException;


import java.util.Random;

import javax.imageio.ImageIO;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

// TODO, rename variables still needs to be done
// put in getters and setters
// TODO change all private shit to private

/*
 * Conventions
 * variable names     lowerCamelCase
 * methods(functions) lowerCamelCase
 * class names        UpperCamelCase
 * 
 * For comments, leave a space and start them with a capital if it is the start of a 
 * paragraph like this.
 */

@SuppressWarnings("serial")
public class GameEx extends Canvas implements Runnable {
	
	public static final int WIDTH = 700;
	public static final int HEIGHT = WIDTH / 12 * 9;
	public static final int SCALE = 1;
	public final String TITLE = "Bird Game";
	
	
	private boolean running = false;
	private Thread thread;
	
	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private BufferedImage spriteSheet = null, backGround = null; 
    BufferedImage birdImg, seeds, wood, catImg, back; //this will all be gone after a sprite sheet
	
	public Rectangle screen, bird, wall, cat;
	public Rectangle bounds = new Rectangle(64, 64, WIDTH-128, HEIGHT-128);
	public Rectangle outerBounds   = new Rectangle(-50, -50, WIDTH+50, HEIGHT+50);
	public Rectangle[][] blocks = new Rectangle[2][100];
	public Rectangle[] blocksOnScreen = new Rectangle[100];
   
	private int MAX_JUMP=24;
	private int spdU, spdD, spdR, spdL, lvl = 0, jump = MAX_JUMP+1, screenx=200, screeny=200, lenIn;
	private boolean[] keys = new boolean[256];
	private boolean  swap, gotSeed, is_shooting=false ;

	
	private Player p;
	private Controller c;
	private Textures tex;
	
	public void init(){

	    
	    // Here I create a bunch of blocks and put them at semi random heights just to jump on.
	    // This is where the level needs to be built. 
	    // another class for this would be nice. Not sure how we would do that though. 
	    for (int i = 1; i < 100; i++){
	        double r = Math.random()*50;
	        System.out.println(r);
	        blocks[0][i]=new Rectangle(100*i, (int)(600+r), 0, 0 ); 
	    }

	    blocks[0][0]=new Rectangle(0, 600, 500, 25); // lower
	    blocks[0][1]=new Rectangle(620, 530, 500, 25); //top
	    blocks[0][4]=new Rectangle(620, 400, 250, 25); //topest
	    blocks[0][3]=new Rectangle(620, 700, 700, 75); // lowest
	    
	    
	    // This is a better img loader, i think everything should be loaded. It only loads the file once.
	    //TODO those should go in the sprite sheet
	    BufferedImageLoader loader = new BufferedImageLoader();
	    try{	        
	        spriteSheet = loader.loadImage("/sprite_sheet.png");
	        backGround = loader.loadImage("/mario.png");
	        wood = loader.loadImage("/wood.png");
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	    
	    addKeyListener(new KeyInput(this));
	    
	    tex = new Textures(this);
	    
	    
	    // Sets up the background to be loaded
        BackGround bg = new BackGround(getBackGround());
        back = bg.grabImage(0, 0, 1500, 1500);
	    p = new Player(500, 100, tex);
	    c = new Controller(this, tex);

	}
	

    // I barely understand how the next bit works until after run() so don't worry
	private synchronized void start(){
        setFocusable(true);
		if(running)
			return;
		
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	private synchronized void stop(){
		if(!running)
			return;
		
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		System.exit(1);
	}
	
    public void run() {
        setFocusable(true);
        init();
        long lastTime = System.nanoTime();
        final double amountOfTicks = 120.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int updates = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();
            while(running){
                long now = System.nanoTime();
                delta += (now - lastTime) / ns;
                lastTime = now;
                if(delta >= 1){
                    tick();
                    updates++;
                    delta--;
                }
                render();
                frames++;
                
                if(System.currentTimeMillis() - timer > 1000){
                    timer += 1000;
                    System.out.println(updates + " Ticks, Fps " + frames);
                    updates = 0;
                    frames = 0;
                }
            }
            stop();
       }
        

   private void tick() {
       look();
       p.tick();
       c.tick();
       // TODO create the level tracker and put it here. This will start out by switching levels when the bird is in the 
       // right spot. Later it will maybe handle moving the screen along with the bird.
       processInput();
           }

	
	   private void render() {
	        setFocusable(true);
	       BufferStrategy bs = this.getBufferStrategy();    
	       if(bs == null) {
	           // from what he said, this makes a buffer of the screen 3 ticks into the future. 
	           // its used to improve performance. Basically the computer is always ready to draw the next three frames
	           createBufferStrategy(3);
	           return;
	       }
	       
	       Graphics g = bs.getDrawGraphics();

	       //stop scrolling so you dont end up off the screen

	       if (screenx < 5)
	           screenx=5;
	       else if (screenx > 790)
	           screenx=790;
	              
	       if (screeny < 5)
	           screeny=5;
           else if (screeny > 975)
               screeny=975;
	       
	       
           Graphics2D g2d = (Graphics2D)g;
           //draw the background
	       g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	       //g.drawImage(backGround, -screenx, -screeny, 1500, 1500, this);

	       
	       // bird is 50 wide by 48 height
	       // this turns the bird. It's kinda broken right now and he spins everytime.
	       // can't figure out how to fix it. Ideas?

	       //draw the player
           p.render(g);
           c.render(g);
	             
	       //System.out.println("x = " + p.getX() + " y = " + p.getY());
	       
	       
	       
	       // Apply textures and draw the blocks
	       TexturePaint woodtp = new TexturePaint(wood, new Rectangle(-screenx, -screeny, 50, 50));
	       g2d.setPaint(woodtp);     

	       
	       //System.out.println(screenx + " y = " + screeny);
	       
	       // This loop looks at all the blocks on the level and makes an array of the ones that would be in the screen
	       // it tests the blocks to see if they intersect with where the screen is
	       // if they do, it will adjust the blocks coordinates relative to the screen and store it in a new array
	       // This is more efficient, since now we have an array of only a few elements to loop over rather than the whole map
	       // when we are drawing
	
           lenIn=0;                   
           for(int i=0; i < 100 ; i++){
               Rectangle test = new Rectangle(blocks[0][i].x - screenx, blocks[0][i].y -screeny, blocks[0][i].width, blocks[0][i].height);
               if(test.intersects(outerBounds)){                  
                   blocksOnScreen[lenIn] = test; 
                   lenIn++;                  
               }
           }
	       
	       // draws the squares that are on the screen. 
           for(int i=0; i <lenIn; i++){
               g2d.fill(blocksOnScreen[i]);  
           }

	       g.dispose();
	       bs.show();
	   }
	
    public void menu(){
        //TODO create a start menu and an inventory
	}

	void look() {

		 // The default amount of pixels movement in the specified directions
		 spdU = 8; spdD = 2; spdR = 4; spdL = 4;
		 
         for(int i=0; i < lenIn ; i++){
             check(blocksOnScreen[i]);
         }
		 
		// I needed a separate check for the blocks bounds since we are always IN the screen
		 boundary(bounds);		
		 
		 // this is a shitty way to move the screen as you fall
		 // the collision detection needs work, all the movement in general does
		 // TODO fix collision detection, make it smoother
		 if (!p.getB().intersects(bounds)){
		     screeny += 4;
		     p.setVelY(-4);
		 } else if (!p.getT().intersects(bounds)){
	             screeny -= 4;
	             p.setVelY(4);
	         }
	 }
	
	private void processInput() {
	    
	    // TODO This class needs to be cleaned up as well. We need to decide the range of motions we want for the bird
	    // this will also change as we fix the collision detection
	    // I was just adding things to make it work for now
	    
	    // TODO Make the screen scroll smooth. It should have an acceleration and deacceleration to it
	    
		// If the up key is pressed move the bird up for 4 iterations. This is done to make the movement smooth
		// instead of moving up in 1 frame
		if(jump <= MAX_JUMP){ 
		    p.setVelY(-spdU);
			jump++;
		} else if (jump > MAX_JUMP) {
		    p.setVelY(spdD);
		}
	
		// If the w key or the up key is pressed, jump
		// If the up direction key is used
	    if(keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]){	  
	    	//playJump(); 	    	// Play the jump sound!
	    	// This is where we check to see if we're on the ground or a surface. 
	        for(int i = 0 ; i < lenIn; i++){
                if(p.getB().intersects(blocksOnScreen[i])) { // if it intersects with a block
                    jump = 0;
                    System.out.println("up");
                } else if (!p.getB().intersects(bounds)){ // if it intersects with the bottom of the map
                    jump = 0;                   
                }
            }     

	    	keys[KeyEvent.VK_UP]=false; keys[KeyEvent.VK_W]=false;

		}
	    
	    // We will only need this if the bird is going into a hole or some other reason to go down
	    if(keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]){
	        //p.setVelY(spdD);

	    } 

	    // If the left direction key is used
	    if(keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]){
	        if(p.getX() < 200) {
	            screenx -= 3;
	            p.setVelX(0);
	        } else if (p.getX() > 200)            
	            p.setVelX(-spdL); 	                 
	        }        
	    
	    // If the right direction key is used
	    if(keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]){
	        swap=true;
            if(p.getX() > 550) {
                screenx += 3;
                p.setVelX(0);
            } else if (p.getX() < 550)            
                p.setVelX(spdR);                    
            }   

	     
	    // Always set the birds speed to be downwards
	    // TODO add a proper gravity. Maybe a floor or a check if they fall off a cliff
	    
        /*for(int i = 0 ; i < lenIn; i++){
            if(!p.getB().intersects(squaresIn[i])) { // if it intersects with a block
                p.setVelY(spdD);
            } else if (p.getB().intersects(bounds)){ // if it intersects with the bottom of the map
                spdD=0;                   
            }
        } */
	    if (keys[KeyEvent.VK_SPACE] && !is_shooting){
	        c.addBullet(new Bullet(p.getX(), p.getY(), tex));
	        is_shooting = true;
	        keys[KeyEvent.VK_SPACE]=false;
	    }
	}
	
	private void processEnemy(){
		// TODO make the cat walk around and if it intersects with the collision boxes
		// reset the position of the bird to the start of the level
		// I think the level could be a class and it could have a starting position that we
		// can set and call	
		if (getCatX() < 60){
			setCatX(getCatX() + 10);
		} else if (getCatX() > 200) {
			setCatX(getCatX() - 10);
		}
	}
		
	public void keyPressed(KeyEvent e) {
	    keys[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
	    keys[e.getKeyCode()] = false;
	    if (e.getKeyCode() == KeyEvent.VK_SPACE){
	        is_shooting = false;
	    }
	    p.setVelX(0);
	    //p.setVelY(0);
//		    swap=true; // I think this is why the bird changes direction when you don't want to
	    // TODO What we need to do is if the left key is pressed
	    // set some boolean isChangeDirection(true) 
	    // then if if that's true and the right key is clicked, trigger the affine transform
	    // and set the variable to false
	    // then vice versa for going left
	}

	public void keyTyped(KeyEvent e) {
	}	
	


	public static void main(String[] args) {
		GameEx game = new GameEx();
			
		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		JFrame frame = new JFrame(game.TITLE);
		frame.add(game);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
        frame.setVisible(true); 

	
        game.start();
	}
	
	/* Start getters and setters */
  
	int getCatY(){return this.cat.y;}
	void setCatY(int catY){this.cat.y = catY;}
	
	int getCatX(){return this.cat.x;}
    void setCatX(int catX){this.cat.x = catX;}
    
    
    // TODO doesn't need to be a function since we only call it once. Or should turn spds into array and rectangles too.
    // rename to checkCollision
    // check if the collision boxes intersects with any objects on the map
	 void check(Rectangle r) {			
         if (p.getT().intersects(r)) {
                spdU=0;
                System.out.println("hit head");
            }  if(p.getB().intersects(r)) {
                spdD=0;
            }  if(p.getR().intersects(r)) {
                spdR=0;         
            }  if(p.getL().intersects(r)) {
                spdL=0;
            }
		 
	 }
	 // check if the collision boxes intersect with the boundaries of the map
	 // rename to boundaryCollision
	 void boundary(Rectangle r) {		
         if (!p.getT().intersects(r)) {
                spdU=0;
            }  if(!p.getB().intersects(r)) {
                spdD=0;
            }  if(!p.getR().intersects(r)) {
                spdR=0;         
            }  if(!p.getL().intersects(r)) {
                spdL=0;
            }
	 }

	 // TODO This function is only called once again so it doesn't need to be a function
	 // Play jump sound
	 // TODO change to playJumpSound/remove
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
	
	// Not implemented, will be played when the bird walks/steps
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
	
	public BufferedImage getSpriteSheet(){
	    return spriteSheet;
	}
    public BufferedImage getBackGround() {
    return backGround;
    }

}