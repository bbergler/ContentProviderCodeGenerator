package com.foxykeep.cpcodegenerator.model;

import com.foxykeep.cpcodegenerator.util.JsonUtils;
import com.foxykeep.cpcodegenerator.util.NameUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FieldData {

    public String name;
    public String type;
    public String default_value;

    public int version;

    public String dbName;
    public String annotation;
    public String dbConstantName;
    public String dbType = null;
    public boolean dbIsPrimaryKey;
    public boolean dbIsId;
    public boolean dbIsAutoincrement = false;
    public boolean dbHasIndex;
    public boolean dbSkipBulkInsert;
    public boolean dbIsModelOnly;
    public String customValue;
    public String dbDefaultValue;
    public boolean dbIsUnique;
    public boolean constructor;

    public FieldData(final JSONObject json) throws JSONException {
        name = json.getString("name");
        setType(json.getString("type"));

        version = json.optInt("version", 1);
        dbIsModelOnly = json.optBoolean("is_model_only", false);
        dbConstantName = NameUtils.createConstantName(name);
        dbIsPrimaryKey = json.optBoolean("is_primary_key", false);
        dbIsId = json.optBoolean("is_id", false);
        annotation = json.optString("annotated_with","");
        default_value = json.optString("default_value","");
        customValue = json.optString("custom_value","");
        constructor = json.optBoolean("ctor",false);
        dbIsAutoincrement = json.optBoolean("is_autoincrement", false);
        if (dbIsId) {
            if (!dbIsPrimaryKey) {
                throw new IllegalArgumentException("Field \"" + name + "\" | is_id can only be "
                        + "used on a field flagged with is_primary_key");
            }
            dbName = "_id";
            if (dbIsAutoincrement && !type.equals("integer")) {
                throw new IllegalArgumentException("Field \"" + name + "\" | is_autoincrement can "
                        + "only be used on an integer type field");
            }
        } else {
            if (dbIsAutoincrement) {
                throw new IllegalArgumentException("Field \"" + name + "\" | id_autoincrement can "
                        + "only be used on a field flagged with is_id");
            }
            dbName = name;
        }
        dbHasIndex = !dbIsPrimaryKey && json.optBoolean("is_index", false);

        dbSkipBulkInsert = json.optBoolean("skip_bulk_insert", false);

        dbDefaultValue = JsonUtils.getStringFixFalseNull(json, "default");
        dbIsUnique = json.optBoolean("unique", false);
    }

    private void setType(final String type) {
        this.type = type;

        if (type.equals("int") || type.equals("integer") || type.equals("long")
                || type.equals("boolean") || type.equals("date")) {
            dbType = "integer";
        } else if (type.equals("float") || type.equals("double") || type.equals("real")) {
            dbType = "real";
        } else if (type.equals("string") || type.equals("text") || type.equals("String") ||  type.contains("enum|")) {
            dbType = "text";
        }
        else
        	dbType=type;
    }

    public static String getDefaultValue(final String type) {
        if (type.equals("string") || type.equals("text") || type.equals("String")) {
            return "''";
        } else {
            return "-1";
        }
    }

    public String getModelType(){
        Map<String,String> map = new HashMap<String, String>();
        map.put("text", "String");
        map.put("real", "Float");
        map.put("double", "Double");
        map.put("integer", "Integer");
        map.put("long", "Long");
        map.put("boolean", "Boolean");
        return map.get(type);
    }

    public String getModelName(){
//        if(dbName.contains("_")){
//            String result="";
//            for (int i = 0; i < dbName.length(); i++) {
//                if(dbName.charAt(i)=='_')
//                {
//                    if(i==0) continue;
//                    i++;
//                    result+=new String(""+dbName.charAt(i)).toUpperCase();
//
//                }else{
//                    result+=dbName.charAt(i);
//                }
//            }
//            return result;
//        }
//        else {
        return dbName;
// }

    }
}
