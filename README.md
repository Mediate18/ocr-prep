# OCR image preparation

This application offers some basic functionality to prepare images for further processing with OCR (Optical Character Recognition) software. It is specifically intended for preprocessing of scanned book pages and runs on single image files (PNG or JPEG), whole directories, or even a Zip file containing images.
The application requires [ImageMagick](http://www.imagemagick.org), Java 1.5 or up, and [Apache Maven](https://maven.apache.org).

### Functionality currently included
- Contrast correction: all of the tools included have a command line option for setting the contrast level;
- Page splitting: all of the tools included split an image in two if the width of the input image exceeds the height;
- Skew correction: correct rotation of input image;
- Margin correction: crop the image to only the section containing the text;
- Line splitting: split a block of text into images of the separate lines;
- Run-length smoothing (RLSA): converts sequences of intermittently colored pixels into a fully colored sequence. This essentially converts the printed parts of a page into a completely colored block. RLSA is used by the application to detect text sections on a page.

### Functionality to be added
- Configurable page splitting: turn off the splitting if needed;
- Column detection: this will improve line splitting for pages with multiple columns;
- Image detection: detect images and other graphics and remove these.

## Installation

Make sure you have [ImageMagick](http://www.imagemagick.org/script/binary-releases.php), [Java](https://www.java.com/en/download/help/download_options.xml), and [Apache Maven](https://maven.apache.org/install.html) installed on your system. Then download or clone this repository and descend into its main folder.
Execute the following command to build the application:

```
	$ mvn package
```

This will create a new folder labeled 'target' in which you will find a file *ocr-prep-X.X.X-SNAPSHOT.jar*. Feel free to rename this file or move it to a more convenient location.

## Usage

The main application will apply all corrections (page splitting, skew correction, margin correction, line splitting) to each input image, with optional contrast correction, using the following command:

```
	$ java -Djava.library.path=/path/to/ImageMagick/ -jar ocr-prep-X.X.X-SNAPSHOT.jar [-c X] [--saveAll] /path/to/input
```

The square brackets denote optional parameters, so remove these when entering the command.
Replace the 'X' with a numerical value. A higher value means more contrast.
The '--saveAll' option, when set, will cause all intermediate images to be saved to disk. If not set, only the final result will be saved (processed image and line images).
The output is written to a folder labeled 'output' in the same directory as the processed image.

Some of the functionality can be called separately using the following command:

```
	$ java -Djava.library.path=/path/to/ImageMagick/ -cp ocr-prep-X.X.X-SNAPSHOT.jar com.mediate18.ocr.tools.ClassName [-c X] [--saveAll] /path/to/input
```

The square brackets denote optional parameters, so remove these when entering the command. Replace 'ClassName' with one of the following classes:
- CorrectSkew
- CorrectMargins
- SplitLines
- RunLengthSmoothing

And again, replace the 'X' with a numerical value to set the contrast, and use '--saveAll' to keep intermediate images.
