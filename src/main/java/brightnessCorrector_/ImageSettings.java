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
import ij.gui.GenericDialog;

public class ImageSettings {
	
	static int classCounter = 0; 
	
	int id = classCounter;
	boolean wasCanceled = false;
	boolean deleteSetting = false;
	boolean keepSettings = false;	
	boolean createAnotherTask = false;
	int numberInTask = 0;
	
	String[] channelNumberAsString;
	String selectedChannel = "Channel 1";	
	int maskChannel = 0;									//starts counting with 1!!
	static final String[] processVariant = {"double circle (fastest)","single pixel calculation (best quality)"};
	String selectedProcessVariant = processVariant[0];
	boolean useCircle;
	
	int maskCompressionFactor = 1;
	boolean[] usedChannels; 				//c[0] is not used according to IJ nomenclature!!!
	double consideredPixelsPercent = 0.001;
	int consideredPixels;
	int radiusUser = 100;
	double sigmaUser = 0;
	int radius;
	double sigma;
	
	public ImageSettings(String name, int settingsInTask, int channels) {
		
		classCounter++;
		usedChannels = new boolean [channels+1];	
		channelNumberAsString = new String [channels];
		for(int i = 0; i < channels; i++) {
			channelNumberAsString[i] = "Channel " + (i+1);
		}
			
		GenericDialog gd = new GenericDialog("Set parameters for image: " + name);
		gd.setInsets(0,0,0);	gd.addMessage("Choose option for a process type on image " + name, new Font("Sansserif", Font.BOLD, 16));	
		gd.setInsets(0,0,0);	gd.addMessage("Image was already assigned to be processed with " + settingsInTask + " different settings", new Font("Sansserif", Font.BOLD, 11));	
		gd.setInsets(5,0,0);	gd.addChoice("Channel used for mask calculation (e.g. DAPI) ", channelNumberAsString, selectedChannel);
		for(int i = 1; i <= channels; i++) {
			gd.addCheckbox("Correct Channel " + i, true);
		}
		gd.addNumericField("Scale factor (reduces mask resolution to increase speed) ", maskCompressionFactor, 0);
		gd.addNumericField("Radius to search for brightest pixel(s) ", radiusUser, 0);
		gd.addNumericField("% of considered pixels", consideredPixelsPercent, 3);
		gd.addChoice("Processing variant", processVariant, selectedProcessVariant);
		gd.addNumericField("σ (Gaussian blur radius)  ", sigmaUser, 1);

		gd.addCheckbox("Add another processing setting for this image", createAnotherTask);
		gd.addCheckbox("Keep setting on this task for all other images", keepSettings);
		gd.addCheckbox("Do not use this setting and go to setting selection for next image", deleteSetting);

		gd.showDialog();
		
		if (gd.wasCanceled()) {
			wasCanceled = true;
			return;
		}
		selectedChannel = gd.getNextChoice();
		for(int i = 1; i <= channels; i++) {
			usedChannels[i] = gd.getNextBoolean();
		}
		maskCompressionFactor = (int) gd.getNextNumber();
		radiusUser = (int) gd.getNextNumber();
		consideredPixelsPercent = gd.getNextNumber();
		selectedProcessVariant = gd.getNextChoice();
		sigmaUser = gd.getNextNumber();
		createAnotherTask = gd.getNextBoolean();
		keepSettings = gd.getNextBoolean();
		deleteSetting = gd.getNextBoolean();

		for(int i = 0; i < channels; i ++) {
			if(selectedChannel.equals(channelNumberAsString[i])) {
				maskChannel = i;
				break;
			}
		}			
		radius = (int) radiusUser/maskCompressionFactor;				
		if(consideredPixelsPercent == 0.001) {
			consideredPixels = 1;
		}
		else {
			consideredPixels = (int) (Math.PI*radius*radius*consideredPixelsPercent/100) + 1;
		}
		if(selectedProcessVariant.equals(processVariant[0])) {
			useCircle = true;
			selectedProcessVariant = "dc";
		}
		else {
			useCircle = false;
			selectedProcessVariant = "sp";
		}
		sigma = sigmaUser/maskCompressionFactor;
	}
	
	public String printSettings () {
		StringBuffer sb = new StringBuffer("\n");
		sb.append("Processing settings:\n");
		sb.append("Scale Factor: " + maskCompressionFactor + "\n" );
		String e = "";
		if(selectedProcessVariant.equals("dc")) {
			e = processVariant[0] + " --> uses accelerated algorithm, thus results may differ from a perfect calculation with the native algorithm";
		}
		else {
			e = processVariant[1];
		}
		sb.append("Processing type:	" + e + "\n");
		sb.append("\n");
		for(int i  = 1; i < usedChannels.length; i++) {
			if(usedChannels[i]) {
				e = "	corrected";
			}else {
				e = "	unchanged";
			}
			if(i == maskChannel-1) {
				e = e + " - used for mask";
			}
			sb.append("Channel " + i + " 	" + e + "\n");
		}
		sb.append("used channel: " + maskChannel + "\nUsed radius: 	" + radius + "		(user's input: 	" + radiusUser + ")" +
				"\nNumber of considered Pixels " + consideredPixels +
				"	(user's input:	" + consideredPixelsPercent + " %)	\nσ (radius for gaussian blurr): " 
				+ sigma +" 		(user's input: 	" + sigmaUser + ")"+ "		(0 means no blurr added)");	
		return sb.toString();
	}
	
	public String printShortPrefix (boolean saveParam) {
		String s;
		if(saveParam) {
			s = "_" + selectedProcessVariant + "_cf=" + maskCompressionFactor + "_" + "r=" + radiusUser + "_%used=" + consideredPixelsPercent;
			if(sigma !=0) {
				s += "_sigma=" + sigmaUser;
			}
		}
		else {
			s = ""+numberInTask;
		}
		return s;
	}
}
