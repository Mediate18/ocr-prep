package com.mediate18.ocr.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class CorrectMargins extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( CorrectMargins.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder containing multiple images.");
		else {
			CorrectMargins processor = new CorrectMargins();
			processor.start(args);
		}
	}
	
	private int getLowestBeforeFirstBlockIndex(Double[] arr, int blockIndex) {
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

	private int getFirstBlockIndex(Double[] arr, int totalSize) {
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

	private Double[] getPercentageArray(int[][] pixels) {
		int h = pixels.length;
		Double[] arr = new Double[h];
		for (int x = 0; x < h; x++) {
			double perc = sum(pixels[x]) / (h * 1.0);
			if (perc < 0.05)
				arr[x] = perc;
			else
				arr[x] = 1.0;
		}
		return arr;
	}
	
	private Double[] reverse(Double[] arr) {
		Double[] reverse = new Double[arr.length];
		System.arraycopy( arr, 0, reverse, 0, arr.length );
		for(int i = 0; i < reverse.length / 2; i++){
		    double temp = reverse[i];
		    reverse[i] = reverse[reverse.length - i - 1];
		    reverse[reverse.length - i - 1] = temp;
		}
		return reverse;
	}
	
	private int sum(int[] arr) {
		int sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}
	
	private int[][] transpose(int[][] arr) {
		int[][] newArr = new int[arr[0].length][arr.length];
		for (int x = 0; x < arr.length; x++)
			for (int y = 0; y < arr[x].length; y++)
				newArr[y][x] = arr[x][y];
		return newArr;
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		PageImage[] images = image.splitVertical2AndSave();
		for (int i = 0; i < images.length; i++) {
			images[i].contrastImage(false);
			String oldName = images[i].getFileName();
			images[i].setFileName(images[i].generateFilename("contrast"));
			images[i].writeImage(new ImageInfo(oldName));
			
			int[][] pixels = images[i].generateRLSAImage(true).generateRLSAImage(false).getPixelMap();
			Double[] rowArr = getPercentageArray(pixels);
			int YfirstBlockIndex = getFirstBlockIndex(rowArr,pixels[0].length);
			int YlowestBeforeFirstBlockIndex = getLowestBeforeFirstBlockIndex(rowArr, YfirstBlockIndex);
			Double[] rowArrReverse = reverse(rowArr);
			int YlastBlockIndex = getFirstBlockIndex(rowArrReverse,pixels[0].length);
			int YlowestAfterLastBlockIndex = rowArr.length - getLowestBeforeFirstBlockIndex(rowArrReverse, YlastBlockIndex);
			
			pixels = transpose(pixels);
			Double[] columnArr = getPercentageArray(pixels);
			int XfirstBlockIndex = getFirstBlockIndex(columnArr,pixels[0].length);
			int XlowestBeforeFirstBlockIndex = getLowestBeforeFirstBlockIndex(columnArr, XfirstBlockIndex);
			Double[] columnArrReverse = reverse(columnArr);
			int XlastBlockIndex = getFirstBlockIndex(columnArrReverse,pixels[0].length);
			int XlowestAfterLastBlockIndex = columnArr.length - getLowestBeforeFirstBlockIndex(columnArrReverse, XlastBlockIndex);
			
			LOGGER.info("x: "+XlowestBeforeFirstBlockIndex+", y: "+YlowestBeforeFirstBlockIndex+", width: "+(XlowestAfterLastBlockIndex - XlowestBeforeFirstBlockIndex)+", height: "+(YlowestAfterLastBlockIndex - YlowestBeforeFirstBlockIndex));
		}
	}

}
