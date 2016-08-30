package com.mediate18.ocr.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public class ImageSplitter {
	private Properties prop = new Properties();
	
	public ImageSplitter() {
		InputStream input = null;
		try {
			File jarPath = new File(RLSA.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	        String propertiesPath = jarPath.getParentFile().getAbsolutePath();
	        File customPropFile = new File(propertiesPath+"/ocr-prep.properties");
	        if (customPropFile.exists() && customPropFile.canRead())
	        	input = new FileInputStream(customPropFile);
	        else
	        	input = getClass().getClassLoader().getResourceAsStream("imagesplitter.properties");
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public PageImage[] splitHorizontal(PageImage image) throws MagickException, IOException {
		Dimension dim = image.getDimension();
		if (dim.height > dim.width) {
			int width = dim.width;
			int halfHeight = Math.round(dim.height/2);
			Rectangle chopInfoTop = new Rectangle(0,0,width,halfHeight);
			Rectangle chopInfoBottom = new Rectangle(0,halfHeight,width,halfHeight);
			PageImage topImage = image.cropAndSave(chopInfoTop);
			PageImage bottomImage = image.cropAndSave(chopInfoBottom);
			return new PageImage[]{topImage, bottomImage};
		}
		return new PageImage[]{image};
	}
	
	public PageImage[] splitVertical(PageImage image) throws MagickException, IOException {
		Dimension dim = image.getDimension();
		if (dim.width > dim.height) {
			int halfWidth = Math.round(dim.width/2);
			int height = dim.height;
			Rectangle chopInfoLeft = new Rectangle(0,0,halfWidth,height);
			Rectangle chopInfoRight = new Rectangle(halfWidth,0,halfWidth,height);
			PageImage leftImage = image.cropAndSave(chopInfoLeft);
			PageImage rightImage = image.cropAndSave(chopInfoRight);
			return new PageImage[]{leftImage, rightImage};
		}
		return new PageImage[]{image};
	}
	
	public BufferedImage[] splitLines(PageImage image) throws MagickException, IOException {
		List<BufferedImage> lines = new ArrayList<BufferedImage>();
		// Get histogram of rows with percentage colored
		int[][] pixels = image.getPixelMap();
		Double[] rowArr = PixelMapUtils.getPercentageArray(pixels);
		double t = ArrayUtils.average(ArrayUtils.getDips(rowArr));
		System.out.println("Using pixel threshold: "+t);
		// Apply threshold
		int[] tRowArr = PixelMapUtils.applyThreshold(rowArr, t);
		Dimension dim = image.getDimension();
		int w = dim.width;
		double h = dim.height * 1.0;
		String arrStr = StringUtils.join(ArrayUtils.toStringArray(tRowArr),"");
		String[] parts = arrStr.replace("10", "1,0").replace("01", "0,1").split(",");
		// Replace the outer values of sequences of zeros with 1's (00000 -> 11011, 0000 -> 1101) 
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.matches("0+")) {
				int l = part.length();
				if ((l % 2) == 0) {
					int n1 = l / 2;
					int n2 = n1 - 1;
					char[] chars1 = new char[n1];
					Arrays.fill(chars1, '1');
					char[] chars2 = new char[n2];
					Arrays.fill(chars2, '1');
					parts[i] = new String(chars1)+"0"+new String(chars2);
				} else {
					int n = l / 2;
					char[] chars = new char[n];
					Arrays.fill(chars, '1');
					parts[i] = new String(chars)+"0"+new String(chars);
				}
			}
		}
		parts = StringUtils.join(parts,"").replace("10", "1,0").replace("01", "0,1").split(",");
		// Split on uncolored sequences
		int offset = 0;
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.matches("1+") && (part.length() / h) > Double.valueOf(prop.getProperty("lineheight_threshold", "0.01"))) {
				lines.add(image.crop(new Rectangle(0, offset, w, part.length())));
			}
			offset += part.length();
		}
		return lines.toArray(new BufferedImage[]{});
	}

}
