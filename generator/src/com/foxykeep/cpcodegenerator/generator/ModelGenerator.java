package com.foxykeep.cpcodegenerator.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foxykeep.cpcodegenerator.FileCache;
import com.foxykeep.cpcodegenerator.model.FieldData;
import com.foxykeep.cpcodegenerator.model.TableData;
import com.foxykeep.cpcodegenerator.util.PathUtils;

public class ModelGenerator {

    public static void generate(String fileName, String classPackage, ArrayList<TableData> classDataList,
			String modelFolder, String classesPrefix, String providerFolder) {


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
                    String accessModifier="    public ";
					if(!fieldData.annotation.isEmpty())
						sbFields.append("    "+fieldData.annotation+"\n");
                    if(fieldData.constructor) {
                          accessModifier+="final ";
                    }

					if(fieldData.getModelType()!=null)
					sbFields.append(accessModifier + fieldData.getModelType() + " " + fieldData.getModelName() + ";\n");
					else if(fieldData.type.contains("enum|"))
					{
						String name =fieldData.getModelName().substring(0, 1).toUpperCase()+fieldData.getModelName().substring(1);
						sbFields.append(accessModifier + name + " " + fieldData.getModelName() + ";\n");
						sbMethods.append(buildEnum(fieldData));
					}
					else
						sbFields.append(accessModifier+ fieldData.type + " " + fieldData.getModelName() + ";\n");
				}
				sbImports.append("import "+classPackage+"."+providerFolder+"."+classesPrefix+"Content."+tableData.dbClassName+";\n");
				sbImports.append("import android.content.ContentValues;\n");
				for (String imports : tableData.importList) {
					sbImports.append("import "+imports+";\n");	
				}
				sbImplements.setLength(0);
                sbMethods.append(generateCtor(tableData.modelName,tableData.fieldList, tableData.dbClassName));
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

    private static String generateCtor(String modelName, List<FieldData> fieldList, String dbClassName) {
        String args="";
        String body="";
        for (FieldData fieldData : fieldList) {
            if(fieldData.constructor)
            {
               if(args.length()>0) args+=", ";
               args+=fieldData.getModelType()+" "+fieldData.dbName;
                body+="      this."+fieldData.dbName+" = "+fieldData.dbName+";\n";
            }
        }

           String ctor ="";
        if(args.length()>0)
        ctor+="    public "+modelName+"("+args+")"+"{\n"+body+"\n    }\n\n";

        return ctor;
    }

    private static String buildEnum(FieldData fieldData) {
		String name =fieldData.dbName.substring(0, 1).toUpperCase()+fieldData.dbName.substring(1);
		String enumCode ="    public enum "+name+"\n    {\n      ";
		enumCode+=fieldData.type.split("\\|")[1]+";\n    }\n\n";
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
			String value=fieldData.getModelName();
			if(!fieldData.customValue.isEmpty())
				value=fieldData.customValue;
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
			String name =fieldData.getModelName();

			s+="        if("+name+" != null){\n";
//			if(name.startsWith("_"))
//				name=name.substring(1);
			s+="          sb.append(\""+name+"= \"+" +fieldData.getModelName()+"+\" \");\n";
            s+="        }\n";
            s+="        else{\n";
            s+="          null_vars=true;\n";
            s+="        }\n";
			
		}
		s+="        if(null_vars){\n";
		s+="          sb.append(\"elements which are null:\");\n";
		for (FieldData fieldData : fieldList) {
			String name =fieldData.getModelName();

		s+="          if("+name+" == null){\n";
//			if(name.startsWith("_"))
//				name=name.substring(1);
		s+="            sb.append(\""+name+" \");\n";
        s+="          }\n";
		}
		s+="        }\n";
		s+="        return sb.toString();\n";
		s+="    }";
		return s;
	}

}
