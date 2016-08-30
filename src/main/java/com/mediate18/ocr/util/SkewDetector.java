package com.mediate18.ocr.util;

import java.awt.Dimension;

//import org.apache.commons.io.FilenameUtils;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public class SkewDetector {
	private static double TANGENT = Math.tan(Math.toRadians(20));
	private static int SPLIT = 5;

	public static double detect(PageImage image) throws MagickException {
		// Get image width, divide into SPLIT parts
		Dimension dim = image.getDimension();
		int width = dim.width;
		int height = dim.height;
		int partSize = Math.round(width / SPLIT);
		int margin = (int) (partSize * TANGENT);
		// Create a matrix with dimensions (SPLIT - 1) * height x (margin * 2) + 1
		CorrelationMatrix matrix = new CorrelationMatrix((SPLIT - 1) * height, (margin * 2) + 1);
		int p = 0;
		for (int d1 = partSize; d1 < partSize * SPLIT; d1 += partSize) {
			int d2 = d1 + partSize;
			// For each pixel in d1
			for (int y = 1; y <= height; y++) {
				int d1Color = ColorDetector.isNotWhite(image.getOnePixel(d1, y));
				// For each pixel in corresponding range on d2
				int d2MinY = y - margin;
				for (int y2 = 0; y2 < (margin * 2) + 1; y2++) {
					int y2Real = d2MinY + y2;
					if (y2Real > 0 && y2Real <= height) {
						int d2Color = ColorDetector.isNotWhite(image.getOnePixel(d2, y2Real));
						// If both pixels are black
						if (d1Color + d2Color == 2)
							// Set correlation to 1
							matrix.set(y-1+(p * height), y2);
					}
				}
			}
			p++;
		}
		// Get column with highest sum
		int maxY = matrix.getMaxSumColumnIndex();
		// Determine and return degree
		double angle = angleOfTwoPoints(maxY-margin,partSize);
//		String file = FilenameUtils.getBaseName(image.getFileName());
//		System.out.println("("+file+") Margin: "+margin+", partSize: "+partSize+", maxY: "+(maxY-margin)+" ("+maxY+"), angle: "+angle);
		return angle;
	}
	
	private static double angleOfTwoPoints(int opposite, int adjacent) {
		if (opposite == 0)
			return 0.0;
		float angle = (float) Math.atan2(adjacent, opposite);
		return Math.toDegrees(angle) - 90;
	}

}
