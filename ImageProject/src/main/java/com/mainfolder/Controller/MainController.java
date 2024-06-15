package com.mainfolder.Controller;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.mainfolder.Service.Implementationclass;

@RequestMapping("/image")
@RestController
public class MainController {
	@Autowired
	private Implementationclass service;
	@Value("${image.output.folder}")
	private String outputFolder;
	@Value("${image.intermediate.write}")
	private String writeIntermediate;
	@GetMapping("/urls")
	public ModelAndView mv() {
		ModelAndView mv=new ModelAndView();
		mv.setViewName("formpage");
		return mv;
	}
	@PostMapping(value="/actions",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> data(@RequestParam("depthimg")MultipartFile depthimg,@RequestParam("intensity")MultipartFile intensityimg) throws Exception {        
		try {
			
		      File folder = new File(outputFolder);
		      File[] files = folder.listFiles();
		      
		      for(File file:files) {
		    	  System.out.println("File Name :"+file);
		      }

			int[][] depth = service.getImagePixelsdata(depthimg,"depthpixels");
			int[][] intensity = service.getImagePixelsdata(intensityimg,"intensitypixels");

		    if (depth.length != intensity.length || depth[0].length != intensity[0].length) {
                return new ResponseEntity<>("Both images are Not in same size",HttpStatus.NOT_ACCEPTABLE);
            }

			double[] depthmean = service.calculateMean(depth);
			double[] depthStdDev = service.calculateStdDev(intensity);
			double[] maxarr=service.maxarr(depthmean,depthStdDev);
			double[] minarr=service.maxarr(depthmean,depthStdDev);
			int[][] correlationMatrix = service.correlationMatrix(depth, intensity);
			boolean[] depthbool = service.getBool(depth);
			boolean[] intensitybool = service.getBool(intensity);
			boolean[] noise = service.getNoise(depthbool, intensitybool);
			if ("YES".equalsIgnoreCase(writeIntermediate)){
			try (SXSSFWorkbook workbook = new SXSSFWorkbook(120)) {
				// Create output stream
				FileOutputStream fos = new FileOutputStream(outputFolder + "\\ImageOutputValues.xlsx");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				// Create sheets
				SXSSFSheet sheet1 = workbook.createSheet("DEPTH");
				SXSSFSheet sheet2 = workbook.createSheet("INTENSITY");
				SXSSFSheet sheet3 = workbook.createSheet("NOISE-REDUCTION");
				SXSSFSheet sheet4 = workbook.createSheet("DIFF MATRIX");
				SXSSFSheet sheet5 = workbook.createSheet("FINAL CLASSIFICATION");
				// Create column names for depth and mean
				String[] depthColumnNames = {"Depth"};
				String[] meanColumnNames = {"Mean","Stddiv","","","Max","Min"};

				// Write column names for depth
				Row depthHeaderRow = sheet1.createRow(0);
				for (int i = 0; i < depthColumnNames.length; i++) {
					Cell cell = depthHeaderRow.createCell(i);
					cell.setCellValue(depthColumnNames[i]);
				}

				// Write column names for mean
				Row meanHeaderRow = sheet1.getRow(0); // Assuming the header row already exists
				for (int i = 0; i < meanColumnNames.length; i++) {
					Cell cell = meanHeaderRow.createCell(depth[0].length + 2 + i); // Add 2 empty columns
					cell.setCellValue(meanColumnNames[i]);
				}

				// Write depth data
				for (int i = 0; i < depth.length; i++) {
					Row row = sheet1.createRow(i + 1); // Start from the second row after the header
					for (int j = 0; j < depth[i].length; j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(depth[i][j]);
					}
					// Add two empty columns after depth values
					for (int k = 0; k < 2; k++) {
						row.createCell(depth[i].length + k);
					}
					// Add depth mean values beside depth values with two empty columns in between
					Cell meanCell = row.createCell(depth[i].length + 2);
					meanCell.setCellValue(depthmean[i]);
					Cell stddevCell = row.createCell(depth[i].length + 2 + 1);
					stddevCell.setCellValue(depthStdDev[i]);
					Cell maxarrCell = row.createCell(depth[i].length + 6);
					maxarrCell.setCellValue(maxarr[i]);
					Cell minarrCell = row.createCell(depth[i].length + 7);
					minarrCell.setCellValue(minarr[i]);  
				}
				double[] intensitymean = service.calculateMean(intensity);
				double[] intensityStdDev = service.calculateStdDev(intensity);
				double[] intensitymaxarr=service.maxarr(intensitymean,intensityStdDev);
				double[] intensityminarr=service.maxarr(intensitymean,intensityStdDev);
				String[] intensityColumnNames = {"Intensity"};
				String[] intensitymeanColumnNames = {"Mean","Stddiv","","","Max","Min"};
				Row intensityHeaderRow = sheet2.createRow(0);
				for (int i = 0; i < intensityColumnNames.length; i++) {
					Cell cell = intensityHeaderRow.createCell(i);
					cell.setCellValue(intensityColumnNames[i]);
				}
				// Write column names for mean
				Row intensitymeanHeaderRow = sheet2.getRow(0); // Assuming the header row already exists
				for (int i = 0; i < intensitymeanColumnNames.length; i++) {
					Cell cell = intensitymeanHeaderRow.createCell(intensity[0].length + 2 + i); // Add 2 empty columns
					cell.setCellValue(meanColumnNames[i]);
				}

				for (int i = 0; i < intensity.length; i++) {
					Row row = sheet2.createRow(i + 1); // Start from the second row after the header
					for (int j = 0; j < intensity[i].length; j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(intensity[i][j]);
					}
					// Add two empty columns after depth values
					for (int k = 0; k < 2; k++) {
						row.createCell(intensity[i].length + k);
					}
					// Add depth mean values beside depth values with two empty columns in between
					Cell meanCell = row.createCell(intensity[i].length + 2);
					meanCell.setCellValue(intensitymean[i]);
					Cell stddevCell = row.createCell(intensity[i].length + 2 + 1);
					stddevCell.setCellValue(intensityStdDev[i]);
					Cell maxarrCell = row.createCell(intensity[i].length + 6);
					maxarrCell.setCellValue(intensitymaxarr[i]);
					Cell minarrCell = row.createCell(intensity[i].length + 7);
					minarrCell.setCellValue(intensityminarr[i]); 
				}	
				String[] collerationColumnNames = {"DIFF MATRIX"};
				String[] collerationmeanColumnNames = {"Mean","Stddiv","","","Max","Min"};
				Row diffHeaderRow = sheet4.createRow(0);
				for (int i = 0; i < collerationColumnNames.length; i++) {
					Cell cell = diffHeaderRow.createCell(i);
					cell.setCellValue(collerationColumnNames[i]);
				}
				// Write column names for mean
				Row collerationmeanHeaderRow = sheet4.getRow(0); // Assuming the header row already exists
				for (int i = 0; i < collerationmeanColumnNames.length; i++) {
					Cell cell = collerationmeanHeaderRow.createCell(intensity[0].length + 2 + i); // Add 2 empty columns
					cell.setCellValue(collerationmeanColumnNames[i]);
				}
				
				double[] collarationmean = service.calculateMean(correlationMatrix);
				double[] collerationStdDev = service.calculateStdDev(correlationMatrix);
				double[] collaramaxarr=service.maxarr(collarationmean,collerationStdDev);
				double[] collaraminarr=service.maxarr(collaramaxarr,collerationStdDev);

				// Write colerationmatrix data
				for (int i = 0; i < correlationMatrix.length; i++) {
					Row row = sheet4.createRow(i + 1); // Start from the second row after the header
					for (int j = 0; j < correlationMatrix[i].length; j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(correlationMatrix[i][j]);
					}
					// Add two empty columns after depth values
					for (int k = 0; k < 2; k++) {
						row.createCell(correlationMatrix[i].length + k);
					}			
					Cell meanCell = row.createCell(correlationMatrix[i].length + 2);
					meanCell.setCellValue(collarationmean[i]);
					Cell stddevCell = row.createCell(correlationMatrix[i].length + 2 + 1);
					stddevCell.setCellValue(collerationStdDev[i]);
					Cell maxarrCell = row.createCell(correlationMatrix[i].length + 6);
					maxarrCell.setCellValue(collaramaxarr[i]);
					Cell minarrCell = row.createCell(correlationMatrix[i].length + 7);
					minarrCell.setCellValue(collaraminarr[i]); 
				}

				Row finalHeaderRow = sheet5.createRow(0);
				for (int i = 0; i < collerationColumnNames.length; i++) {
					Cell cell = finalHeaderRow.createCell(i);
					cell.setCellValue(collerationColumnNames[i]);
				}

				for (int i = 0; i < correlationMatrix.length; i++) {
					Row row = sheet5.createRow(i + 1); // Start from the second row after the header
					for (int j = 0; j < correlationMatrix[i].length; j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(correlationMatrix[i][j]);
					}
				}
				Row noicedepthHeaderRow = sheet3.createRow(0);
				for (int i = 0; i < depthColumnNames.length; i++) {
					Cell cell = noicedepthHeaderRow.createCell(i);
					cell.setCellValue(depthColumnNames[i]);
				}
				Row noicemeanHeaderRow = sheet3.getRow(0); // Assuming the header row already exists
				for (int i = 0; i < intensityColumnNames.length; i++) {
					Cell cell = noicemeanHeaderRow.createCell(intensity[0].length + 5 + i); // Add 2 empty columns
					cell.setCellValue(intensityColumnNames[i]);
				}

				for (int i = 0; i < depth.length; i++) {
					Row row = sheet3.createRow(i + 1); // Start from the second row after the header
					for (int j = 0; j < depth[i].length; j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(depth[i][j]);
					}
					
					// Add intensity data with a 5-column gap
					for (int j = 0; j < intensity[i].length; j++) {
						Cell cell = row.createCell(depth[0].length + 5 + j);
						cell.setCellValue(intensity[i][j]);
					}
				}
				workbook.write(bos);
				workbook.dispose();
				bos.close();
				boolean[] correlationmatrix_bool = service.getBool(correlationMatrix);
				boolean[] green_scale_df = service.getNoise(noise, correlationmatrix_bool);
				int[][] greenScaleImg = service.getGreenScaleImg(green_scale_df, intensity);
				service.createOutputImage(greenScaleImg,green_scale_df,outputFolder);
			}
		}
			boolean[] correlationmatrix_bool = service.getBool(correlationMatrix);
			boolean[] green_scale_df = service.getNoise(noise, correlationmatrix_bool);
			int[][] greenScaleImg = service.getGreenScaleImg(green_scale_df, intensity);
			service.createOutputImage(greenScaleImg,green_scale_df,outputFolder);

		}catch (Exception e) {
			return new ResponseEntity<>("File Not Found",HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<String>("Success",HttpStatus.OK);
	}

}
