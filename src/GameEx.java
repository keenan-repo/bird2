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
	private BufferedImage spriteSheet = null, backGround = null; //this will be used for the sprite sheet when we get one
	//private BufferedImage bird; //this is used in the sprite sheet stuff
	

	public Rectangle screen, bird, wall, top, bot, left, right, cat;
	public Rectangle bounds, outerBounds; 
	public Rectangle[][] blocks = new Rectangle[2][5];
	public Rectangle[] squares = new Rectangle[100];
	public Rectangle[] squaresIn = new Rectangle[100];

	private int spdU, spdD, spdR, spdL, lvl = 0, jump = 5, screenx=200, screeny=200, lenIn;
	private boolean[] keys = new boolean[256];
	private boolean  swap, gotSeed ;
	BufferedImage birdImg, seeds, wood, catImg, back;
	
	private Player p;
	
	public void init(){
	    
	    // Here I create a bunch of blocks and put them at semi random heights just to jump on.
	    // This is where the level needs to be built. 
	    // another class for this would be nice. Not sure how we would do that though. 
	    for (int i = 1; i < 100; i++){
	        double r = Math.random()*50;
	        System.out.println(r);
	        squares[i]=new Rectangle(100*i, (int)(600+r), 0, 0 ); 
	    }

	    squares[0]=new Rectangle(0, 600, 500, 25); // lower
	    squares[1]=new Rectangle(620, 530, 500, 25); //top
	    squares[3]=new Rectangle(620, 700, 700, 75); // lowest
	    
	    
	    // This is a better img loader, i think everything should be loaded. It only loads the file once.
	    //TODO load other images here, textures, sprites. Longterm todo those should go in the sprite sheet
	    BufferedImageLoader loader = new BufferedImageLoader();
	    try{	        
	        spriteSheet = loader.loadImage("/sprite_sheet.png");
	        backGround = loader.loadImage("/mario.png");
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	    
	    addKeyListener(new KeyInput(this));
	    
	    
	    // Sets up the background to be loaded
        BackGround bg = new BackGround(getBackGround());
        back = bg.grabImage(0, 0, 1500, 1500);
	    p = new Player(500, 300, this);

	}
	

    // I barely understand how the next bit works until after run() so don't worry
	private synchronized void start(){
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
        init();
        long lastTime = System.nanoTime();
        final double amountOfTicks = 60.0;
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
       // TODO create the level tracker and put it here. This will start out by switching levels when the bird is in the 
       // right spot. Later it will maybe handle moving the screen along with the bird.
       processInput();
           }

	private GameEx()  {
	    // I don't think this is really needed anymore. Everything done in here is done in init()
	    // Something to look into
		setFocusable(true);

		cat      = new Rectangle(50,  410, 10, 10);
		bounds   = new Rectangle(64, 64, WIDTH-128, HEIGHT-128);
		outerBounds   = new Rectangle(-50, -50, WIDTH+50, HEIGHT+50);
		gotSeed  = false;
		
		// IT IS VERY IMPORTANT THAT THEY ARE THE SAME SIZE. FILL UNUSED SLOTS WITH EMPTY RECTANGLES
		// well...it doesn't. Each... level could have it's own length of blocks and we could just loop through that.
		// It could be a 1 d array of arrays. Then the arrays can be different lengths (I think)
		// The blocks array are the blocks in the level
		
		//I agree this needs a better way to work. Maybe, once we get the scolling to work we can reduce the number of levels
		//by loading a bunch at once then scrolling around
		// Level 0
		blocks[0][0] = new Rectangle(10,200,300,50);
		blocks[0][1] = new Rectangle(350,300,50,50);
		blocks[0][2] = new Rectangle(400,350,50,50);
		blocks[0][3] = new Rectangle(0,0,0,0);
		blocks[0][4] = new Rectangle(0,0,0,0);
		
		// Level 1
		blocks[1][0] = new Rectangle(550,200,100,50); 
		blocks[1][1] = new Rectangle(50,150,150,50);
		blocks[1][2] = new Rectangle(320,260,50,50);
		blocks[1][3] = new Rectangle(270,170,50,50);
		blocks[1][4] = new Rectangle(0,0,0,0);

			    
	    // Start the music and load the sprites
	    try {
	    	//birdImg = ImageIO.read(new File("bird.png"));  // Main character bird image dont need this anymore
	    	seeds   = ImageIO.read(new File("seeds.png")); // Seeds  image
	    	wood    = ImageIO.read(new File("wood.png"));  // Blocks image
	    	catImg  = ImageIO.read(new File("cat.png"));   // Bad guy cat image
	    	
	        // Open an audio input stream.
	    	File soundFile = new File("8bit.wav");
	    	AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
	        // Get a sound clip resource.
	        Clip clip = AudioSystem.getClip();
	        // Open audio clip and load samples from the audio input stream.
	        clip.open(audioIn);
	        //clip.start();
	        //clip.loop(Clip.LOOP_CONTINUOUSLY);// Turning this off for now because my ears
	     } catch (UnsupportedAudioFileException e) {
	        e.printStackTrace();
	     } catch (IOException e) {
	        e.printStackTrace();
	     } catch (LineUnavailableException e) {
	        e.printStackTrace();
	     }
	    
	}
	
	   private void render() {
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
	       //g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	       g.drawImage(back, -screenx, -screeny, 1500, 1500, this);

    
	       
	       // bird is 50 wide by 48 height
	       // this turns the bird. It's kinda broken right now and he spins everytime.
	       // can't figure out how to fix it. Ideas?
	      /* AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	       
	       if(swap && (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT])){
	           tx.translate(-birdImg.getWidth(null), 0);
	           AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	           birdImg = op.filter(birdImg, null);
	           swap=false;
	       } */
	       
	       //draw the player
           p.render(g);
	             
	       //System.out.println("x = " + p.getX() + " y = " + p.getY());
	       
	       
	       
	       // Apply textures and draw the blocks
	       TexturePaint woodtp = new TexturePaint(wood, new Rectangle(0, 0, 50, 50));
	       g2d.setPaint(woodtp);     

	       
	       //System.out.println(screenx + " y = " + screeny);
	       
	       // This loop looks at all the blocks on the level and makes an array of the ones that would be in the screen
	       // it tests the blocks to see if they intersect with where the screen is
	       // if they do, it will adjust the blocks coordinates relative to the screen and store it in a new array
	       // This is more efficient, since now we have an array of only a few elements to loop over rather than the whole map
	       // when we are drawing
	
           lenIn=0;                   
           for(int i=0; i < 100 ; i++){
               Rectangle test = new Rectangle(squares[i].x - screenx, squares[i].y -screeny, squares[i].width, squares[i].height);
               if(test.intersects(outerBounds)){                  
                   squaresIn[lenIn] = test; 
                   lenIn++;                  
               }
           }
	       
	       // draws the squares that are on the screen. 
           for(int i=0; i <lenIn; i++){
               g2d.fill(squaresIn[i]);  
           }
	       g.dispose();
	       bs.show();
	   }
	
    public void menu(){
        //TODO create a start menu and an inventory
	}

	void look() {

		 // The default amount of pixels movement in the specified directions
		 spdU = 32; spdD = 8; spdR = 8; spdL = 8;
		 
         for(int i=0; i < lenIn ; i++){
             check(squaresIn[i]);
         }
		 
		 // Loop through the objects in the map and see if the any of them interesect with the collision boxes
		 /*for(int i = 0 ; i < blocks[1].length; i++){
			 check(blocks[lvl][i]);
		 }*/
		 
		// I needed a separate check for the blocks bounds since we are always IN the screen
		 boundary(bounds);		
		 
		 // this is a shitty way to move the screen as you fall
		 // the collision detection needs work, all the movement in general does
		 // TODO fix collision detection, make it smoother
		 if (!p.getB().intersects(bounds)){
		     screeny += 5; 
		 }
	 }
	
	private void processInput() {
	    
	    // TODO This class needs to be cleaned up as well. We need to decide the range of motions we want for the bird
	    // this will also change as we fix the collision detection
	    // I was just adding things to make it work for now
	    
	    // TODO Make the screen scroll smooth. It should have an acceleration and deacceleration to it
	    
		// If the up key is pressed move the bird up for 4 iterations. This is done to make the movement smooth
		// instead of moving up in 1 frame
		// it will stop moving up until you hit up and you're touching something ( TODO this comment doesn't make sense)
		if(jump <= 4){ // TODO can jump be a local variable?
			// subtract the speed up because the origin is at the top left corner so
			// when the bird jumps he is technically moving down
		    p.setVelY(-spdU);
			jump++;
		} else if (jump > 4) {
		    p.setVelY(spdD);
		}
	
		// If the w key or the up key is pressed, jump
		// If the up direction key is used
	    if(keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]){	  
	    	//playJump(); 	    	// Play the jump sound!
	    	// This is where we check to see if we're on the ground or a surface. 
	        for(int i = 0 ; i < lenIn; i++){
                if(p.getB().intersects(squaresIn[i])) { // if it intersects with a block
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
	        screeny += 3;

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