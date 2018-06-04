package FilesDialog;

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

import java.io.File;

import javax.swing.JFileChooser;

public class SimpleDialog {
	
	public static String openFile(String message, String defaultpath) {
		
		if(defaultpath == "") {
			defaultpath = System.getProperty("user.dir");
		}
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setCurrentDirectory(new File(defaultpath));
		if (fc.showDialog(fc, message) == JFileChooser.APPROVE_OPTION) {
//		   System.out.println(fc.getSelectedFile().getAbsoluteFile());
		}
		String selectedpath  = fc.getSelectedFile().getPath();
		return selectedpath;
	}
	
	public static String openFile(String message){
		String userDir = System.getProperty("user.home");
		String defaultpath = userDir + "/Desktop";
		String selectedpath = openFile(message, defaultpath);
		return selectedpath;
	}
}
