package uk.ac.imperial.adpredictor; /* My package. */

import java.lang.Math;
import java.util.Random;

class Gaussian {
	
	/*
	 * Variables are:
	 * 
	 * `m` is mean (or, mu)
	 * `d` is standard deviation (or, sigma)
	 * `v` is variance (or, d-squared)
	 * `p` is precision, the inverse of variance (or, pi)
	 * `t` is precision adjusted mean
	 */
	public double m, d, v, p, t;
	
	/* 
	 * Used for sampling
	 */
	Random r;

	public Gaussian (double mean, double deviation) {
		this.m = mean;
		this.d = deviation;
		this.v = deviation * deviation;
		this.p = 1./v;
		this.t = p * m;
		
		this.r = new Random();
	}
	
	public Gaussian (Gaussian g) {
		this.m = g.m;
		this.d = g.d;
		this.v = g.v;
		this.p = g.p;
		this.t = g.t;
		
		this.r = new Random();
	}
	
	public void update (double mean, double deviation) {
		this.m = mean;
		this.d = deviation;
		this.v = deviation * deviation;
		this.p = 1./v;
		this.t = p * m;
	}
	
	public String toString() {
		String s = "[";
		s += ("m=" + m + "; ");
		s += ("d=" + d + "; ");
		s += ("v=" + v + "; ");
		s += ("p=" + p + "; ");
		s += ("t=" + t + "] ");
		return s;
	}
	
	/* 
	 * Multiplication of two Gaussian distributions 
	 *
	 */
	public void times(Gaussian x) {
		p += x.p;
		t += x.t;
		m = t /p;
		d = Math.sqrt(1./p);
		v = d * d;
		return;
	}
	
	public double sample() {
		double x = r.nextGaussian();
		return (x * d) + m; 
	}
}
