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
	int STEPS_PER_SECOND = 30;
	ControlP5 cp5;
	Textfield digit_box;
	Button slow_mode;
	Minim minim;
	AudioPlayer song;
	Oscil wave;
	FFT fft;
	int nfft = 1024;
	int curID=3;
	float bx;
	float by;
	int dotSize = 3;
	boolean isPaused = false;
	boolean overDot;
	boolean locked;
	float drag_ellipseX = 0.0f;
	float drag_ellipseY = 0.0f;
	//ArrayList<FourierComponent> afc = new ArrayList<FourierComponent>();
	ArrayList<Button> digit_buttons = new ArrayList<Button>();
	ArrayList<ArrayList<FourierComponent>> digit_fcs = new ArrayList<ArrayList<FourierComponent>>();
	
	FourierComponent draggedFc;
	ButtonBar digit_bar;
	Group digit_group;
	ControlFont font;
	int digit_selected;
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

	private static final ArrayList<Integer> tones;
	static
	{
		tones = new ArrayList<Integer>();
		tones.add(697);
		tones.add(770);
		tones.add(852);
		tones.add(941);
		tones.add(1209);
		tones.add(1336);
		tones.add(1477);
		tones.add(1633);
	}
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

	private void drawDigitButtons(int startx, int starty, String init_state)
	{
		fill(white);
		textSize(32);
		digit_group = cp5.addGroup("Phone Number").setPosition(startx, starty).setSize(300, 300);
		//digit_group.getCaptionLabel().setFont(font);
		int id = 0;
		for(int i = 0; i < init_state.length(); i++)
		{
			String s = init_state.substring(i,i+1);
			if (s.matches("[0-9]"))
			{
				Button b = new Button(cp5, "b" + id);
				//b.setPosition(startx+i*50,starty);
				b.getCaptionLabel().setFont(font);
				b.setSize(40,60);
				b.setLabel(init_state.substring(i,i+1));
				b.setSwitch(true);
				b.setGroup(digit_group);
				b.setPosition(i*50,0);
				b.setId(id++);
				if(b.getId() == curID)
				{
					b.setOn();
				}
				ArrayList<FourierComponent> fc = new ArrayList<FourierComponent>();
				setFourierComponentsToDTMFDigit(fc,s.charAt(0));
				digit_fcs.add(fc);
				digit_buttons.add(b);
			}
			else
			{
				println("S: " + s);
				Textarea ta = cp5.addTextarea("h" + i, s,i*50+10,0,80,80);
				ta.setFont(font);
				ta.setGroup(digit_group);

			}
		}
	}
	private void drawActionsButtons(int startx, int starty, String init_state)
	{
		fill(white);
		textSize(32);
		
	}
	private void setFourierComponentsToDTMFDigit(ArrayList<FourierComponent> a, char digit)
	{
		a.clear();
		int f1 = DTMF_tones.get(digit).f1;
		int f2 = DTMF_tones.get(digit).f2;
		for(int t = 0; t < tones.size(); t++)
		{
			if(tones.get(t) == f1 || tones.get(t) == f2)
			{
				a.add(new FourierComponent(tones.get(t), 30f));
			}
			else
			{
				a.add(new FourierComponent(tones.get(t), .001f));
			}
		}
		
	}
	public void setup() {
		size(1800,1000,P2D);
		//size(1800,1000);

		background(255,255,255);
		frameRate(30);
		cp5 = new ControlP5(this);
		overDot = false;
		locked = false;
		PFont pfont = createFont("Serif",60); // use true/false for smooth/no-smooth
		font = new ControlFont(pfont,60);

		slow_mode = cp5.addButton("SLOW");
		slow_mode.setPosition(0,110);
		slow_mode.setSize(40,40);
		slow_mode.setSwitch(true);
		slow_mode.setOn();
		//slow_mode.getCaptionLabel().setFont(font);

		drawDigitButtons(100,50,"603-867-5309");


		
		ellipseMode(RADIUS);
	}

	public synchronized void startSong()
	{
		monoSamples = song.left.toArray();

	}
	public void draw() {
		background(black);
		noFill();
		
		for(Button b : digit_buttons)
		{
			if(b.getBooleanValue() && b.getId() != curID)
			{
				b.setOff();
			}
			else if(!b.getBooleanValue() && b.getId() == curID)
			{
				b.setOn();
			}
		}
		drawSuperPosition();
		drawManipulationComponents(1300,20,10,4);
		if(!isPaused)
		{
			stepComponents();
		}
		stroke(white);
		fill(white);
		textSize(32);
		String fr_string = String.format("%.2f", frameRate);
		text(fr_string,width-100,height-50);
	}
	void drawManipulationComponents(float startx, float starty, float scale, int columns)
	{
		float column_spacing = scale*10;
		float row_spacing = scale*15;

		textSize(20);

		overDot = false;
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);
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
			if(mouseX > startx + radius*cos(phase) - 2*dotSize && mouseX < startx + radius*cos(phase) + 2*dotSize 
					&& mouseY > ycirc + radius*sin(phase) - 2*dotSize && mouseY < ycirc + radius*sin(phase) + 2*dotSize  )
			{
				stroke(white);
				overDot = true;
				drag_ellipseX = startx;
				drag_ellipseY = ycirc;
				draggedFc = afc.get(i);
			}

			ellipse(startx + radius*cos(phase),ycirc + radius*sin(phase),dotSize,dotSize);
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
		println("MOUSE PRESS :" + overDot);
		if(overDot) { 
			locked = true; 
			println("LOCKING");
		} else {
			locked = false;
		}


	}
	@Override
	public void mouseDragged() {
		if(locked) {
			float new_radius = sqrt(pow(drag_ellipseX-mouseX,2) + pow(drag_ellipseY-mouseY,2));
			float new_phase = atan2(mouseY-drag_ellipseY,mouseX-drag_ellipseX);
			draggedFc.setMag(new_radius);
			draggedFc.setPhase(new_phase);
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
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);

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
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);
		for(FourierComponent fc: afc)
		{
			if(!slow_mode.getBooleanValue())
			{
			fc.step(STEPS_PER_SECOND);
			}
			else
			{
				fc.step(STEPS_PER_SECOND*2000);
			}
		}
	}
	//@Override
	public void controlEvent(ControlEvent theEvent)
	{

		if (theEvent.getController().getName().matches("b[0-9]"))
		{
			curID = theEvent.getController().getId();
		}		

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
	private float phase;
	private float mag;
	public FourierComponent(int freq, float mag)
	{
		phase = 0;
		this.freq = freq;
		w_freq = this.freq*TWO_PI;
		this.mag = mag;
	}
	public void step(float steps_per_second)
	{
		phase += w_freq/steps_per_second;
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