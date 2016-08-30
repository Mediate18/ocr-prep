package com.mediate18.ocr.util;

import magick.PixelPacket;

public class ColorDetector {

	public static int isNotWhite(PixelPacket pixel) {
		int blue = pixel.getBlue();
		int red = pixel.getRed();
		int green = pixel.getGreen();
		int requiredValue = 65000;
		if ( blue >= requiredValue && red >= requiredValue && green >= requiredValue )
			return 0;
		else
			return 1;
	}

}
