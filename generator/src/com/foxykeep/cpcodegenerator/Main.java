package com.foxykeep.cpcodegenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.foxykeep.cpcodegenerator.generator.DatabaseGenerator;
import com.foxykeep.cpcodegenerator.generator.ModelGenerator;
import com.foxykeep.cpcodegenerator.model.TableData;
import com.foxykeep.cpcodegenerator.util.PathUtils;

public class Main {

	public static void main(final String[] args) {

		if (args.length > 0) {

			File input = new File(args[0]);
			generateForFile(input);
		} else {
			final File fileInputDir = new File("input/");
			if (!fileInputDir.exists() || !fileInputDir.isDirectory()) {
				return;
			}
			for (File file : fileInputDir.listFiles()) {
				final String fileName = file.getName();
				if (fileName.equals("example")) {
					// Bypass the example folder
					continue;
				}
				System.out.println("Generating code for " + fileName);

				generateForFile(file);

			}
		}

		// For each file in the input folder

	}

	private static void generateForFile(File file) {

		String columnMetadataText;
		final StringBuilder sb = new StringBuilder();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File("res/column_metadata.txt")));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
            }
			columnMetadataText = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final Reader in;
		sb.setLength(0);
		final char[] buffer = new char[2048];
		System.out.println(remove(file.getName()));
		final String fileName = remove(file.getName())+".gen";
		try {
			in = new InputStreamReader(new FileInputStream(file), "UTF-8");
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if (read != -1) {
					sb.append(buffer, 0, read);
				}
			} while (read >= 0);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final String content = sb.toString();
		if (content.length() == 0) {
			System.out.println("file is empty.");
			return;
		}

		try {
			final JSONObject root = new JSONObject(content);
			final JSONObject jsonDatabase = root.getJSONObject("database");

			// Classes generation
			String classPackage, classesPrefix, contentClassesPrefix, dbAuthorityPackage, providerFolder, modelFolder, path;
			int dbVersion;
            boolean hasProviderSubclasses;
			classPackage = jsonDatabase.getString("package");
			classesPrefix = jsonDatabase.getString("classes_prefix");
			contentClassesPrefix = jsonDatabase.optString("content_classes_prefix", "");
			dbAuthorityPackage = jsonDatabase.optString("authority_package", classPackage);
			providerFolder = jsonDatabase.optString("provider_folder", PathUtils.PROVIDER_DEFAULT);
			modelFolder = jsonDatabase.optString("model_folder", PathUtils.MODEL_DEFAULT);
			dbVersion = jsonDatabase.getInt("version");
            hasProviderSubclasses = jsonDatabase.optBoolean("has_subclasses");
			path = file.getAbsoluteFile().getParent()+"/"+jsonDatabase.optString("path",PathUtils.OUTPUT_DEFAULT);
			

			ArrayList<TableData> classDataList = TableData.getClassesData(root.getJSONArray("tables"),
					contentClassesPrefix, dbVersion);
	


			// Database generation
			DatabaseGenerator.generate(path + fileName, classPackage, dbVersion, dbAuthorityPackage,
                             classesPrefix, classDataList, providerFolder, hasProviderSubclasses);

			ModelGenerator.generate(path + fileName, classPackage, classDataList, modelFolder,classesPrefix,providerFolder);
			
			FileCache.saveFile(PathUtils.getAndroidFullPath(path + fileName, classPackage,
                    providerFolder + "." + PathUtils.UTIL) + "ColumnMetadata.java",
                    String.format(columnMetadataText, classPackage,
                            providerFolder + "." + PathUtils.UTIL));


		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		return;
	}
	
	 private static String remove(String in){
	        if(in == null) {
	            return null;
	        }
	        int p = in.lastIndexOf(".");
	        if(p <= 0){
	            return in;
	        }
	        return in.substring(0, p);
	    }
}
