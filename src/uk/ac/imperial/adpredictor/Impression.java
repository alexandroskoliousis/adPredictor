package uk.ac.imperial.adpredictor; /* My package. */

import java.io.*;

/*
 * An impression is a collection of N `Feature` objects,
 * plus a result (clicked or not).
 *
 */

public class Impression {
	
	private int N;
	private Feature X [];
	private boolean clicked = false;
	
	public Impression (String s) throws Exception {
		int i;
		String [] features = s.split(":");
		N = features.length -1;
		X = new Feature [N];
		for (i = 0; i < N; i++) {
			X[i] = new Feature(features[i]);
			if (! X[i].isValid())
				throw new Exception ("Feature " + features[i] + " is not valid.");
		}
		clicked = (Integer.parseInt(features[N]) == 1);
	}
	
	private String _click_() {
		return (clicked) ? "Clicked" : "";
	}
	
	public int isClicked() {
		return (clicked) ? 1 : -1;
	}

	public String toString() {
		String s = "";
		int i;
		for (i = 0; i < N; i++) s += X[i];
		return "[N=" + N + "]: " + s + _click_();
	}
	
	public int [][] toArray() {
		int i, j, M;
		int [][] A = new int [N][];
		for (i = 0; i < N; i++) {
			M = X[i].size();
			A[i] = new int [M];
			for (j = 0; j < M; j++)
				A[i][j] = X[i].elementAt(j);
		}
		return A;
	}
}

