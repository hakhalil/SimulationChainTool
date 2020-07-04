package edu.arsl.intgra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Class to handle all file system operations
 * 
 * @author Hoda
 *
 */
public class FileManagement {

	final static Logger log = LogManager.getLogger(FileManagement.class);
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

		File defaultDir = new File(folderName);

		// The file has to exist and is a directory (Not just a child file)
		return (defaultDir.exists() && defaultDir.isDirectory());

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
			File localFile = new File(getOutputFolderName() + "/" + filedName);

			//writing the inputstream to the file
			writer(fileContent, localFile);
			success = true;

		} catch (IOException e) {
			log.fatal("Could not write file", e);
			success = false;
		}
		return success;
	}

	private static void writer(InputStream inputStream, File outputFile) throws IOException {
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		IOUtils.copy(inputStream, outputStream);
		outputStream.flush();
		outputStream.close();

	}

	private static void deleteFolderRecursively(File file) {
		if(!file.isDirectory()) {
			boolean isDeleted = file.delete();
			if(!isDeleted)
				log.info("Could not delete file "+ file.getName());
		} else {
			File[] files = file.listFiles();
			for (int i = 0; files != null && i < files.length; i++) {
				deleteFolderRecursively(files[i]);
				files[i].delete();
			}
		}
	}
	
	/**
	 * Checks if the output folder for the application exists or not. If not, creat it.
	 * If exits, delete all files and subfolder to have a clean start
	 */
	public static void initializeOutputFolder() {
		String default_folder = PropertiesFile.getInstance().getProperty("default_folder");
		
		// The file has to exist and is a directory (Not just a child file)
		String fullQualifiedFolderName = getFullyQualifiedFileName(default_folder);
		File defaultDir = new File(fullQualifiedFolderName);
		if (!checkFolder(fullQualifiedFolderName)) {
			boolean dirCreated = defaultDir.mkdir();
			if(dirCreated == false) {
				log.error("Could not create generation folder");
			}
		} else
			deleteFolderRecursively(defaultDir);
	}
	
	/**
	 * Downloads a file from a given URI
	 * @param uri
	 * @param fileName
	 */
	public static void downloadFile(String uri, String fileName) {

		try (BufferedInputStream in = new BufferedInputStream(new URL(uri).openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
			IOUtils.copy(in, fileOutputStream);
			in.close();
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			log.fatal("Failed while downloaidng the file", e);
		}
	}

	/**
	 * *************************************************************
	 * *************************************************************
	 * ********  CDGeneration output file editing methods   ********
	 * *************************************************************
	 * *************************************************************
	 * 
	 */

	/**
	 * Read the dimensions of the model as well as change the status of the windows if required
	 * @param folderName
	 * @return
	 * @throws Exception
	 */
	public static String manipluateMakeFile(boolean closeWindow) throws Exception {
		String dim = "(1,1)";
		String makeFileName = PropertiesFile.getInstance().getProperty("input_qualifier")+ Constants.MAKE_FILE_EXTENSION;
		
		//create a folder with the input qualifier under the output path. This is needed because RISE expects the zip to have a folder
		//and for this folder we are using the input qualifier
		File f = new File(getRISEInputFolder() + "/" + makeFileName );
				
		List<String> lines = IOUtils.readLines(new FileInputStream(f), "CP1252");
		
		FileOutputStream os = new FileOutputStream(f);
		for (String line : lines) {
			
			//if this line shows the dimensions of the model, update it based
			//on the room width entered by the user
			if (line.startsWith(Constants.DIMENSION_CLAUSE)) {
				dim = line.substring(line.indexOf(":") + 1).trim();
				//break;
			}
			
			//only write the line if it is not the windows rule or if it is the window rule but the
			//window is not closed
			if (line.endsWith(Constants.WINDOW_RULE) && closeWindow) {
				os.write("rule : { ~c := $conc; ~ty := $type; } { $conc := -10; } 1000 { $type = -500 }".getBytes());
				os.write("\r\n".getBytes());
			} else {
				os.write(line.getBytes());
				os.write("\r\n".getBytes());
			}
		}
		//reader.close();
		os.close();
		return dim;
	}

	/**
	 * For CDPP simulations, we need to add the model dimension to the XML provided to RISE
	 * In all cases, the template XML must be written to the generation folder
	 * @param dim
	 * @throws Exception
	 */
	public static void editXML(String dim) throws Exception {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		String fileName = PropertiesFile.getInstance().getProperty("input_qualifier")+ ".xml";
		Document xmlDocument = builder.parse(new File(getFullyQualifiedFileName(fileName)));

		/*
		 * String expression = "ConfigFramework/DCDpp/Servers/Server/Zone"; XPath xPath
		 * = XPathFactory.newInstance().newXPath(); NodeList nodeList = (NodeList)
		 * xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET); Node
		 * zone = nodeList.item(0);
		 * 
		 * String zoneRange = zone.getTextContent(); zoneRange = zoneRange.substring(0,
		 * zoneRange.lastIndexOf(".") + 1) + dim; zone.setTextContent(zoneRange);
		 */

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(xmlDocument);
		String s = getOutputFolderName() + "/" + fileName;
		StreamResult streamResult = new StreamResult(new File(s));
		transformer.transform(domSource, streamResult);

	}
	
	/**
	 * The generation tool outputs a palette which does not account to vents, doors or windows. 
	 * Since the palette is constant, we overwrite the generated output with a templated one
	 * @throws Exception
	 */
	public static void overwritePal() throws Exception {

		String fileName = PropertiesFile.getInstance().getProperty("input_qualifier")+ Constants.PALETTE_FILE_EXTENSION;
		File templateFile = new File(getFullyQualifiedFileName("cell") +"/" + fileName );

		File newPal = new File(getRISEInputFolder()+"/" + fileName );

		FileInputStream input = new FileInputStream(templateFile);
		writer(input, newPal);
	}

	/**
	 * Randomly add occupants in non-obstacle space in the model
	 * @param dimClause - size of the model to know the boundaries of where to put the occupants
	 * @param numberOfOccupants - number of occupants to insert
	 */
	public static void addOccupantsToValues(String dimClause, int numberOfOccupants) {
		String[] coords = dimClause.substring(1, dimClause.length()-1).split(",");
		String valuesFileName = getRISEInputFolder()+"/"+PropertiesFile.getInstance().getProperty("input_qualifier")+ Constants.VALUES_FILE_EXTENSION;
		File valuesFile = new File(valuesFileName);
		OutputStream os = null;
		try {
			FileInputStream is = new FileInputStream(valuesFile);
			
			List<String> lines = IOUtils.readLines(is, "CP1252");
			
			
			os = new FileOutputStream(valuesFile);
			
			//save all existing coordinates. Coordinates that do not exist can host an occupant
			//Hashmap provides a quick search
			HashMap<String, Integer> mapOfCoords = new HashMap<String, Integer>();
			
			//re-writing the file (line by line) and storing the coordinates on each line as we go
			for(int i=0; i<lines.size();i++) {
				int equalityIndex = lines.get(i).indexOf("=");
				String coordPart = null;
				if(equalityIndex != -1)
					coordPart =lines.get(i).substring(0,equalityIndex).trim();
				else 
					coordPart =lines.get(i);
				os.write(lines.get(i).getBytes());
				os.write("\r\n".getBytes());
				mapOfCoords.put(coordPart, 1);
			}
			
			//start randomizing coordiantes then check if they exist. If they don't add an occupant there
			int xCoord = Integer.parseInt(coords[0]);
			int yCoord = Integer.parseInt(coords[1]);
			
			Random xRandomizer = new Random();
			Random yRandomizer = new Random();
			
			for (int i=0; i<numberOfOccupants;i++) {
				
				String s = null;
				//will keep generate random coordinate until one is found that does not already exist. 
				do {
					s = "("+xRandomizer.nextInt(xCoord) + ", "+yRandomizer.nextInt(yCoord)+")";
				}while(mapOfCoords.containsKey(s));
				
				s+= "= 500 -200 -1 \r\n"; 
				os.write(s.getBytes());
			}
		
		} catch(Exception e) {
			log.fatal("Failed while editing the values file", e);
		}finally{
			if(os != null) try {os.close();}catch(Exception e) {}
		}
	}

	
	/**
	 * *************************************************************
	 * *************************************************************
	 * *****************  Zip helper methods   ******************
	 * *************************************************************
	 * *************************************************************
	 * 
	 */
	
	/**
	 * Extracts a filename (with its parent) based on the folder name being zipped
	 * @param source
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static String getEntryName(File source, File file) throws IOException {
		int index = source.getAbsolutePath().length() + 1;
		String path = file.getCanonicalPath();

		return path.substring(index);
	}

	/** 
	 * Adds the fodler <c>dirPath</c> and all its children to the zip <c>zipFileName</c>
	 * @param dirPath
	 * @param zipFileName
	 * @throws Exception
	 */
	public static void compressFolder(String dirPath, String zipFileName) throws Exception {
		File destination = new File(zipFileName);
		File source = new File(dirPath);
		OutputStream archiveStream = new FileOutputStream(destination);
		ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP,
				archiveStream);

		Collection<File> fileList = FileUtils.listFiles(source, null, true);

		//loop through all files and add them one by one to the archive
		for (File file : fileList) {
			String entryName = getEntryName(source, file);
			ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
			archive.putArchiveEntry(entry);

			BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

			IOUtils.copy(input, archive);
			input.close();
			archive.closeArchiveEntry();
		}

		archive.finish();
		archiveStream.close();
	}


	/**
	 * Extracting content of a zip archive to the current location
	 * This method is customized to extract a log file out of the result zip that comes back from RISE
	 * @param archiveFile
	 * @param destinationPath
	 */
	public static void extractZip(String archiveFile, String destinationPath) {
		File targetDir = new File(destinationPath);
		try (InputStream is = new FileInputStream(new File(archiveFile));
				ArchiveInputStream i = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is);) {
			ArchiveEntry entry = null;
			while ((entry = i.getNextEntry()) != null) {
				if (!i.canReadEntryData(entry)) {
					log.error("entry cannot be read!! " + entry.getName());
					continue;
				}
				
				
				if (entry.isDirectory()) {
					// ignore dirs as we don't want to extract any directories (non will exist in this case)
				} else {
					//no point in unzipping empty files
					if(entry.getSize() > 0) {
						String name = entry.getName();
						//in case the file was a child to a folder, we remove the parent name
						if(name.lastIndexOf("/")!=-1) {
							name = name.substring(name.lastIndexOf("/")+1);
						}
						
						File f = new File(targetDir + "/" + name);
						OutputStream o = Files.newOutputStream(f.toPath());
						IOUtils.copy(i, o);
						o.close();
					}
				}
			}
		} catch (Exception e) {
			log.fatal("Exception thrown while zipping", e);
		}
	}
	
	/**
	 * *************************************************************
	 * *************************************************************
	 * **************  Common folders helper methods   *************
	 * *************************************************************
	 * *************************************************************
	 * 
	 */
	/**
	 * Get folder name that has the output of this application
	 * @return
	 */
	public static String getOutputFolderName() {
		return PropertiesFile.getSystemRoot()+PropertiesFile.getInstance().getProperty("default_folder");
	}
	
	/** 
	 * Add system path to the filename in order to get a fully qualified name
	 * @param FileName
	 * @return
	 */
	public static String getFullyQualifiedFileName(String FileName) {
		String systemRoot = PropertiesFile.getSystemRoot();
		return systemRoot + FileName;
	}
	
	/**
	 * returns the folder where the output of the CDGenerator is written
	 * @return
	 */
	public static String getGeneratorOutputFolder() {
		return getOutputFolderName() + "/" + PropertiesFile.getInstance().getProperty("output_qualifier");
	}
	

	/** 
	 * returns the folder that would be zipped and sent to RISE as input
	 * @return
	 */
	public static String getRISEInputFolder() {
		return getGeneratorOutputFolder()  + "/" + PropertiesFile.getInstance().getProperty("input_qualifier");
	}	
}
