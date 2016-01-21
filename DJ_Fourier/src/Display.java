import processing.core.*;
import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;
import ddf.minim.*;
import ddf.minim.analysis.FFT;
import ddf.minim.ugens.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.*;

public class Display extends PApplet { //Using Processing 2.2.1
static int FPS = 30;
ControlP5 cp5;
Textfield digit_box;
Minim minim;
AudioPlayer song;
Oscil wave;
FFT fft;
int nfft = 1024;

float bx;
float by;
int dotSize = 3;
boolean isPaused = false;
boolean overDot = false;
boolean locked = false;
float xOffset = 0.0f;
float yOffset = 0.0f;
ArrayList<FourierComponent> afc = new ArrayList<FourierComponent>();
ButtonBar digit_bar;
int black = color(0,0,0);
int white = color(255,255,255);
int green = color(0,128,0);
int purple = color(255,0,255);
int red = color(255,0,0);
/*Traverse 64 pixels per second
 * Using 32 oscillators we can represent this...4
 * 
 */
float [] monoSamples;


private static final HashMap<Character,DTMF_tone> DTMF_tones;
static
{
	DTMF_tones = new HashMap<Character, DTMF_tone>(); 
	DTMF_tones.put('1',new DTMF_tone(697,1209));
	DTMF_tones.put('2',new DTMF_tone(697,1336));
	DTMF_tones.put('3',new DTMF_tone(697,1477));

	DTMF_tones.put('4',new DTMF_tone(770,1209));
	DTMF_tones.put('5',new DTMF_tone(770,1336));
	DTMF_tones.put('6',new DTMF_tone(770,1477));

	DTMF_tones.put('7',new DTMF_tone(852,1209));
	DTMF_tones.put('8',new DTMF_tone(852,1336));
	DTMF_tones.put('9',new DTMF_tone(852,1477));

	DTMF_tones.put('*',new DTMF_tone(941,1209));
	DTMF_tones.put('0',new DTMF_tone(941,1336));
	DTMF_tones.put('#',new DTMF_tone(941,1477));
	
}

	public void setup() {
		size(1800,1000,P2D);
		//size(1800,1000);

		background(255,255,255);
		frameRate(30);
		cp5 = new ControlP5(this);
		
		  PFont pfont = createFont("Serif",20); // use true/false for smooth/no-smooth
		  ControlFont font = new ControlFont(pfont,120);
		  //digit_box = cp5.addTextfield("digit_box").setFont(font).setSize(700, 100).setText("603-867-5309");
		  digit_bar = cp5.addButtonBar("bar").setPosition(100,100).setSize(400,20).addItems(split("a b c d e f g h i j"," "));
		  digit_bar.getCaptionLabel().setFont(font);
		  //cp5.getController("bar")
		  
		  for (Object b :  digit_bar.getItems())
		  {
			  Class cls = b.getClass();
			  println(cls.toGenericString());
		  }
		  
		  for(int i = 1; i <= 8; i++)
		{
			FourierComponent fc = new FourierComponent(i, 42.50f-(i*2.50f),900);
			//fc.setPhase(random(0,TWO_PI));
			afc.add(fc);
		}
		//afc.get(1).setPhase((float) Math.PI);
		  ellipseMode(RADIUS);
	}
	
	public synchronized void startSong()
	{
		monoSamples = song.left.toArray();

	}
	public void draw() {
		background(black);
		noFill();
		drawSuperPosition();
		drawManipulationComponents(1300,20,10,4);
		if(!isPaused)
		{
			stepComponents();
		}
		stroke(white);
		textSize(32);
		String fr_string = String.format("%.2f", frameRate);
		text(fr_string,width-100,height-50);
	}
	void drawManipulationComponents(float startx, float starty, float scale, int columns)
	{
		float column_spacing = scale*10;
		float row_spacing = scale*15;
		
		textSize(20);
		for(int i = 0; i < afc.size(); i++)
		{
			float radius = afc.get(i).getMag();
			float phase = afc.get(i).getPhase();
			float ycirc = starty + column_spacing*.75f;
			stroke(green);
			noFill();
			ellipse(startx,ycirc,radius,radius);
			
			stroke(red);
			ellipse(startx,ycirc,3,3);
			
			ellipse(startx + radius*cos(phase),ycirc + radius*sin(phase),dotSize,dotSize);
			if(mouseX > startx + radius*cos(phase) - dotSize && mouseX < startx + radius*cos(phase) + dotSize 
					&& mouseY > ycirc + radius*sin(phase) - dotSize && mouseY < ycirc + radius*sin(phase) + dotSize  )
			{
				println("OVER DOT");
			}
			fill(purple);
			text(afc.get(i).freq + "Hz",startx,starty);

			if(i!= 0 && (i+1) % columns == 0)
			{
				startx -= (columns-1)*column_spacing;
				starty += row_spacing;
			}
			else
			{
				startx += column_spacing;
			}
		}
	}
	@Override
	public void keyPressed()
	{
		if(key == ' ')
		{
			println("PAUSE TOGGLE");
			isPaused = !isPaused;
		}
	}
	@Override
	public void mousePressed() {
		  if(overDot) { 
		    locked = true; 
		    fill(255, 255, 255);
		  } else {
		    locked = false;
		  }
		  xOffset = mouseX-bx; 
		  yOffset = mouseY-by; 

	}
	@Override
	public void mouseDragged() {
	  if(locked) {
	    bx = mouseX-xOffset; 
	    by = mouseY-yOffset; 
	  }
	}
	@Override
	public void mouseReleased() {
	  locked = false;
	}
	void drawSuperPosition()
	{
		float fociX = 500;
		float fociY = 500;
		for(int i = 0; i < afc.size(); i++)
		{
			stroke(red);
			fill(red);
			ellipse(fociX,fociY,3,3);
			float radius = afc.get(i).getMag();
			float phase = afc.get(i).getPhase();
			stroke(green);
			noFill();
			ellipse(fociX,fociY,radius,radius);
			
			float old_fociX = fociX;
			float old_fociY = fociY;
			fociX += radius*cos(phase);
			fociY += radius*sin(phase);
			stroke(purple);
			if(i != afc.size() - 1)
			{
				line(old_fociX,old_fociY,fociX,fociY);
			}
		}
	}
	void stepComponents()
	{
		for(FourierComponent fc: afc)
		{
			fc.step();
		}
	}
	
	public void controlEvent(ControlEvent theEvent)
	{
		println(theEvent.getController().getName());
		//if (theEvent.getController().getName().equals("colorA"))

	}
	public static void main(String args[])
	{
		//PApplet.main(new String[] {"--present","Display"});
		PApplet.main(new String[] {"Display"});

	}
}
class DTMF_tone
{
	public int f1;
	public int f2;
	public DTMF_tone(int f1, int f2)
	{
		this.f1 = f1;
		this.f2 = f2;
	}
}
class FourierComponent
{
	static float TWO_PI = (float)(2.0*Math.PI);
	protected int freq;
	private float w_freq; //Rad/s
	private float w_frameStep; //Radians per frame
	private float phase;
	private float mag;
	public FourierComponent(int freq, float mag,  int fps)
	{
		phase = 0;
		this.freq = freq;
		w_freq = this.freq*TWO_PI;
		w_frameStep = w_freq/(float)fps;
		this.mag = mag;
	}
	public void step()
	{
		phase += w_frameStep;
		phase = clipAngle(phase);
	}
	public float getPhase()
	{
		return phase;
	}
	public void setPhase(float ph)
	{
		this.phase = clipAngle(ph);
	}
	
	public void setMag(float m)
	{
		mag = m;
	}
	public float getMag()
	{
		return mag;
	}
	
	private float clipAngle(float d)
	{
		while (d >= TWO_PI)
		{
			d -= TWO_PI;
		}
		while(d < 0.0)
		{
			d += TWO_PI;
		}
		return d;
	}
	
}