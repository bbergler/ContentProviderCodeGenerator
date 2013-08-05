package com.foxykeep.cpcodegenerator.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foxykeep.cpcodegenerator.FileCache;
import com.foxykeep.cpcodegenerator.model.FieldData;
import com.foxykeep.cpcodegenerator.model.TableData;
import com.foxykeep.cpcodegenerator.util.PathUtils;

public class ModelGenerator {

//	enum Types {
//		text("String"), integer("Integer"), float("Float");
//
//		Types(String str) {
//			this.str = str;
//		}
//
//		private String str;
//
//		public String val() {
//			return str;
//		}
//
//	}
	static {

	}
	public ModelGenerator() {

	}

    public static void generate(String fileName, String classPackage, ArrayList<TableData> classDataList,
			String modelFolder, String classesPrefix, String providerFolder) {
		System.out.println(classPackage + " " + classDataList.toString());
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("text", "String");
		map.put("real", "Float");
		map.put("double", "Double");
		map.put("integer", "Integer");
        map.put("long", "Long");
        map.put("boolean", "Boolean");
		final StringBuilder sb = new StringBuilder();
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new FileReader(new File("res/model_class.txt")));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			final String modelClass = sb.toString();

			final StringBuilder sbFields = new StringBuilder();

			final StringBuilder sbMethods = new StringBuilder();
			
			final StringBuilder sbImplements = new StringBuilder();
			
			final StringBuilder sbImports = new StringBuilder();

			for (TableData tableData : classDataList) {
				sbFields.setLength(0);
				sbMethods.setLength(0);
				sbImports.setLength(0);
				

				for (FieldData fieldData : tableData.fieldList) {
					if(!fieldData.annotation.isEmpty())
						sbFields.append("     "+fieldData.annotation+"\n");
					if(map.get(fieldData.type)!=null)
					sbFields.append("     public " + map.get(fieldData.type) + " " + fieldData.dbName + ";\n");
					else if(fieldData.type.contains("enum|"))
					{
						String name =fieldData.dbName.substring(0, 1).toUpperCase()+fieldData.dbName.substring(1);
						sbFields.append("     public " + name + " " + fieldData.dbName + ";\n");
						sbMethods.append(buildEnum(fieldData));
					}
					else
						sbFields.append("     public " + fieldData.type + " " + fieldData.dbName + ";\n");
				}
				sbImports.append("import "+classPackage+"."+providerFolder+"."+classesPrefix+"Content."+tableData.dbClassName+";\n");
				sbImports.append("import android.content.ContentValues;\n");
				for (String imports : tableData.importList) {
					sbImports.append("import "+imports+";\n");	
				}
				sbImplements.setLength(0);
				sbMethods.append(generateToContentValues(tableData.fieldList,tableData.dbClassName)+"\n");
				sbMethods.append(generateToString(tableData.fieldList,tableData.modelName)+"\n");

				FileCache.saveFile(
						PathUtils.getAndroidFullPath(fileName, classPackage, modelFolder) + tableData.modelName
								+ ".java",
						String.format(modelClass, classPackage, modelFolder,sbImports.toString(), tableData.modelName,sbImplements.toString(), sbFields.toString(),
								sbMethods.toString()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String buildEnum(FieldData fieldData) {
		String name =fieldData.dbName.substring(0, 1).toUpperCase()+fieldData.dbName.substring(1);
		String enumCode ="    public enum "+name+"\n    {\n      ";
//		System.out.println(Arrays.toString(fieldData.dbType.split("|")));
		enumCode+=fieldData.type.split("\\|")[1]+";\n    }\n\n";
//		public enum V
//		{
//			test,asd;
//		}
		return enumCode;
	}

	private static String generateToContentValues(List<FieldData> fieldList, String dbClassName) {
		String s="    public ContentValues getContentValues(){\n";
		s+="        ContentValues cv = new ContentValues();\n";
		for (FieldData fieldData : fieldList) {
//			
//			cv.put(OfferContent.Columns.NAME.getName(), name);
			String name =fieldData.dbName.toUpperCase();
			if(fieldData.dbIsModelOnly)
				continue;
			if(name.equals("_ID")){
				name="ID";
				if(!fieldData.annotation.contains("@SerializedName(\"id\")"))
				continue;
			}
			String value=fieldData.dbName;
			if(!fieldData.custom_value.isEmpty())
				value=fieldData.custom_value;
			s+="        cv.put("+dbClassName+".Columns."+name+".getName(), " +value+");\n";
			
		}
		s+="        return cv;\n";
		s+="    }\n";
		return s;
	}
	
	private static String generateToString(List<FieldData> fieldList, String modelName) {
		String s="    public String toString(){\n";
		s+="        boolean null_vars=false;\n";
		s+="        StringBuilder sb = new StringBuilder();\n";
		s+="        sb.append(\"Model:"+modelName+"\");\n";
		
		for (FieldData fieldData : fieldList) {
//			
			String name =fieldData.dbName;

			s+="        if("+name+" != null)\n";
			if(name.startsWith("_"))
				name=name.substring(1);
			s+="          sb.append(\""+name+"= \"+" +fieldData.dbName+"+\" \");\n";
			s+="        else null_vars=true;\n";
			
		}
		s+="        if(null_vars){\n";
		s+="          sb.append(\"elements which are null:\");\n";
		for (FieldData fieldData : fieldList) {
//			
			String name =fieldData.dbName;

		s+="          if("+name+" == null)\n";
			if(name.startsWith("_"))
				name=name.substring(1);
		s+="            sb.append(\""+name+" \");\n";
			
		}
		s+="        }";
		s+="        return sb.toString();\n";
		s+="    }";
		return s;
	}

}
