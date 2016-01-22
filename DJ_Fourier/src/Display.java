import processing.core.*;
import controlP5.*;
import ddf.minim.*;
import ddf.minim.ugens.*;
import java.util.ArrayList;
import java.util.HashMap;



public class Display extends PApplet { //Using Processing 2.2.1

	private static final long serialVersionUID = 6732597443834822537L;
	static final int STEPS_PER_SECOND = 30;
	static final int TRACE_LENGTH = 1200;
	static final float MAG_MAX = 200.0f;
	static final int DOT_SIZE = 3;
	
	int sequence_start_time;
	ControlP5 cp5;
	Textfield digit_box;
	Minim minim;
	AudioOutput out;
	int curID;
	int saved_curID;
	boolean overDot;
	boolean locked;
	float drag_ellipseX = 0.0f;
	float drag_ellipseY = 0.0f;
	boolean is720p = false;
	//ArrayList<FourierComponent> afc = new ArrayList<FourierComponent>();
	ArrayList<Button> digit_buttons = new ArrayList<Button>();
	ArrayList<Button> dialer_buttons = new ArrayList<Button>();

	ArrayList<ArrayList<FourierComponent>> digit_fcs = new ArrayList<ArrayList<FourierComponent>>();
	//ArrayList<RingBuffer> traces = new ArrayList<RingBuffer>();
	RingBuffer trace = new RingBuffer(TRACE_LENGTH);
	FourierComponent draggedFc;
	Group digit_group,control_group,dialer_group;
	ControlFont font;
	
	Button slow_button, play_button, sequence_button;

	int digit_selected;
	int black = color(0,0,0);
	int white = color(255,255,255);
	int green = color(0,128,0);
	int purple = color(255,0,255);
	int red = color(255,0,0);
	int light_red = color(255,128,128);
	int orange = color(255,128,0);
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
	private final HashMap<Integer,Oscil> ugens = new HashMap<Integer,Oscil>();

	private void drawDigitButtons(int startx, int starty, String init_state)
	{
		fill(white);
		textSize(32);
		digit_group = cp5.addGroup("Phone Number").setPosition(startx, starty).setSize(600, 100);
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
	private void drawDialerButtons(int startx, int starty)
	{
		fill(white);
		textSize(32);
		dialer_group = cp5.addGroup("Dialer").setPosition(startx, starty).setSize(230, 300);
		//digit_group.getCaptionLabel().setFont(font);
		String s = "123456789*0#";
		int id = 11;
		for(int i = 0; i < s.length(); i++)
		{
				Button b = new Button(cp5, "d" + id);
				b.getCaptionLabel().setFont(font);
				b.setSize(60,60);

				b.setLabel(s.substring(i,i+1));
				b.setGroup(dialer_group);
				b.setPosition((i%3)*70,(i/3)*80);
				b.setId(id++);
		}
	}
	private void changeCurID(int newID)
	{
		curID = newID;
		//adjustMinim();
	}
	private void adjustMinim()
	{
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);
		if(play_button.getBooleanValue())
			out.unmute();
		else
			out.mute();
		for (FourierComponent fc : afc)
		{
			if(fc.getMag() <= 0.0f)
				ugens.get(fc.freq).setAmplitude(0.0f);
			else
				ugens.get(fc.freq).setAmplitude(fc.getMag()/MAG_MAX);
		}
	}
	private void drawActionsButtons(int startx, int starty)
	{
		fill(white);
		textSize(32);
		control_group = cp5.addGroup("Controls",startx,starty);
		slow_button = new Button(cp5, "SLOW");
		slow_button.setGroup(control_group);

		slow_button.setSize(50,50);
		slow_button.setSwitch(true);
		slow_button.setOn();
		slow_button.setPosition(0,0);
		
		play_button = new Button(cp5,"PLAY");
		play_button.setGroup(control_group);
		play_button.setSize(50,50);
		play_button.setSwitch(true);
		play_button.setOn();
		play_button.setPosition(60,0);
		
		sequence_button = new Button(cp5,"SEQUENCE");
		sequence_button.setGroup(control_group);
		sequence_button.setSize(50,50);
		sequence_button.setSwitch(true);
		sequence_button.setOff();
		sequence_button.setPosition(120,0);
		
		
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
				a.add(new FourierComponent(tones.get(t), 0.0f));
			}
		}
		
	}
	public void setup() {
		if (is720p)
			size(1280,720,P2D);
		else
			size(1920,1080,P2D);
		//size(1800,1000);

		background(255,255,255);
		frameRate(30);
		
		
		minim = new Minim(this);
		out = minim.getLineOut();
		for (Integer tone : tones)
		{
			Oscil osc = new Oscil(tone, 0.0f, Waves.SINE);
			ugens.put(tone, osc);
			osc.patch(out);
		}
		
		
		cp5 = new ControlP5(this);
		overDot = false;
		locked = false;
		PFont pfont = createFont("Serif",60); // use true/false for smooth/no-smooth
		font = new ControlFont(pfont,60);
		
		drawActionsButtons(20,50);
		//slow_button.getCaptionLabel().setFont(font);

		drawDigitButtons(200,50,"603-867-5309");
		drawDialerButtons(850,50);

		/*
		for(Button b: digit_buttons)
		{
			RingBuffer rb = new RingBuffer(TRACE_LENGTH);
			traces.add(rb);
		}
		*/
		changeCurID(3);
		
		ellipseMode(RADIUS);
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
		drawSuperPosition(200,is720p ? 400 : 700);
		drawManipulationComponents(is720p ? 900 : 1300,20,10,4);

		if(play_button.getBooleanValue())
		{
			stepComponents();
		}
		stroke(white);
		fill(white);
		textSize(32);
		String fr_string = String.format("%.2f", frameRate);
		text(fr_string,width-100,height-50);
		adjustMinim();
		if(sequence_button.getBooleanValue())
		{
			int ticks_since_begin = (millis() - sequence_start_time)/300;
			if (ticks_since_begin < digit_buttons.size())
			{
					changeCurID(ticks_since_begin);
			}
			else
			{
				changeCurID(saved_curID);
				sequence_button.setOff();
				play_button.setOff();
			}
		}
	}
	private void drawManipulationComponents(float startx, float starty, float scale, int columns)
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
			if(mouseX > startx + radius*cos(phase) - 2*DOT_SIZE && mouseX < startx + radius*cos(phase) + 2*DOT_SIZE 
					&& mouseY > ycirc + radius*sin(phase) - 2*DOT_SIZE && mouseY < ycirc + radius*sin(phase) + 2*DOT_SIZE  )
			{
				stroke(white);
				overDot = true;
				drag_ellipseX = startx;
				drag_ellipseY = ycirc;
				draggedFc = afc.get(i);
			}

			ellipse(startx + radius*cos(phase),ycirc + radius*sin(phase),DOT_SIZE,DOT_SIZE);
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
			if(play_button.getBooleanValue()) play_button.setOff();
			else play_button.setOn();
		}
		else if (key >= '0' && key <= '9' && locked)
		{
			draggedFc.setMag(10.0f*Character.getNumericValue(key));
			locked = false;
		}
	}
	@Override
	public void mousePressed() {
		println("MOUSE PRESS :" + overDot);
		if(overDot) { 
			locked = true; 
			println("LOCKING");
			Button b = digit_buttons.get(curID);
			b.getColor();
			b.setColorActive(red);
			b.setColorBackground(light_red);
			b.setColorForeground(red);
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
	void drawSuperPosition(float startX, float startY)
	{

		float fociX = startX;
		float fociY = startY;
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);
		float realAxisHeight = is720p ?  400f : 700f;
		float realAxisOffset = 400f;
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
			line(old_fociX,old_fociY,fociX,fociY);
			
		}
		stroke(white);
		fill(white);
		line(startX+realAxisOffset,startY-realAxisHeight/2 ,startX+realAxisOffset,startY+realAxisHeight/2);
		ellipse(startX+realAxisOffset,startY-realAxisHeight/2,DOT_SIZE,DOT_SIZE);
		ellipse(startX+realAxisOffset,startY+realAxisHeight/2,DOT_SIZE,DOT_SIZE);
		stroke(orange);
		fill(orange);
		ellipse(fociX,fociY,DOT_SIZE,DOT_SIZE); 
		line(fociX,fociY,startX+realAxisOffset,fociY);
		ellipse(startX+realAxisOffset,fociY,DOT_SIZE,DOT_SIZE);
		
		//RingBuffer trace = traces.get(curID);
		if(play_button.getBooleanValue())
		{
		trace.push(fociY);
		}
	
		float lineX = startX+realAxisOffset;
		float lineY = fociY;
		//line(lineX,lineY,++lineX,fociY);
		for(int i = 0; i < trace.size(); i++)
		{
			float nextY = trace.get(i);
			line(lineX,lineY,++lineX,nextY);
			lineY = nextY;
		}
	
	}
	void stepComponents()
	{
		ArrayList<FourierComponent> afc = digit_fcs.get(curID);
		for(FourierComponent fc: afc)
		{
			if(!slow_button.getBooleanValue())
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
			changeCurID( theEvent.getController().getId());
		}

		else if (theEvent.getController().getName().matches("d[0-9]{2}"))
		{
			
			CColor cc = slow_button.getColor();
			Button b = (Button) cp5.get("b" + curID);
			b.setColor(cc);
			b.setLabel(theEvent.getController().getLabel());
			ArrayList<FourierComponent> afc = digit_fcs.get(curID);

			setFourierComponentsToDTMFDigit(afc,theEvent.getController().getLabel().charAt(0));
		}
		else if(theEvent.isFrom(sequence_button) && sequence_button.getBooleanValue())
		{
			play_button.setOn();
			sequence_start_time = millis();
			saved_curID = curID;
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
	static final float MAG_MAX = 200.0f;
	static final float TWO_PI = (float)(2.0*Math.PI);
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
		if(m <= MAG_MAX)
		{
		mag = m;
		}
		else
			mag = MAG_MAX;
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