package com.mediate18.ocr.util;

import java.util.Arrays;
import java.util.List;

public class PixelMapUtils {
	
	public static int getLowestBeforeFirstBlockIndex(Double[] arr, int blockIndex) {
		double lowest = 1.0;
		int lowestIndex = blockIndex;
		int lowestCount = 0;
		for (int i = blockIndex-1; i > -1; i--) {
			if (arr[i] < lowest) {
				lowest = arr[i];
				lowestIndex = i;
			} else if (arr[i] == lowest)
				lowestCount += 1;
			else {
				lowestIndex = Math.round(lowestIndex + lowestCount);
				break;
			}
		}
		return lowestIndex;
	}

	public static int getFirstBlockIndex(Double[] arr, int totalSize) {
		List<Double> list = Arrays.asList(arr);
		boolean isBlock = false;
		int index = list.indexOf(1.0);
		int displacedBy = 0;
		while (index > -1 && !isBlock) {
			int size = 1;
			for (int i = index + 1; i < list.size(); i++) {
				if (list.get(i) == 1.0)
					size += 1;
				else
					break;
			}
			double div = size / (totalSize * 1.0);
			if (div >= 0.05) {
				isBlock = true;
				index += displacedBy;
			} else {
				int displacement = index + size;
				displacedBy += displacement;
				list = list.subList(displacement, list.size()-1);
				index = list.indexOf(1.0);
			}
		}
		return index;
	}

	public static Double[] getPercentageArray(int[][] pixels) {
		int h = pixels.length;
		Double[] arr = new Double[h];
		for (int x = 0; x < h; x++) {
			arr[x] = ArrayUtils.sum(pixels[x]) / (h * 1.0);
		}
		return arr;
	}

	public static Double[] getPercentageArray(int[][] pixels, double threshold) {
		int h = pixels.length;
		Double[] arr = new Double[h];
		for (int x = 0; x < h; x++) {
			double perc = ArrayUtils.sum(pixels[x]) / (h * 1.0);
			if (perc < threshold)
				arr[x] = perc;
			else
				arr[x] = 1.0;
		}
		return arr;
	}
	
	public static int[] applyThreshold(Double[] arr, double threshold) {
		int[] tArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] < threshold)
				tArr[i] = 0;
			else
				tArr[i] = 1;
		}
		return tArr;
	}

}
