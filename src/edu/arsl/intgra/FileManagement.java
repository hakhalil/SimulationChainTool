package edu.arsl.intgra;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileEntry;

/**
 * Class to handle all file system operations
 * 
 * @author Amr
 *
 */
public class FileManagement {

	/**
	 * Checks that a file is of a given type. The method only relies on the
	 * extension
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean checkFileType(String fileName) {
		// properties file has a comma delimited list of extensions
		String extension_value = PropertiesFile.getInstance().getProperty("accepted_ext");

		if (extension_value != null) {
			String[] extensions = extension_value.split(",");
			for (int i = 0; extensions != null && i < extensions.length; i++) {
				if (fileName.endsWith(extensions[i].trim()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a folder exists within the root folder of the application. If the
	 * file does not exist or exists but not a directory, it will be created.
	 */
	static boolean checkFolder(String folderName) {

		File defaultDir = fullyQualifyFolder(folderName);
		FileEntry fileEntry = new FileEntry(defaultDir);

		// The file has to exist and is a directory (Not just a child file)
		return (!fileEntry.isExists() || fileEntry.isDirectory());
		
	}

	/**
	 * Method for convenience to write an input Stream to the file name
	 * 
	 * @param filedName   - does not include the path of the file
	 * @param fileContent
	 * @return
	 */
	public static boolean writeFile(String filedName, InputStream fileContent) {
		boolean success = false;
		try {

			String default_folder = PropertiesFile.getInstance().getProperty("default_folder");
			initializeFolder(default_folder);
			File localFile = new File(PropertiesFile.getInstance().getFullyQualifiedGenertorDir() + "/" + filedName);

			/*
			 * OutputStream outputStream = new FileOutputStream(localFile); int b=0;
			 * while((b = fileContent.read())!=-1) { if(b < 0) b+=256;
			 * outputStream.write(b); } outputStream.flush(); outputStream.close();
			 */

			writer(fileContent, localFile);

			success = true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	public static String getCDInputFileName() {
		String path = PropertiesFile.getInstance().getFullyQualifiedGenertorDir();
		File f = new File(path);
		String[] listOfFiles = f.list();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].trim().startsWith(PropertiesFile.getInstance().getProperty("input_file_name")))
				return listOfFiles[i];
		}

		return null;
	}

	private static void writer(InputStream inputStream, File outputFile) throws IOException {
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		byte[] buffer = new byte[16384];

		while (inputStream.read(buffer) != -1) {
			outputStream.write(buffer);
		}
		outputStream.flush();
		outputStream.close();

	}
	
	private static void initializeFolder(String folderName) {
		// The file has to exist and is a directory (Not just a child file)
		File defaultDir = fullyQualifyFolder(folderName);
		if (!checkFolder(folderName)) {
			defaultDir.mkdir();
		}
		File[] files = defaultDir.listFiles();
		for(int i=0; i<files.length; i++) {
			if(files[i].isDirectory()) {
				File[] innerFiles = files[i].listFiles();
				for(int j=0; j<innerFiles.length; j++) {
					innerFiles[j].delete();
				}
			}
			files[i].delete();
		}
	}
	
	public static File fullyQualifyFolder(String folderName) {
		String systemRoot = PropertiesFile.getSystemRoot();
		File defaultDir = new File(systemRoot + folderName);
		
		return defaultDir;
	}
	
	public static void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException {
	    File[] files = folder.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            addFolderToZip(file, zip, baseName);
	        } else {
	            String name = file.getAbsolutePath().substring(baseName.length());
	            ZipEntry zipEntry = new ZipEntry(name);
	            zip.putNextEntry(zipEntry);
	            IOUtils.copy(new FileInputStream(file), zip);
	            zip.closeEntry();
	        }
	    }
	}
}
