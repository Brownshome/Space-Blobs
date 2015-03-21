package math;

import static java.lang.Math.*;

public class Box {
	public static final Box MIN = new Box(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	
	public final double x0, x1, y0, y1;
	
	public Box(double x0, double x1, double y0, double y1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}

	public Box resize(Box b) {
		if(b.x0 < x0 || b.y0 < y0 || b.x1 > x1 || b.y1 > y1)
			return new Box(min(x0, b.x0), max(x1, b.x1), min(y0, b.y0), max(y1, b.y1));
		
		return this;
	}

	public double width() {
		return x1 - x0;
	}

	public double height() {
		return y1 - y0;
	}
}
