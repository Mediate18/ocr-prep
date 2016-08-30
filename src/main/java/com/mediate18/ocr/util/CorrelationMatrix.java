package com.mediate18.ocr.util;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrelationMatrix {
	private int rows = 0;
	private int columns = 0;
	private int[][] data;
	
	public CorrelationMatrix(int r, int c) {
		rows = r;
		columns = c;
		data = new int[rows][columns];
		reset();
	}
	
	public Dimension getDimension() {
		Dimension dim = new Dimension();
		dim.setSize(columns, rows);
		return dim;
	}
	
	public void reset() {
		for (int i = 0; i < rows; i++)
			Arrays.fill(data[i], 0);
	}
	
	public void set(int x, int y) {
		data[x][y] = 1;
	}
	
	public void unset(int x, int y) {
		data[x][y] = 0;
	}
	
	public int getMaxSumColumnIndex() {
		int maxSum = 0;
		List<Integer> maxIndices = new ArrayList<Integer>();
		for (int y = 0; y < columns; y++) {
			int sum = 0;
			for (int x = 0; x < rows; x++)
				sum += data[x][y];
			if (sum > maxSum) {
				maxSum = sum;
				maxIndices = new ArrayList<Integer>();
				maxIndices.add(y);
			} else if (sum == maxSum)
				maxIndices.add(y);
		}
		return getMedian(maxIndices.toArray(new Integer[ maxIndices.size() ]));
	}
	
	private int getMedian(Integer[] numberList) {
		int median = 0;
		int pos1 = (int) Math.round(Math.floor((numberList.length - 1) / 2));
		int pos2 = (int) Math.round(Math.ceil((numberList.length - 1) / 2));
		if (pos1 == pos2) {
			median = numberList[(int)pos1];
		} else {
			median = (int) Math.round((numberList[(int)pos1] + numberList[(int)pos2]) / 2);
		}
		return median;
	}

}
