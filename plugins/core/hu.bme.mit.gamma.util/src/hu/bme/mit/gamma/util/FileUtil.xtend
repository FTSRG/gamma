/********************************************************************************
 * Copyright (c) 2018-2020 Contributors to the Gamma project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-1.0
 ********************************************************************************/
package hu.bme.mit.gamma.util

import java.io.File
import java.io.FileWriter
import java.util.AbstractMap
import java.util.ArrayList
import java.util.Collections
import java.util.Map
import java.util.Scanner

class FileUtil {
	// Singleton
	public static final FileUtil INSTANCE = new FileUtil
	protected new() {}
	//
	def saveString(String uri, String string) {
		new File(uri).saveString(string)
	}
	
	def saveString(File file, String string) {
		file.parentFile.mkdirs
		try (val fileWriter = new FileWriter(file)) {
			fileWriter.write(string)
		}
	}
	
	def loadString(File file) {
		val builder = new StringBuilder
		try (val scanner = new Scanner(file)) {
			while (scanner.hasNext) {
				builder.append(scanner.nextLine + System.lineSeparator)
			}
		}
		return builder.toString
	}
	
	def toPath(String packageName) {
		return packageName.replaceAll("\\.", "\\"+ File.separator)
	}
	
	def getFile(File sourceFolder, String packageName, String className) {
		return getFile(sourceFolder.toString, packageName, className)
	} 
	
	def getFile(String sourceFolder, String packageName, String className) {
		return new File(sourceFolder + File.separator + packageName.toPath + File.separator + className + ".java")
	}
	
	def getExtensionlessName(File file) {
		return file.getName.extensionlessName
	}
	
	def getExtensionlessName(String fileName) {
		val lastIndex = fileName.lastIndexOf(".")
		if (lastIndex <= 0) { // <= 0 so hidden files are handled
			return fileName
		}
		return fileName.substring(0, lastIndex)
	}
	
	def getExtension(String fileName) {
		val lastIndex = fileName.lastIndexOf(".")
		if (lastIndex <= 0) { // <= 0 so hidden files are handled
			return ""
		}
		return fileName.substring(lastIndex + 1)
	}
	
	def isHiddenFile(String fileName) {
		return fileName.startsWith(".")
	}
	
	def toHiddenFileName(String fileName) {
		return "." + fileName
	}
	
	def changeExtension(String fileName, String newExtension) {
		return fileName.extensionlessName + "." + newExtension
	}
	
	def File exploreRelativeFile(File anchor, String relativePath) {
		val path = anchor.toString + File.separator + relativePath
		val file = new File(path)
		if (file.exists) {
			return file
		}
		val parent = anchor.parentFile
		return parent.exploreRelativeFile(relativePath)
	}
	
	def isValidRelativeFile(File anchor, String relativePath) {
		try {
			anchor.exploreRelativeFile(relativePath)
			return true
		} catch (NullPointerException e) {
			return false
		}
	}
	
    /**
     * Returns the next valid name for the file that is suffixed by indices.
     */
    def Map.Entry<String, Integer> getFileName(File folder, String fileName, String fileExtension) {
    	val usedIds = new ArrayList<Integer>();
    	folder.mkdirs();
    	// Searching the trace folder for highest id
    	for (File file: folder.listFiles()) {
    		if (file.getName().matches(fileName + "[0-9]+\\..*")) {
    			val id = file.getName().substring(fileName.length(), file.getName().length() - ("." + fileExtension).length());
    			usedIds.add(Integer.parseInt(id));
    		}
    	}
    	if (usedIds.isEmpty()) {
    		return new AbstractMap.SimpleEntry<String, Integer>(fileName + "0." + fileExtension, 0);
    	}
    	Collections.sort(usedIds);
    	val biggestId = usedIds.get(usedIds.size() - 1);
    	return new AbstractMap.SimpleEntry<String, Integer>(
    			fileName + (biggestId + 1) + "." + fileExtension, (biggestId + 1));
    }
	
}