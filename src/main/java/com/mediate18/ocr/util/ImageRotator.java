package com.mediate18.ocr.util;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class ImageRotator {
	
	public static void rotate(double deg, String inputFileName, String outputFileName) throws MagickException {
		ImageInfo origInfo = new ImageInfo(inputFileName); //load image info
		PageImage image = new PageImage(origInfo); //load image
		image = (PageImage) image.rotateImage(deg); // rotate image
		image.setFileName(outputFileName); //give new location
		image.writeImage(origInfo); //save
	}
	
}
