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

import java.util.ArrayList;

import exceptions.GdCancelledException;
import ij.IJ;

public class Task {
	static ArrayList<ImageSettings> sample;
	static boolean sampleIsSet = false;
	
	String name;
	String dir;
	ArrayList<ImageSettings> list;
	boolean isSample = false;
	int settingsInTask = 0;
	
	public Task(String name, String dir, int channels) throws GdCancelledException{

		this.name = name;
		this.dir = dir;
		if(!sampleIsSet) {
			this.list = new ArrayList<ImageSettings>(1);
			ImageSettings  iS;
			do{			
				iS = new ImageSettings(name, (list.size()), channels);
				if(iS.wasCanceled) {
					throw new GdCancelledException();
				}
				if(!iS.deleteSetting) {
					list.add(iS);
					if(iS.keepSettings) {
						sampleIsSet = true;
						sample = this.list;
						break;
					}
				}else {
					break;
				}
			} while(iS.createAnotherTask);					
		}
		else {
			this.list = sample;
		}
	}
	
	public static void reset() {
		sampleIsSet =false;
		sample = null;
	}
}
