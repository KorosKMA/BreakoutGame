import acm.graphics.*;
import acm.program.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 20;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_DIAMETR = 20;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	
/** Animation delay */
	private static final int DELAY = 10;
	
/** Starting velocity */
	private static final int V_START = 3;
	
/** Pi constant */
	private static final double PI = Math.PI;
	

/** Runs the Breakout program. */
	public void run() {
		setup();
		while(!gameOver(checkForCollisions())){
			if(started)
			moveBall();
			pause(DELAY);
		}
		remove(ball);
	}
	
	private void setup() {
		this.setSize(WIDTH,HEIGHT);
		brickSetup();
		gameSetup();
		addMouseListeners();
}
	
	private void brickSetup() {
		for(int i=0;i<NBRICK_ROWS;i++)
			for(int j=0;j<NBRICKS_PER_ROW;j++){
				brick = new GRect(j*(BRICK_WIDTH+BRICK_SEP)+BRICK_SEP/2,BRICK_Y_OFFSET+i*(BRICK_HEIGHT+BRICK_SEP),BRICK_WIDTH,BRICK_HEIGHT);
				((GRect) brick).setFilled(true);
				Color color = brickColor(i);
				((GRect) brick).setFillColor(color);
				brick.setColor(color);
				add(brick);
			}
	}

	private void gameSetup(){
		paddle = new GRect((WIDTH-PADDLE_WIDTH)/2,HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT,PADDLE_WIDTH,PADDLE_HEIGHT);
		((GRect) paddle).setFilled(true);
		add(paddle);
		
		ball = new GOval(paddle.getX()+(PADDLE_WIDTH-BALL_DIAMETR)/2, paddle.getY()-BALL_DIAMETR-1,BALL_DIAMETR,BALL_DIAMETR);
		((GOval) ball).setFilled(true);
		add(ball);
		/*All labels are stylized according to Dark Souls item names*/
		greatSouls = new GLabel("GREAT SOULS: "+lives,10,HEIGHT-9);
		add(greatSouls);
		souls = new GLabel("SOULS: "+fractureLives,WIDTH-100,HEIGHT-9);
		add(souls);	
	}
	
	public void mouseMoved(MouseEvent e){
		if(e.getX()<=WIDTH-PADDLE_WIDTH/2&&e.getX()>=PADDLE_WIDTH/2)
			paddle.setLocation(e.getX()-PADDLE_WIDTH/2, HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT);
		else if(e.getX()>WIDTH-PADDLE_WIDTH/2)
			paddle.setLocation(WIDTH-PADDLE_WIDTH, HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT);
		else
			paddle.setLocation(0, HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT);
		if(!started)
			ball.setLocation(paddle.getX()+(PADDLE_WIDTH-BALL_DIAMETR)/2, paddle.getY()-BALL_DIAMETR-1);
	}
	
	public void mouseReleased(MouseEvent e){
		started=true;
	}

	private void moveBall(){
		if(ball.getX()<=0||ball.getX()+BALL_DIAMETR>=WIDTH)
			vx=-vx;
		if(ball.getY()<=0)
			vy=-vy;
		if(ball.getY()>=HEIGHT-BALL_DIAMETR){
			greatSouls(--lives);
			started=false;
			vy=-1;
			ball.setLocation(paddle.getX()+(PADDLE_WIDTH-BALL_DIAMETR)/2, paddle.getY()-BALL_DIAMETR-1);
		}else
			ball.move(v*vx, v*vy);	
	}

	private boolean checkForCollisions(){
		GObject collObject;
		double ballCentreX=ball.getX()+BALL_DIAMETR/2;
		double ballCentreY=ball.getY()+BALL_DIAMETR/2;
		for(int r=0;r<360;r++){	//check ball surface
			collObject = getElementAt(ballCentreX+Math.cos(r*PI/180)*BALL_DIAMETR/2,ballCentreY+Math.sin(r*PI/180)*BALL_DIAMETR/2);	//get object at point on the ball surface
			if(collObject!=null&&collObject!=ball&&collObject!=greatSouls&&collObject!=souls){
				int collObjectWidth=PADDLE_WIDTH;
				int collObjectHeight=PADDLE_HEIGHT;
				if(collObject!=paddle){
					collObjectWidth=BRICK_WIDTH;
					collObjectHeight=BRICK_HEIGHT;
					v=V_START*newSpeed(collObject.getColor());		//velocity change
					ball.setColor(collObject.getColor());			//depending on the color of the removed brick
					remove(collObject);
					bricksLeft--;
					fractureLives++;
					if(fractureLives==NBRICKS_PER_ROW){
						fractureLives=0;
						greatSouls(++lives);
					}
					souls(fractureLives);
				}
				if((ballCentreX<=collObject.getX()&&vx>0)||(ballCentreX>=collObject.getX()+collObjectWidth&&vx<0))
					vx=-vx;
				if((ballCentreY<=collObject.getY()&&vy>0)||(ballCentreY>=collObject.getY()+collObjectHeight&&vy<0))
					vy=-vy;
				break;
			}
		}
		if(bricksLeft==0)
			return true;
		return false;
	}
	
	private boolean gameOver(boolean bricksCleared) {
		if(bricksCleared){
			add(new GLabel("VICTORY ACHIEVED",WIDTH/2-40,HEIGHT/2));
			return true;
		}
		if(lives<1){
			add(new GLabel("YOU DIED",WIDTH/2-20,HEIGHT/2));
			return true;
		}
		return false;
	}
	
	/**Label for souls of the fallen*/
	private void souls(int i) {		
		remove(souls);
		souls = new GLabel("SOULS: "+i,WIDTH-100,HEIGHT-9);
		add(souls);	
	}

	/**Label for lives*/
	private void greatSouls(int i) {
		remove(greatSouls);
		greatSouls = new GLabel("GREAT SOULS: "+i,10,HEIGHT-9);
		add(greatSouls);
	}
	
	private Color brickColor(int i) {
		if(i<NBRICK_ROWS/5)
			return Color.red;
		if(i<2*NBRICK_ROWS/5)
			return Color.orange;
		if(i<3*NBRICK_ROWS/5)
			return Color.yellow;
		if(i<4*NBRICK_ROWS/5)
			return Color.green;
		return Color.cyan;
	}
	
	/**Speed multiplier */
	private double newSpeed(Color color) {
		if(color==Color.cyan)
			return 1.4;
		if(color==Color.green)
			return 1.8;
		if(color==Color.yellow)
			return 2.2;
		if(color==Color.orange)
			return 2.6;
		if(color==Color.red)
			return 3;
		return 1;
	}
	
	/**If ball has started moving*/
	private boolean started=false;	
	/**Direction*/
	private int vx = 1, vy = -1;
	/**Velocity*/
	private double v=V_START;			
	/**Number of bricks on the field*/
	private int bricksLeft=NBRICK_ROWS*NBRICKS_PER_ROW;	
	/**Amount of turns available*/
	private int lives = NTURNS;
	/**({@link #bricksLeft bricksLeft}--) -> ({@link #fractureLives fractureLives}++).
	 * Collect {@link #NBRICKS_PER_ROW a row} of {@link #fractureLives fractureLives} -> {@link #lives lives}++*/
	private int fractureLives=0;
	private GObject brick;
	private GObject ball;
	private GObject paddle;
	/**Label for {@link #lives lives}*/
	private GLabel greatSouls;	
	/**Label for {@link #fractureLives fractureLives}*/
	private GLabel souls;
}
