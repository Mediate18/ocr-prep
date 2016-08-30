package com.mediate18.ocr.util;

import java.awt.Rectangle;
import java.io.IOException;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public class MarginDetector {
	
	public static Rectangle detect(PageImage image) throws MagickException, IOException {
		int[][] pixels = image.getPixelMap();
		Double[] rowArr = PixelMapUtils.getPercentageArray(pixels, 0.05);
		int YfirstBlockIndex = PixelMapUtils.getFirstBlockIndex(rowArr,pixels[0].length);
		int YlowestBeforeFirstBlockIndex = PixelMapUtils.getLowestBeforeFirstBlockIndex(rowArr, YfirstBlockIndex);
		Double[] rowArrReverse = ArrayUtils.reverse(rowArr);
		int YlastBlockIndex = PixelMapUtils.getFirstBlockIndex(rowArrReverse,pixels[0].length);
		int YlowestAfterLastBlockIndex = rowArr.length - PixelMapUtils.getLowestBeforeFirstBlockIndex(rowArrReverse, YlastBlockIndex);
		
		pixels = ArrayUtils.transpose(pixels);
		Double[] columnArr = PixelMapUtils.getPercentageArray(pixels, 0.05);
		int XfirstBlockIndex = PixelMapUtils.getFirstBlockIndex(columnArr,pixels[0].length);
		int XlowestBeforeFirstBlockIndex = PixelMapUtils.getLowestBeforeFirstBlockIndex(columnArr, XfirstBlockIndex);
		Double[] columnArrReverse = ArrayUtils.reverse(columnArr);
		int XlastBlockIndex = PixelMapUtils.getFirstBlockIndex(columnArrReverse,pixels[0].length);
		int XlowestAfterLastBlockIndex = columnArr.length - PixelMapUtils.getLowestBeforeFirstBlockIndex(columnArrReverse, XlastBlockIndex);
		
		return new Rectangle(XlowestBeforeFirstBlockIndex,YlowestBeforeFirstBlockIndex,(XlowestAfterLastBlockIndex - XlowestBeforeFirstBlockIndex),(YlowestAfterLastBlockIndex - YlowestBeforeFirstBlockIndex));
	}

}
