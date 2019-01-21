package uk.ac.imperial.adpredictor; /* My package. */

/*
 * A feature is a sparse binary vector of M values.
 * Only one value is 1; the remaining values are 0.
 *
 */
class Feature {
	
	private int x [];
	private int M;
	
	public Feature(String s) {
		int i;
		String [] values = s.split(",");
		M = values.length;
		x = new int [M];
		for (i = 0; i < values.length; i++)
			x[i] = Integer.parseInt(values[i]);
	}
	
	public boolean isValid() {
		int i, sum = 0;
		for (i = 0; i < M; i++) sum += x[i];
		return (sum == 1);
	}
	
	public int size() { return M; }
	
	public int elementAt(int i) { return x[i]; }

	public String toString() {
		String s = "[";
		int i;
		for (i = 0; i < M; i++) {
			s += x[i];
			s += ((i < M-1) ? "," : "] ");
		}
		return "[M=" + M + "]" + s;
	}
}
