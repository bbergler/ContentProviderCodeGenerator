package com.foxykeep.cpcodegenerator.util;

public class PathUtils {

    private PathUtils() {
    }

    public static final String PROVIDER_DEFAULT = "data.provider";
    public static final String UTIL = "util";
	public static final String MODEL_DEFAULT = "model";
	public static final String OUTPUT_DEFAULT = "output/";

    public static String getAndroidFullPath(final String fileName, final String classPackage, final String path) {
        return getOutputPath(fileName) + classPackage.replace(".", "/") + "/" + path.replace(".", "/") + "/";
    }

    private static String getOutputPath(final String fileName) {
        return fileName + "/";
    }
}
