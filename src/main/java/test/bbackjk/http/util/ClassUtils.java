package test.bbackjk.http.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

@UtilityClass
public class ClassUtils extends org.springframework.util.ClassUtils {
    
    private final char PACKAGE_SEPARATOR = '.';
    private final char FILE_SEPARATOR = '/';
    private final String FILE_CLASS = ".class";

    public Set<Class<?>> scanningClassByAnnotation(String packageName, Class<? extends Annotation> annotationClazz) throws IOException, ClassNotFoundException {
        String resourcePath = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
        List<File> files = getAllResourceFile(resourcePath);

        Set<Class<?>> classes = new LinkedHashSet<>();
        int fileCount = files.size();
        for (int i=0; i<fileCount; i++) {
            File file = files.get(i);
            if ( file.isDirectory() ) {
                classes.addAll(findClassesByFile(file, packageName, annotationClazz));
            }
        }

        return classes;
    }

    public String toCamel(String value) {
        if ( value == null || value.isBlank() ) {
            return "";
        }
        String firstVal = value.substring(0, 1);
        return value.replaceFirst(firstVal, firstVal.toLowerCase());
    }

    public String toPascal(String value) {
        if ( value == null || value.isBlank() ) {
            return "";
        }
        String firstVal = value.substring(0, 1);
        return value.replaceFirst(firstVal, firstVal.toUpperCase());
    }

    private List<File> getAllResourceFile(String resourcePath) throws IOException {
        List<File> files = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(resourcePath);
        while ( resources.hasMoreElements() ) {
            files.add(new File(resources.nextElement().getFile()));
        }
        return files;
    }

    private Set<Class<?>> findClassesByFile(File dir, String packageName, Class<? extends Annotation> annotationClazz) throws ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();
        if (!dir.exists()) {
            return classes;
        }

        File[] files = dir.listFiles();
        if ( files == null ) {
            return classes;
        }

        int fileCount = files.length;
        for (int i=0; i<fileCount; i++) {
            File file = files[i];
            String fileName = file.getName();
            if ( file.isDirectory() ) {
                classes.addAll(findClassesByFile(file, packageName + PACKAGE_SEPARATOR + fileName, annotationClazz));
            } else if ( isClassByFileName(fileName) ) {
                String className = packageName + PACKAGE_SEPARATOR + fileName.substring(0, fileName.length() - 6);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<?> clazz = Class.forName(className, false, classLoader);
                Annotation annotation = clazz.getAnnotation(annotationClazz);
                if ( annotation != null ) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }


    private boolean isClassByFileName(String fileName) {
        return fileName != null && fileName.endsWith(FILE_CLASS);
    }
}
