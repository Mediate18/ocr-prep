package com.mediate18.ocr.tools;

import java.io.IOException;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class RunLengthSmoothing extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( RunLengthSmoothing.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder containing multiple images.");
		else {
			RunLengthSmoothing processor = new RunLengthSmoothing();
			processor.start(args);
		}
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		PageImage[] images = image.splitVertical2AndSave();
		for (int i = 0; i < images.length; i++) {
			images[i].contrastImage(false);
			String oldName = images[i].getFileName();
			images[i].setFileName(images[i].generateFilename("contrast"));
			images[i].writeImage(new ImageInfo(oldName));
			images[i].generateRLSAImage(true).generateRLSAImage(false).getPixelMap();
		}
	}

}
