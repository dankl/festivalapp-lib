package com.dank.festivalapp.lib;

/**
 * a simple pair class, similar to c++ pair
 * @author dank
 *
 * @param <T>
 * @param <U>
 */
public class Pair<T, U> {         
	public final T first;
	public final U second;

	public Pair(T first, U second) {         
		this.first= first;
		this.second= second;
	}
}