package brightnessCorrector_;

/** ===============================================================================
* BrightnessCorrector_.java Version 0.0.1
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation 
* (http://www.gnu.org/licenses/gpl.txt )
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/gpl-3.0.html>.
*  
* Copyright (C) 2018: Sebastian Raßmann, Jan N. Hansen, and Jan F. Jikeli;
* 		research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany
* 		(http://www.iii.uni-bonn.de/en/wachten_lab/).
* 
* Funding: DFG priority program SPP 1926
*    
* For any questions please feel free to contact me (rassmann@uni-bonn.de).
*
* =============================================================================== */

import java.awt.Font;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.UIManager;

import FilesDialog.SimpleDialog;
import exceptions.ChoosenNumberOfChannelsDoesNotMatchImpException;
import exceptions.FileIsNoImageException;
import exceptions.GdCancelledException;
import exceptions.UnknownTaskVariantException;
import calculations.GaussianBlur;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import ij.text.TextPanel;

public class Main implements PlugIn{
	
	//Name variables
	static final String PLUGINNAME = "Brightness Corrector";
	static final String PLUGINVERSION = "0.0.1";
	static String PLUGINBUIDDATE = "unknown Date";
	long start, stop, runtime;
	
	//Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 20);
	
	//Fix formats
	DecimalFormat dformat6 = new DecimalFormat("#0.000000");
	DecimalFormat dformat3 = new DecimalFormat("#0.000");
	DecimalFormat dformat0 = new DecimalFormat("#0");
	DecimalFormat dformatDialog = new DecimalFormat("#0.000000");	
		
	static final String[] nrFormats = {"US (0.00...)", "Germany (0,00...)"};	
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//Progress Dialog
	ProgressDialog progressDialog;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	Date startDate;
	
	//--------------define params for Generic Dialog-----------------
	static final String[] taskVariant = {"active image in FIJI","multiple images (open multi-task manager)", "all images open in FIJI"};
	String selectedTaskVariant = taskVariant[1];
	static final String[] bioFormats = {".tif" , "raw microscopy file (e.g. OIB-file)"};
	String bioFormat = bioFormats [1];
	boolean saveDate = false;
	boolean saveParam = true;
	String ChosenNumberFormat = "Germany (0,00...)";
	boolean saveMask = true;
	boolean resultsToNewFolder = true;
	int channels = 4;
	
	//--------------task/file/meta data---------------------	
	TextPanel tp1;
	Date currentDate;
	String taskFolder, resultFolder;
	
	int tasks = 1;
	int task;
	ArrayList<Task> taskList;
	int subTasks = 0;
	int subTask = 0;
	ArrayList<ImageSettings> iSList;
	int settingCounter = 0;				//counts setting in process within the recent task

	Task recentTask;
	ImageSettings iS;
	String name [] = {"",""};
	String dir [] = {"",""};
	ImagePlus allImps [] = new ImagePlus [2]; 
	
	//--------------processing variables-------------	
	String filePrefix;
	ImagePlus imp;
	ImagePlus maskImp = null;
	GaussianBlur blurr = null;
	Circle circle;
	StackAsArray originalStack;
	
	double image_min, image_max;
	double [][] image, mask;
	int c, s, f, x, y;
	int stackindex= 0;
	 
public void run(String arg) {
	Task.reset();
	startDate = new Date();
	PLUGINBUIDDATE = getdate.getClassBuildTime.getDateString();	
	if(!startGenericDialog())return;	
	try{
		if(!startFileSelector())return;
	} catch(Exception e){return;}
		startTargetFileSelector();
	try{
		getProcessSettings();
	} catch (Exception e) {
		new WaitForUserDialog("Plug-in canceled by user").show();
		return;
	}
	for(int i = 0; i < tasks; i++) {
	}
	startProgressDialog();
	for(task = 0; task < tasks; task++) {
		recentTask = taskList.get(task);
		for(settingCounter = 0; settingCounter < recentTask.list.size(); settingCounter++, subTasks++){
			iS = recentTask.list.get(settingCounter);
		running: while(continueProcessing){
			progressDialog.updateBarText("in progress...");
			//Check for problems
					if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".txt")){
						progressDialog.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
						progressDialog.moveTask(task);	
						break running;
					}
					if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".zip")){	
						progressDialog.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
						progressDialog.moveTask(task);	
						break running;
					}		
					
			//open Image		
			try{
				openImage();
			} catch (FileIsNoImageException e) {
				break running;
			} catch (UnknownTaskVariantException e) {
				break running;
			}
			setPrefix();
		
		   	/******************************************************************
			*** 						PROCESSING							***	
			*******************************************************************/
			start = System.currentTimeMillis();	
			if(iS.sigma != 0) {blurr = new GaussianBlur(iS.sigma);}			
			try {
				originalStack = new StackAsArray(imp, iS.usedChannels, iS.maskCompressionFactor, channels);			
			}
			catch (ChoosenNumberOfChannelsDoesNotMatchImpException e){
				progressDialog.notifyMessage("Error on task " + tasks + ": " + e.getMessage() + " - task skipped!", 1);
				break running;
			}
			if(saveMask) maskImp = IJ.createHyperStack("mask", originalStack.cWidth, originalStack.cHeight, 
					1, originalStack.slices, originalStack.frames, originalStack.bitdepth);

			mask = new double[originalStack.cWidth][originalStack.cHeight];
			image = new double[originalStack.width][originalStack.height];
			
			for(f = 0; f < originalStack.frames; f++) {
				for(s = 0; s < originalStack.slices; s++) {
					originalStack.setIndices(iS.maskChannel, s, f);
					stackindex = imp.getStackIndex(iS.maskChannel+1, s+1, f+1)-1;				
					if(iS.useCircle){ 						//first Circle moves through the whole image
						circle = new Circle(iS.radius, new Point(0,0),iS.consideredPixels, true);
						mask [0][0] = circle.initializePixelList(originalStack.getCompressedSliceArray());
						y = 0;
						while(y < originalStack.cHeight) {				
							for(x = 1; x < originalStack.cWidth; x++) {
								mask[x][y] = circle.moveRight(originalStack.getCompressedSliceArray());
							}					
							y++;
							if(y >= originalStack.cHeight) {break;}
							x = originalStack.cWidth-1;
							mask[x][y] = circle.moveDownward(originalStack.getCompressedSliceArray());				
							for(x--; x >= 0; x--) {
								mask[x][y] = circle.moveLeft(originalStack.getCompressedSliceArray());
							}
							y++;
							if(y >= originalStack.cHeight) {break;}
							x = 0; 						
							mask[x][y] = circle.moveDownward(originalStack.getCompressedSliceArray());
							progressDialog.updateBarText("Calculating mask in slice " + (f*originalStack.slices + s+1) + "/" + (originalStack.slices*originalStack.frames)+ ", line " + y +"/" + originalStack.cHeight + " in 1st Circle");
						}
					}
					if(iS.useCircle) { 			//second Circle in opposite direction in every line
						x = originalStack.cWidth-1; y=0;
						circle = new Circle(iS.radius, new Point(x, y), iS.consideredPixels, true);
						mask [x][y] = circle.initializePixelList(originalStack.getCompressedSliceArray());
						double pixelvalue;
						while(y < originalStack.cHeight) {				
							for(x--; x >= 0; x--) {
								pixelvalue = circle.moveLeft(originalStack.getCompressedSliceArray());
								if(pixelvalue > mask [x][y]) {
									mask[x][y] = pixelvalue;
								}
							}					
							y++;	x = 0;
							if(y >= originalStack.cHeight) {break;}
							mask[x][y] = circle.moveDownward(originalStack.getCompressedSliceArray());
							for(x = 1; x < originalStack.cWidth; x++) {
								pixelvalue = circle.moveRight(originalStack.getCompressedSliceArray());
								if(pixelvalue > mask [x][y]) {
									mask[x][y] = pixelvalue;
								}
							}
							y++;
							if(y >= originalStack.cHeight) {break;}
							x = originalStack.cWidth-1;
							mask[x][y] = circle.moveDownward(originalStack.getCompressedSliceArray());
							progressDialog.updateBarText("Calculating mask in slice " + (f*originalStack.slices + s+1) + "/" + (originalStack.slices*originalStack.frames)+ ", line " + y +"/" + originalStack.cHeight + " in 2nd Circle");
							
						}
					}else {
						for(y = 0; y < originalStack.cHeight; y++) {
							x = 0;
							circle = new Circle(iS.radius, new Point(x,y), iS.consideredPixels, false);
							for(x = 0; x < originalStack.cWidth; x++) {
								mask[x][y] = circle.initializePixelList(originalStack.getCompressedSliceArray());
								circle.moveRight();
								progressDialog.updateBarText("Calculating mask in slice " + (f*originalStack.slices + s+1) + "/" + (originalStack.slices*originalStack.frames)+ ", line " + y +"/" + originalStack.cHeight);
							}
						}
					}
					if(iS.sigma != 0) {
						mask = blurr.addBlurr(mask);
						progressDialog.updateBarText("adding blurr in slice " + (f*originalStack.slices + s+1) + "/" + (originalStack.slices*originalStack.frames));
					}
					for(c = 0; c < originalStack.channels; c++) {		
						if(!iS.usedChannels[c+1]) {
							copySlice(imp.getStackIndex(c+1, s+1, f+1)-1);
						}else {
							progressDialog.updateBarText("calculating slice "+ (f * originalStack.slices + s+1) + "/" + (originalStack.slices*originalStack.frames) + " channel " + c);
							originalStack.setIndices(c, s, f);
							stackindex = imp.getStackIndex(c+1, s+1, f+1)-1;				
							image_max = 0; image_min = 0;
							for(x = 0; x < originalStack.width; x++) {
								for(y = 0; y < originalStack.height; y++) {				//calculate Intensity method divides the coordinate's original value through it's mask value and saves it to the image array
									calculateIntensity(x,y);				//while looping through it is checking for min an max values in order to reset the masked image to it's original range
								}
							}										//whole picture is looped through and filled with uncorrected values, min and max are obtained
							originalStack.setNewRange(image_min, image_max);
							writeToImp();
						}
					}				
				}
			}
			stop = System.currentTimeMillis();
			runtime = stop - start;
			if(saveMask)IJ.saveAsTiff(maskImp, filePrefix +"_mask.png");
			IJ.saveAsTiff(imp, filePrefix + "_corrected.png");			
			createMetadataFile();		
			processingDone = true;
			break running;
		}	
		progressDialog.updateBarText("finished!");
		progressDialog.setBar(1.0);
		progressDialog.moveTask(settingCounter);
		}
	}
}

	//--------------Calculations------------------------
private void writeToImp() {
	for(x = 0; x < originalStack.width; x++) {
		for(y = 0; y < originalStack.height; y++) {
			imp.getStack().setVoxel(x, y, stackindex, originalStack.setToOriginalRange(image[x][y]));
		}
	}
	if(saveMask) {
		for(x=0; x< originalStack.cWidth; x++) {
			for(y=0; y<originalStack.cHeight; y++) {
				maskImp.getStack().setVoxel(x, y, maskImp.getStackIndex(1, s+1, f+1)-1, mask [x][y]);
			}
		}
	}
}

private void calculateIntensity(int x, int y) {
	image [x][y] = originalStack.getPixelValue(x, y)/ mask [(int)x/iS.maskCompressionFactor][(int)y/iS.maskCompressionFactor];
	if(image [x][y] > image_max) {
		image_max = image [x][y];
	}
	if(image [x][y] < image_min) {
		image_min = image [x][y];
	}
}

private void copySlice(int stackindex) {
	int x,y;	
	for(x = 0; x < originalStack.width; x++) {
		for(y = 0; y < originalStack.height; y++) {
			imp.getStack().setVoxel(x, y, stackindex, originalStack.getSliceArray()[x][y]);
		}
	}	
}

	//--------------Process Methods-----------------------
private boolean startFileSelector() {

	//Improved file selector
	try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	catch(Exception e){
		return false;
	}
	if(selectedTaskVariant.equals(taskVariant[1])){
		OpenFilesDialog od = new OpenFilesDialog ();
		od.setLocation(0,0);
		od.setVisible(true);
		
		od.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
					try {
						throw new GdCancelledException();
					} catch (GdCancelledException e) {}
	        }
	    });
	
		//Waiting for od to be done
		while(od.done==false){
			try{
				Thread.currentThread().sleep(50);
		    }catch(Exception e){
		    }
		}			
		tasks = od.filesToOpen.size();
		name = new String [tasks];
		dir = new String [tasks];
		for(int task = 0; task < tasks; task++){
			name[task] = od.filesToOpen.get(task).getName();
			dir[task] = od.filesToOpen.get(task).getParent() + System.getProperty("file.separator");
		}		
	}
	else if(selectedTaskVariant.equals(taskVariant[0])){
		if(WindowManager.getIDList()==null){
			new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
			return false;
		}
		FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
		name [0] = info.fileName;	//get name
		dir [0] = info.directory;	//get directory
		tasks = 1;
	}
	else if(selectedTaskVariant.equals(taskVariant[2])){	// all open images
		if(WindowManager.getIDList()==null){
			new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
			return false;
		}
		int IDlist [] = WindowManager.getIDList();
		tasks = IDlist.length;	
		if(tasks == 1){
			selectedTaskVariant=taskVariant[0];
			FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
			name [0] = info.fileName;	//get name
			dir [0] = info.directory;	//get directory
		}else{
			name = new String [tasks];
			dir = new String [tasks];
			allImps = new ImagePlus [tasks];
			for(int i = 0; i < tasks; i++){
				allImps[i] = WindowManager.getImage(IDlist[i]); 
				FileInfo info = allImps[i].getOriginalFileInfo();
				name [i] = info.fileName;	//get name
				dir [i] = info.directory;	//get directory
			}		
		}					
	}
	return true;
}

private boolean startGenericDialog() {
	
	GenericDialog gd = new GenericDialog(PLUGINNAME + " - General Settings");	
	gd.setInsets(0,0,0);	gd.addMessage(PLUGINNAME + " - Version " + PLUGINVERSION, SubHeadingFont);	
	gd.setInsets(5,0,0);	gd.addChoice("Type of file selection ", taskVariant, selectedTaskVariant);
	gd.setInsets(10,0,0);	gd.addChoice("Input filetype", bioFormats, bioFormat);
	gd.setInsets(10,0,0);	gd.addMessage("Output settings", SubHeadingFont);
	gd.setInsets(0,0,0);	gd.addNumericField("Insert number of channels ", channels, 0);
	gd.setInsets(0,0,0);	gd.addCheckbox("Save mask as image ", saveMask);	
	gd.setInsets(0,0,0);	gd.addCheckbox("Save date in output file names", saveDate);
	gd.setInsets(0,0,0);	gd.addCheckbox("Indicate parameters in output file names", saveParam);
	gd.setInsets(0,0,0);	gd.addCheckbox("Save results in new folder", resultsToNewFolder);
	
	gd.showDialog();

	//show Dialog-----------------------------------------------------------------

	//read and process variables--------------------------------------------------	
	selectedTaskVariant = gd.getNextChoice();
	bioFormat = gd.getNextChoice();
	channels = (int) gd.getNextNumber();
	saveMask = gd.getNextBoolean();	
	saveDate = gd.getNextBoolean();	
	saveParam = gd.getNextBoolean();
	resultsToNewFolder = gd.getNextBoolean();	
	
	if (gd.wasCanceled()) return false;
	
	return true;
}

private void startProgressDialog() {
	progressDialog = new ProgressDialog(wrapNamesInArray(taskList), subTasks);
	progressDialog.setLocation(0,0);
	progressDialog.setVisible(true);
	progressDialog.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(WindowEvent winEvt) {
        	if(processingDone==false){
        		IJ.error("Script stopped...");
        	}
        	continueProcessing = false;	        	
        	return;
        }
	});
}

private void openImage () throws UnknownTaskVariantException, FileIsNoImageException{
   	try{
   		if(selectedTaskVariant.equals(taskVariant[1])){
   			if(bioFormat.equals(bioFormats[0])){
   				//TIFF file
   				imp = IJ.openImage(""+dir[task]+name[task]+"");		
   			}else if(bioFormat.equals(bioFormats[1])){
   				//bio format reader
   				IJ.run("Bio-Formats", "open=[" +dir[task] + name[task]
   						+ "] autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
   				imp = WindowManager.getCurrentImage();		   				
   			}else{
   				progressDialog.notifyMessage("Task " + (task+1) + "/" + tasks + ": Image could not be opened!", ProgressDialog.ERROR);
				progressDialog.moveTask(task);	
				throw new UnknownTaskVariantException();
   			}
   			imp.hide();
			imp.deleteRoi();
   		}else if(selectedTaskVariant.equals(taskVariant[0])){
   			imp = WindowManager.getCurrentImage();
   			imp.deleteRoi();
   		}else{
   			imp = allImps[task];
   			imp.deleteRoi();
   		}
   	}catch (Exception e) {
   		progressDialog.notifyMessage("Task " + (task+1) + "/" + tasks + ": file is no image - could not be processed!", ProgressDialog.ERROR);
		progressDialog.moveTask(task);
		throw new FileIsNoImageException(task, tasks);
	}
}

private void startTargetFileSelector() {
	if(resultsToNewFolder) {
		resultFolder = SimpleDialog.openFile("choose Directory for results", System.getProperty("user.dir"))
				+ System.getProperty("file.separator");
	}
}

private void setPrefix () {
	//Define Output File Names and Folder
	
	currentDate = new Date();			
	if(name[task].contains(".")){
		filePrefix = name[task].substring(0,name[task].lastIndexOf("."));
	}else{
		filePrefix = name[task];
	}
	
	if(saveDate){
		filePrefix += "_" + NameDateFormatter.format(currentDate);
	}
	
	filePrefix += iS.printShortPrefix(saveParam);
	
	if(resultsToNewFolder) {
		taskFolder = resultFolder + name[task] + System.getProperty("file.separator");
		File folder = new File(taskFolder);
		folder.mkdirs();
		filePrefix = taskFolder + filePrefix;
	}
	else {
		filePrefix = dir[task] + filePrefix;
	}
}

private void getProcessSettings() throws GdCancelledException{
	Task t = null;
	taskList = new ArrayList<Task>(tasks);
	for(task=0; task < tasks; task++) {
		try {
			t = new Task(name[task], dir[task], channels);
		}
		catch(Exception e){
			throw new GdCancelledException();
		}
		subTasks += t.list.size();
		taskList.add(t); 
	}
}

private String[] wrapNamesInArray(ArrayList<Task> taskList) {
	int counter=0;
	String [] names = new String [subTasks];
	for(int j = 0; j < tasks; j++) {
		for(int i = 0; i < taskList.get(j).list.size(); i++) {
			names[counter] = taskList.get(j).name;
			counter ++;
		}
	}	
	return names;
}

private void createMetadataFile() {
	//start metadata file
	tp1 = new TextPanel("Results");
	tp1.append("Saving date:	" + FullDateFormatter.format(currentDate)
				+ "	Starting date:	" + FullDateFormatter.format(startDate));
	tp1.append("Image name:	" + name[task] + "				Image " + (task+1) + "/" + tasks);
	tp1.append("process on Image:	" + (settingCounter+1) + "/"+ (recentTask.list.size()));
	tp1.append("");
	tp1.append("runtime in ms: " + runtime);
	tp1.append("");
	tp1.append(iS.printSettings());
	addFooter(tp1, currentDate);				
	tp1.saveAs(filePrefix + ".txt");
}

private void addFooter(TextPanel tp, Date currentDate){
	tp.append("");
	tp.append("The tool calculates the mask by checking for the brightest pixels in the distance inside the chosen radius (see above) and takes the average of the brightest Pixels.");
	tp.append("");
	tp.append("Datafile was generated on " + FullDateFormatter2.format(currentDate) + " by '"+PLUGINNAME+"', an ImageJ plug-in by Sebastian Raßmann (rassmann@uni-bonn.de).");
	tp.append("The plug-in '"+PLUGINNAME+"' is distributed in the hope that it will be useful,"
			+ " but WITHOUT ANY WARRANTY; without even the implied warranty of"
			+ " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
	tp.append("Plug-in version:	V"+PLUGINVERSION + " build on " + PLUGINBUIDDATE);
	
}

}//end main class