package com.enjoy;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class OcrTest {

	private final static String LANG_OPTION = "-l";
	private final static String EOL = System.getProperty("line.separator");
	
	/**
	 * Tesseract-OCR的安装路径
	 */
	private static String tessPath = "C:\\Users\\Administrator\\Tesseract-OCR";

	public static void main(String[] args) throws Exception {
		//System.out.println(OcrTest.recognizeText(new File("D:\\puppy\\3.png")));
		
		if(args == null || args.length != 4) {
			throw new Exception("待差处理的文件路径不正确");
		}
		String inputFilePath = args[0];
		String outputFilePath = args[1];
		String language = args[2];
		tessPath = args[3];
		if(inputFilePath.endsWith("pdf")) {
			PDF2IMG.pdf2Img(inputFilePath);
			inputFilePath = inputFilePath.substring(0,inputFilePath.indexOf(".")) + ".jpg";
		}
		recognizeText(new File(inputFilePath),new File(outputFilePath),language);
	}

	/**
	 * @param imageFile   传入的图像文件
	 * @param imageFormat 传入的图像格式
	 * @return 识别后的字符串
	 */
	public static String recognizeText(File inputFile,File outputFile,String language) throws Exception {
		StringBuffer strB = new StringBuffer();
		List<String> cmd = new ArrayList<String>();

		String outputFileName = outputFile.getName();
		
		cmd.add(tessPath + "//tesseract");
		cmd.add("");
		cmd.add(outputFile.getParent() + "\\" + outputFileName.substring(0,outputFileName.indexOf(".")));
		cmd.add(LANG_OPTION);
		cmd.add(language);
		// cmd.add("eng");

		ProcessBuilder pb = new ProcessBuilder();
		/**
		 * Sets this process builder's working directory.
		 */
		pb.directory(inputFile.getParentFile());
		cmd.set(1, inputFile.getName());
		pb.command(cmd);
		pb.redirectErrorStream(true);
		long startTime = System.currentTimeMillis();
		String format = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format);  
		System.out.println("开始时间：" + sdf.format(new Date(startTime)));
		Process process = pb.start();
		// tesseract.exe 1.jpg 1 -l chi_sim
		// 不习惯使用ProcessBuilder的，也可以使用Runtime，效果一致
		// Runtime.getRuntime().exec("tesseract.exe 1.jpg 1 -l chi_sim");
		/**
		 * the exit value of the process. By convention, 0 indicates normal termination.
		 */
//	      System.out.println(cmd.toString());
		int w = process.waitFor();
		if (w == 0)// 0代表正常退出
		{
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(outputFile), "UTF-8"));
			String str;

			while ((str = in.readLine()) != null) {
				strB.append(str).append(EOL);
			}
			in.close();

			long endTime = System.currentTimeMillis();
			System.out.println("结束时间：" + sdf.format(new Date(endTime)));
			System.out.println("耗时：" + (endTime - startTime)/1000 + "秒");
		} else {
			String msg;
			switch (w) {
			case 1:
				msg = "Errors accessing files. There may be spaces in your image's filename.";
				break;
			case 29:
				msg = "Cannot recognize the image or its selected region.";
				break;
			case 31:
				msg = "Unsupported image format.";
				break;
			default:
				msg = "Errors occurred.";
			}
			throw new RuntimeException(msg);
		}
		// new File(outputFile.getAbsolutePath() + ".txt").delete();
		return strB.toString().replaceAll("\\s*", "");
	}

	private static class PDF2IMG { 
	    public static void pdf2Img(String inputFilePath) throws IOException {
	        System.setProperty("apple.awt.UIElement", "true");
	        String password = "";
	        File inputFile = new File(inputFilePath);
	        String pdfFile = inputFile.getPath();	 
	        String outputFilePath=inputFile.getParentFile().getPath() + "\\";	 
	        String outputPrefix = inputFile.getName().substring(0, inputFile.getName().indexOf("."));
	        String imageFormat = "jpg";
	        int startPage = 1;
	        int endPage = 2147483647;
	        String color = "rgb";
	        float cropBoxLowerLeftX = 0.0F;
	        float cropBoxLowerLeftY = 0.0F;
	        float cropBoxUpperRightX = 0.0F;
	        float cropBoxUpperRightY = 0.0F;
	        boolean showTime = false;
	 
	        int dpi;
	        try {
	            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
	        } catch (HeadlessException var28) {
	            dpi = 96;
	        }
	 
	        if(pdfFile == null) {
	            usage();
	        } else {
	            PDDocument var30 = null;
	 
	            try {
	                var30 = PDDocument.load(new File(pdfFile), password);
	                ImageType imageType = null;
	                if("bilevel".equalsIgnoreCase(color)) {
	                    imageType = ImageType.BINARY;
	                } else if("gray".equalsIgnoreCase(color)) {
	                    imageType = ImageType.GRAY;
	                } else if("rgb".equalsIgnoreCase(color)) {
	                    imageType = ImageType.RGB;
	                } else if("rgba".equalsIgnoreCase(color)) {
	                    imageType = ImageType.ARGB;
	                }
	 
	                if(imageType == null) {
	                    System.err.println("Error: Invalid color.");
	                    System.exit(2);
	                }
	 
	                if(cropBoxLowerLeftX != 0.0F || cropBoxLowerLeftY != 0.0F || cropBoxUpperRightX != 0.0F || cropBoxUpperRightY != 0.0F) {
	                    changeCropBox(var30, cropBoxLowerLeftX, cropBoxLowerLeftY, cropBoxUpperRightX, cropBoxUpperRightY);
	                }
	 
	                long startTime = System.nanoTime();
	                boolean success = true;
	                endPage = Math.min(endPage, var30.getNumberOfPages());
	                PDFRenderer renderer = new PDFRenderer(var30);
	 
	                for(int endTime = startPage - 1; endTime < endPage; ++endTime) {
	                    BufferedImage image = renderer.renderImageWithDPI(endTime, (float)dpi, imageType);
	                    String duration = outputFilePath + outputPrefix + "." + imageFormat;
	                    success &= ImageIOUtil.writeImage(image, duration,dpi);
	                }
	 
	                long var31 = System.nanoTime();
	                long var32 = var31 - startTime;
	                int count = 1 + endPage - startPage;
	                if(showTime) {
	                    System.err.printf("Rendered %d page%s in %dms\n", new Object[]{Integer.valueOf(count), count == 1?"":"s", Long.valueOf(var32 / 1000000L)});
	                }
	 
	                if(!success) {
	                    System.err.println("Error: no writer found for image format \'" + imageFormat + "\'");
	                    System.exit(1);
	                }
	            } catch(Exception ex){
	                ex.printStackTrace();
	            }finally {
	                if(var30 != null) {
	                    var30.close();
	                }
	 
	            }
	            System.out.println("pdf转换jpg完成");
	 
	        }
	 
	    }
	 
	    private static void usage() {
	        String message = "Usage: java -jar pdfbox-app-x.y.z.jar PDFToImage [options] <inputfile>\n\nOptions:\n  -password  <password>            : Password to decrypt document\n  -format <string>                 : Image format: " + getImageFormats() + "\n" + "  -prefix <string>                 : Filename prefix for image files\n" + "  -page <number>                   : The only page to extract (1-based)\n" + "  -startPage <int>                 : The first page to start extraction (1-based)\n" + "  -endPage <int>                   : The last page to extract(inclusive)\n" + "  -color <int>                     : The color depth (valid: bilevel, gray, rgb, rgba)\n" + "  -dpi <int>                       : The DPI of the output image\n" + "  -cropbox <int> <int> <int> <int> : The page area to export\n" + "  -time                            : Prints timing information to stdout\n" + "  <inputfile>                      : The PDF document to use\n";
	        System.err.println(message);
	        System.exit(1);
	    }
	 
	    private static String getImageFormats() {
	        StringBuilder retval = new StringBuilder();
	        String[] formats = ImageIO.getReaderFormatNames();
	 
	        for(int i = 0; i < formats.length; ++i) {
	            if(formats[i].equalsIgnoreCase(formats[i])) {
	                retval.append(formats[i]);
	                if(i + 1 < formats.length) {
	                    retval.append(", ");
	                }
	            }
	        }
	 
	        return retval.toString();
	    }
	 
	    private static void changeCropBox(PDDocument document, float a, float b, float c, float d) {
	        Iterator<?> i$ = document.getPages().iterator();
	 
	        while(i$.hasNext()) {
	            PDPage page = (PDPage)i$.next();
	            System.out.println("resizing page");
	            PDRectangle rectangle = new PDRectangle();
	            rectangle.setLowerLeftX(a);
	            rectangle.setLowerLeftY(b);
	            rectangle.setUpperRightX(c);
	            rectangle.setUpperRightY(d);
	            page.setCropBox(rectangle);
	        }
	 
	    }
	}
}
