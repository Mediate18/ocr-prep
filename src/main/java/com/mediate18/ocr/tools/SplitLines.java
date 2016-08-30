package com.mediate18.ocr.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.mediate18.ocr.image.PageImage;
import com.mediate18.ocr.util.ImageSplitter;

import magick.MagickException;

public class SplitLines extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( SplitLines.class.getName() );

	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder or Zip file containing multiple images.");
		else {
			SplitLines processor = new SplitLines();
			processor.start(args);
		}
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		String fileName = image.getFileName();
		String base = FilenameUtils.getBaseName(fileName);
		LOGGER.info("Processing file "+base);
		String ext = FilenameUtils.getExtension(fileName);
		String path = FilenameUtils.getPathNoEndSeparator(fileName);
		String linePath = "/"+path+"/"+base+"-line_extract_"+ext;
		new File(linePath).mkdirs();
		ImageSplitter splitter = new ImageSplitter();
		BufferedImage[] lines = splitter.splitLines(image);
		for (int i = 0; i < lines.length; i++) {
			String ii = "0"+i;
			if (i > 9)
				ii = ""+i;
			BufferedImage line = lines[i];
			String newFileName = linePath+"/line"+ii+"."+ext;
			ImageIO.write(line, ext, new File(newFileName));
		}
	}

}
