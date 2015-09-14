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

// TODO, rename variables
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
	private BufferedImage spriteSheet = null; //this will be used for the sprite sheet when we get one
	//private BufferedImage bird; //this is used in the sprite sheet stuff
	

	public Rectangle screen, bird, wall, top, bot, left, right, cat;
	public Rectangle bounds; 
	public Rectangle[][] blocks = new Rectangle[2][5];

	// we don't need a up and down speed. We can just have a vertical speed
	// that can be positive and negative, we can also do that with left/right
	private int xPos = 550 , yPos = 100, spdU, spdD, spdR, spdL, lvl = 0, jump = 5;
	private boolean[] keys = new boolean[256];
	private boolean  swap, gotSeed ;
	BufferedImage birdImg, seeds, wood, catImg;
	
	
	// This will be untilized with the sprite sheet
	public void init(){
	    /*BufferedImageLoader loader = new BufferedImageLoader();
	    try{	        
	        spriteSheet = loader.loadImage("/sprite_sheet.png");	        
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	    
	    SpriteSheet ss = new SpriteSheet(spriteSheet);
	    bird = ss.grabImage(1, 1, 32, 32);
	    */
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
       // TODO create the level tracker and put it here. This will start out by switching levels when the bird is in the 
       // right spot. Later it will maybe handle moving the screen along with the bird.
       look();
       processInput();
           }

	private GameEx()  {
		
		KeyListener listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);

		bird     = new Rectangle(xPos,  yPos, 10, 10);
		cat      = new Rectangle(50,  410, 10, 10);
		bounds   = new Rectangle(50, 50, 600, 400);
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
	    	birdImg = ImageIO.read(new File("bird.png"));  // Main character bird image
	    	seeds   = ImageIO.read(new File("seeds.png")); // Seeds  image
	    	wood    = ImageIO.read(new File("wood.png"));  // Blocks image
	    	catImg  = ImageIO.read(new File("cat.png"));   // Bad guy cat image
	    	
	        // Open an audio input stream.
	    	File soundFile = new File("Shy-Animal2.wav");
	    	AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
	        // Get a sound clip resource.
	        Clip clip = AudioSystem.getClip();
	        // Open audio clip and load samples from the audio input stream.
	        clip.open(audioIn);
	        //clip.loop(Clip.LOOP_CONTINUOUSLY); Turning this off for now because my ears
	     } catch (UnsupportedAudioFileException e) {
	        e.printStackTrace();
	     } catch (IOException e) {
	        e.printStackTrace();
	     } catch (LineUnavailableException e) {
	        e.printStackTrace();
	     }
	    
	    // Hit box. This should be turned into an array as well
		top   = new Rectangle(getBirdX(), getBirdY() - 10, birdImg.getWidth(), 10);
		bot   = new Rectangle(getBirdX(), getBirdY() + birdImg.getHeight(), birdImg.getWidth(), 10);
		left  = new Rectangle(getBirdX() - 10,  getBirdY(), 10, birdImg.getHeight());
		right = new Rectangle(getBirdX() + birdImg.getWidth(),  getBirdY(), 10, birdImg.getHeight());
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
	       //draws the black screen
	       g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	       Graphics2D g2d = (Graphics2D)g;
	       
	       
	       // bird is 50 wide by 48 height
	       // this turns the bird. It's kinda broken right now and he spins everytime.
	       // can't figure out how to fix it. Ideas?
	       AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	       
	       if(swap && (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT])){
	           tx.translate(-birdImg.getWidth(null), 0);
	           AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	           birdImg = op.filter(birdImg, null);
	           swap=false;
	       } 
	       
	       //draw the bird
	       g.drawImage(birdImg, getBirdX(),  getBirdY(), null);
	       
	       //draw cat
	       g.drawImage(catImg, getCatX(),  getCatY(), null);
	       
	       // enable this to see the birds coordinates
	       //System.out.println("X= " + getBirdX() + " Y= " + getBirdY() + " lvl= " + lvl);
	       
	       // The level stuff needs to be contained it its own method incase it gets big
	       if (getBirdX()==50 && getBirdY()==150 && lvl==0){
	           lvl=1;
	           setBirdX(590);
	           setBirdY(150);
	       } 
	       if (getBirdY()>400 && lvl==1){
	           lvl=0;
	           setBirdX(590);
	           setBirdY(390);
	       } 
	       if(getBirdX()==600 && getBirdY()==150 && lvl==1){
	           lvl=0;
	           setBirdX(60);
	           setBirdY(150);
	       } 
	       if(lvl == 1 && !gotSeed){
	           g.drawImage(seeds, 50,  50, null);
	           if (getBirdX()==50 && getBirdY() == 50){
	               gotSeed = true;
	           }
	       }
	       
	       //top       
	       setTop(getBirdX(),  getBirdY()-10, birdImg.getWidth(), 10);
	       
	       //bottom
	       setBot(getBirdX(),  getBirdY()+birdImg.getHeight(), birdImg.getWidth(), 10);
	       
	       //left
	       setLeft(getBirdX()-10,  getBirdY(), 10, birdImg.getHeight());
	       
	       //right
	       setRight(getBirdX()+birdImg.getWidth(),  getBirdY(), 10, birdImg.getHeight());
	       
	       /*g2d.draw(top);
	       g2d.draw(bot);
	       g2d.draw(left);
	       g2d.draw(right);*/
	       
	       // Apply textures and draw the blocks
	       TexturePaint woodtp = new TexturePaint(wood, new Rectangle(0, 0, 50, 50));
	       g2d.setPaint(woodtp);     
	       for(int i=0; i < blocks[0].length ; i++){
	           g2d.fill(blocks[lvl][i]);
	       }
	       
	       g2d.draw(bounds);
	       g.dispose();
	       bs.show();
	   }
	
    public void menu(){
        //TODO create a start menu and an inventory
	}

	void look() {

		 // The default amount of pixels movement in the specified directions
		 spdU = 30; spdD = 5; spdR = 10; spdL = 10;
		 
		 // Loop through the objects in the map and see if the any of them interesect with the collision boxes
		 for(int i = 0 ; i < blocks[1].length; i++){
			 check(blocks[lvl][i]);
		 }
		 
		// I needed a separate check for the blocks bounds since we are always IN the screen
		 boundary(bounds);		
	 }
	
	private void processInput() {
		// If the up key is pressed move the bird up for 4 iterations. This is done to make the movement smooth
		// instead of moving up in 1 frame
		// it will stop moving up until you hit up and you're touching something ( TODO this comment doesn't make sense)
		// TODO Can just check if it's less than the jump (jump < 5)
		if(jump <= 4){ // TODO can jump be a local variable?
			// subtract the speed up because the origin is at the top left corner so
			// when the bird jumps he is technically moving down
			setBirdY(getBirdY() - spdU); 
			jump++;
		}
		
		// If the w key or the up key is pressed, jump
		// If the up direction key is used
	    if(keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]){	    	

	    	playJump(); 	    	// Play the jump sound!
	    	// This is where we check to see if we're on the ground or a surface. 
	    	for(int i = 0 ; i < blocks[1].length; i++){
		    	if(bot.intersects(blocks[lvl][i])) { // if it intersects with a block
					jump = 0;
		    	} else if (!bot.intersects(bounds)){ // if it intersects with the bottom of the map
		    		jump = 0;
                      
		    	}
	    	}	
	    	keys[KeyEvent.VK_UP]=false; keys[KeyEvent.VK_W]=false;
		}
	    
	    
	    
	    // We will only need this if the bird is going into a hole or some other reason to go down
	    if(keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]){
	        setBirdY(getBirdY() + spdD);
	    } 

	    // If the left direction key is used
	    if(keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]){
	        setBirdX(getBirdX() - spdL);	    	
	    }
	    

	    // If the right direction key is used
	    if(keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]){
	        setBirdX(getBirdX() + spdR);
	    } 
	    // Always set the birds speed to be downwards
	    // This acts as gravity
	    setBirdY(getBirdY() + spdD);	    
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
		
	private class MyKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
		    keys[e.getKeyCode()] = true;
	        System.out.println("Left");
		}

		public void keyReleased(KeyEvent e) {
		    keys[e.getKeyCode()] = false;
//		    swap=true; // I think this is why the bird changes direction when you don't want to
		    // TODO What we need to do is if the left key is pressed
		    // set some boolean isChangeDirection(true) 
		    // then if if that's true and the right key is clicked, trigger the affine transform
		    // and set the variable to false
		    // then vice versa for going left
		}

		public void keyTyped(KeyEvent e) {
		}	
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
	private int getBirdY(){return this.bird.y;}
	private void setBirdY(int birdY){this.bird.y = birdY;}
	
 	private int getBirdX(){return this.bird.x;}
    private void setBirdX(int birdX){this.bird.x = birdX;}
    
	int getCatY(){return this.cat.y;}
	void setCatY(int catY){this.cat.y = catY;}
	
	int getCatX(){return this.cat.x;}
    void setCatX(int catX){this.cat.x = catX;}
    
    // The collision boxes could be there own class but eh
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
    
    // TODO doesn't need to be a function since we only call it once
    // rename to checkCollision
    // check if the collision boxes intersects with any objects on the map
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
	 // check if the collision boxes intersect with the boundaries of the map
	 // rename to boundaryCollision
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

}