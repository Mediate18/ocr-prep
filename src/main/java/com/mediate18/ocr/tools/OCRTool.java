package com.mediate18.ocr.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public abstract class OCRTool {
	protected static List<String> ALLOWED = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 3551467596496765208L;

	{
	    add("png");
	    add("jpg");
	    add("zip");
	}};
	protected static ZipFile fis;
	protected int increaseContrast = 0;
	protected boolean saveAllFiles = false;

	protected void start(String[] args) throws IOException, MagickException {
		if (args.length > 1) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-c"))
					this.increaseContrast = Integer.valueOf(args[i+1]);
				else if (args[i].equals("--saveAll") || (args[i].equals("-s") && args[i+1].equals("true")))
					this.saveAllFiles = true;
			}
		}
		String inputFileName = args[args.length-1];
		File inputFile = new File(inputFileName);
		if (!inputFile.exists())
			System.err.println("File or folder not found: "+inputFileName);
		else if (!inputFile.canRead())
			System.err.println("File or folder not readable: "+inputFileName);
		else if (inputFile.isDirectory()) {
			File[] listOfFiles = inputFile.listFiles();
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	if (listOfFiles[i].isFile()) {
		    		String ext = FilenameUtils.getExtension(listOfFiles[i].getAbsolutePath());
		    		if (ALLOWED.contains(ext))
		    			if (ext.equals("zip"))
		    				this.processZipFile(listOfFiles[i].getAbsolutePath());
		    			else
		    				this.processImageFile(listOfFiles[i].getAbsolutePath());
		    	}
		    }
		} else {
			String ext = FilenameUtils.getExtension(inputFileName);
    		if (ALLOWED.contains(ext))
    			if (ext.equals("zip"))
    				this.processZipFile(inputFileName);
    			else
    				this.processImageFile(inputFileName);
		}
	}
	
	protected void processImageFile(String path) throws MagickException, IOException {
		run(new PageImage(path));
	}

	protected void processZipFile(String path) throws IOException, MagickException {
		fis = new ZipFile(path);
		for (Enumeration<?> e = fis.entries(); e.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			InputStream in = fis.getInputStream(entry);
			BufferedImage bf = ImageIO.read(in);
			run(PageImage.fromBufferedImage(bf));
		}
	}
	
	protected abstract void run(PageImage image) throws MagickException, IOException;

}
