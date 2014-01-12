package com.bme.mlogo;

import com.bme.logo.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import static com.bme.logo.Primitives.*;

public class TurtleGraphics {

	static final int WIDTH  = 640;
	static final int HEIGHT = 480;
	private final Turtle turtle = new Turtle();
	private final Image buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private final Graphics g = buffer.getGraphics();
	private final Environment e;

	public TurtleGraphics(Environment e) {
		this.e = e;
		primitiveTurtle(e);
	}

	private void setup() {
		if (turtle.window == null) {
			synchronized(buffer) {
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, WIDTH, HEIGHT);
			}
			turtle.window = new JFrame("Turtle Graphics");
			turtle.window.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			turtle.window.setResizable(false);
			turtle.window.add(new TurtlePanel(turtle, buffer));
			turtle.window.pack();
		}
	}

	public void primitiveTurtle(Environment e) {
		final LWord a = new LWord(LWord.Type.Name, "argument1");
		final LWord b = new LWord(LWord.Type.Name, "argument2");
		final LWord c = new LWord(LWord.Type.Name, "argument3");
		
		e.bind(new LWord(LWord.Type.Prim, "showturtle") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "hideturtle") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(false);
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "forward") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				turtle.goalDistance = -num(e, a);
				e.pause();
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "back") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				turtle.goalDistance = num(e, a);
				e.pause();
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "left") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				turtle.goalDegrees = -num(e, a);
				e.pause();
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "right") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				turtle.goalDegrees = num(e, a);
				e.pause();
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "clear") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				synchronized(buffer) {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, WIDTH, HEIGHT);
				}
				turtle.window.repaint();
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "home") {
			public void eval(Environment e) {
				setup();
				turtle.window.setVisible(true);
				synchronized(buffer) {
					turtle.degrees = 90;
					turtle.x = WIDTH  / 2;
					turtle.y = HEIGHT / 2;
				}
				turtle.window.repaint();
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "penup") {
			public void eval(Environment e) {
				synchronized(buffer) {
					turtle.pendown = false;
				}
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "pendown") {
			public void eval(Environment e) {
				synchronized(buffer) {
					turtle.pendown = true;
				}
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "setcolor") {
			public void eval(Environment e) {
				synchronized(buffer) {
					turtle.pencolor = new Color(
						num(e, a) & 0xFF,
						num(e, b) & 0xFF,
						num(e, c) & 0xFF
					);
				}
			}
		}, a, b, c);
	}

	public boolean update() {
		if (turtle.goalDegrees != 0) {
			int rotated = (int)(Math.signum(turtle.goalDegrees) *
				Math.min(5, Math.abs(turtle.goalDegrees)));
			turtle.goalDegrees -= rotated;
			turtle.degrees += rotated;
			turtle.window.repaint();
			return turtle.goalDegrees == 0;
		}
		if (turtle.goalDistance != 0) {
			int traveled = (int)(Math.signum(turtle.goalDistance) *
				Math.min(5, Math.abs(turtle.goalDistance)));
			turtle.goalDistance -= traveled;

			synchronized(buffer) {
				int ox = (int)turtle.x;
				int oy = (int)turtle.y;
				turtle.x += traveled * Math.cos(Math.toRadians(turtle.degrees));
				turtle.y += traveled * Math.sin(Math.toRadians(turtle.degrees));
				if (turtle.pendown) { 
					g.setColor(turtle.pencolor);
					g.drawLine(ox, oy, (int)turtle.x, (int)turtle.y);
				}
			}
			turtle.window.repaint();
			return turtle.goalDistance == 0;
		}
		return true;
	}
}

class Turtle {
	JFrame window = null;

	int goalDegrees;
	int goalDistance;
	
	int degrees = 90;
	double x = TurtleGraphics.WIDTH  / 2;
	double y = TurtleGraphics.HEIGHT / 2;

	boolean pendown = true;
	Color pencolor = Color.GREEN;
}

class TurtlePanel extends JPanel {
	static final long serialVersionUID = 1;

	private final Turtle turtle;
	private final Image  buffer;

	public TurtlePanel(Turtle turtle, Image buffer) {
		setPreferredSize(new Dimension(TurtleGraphics.WIDTH, TurtleGraphics.HEIGHT));
		this.turtle = turtle;
		this.buffer = buffer;
	}

	public void paint(Graphics g2) {
		Graphics2D g = (Graphics2D)g2;
		synchronized(buffer) {
			g.drawImage(buffer, 0, 0, this);

			// draw turtle
			g.setColor(Color.GREEN);
			g.translate(turtle.x, turtle.y);
			g.rotate(Math.toRadians(turtle.degrees) - Math.PI/2);
			g.drawLine( 0, -10,  8, 10);
			g.drawLine( 0, -10, -8, 10);
			g.drawLine(-8,  10,  8, 10);
		}
	}
}