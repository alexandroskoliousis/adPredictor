package uk.ac.imperial.adpredictor; /* My package. */

import java.io.*;

class TestA {
	
	/* The parameter `beta` scales the steepness of the inverse link function */
	static final double beta = 0.1;
	
	/* Factors f(i) sample weights `w` from the Gaussian prior p(w) */
	public static double [][] F (Gaussian [][] prior) {
		double [][] w = new double[prior.length][];
		
		/* Compute the factorised prior distribution `g` */
		
		Gaussian g = null;
		for (int i = 0; i < prior.length; i++) {
			w[i] = new double [prior[i].length];
			for (int j = 0; j < prior[i].length; j++) {
				if (g == null) 
					g = new Gaussian(prior[i][j]);
				else 
					g.times(prior[i][j]);
			}
		}
		
		/* Sample prior `g` */
		
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++)
				w[i][j] = g.sample();
		return w;
	}
	
	/* Factor g calculates the score s for x as the inner product w(tau) x such 
	 * that p(s|x,w) := delta(s = w(tau) x)
	 */
	public static double G (double [][] w, int [][] x) {
		double s = 0.;
		
		/* Compute score `s` as the inner product of `w` and `x` */

		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++)
				s += (w[i][j] * (double) x[i][j]);
		return s;
	}
	
	/* Factor h adds zero-mean Gaussian noise to obtain `t` from `s`, such that
	 * p(t|s) := N(t; s, beta squared)
	 */
	public static Gaussian H (double s) {
		return new Gaussian(s, beta);
	}
	
	public static int sign(double x) {
		if (x < 0) return -1;
		else if (x > 0) return 1;
		else return 0;
	}
	
	/* Factor q determines y by a threshold on the noisy score `t` at zero, such
	 * that p(y|t) := delta(y = sign(t))
	 */
	public static int Q (Gaussian t) {
		return sign(t.sample());
	}
	
	public static double PDF (double t) {
		double I2P = 0.398942280401433;
		return I2P * Math.exp( -(t * t / 2.0) );
	}

	public static double ERF (double x) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
		double a = 1 - t * Math.exp(-x * x - 1.26551223 +
			t * ( 1.00002368 +
			t * ( 0.37409196 + 
			t * ( 0.09678418 + 
			t * (-0.18628806 + 
			t * ( 0.27886807 + 
			t * (-1.13520398 + 
			t * ( 1.48851587 + 
			t * (-0.82215223 + 
			t * ( 0.17087277)))))))))
		);
        if (x >= 0) return a;
        else
			return -a;
    }
	
	public static double CDF (double t) {
		double SR2 = 1.4142135623730951;
		return ( ERF(-t / SR2) / 2.0);
	}
	
	public static double V (double t) {
		
		return PDF(t) / CDF(t);
	}
	
	public static double W (double t) {
		double v = V(t);
		return v * (v + t);
	}
	
	public static void update (int [][] x, Gaussian [][] prior, int y) {
		double s = 0., m = 0.;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				s += (x[i][j] * prior[i][j].v);
				m += (x[i][j] * prior[i][j].m);
			}
		}
		double S = Math.sqrt(beta * beta + s);
		double t = (y * m) / S;
		
		for (int i = 0; i < prior.length; i++) {
			for (int j = 0; j < prior[i].length; j++) {
				double m_ = prior[i][j].m + 
					 y * x[i][j] * (prior[i][j].v / S) * V(t);
				double v_ = prior[i][j].v * 
					(1 - x[i][j] * (prior[i][j].v / S) * W(t));
				prior[i][j].update(m_, Math.sqrt(v_));
			}
		}
		return;
	}
	
	public static void main (String [] args) {
		try {
			Gaussian [][] prior = null;
			FileInputStream f = new FileInputStream(args[0]);
			DataInputStream d = new DataInputStream(f);
			BufferedReader b = new BufferedReader(new InputStreamReader(d));
			String line;
			while ((line = b.readLine()) != null) {
				Impression I = new Impression(line);
				System.out.println(I);
				int [][] x = I.toArray();
				if (prior == null) {
					prior = new Gaussian[x.length][x[0].length];
					for (int i = 0; i < prior.length; i++)
						for (int j = 0; j < prior[i].length; j++)
							prior[i][j] = new Gaussian(25.0, (25.0/3.0));
				}
				double [][] w = F(prior);
				double s = G(w, x);
				Gaussian t = H(s);
				int y = Q(t);
				System.out.println("y = " + y + "(" + I.isClicked() + ")");
				update(x, prior, I.isClicked());
			}
			d.close();
			
		} catch (Exception e) { System.err.println(e.getMessage()); }
		return;
	}
}
