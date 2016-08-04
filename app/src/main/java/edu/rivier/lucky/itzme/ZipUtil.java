package edu.rivier.lucky.itzme;

import java.io.*;
import java.util.zip.*;

public class ZipUtil {

	static int BUFFER = 2048;
	public static void zip(String dataFile, String signedFile, String pubKeyFile, String outFile) throws IOException
	{
		FileOutputStream dest = new  FileOutputStream(outFile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

		addZipEntry(out, dataFile, "IMGFILE");
		addZipEntry(out, signedFile, "SIGNATURE");
		addZipEntry(out, pubKeyFile, "PUBKEY");

		out.close();
	}

	public static void addZipEntry(ZipOutputStream zipOutStream, String filePath, String entryName) throws IOException
	{

		File infile = new File(filePath);

		FileInputStream fis = new FileInputStream(infile);
		BufferedInputStream filebis = new  BufferedInputStream(fis, BUFFER);
		ZipEntry entry = new ZipEntry(entryName);
		zipOutStream.putNextEntry(entry);

		int count;
		byte data[] = new byte[2048];
		while((count = filebis.read(data, 0, BUFFER)) != -1) {
			zipOutStream.write(data, 0, count);
		}
		filebis.close();
	}

	public static void unzip(String zipFile, String outDirPath) throws  FileNotFoundException, IOException
	{
		FileInputStream fis = new FileInputStream(zipFile);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

		File outDir=new File(outDirPath);

		outDir.mkdir();

		ZipEntry entry;
		while((entry = zis.getNextEntry()) != null)
		{
			int count;
			byte data[] = new byte[BUFFER];
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(outDirPath + File.separator + entry.getName());
			BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = zis.read(data, 0, BUFFER)) != -1)
			{
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}
		zis.close();
	}

	public static void main (String argv[]) throws Exception
	{
		zip("c:/temp/file.txt", "c:/temp/signedFile", "c:/temp/pubkey", "c:/temp/zippedfile.zip");
		unzip("zippedfile.zip", "unzipdir");
	}
}
