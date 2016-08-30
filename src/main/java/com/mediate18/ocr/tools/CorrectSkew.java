package com.mediate18.ocr.tools;

import java.io.IOException;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public class CorrectSkew extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( CorrectSkew.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder containing multiple images.");
		else {
			CorrectSkew processor = new CorrectSkew();
			processor.start(args);
		}
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		// TODO Auto-generated method stub
		
	}

}
