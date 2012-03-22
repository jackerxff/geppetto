/**
 * Copyright (c) 2011 Cloudsmith Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Cloudsmith
 * 
 */
package org.cloudsmith.xtext.textflow;

/**
 * An implementation of utility CharSequences providing efficient strings consisting of a fixed space,
 * or specified character, and a repeating sequence.
 * 
 */
public class CharSequences {

	/**
	 * A CharSequence consisting of a concatenation of two sequences
	 * 
	 */
	public static class Concatenation implements CharSequence {
		private CharSequence b;

		private CharSequence a;

		private int aLength;

		public Concatenation(CharSequence a, CharSequence b) {
			this.a = a;
			this.aLength = a.length();
			this.b = b;
		}

		@Override
		public char charAt(int index) {
			if(index < aLength)
				return a.charAt(index);
			return b.charAt(index - aLength);
		}

		@Override
		public int length() {
			return a.length() + b.length();
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubSequence(this, start, end);
		}

	}

	/**
	 * An CharSequence consisting of a sequence of the same character.
	 * 
	 */
	public static class Fixed implements CharSequence {

		private int count;

		private char c;

		public Fixed(char c, int count) {
			this.c = c;
			this.count = count;
		}

		@Override
		public char charAt(int index) {
			return c;
		}

		@Override
		public int length() {
			return count;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new Fixed(c, end - start);
		}
	}

	/**
	 * An CharSequence consisting of a sequence of the same character.
	 * 
	 */
	public static class RepeatingString implements CharSequence {

		private int count;

		private String s;

		private int length;

		public RepeatingString(String s, int count) {
			this.s = s;
			this.count = count;
			this.length = s.length();
		}

		@Override
		public char charAt(int index) {
			return s.charAt(index % length);
		}

		@Override
		public int length() {
			return count * length;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubSequence(this, start, end);
		}
	}

	/**
	 * An CharSequence consisting of only space
	 * 
	 */
	public static class Spaces implements CharSequence {

		private int count;

		public Spaces(int count) {
			this.count = count;
		}

		@Override
		public char charAt(int index) {
			return ' ';
		}

		@Override
		public int length() {
			return count;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new Spaces(end - start);
		}
	}

	public static class SubSequence implements CharSequence {
		private CharSequence original;

		private int start;

		private int end;

		public SubSequence(CharSequence original, int start, int end) {
			this.original = original;
			this.start = start;
			this.end = end;
		}

		@Override
		public char charAt(int index) {
			return original.charAt(start + index);
		}

		@Override
		public int length() {
			return end - start;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubSequence(this, start, end);
		}

	}

	/**
	 * Produces an efficient concatenation of two CharSequences in a safe way (they may be null).
	 * No copies are being made (thus for excessive iteration of the result it is better to
	 * make a copy of the result).
	 * If a concatenation is not really needed (one is null or empty) the other sequence is returned.
	 * 
	 * @param a
	 * @param b
	 * @return a CharSequence being a concatenation
	 */
	public static CharSequence concatenate(CharSequence a, CharSequence b) {
		if(a == null)
			a = "";
		if(b == null)
			b = "";
		if(a.length() == 0)
			return b;
		if(b.length() == 0)
			return a;
		return new Concatenation(a, b);
	}
}