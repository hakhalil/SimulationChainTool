package edu.arsl.intgra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileEntry;
import org.w3c.dom.Document;

/**
 * Class to handle all file system operations
 * 
 * @author Hoda
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
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File[] innerFiles = files[i].listFiles();
				for (int j = 0; j < innerFiles.length; j++) {
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
				 byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
	                zip.write(bytes, 0, bytes.length);
				zip.closeEntry();
			}
		}
	}

	public static String readDimFromMake(String outputName) throws Exception {
		String dim = "(1,1)";
		String fileName = PropertiesFile.getInstance().getProperty("input_file_name");
		File f = new File(outputName + "/" + fileName + "/" + fileName + ".ma");
		FileReader reader = new FileReader(f);
		List<String> lines = IOUtils.readLines(reader);
		for (String line : lines) {
			if (line.startsWith("dim")) {
				dim = line.substring(line.indexOf(":") + 1).trim();
				break;
			}
		}
		reader.close();
		return dim;
	}

	public static void editXML(String dim) throws Exception {
		String xmlFilePath = PropertiesFile.getSystemRoot();

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		String fileName = PropertiesFile.getInstance().getProperty("input_file_name");
		Document xmlDocument = builder.parse(new File(xmlFilePath + fileName+".xml"));

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
		String s = PropertiesFile.getInstance().getFullyQualifiedGenertorDir() + "/"+fileName+".xml";
		StreamResult streamResult = new StreamResult(new File(s));
		transformer.transform(domSource, streamResult);

	}
	 private static String getEntryName(File source, File file) throws IOException {
	        int index = source.getAbsolutePath().length() + 1;
	        String path = file.getCanonicalPath();

	        return path.substring(index);
	    }
	public static void compress(String dirPath, String zipFileName) throws Exception {
		File destination = new File(zipFileName);
		File source = new File(dirPath);
		OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);

        Collection<File> fileList = FileUtils.listFiles(source, null, true);

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
}

