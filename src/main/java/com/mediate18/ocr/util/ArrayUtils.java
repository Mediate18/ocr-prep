package com.mediate18.ocr.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {
	
	public static Double average(Double[] arr) {
		return sum(arr) / arr.length;
	}
	
	public static Double[] reverse(Double[] arr) {
		Double[] reverse = new Double[arr.length];
		System.arraycopy( arr, 0, reverse, 0, arr.length );
		for(int i = 0; i < reverse.length / 2; i++){
		    double temp = reverse[i];
		    reverse[i] = reverse[reverse.length - i - 1];
		    reverse[reverse.length - i - 1] = temp;
		}
		return reverse;
	}
	
	public static Integer[] reverse(Integer[] arr) {
		Integer[] reverse = new Integer[arr.length];
		System.arraycopy( arr, 0, reverse, 0, arr.length );
		for(int i = 0; i < reverse.length / 2; i++){
		    int temp = reverse[i];
		    reverse[i] = reverse[reverse.length - i - 1];
		    reverse[reverse.length - i - 1] = temp;
		}
		return reverse;
	}
	
	public static String[] reverse(String[] arr) {
		String[] reverse = new String[arr.length];
		System.arraycopy( arr, 0, reverse, 0, arr.length );
		for(int i = 0; i < reverse.length / 2; i++){
		    String temp = reverse[i];
		    reverse[i] = reverse[reverse.length - i - 1];
		    reverse[reverse.length - i - 1] = temp;
		}
		return reverse;
	}
	
	public static double sum(double[] arr) {
		double sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}
	
	public static double sum(Double[] arr) {
		double sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}
	
	public static int sum(int[] arr) {
		int sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}
	
	public static int sum(Integer[] arr) {
		int sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}
	
	public static double[][] transpose(double[][] arr) {
		double[][] newArr = new double[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}
	
	public static Double[][] transpose(Double[][] arr) {
		Double[][] newArr = new Double[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}
	
	public static int[][] transpose(int[][] arr) {
		int[][] newArr = new int[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}
	
	public static Integer[][] transpose(Integer[][] arr) {
		Integer[][] newArr = new Integer[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}
	
	public static String[][] transpose(String[][] arr) {
		String[][] newArr = new String[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}
	
	public static String[] toStringArray(int[] arr) {
		String[] newArr = new String[arr.length];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = String.valueOf(arr[i]);
		}
		return newArr;
	}

	public static Double[] getDips(Double[] arr) {
		List<Double> dips = new ArrayList<Double>();
		double dip = arr[0];
		boolean rising = true;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < dip) {
//			System.out.println("arr: "+arr[i]+", dip: "+dip+" -> falling");
				dip = arr[i];
				rising = false;
			} else if (dip < arr[i]) {
//				System.out.println("arr: "+arr[i]+", dip: "+dip+" -> rising");
				dip = arr[i];
				if (!rising) {
					dips.add(dip);
					rising = true;
				}
			}
		}
		return dips.toArray(new Double[dips.size()]);
	}

}
