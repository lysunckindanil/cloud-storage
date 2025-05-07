package org.example.cloudstorage.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class PathUtils {

    private static final Pattern VALID_PATH_PATTERN =
            Pattern.compile("^[a-zA-Zа-яА-Я0-9!\\-_.*'()/+ ]+$");

    /**
     * Returns breadcrumb for file or directory path
     * <p>
     * Name conventions
     * If a path is directory => name = 'folder_name/'
     * If a path is a file => name = 'file_name'
     * <p>
     * Path conventions
     * If a path is root => path = '/'
     * or else path is parent of file/directory
     *
     * @param path        file/directory path; expected that valid; '+' will be replaced with space
     * @param isDir       to mark the path as file or directory
     * @param parentStart from which parent to start (e.g.,
     *                    if equals 0 then the returned path will be 0/1/2/n/)
     *                    if equals 1 then the returned path will be 1/2/n/)
     * @return breadcrumb which contains path and name
     * @throws IllegalArgumentException if path is invalid or path and parentStart are not compatible
     */
    public static Breadcrumb constructBreadcrumb(String path, boolean isDir, int parentStart) {
        try {
            Path file = Paths.get(path);
            file = file.subpath(parentStart, file.getNameCount());

            String filePath = file.getParent() == null ? "/" : (file.getParent().toFile() + "/");
            String fileName = isDir ? file.getFileName() + "/" : file.getFileName().toString();
            return new Breadcrumb(
                    filePath.replace("\\", "/"),
                    fileName.replace("+", " ")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
    }

    public static boolean isPathValid(String path) {
        if (path.isEmpty())
            return true;

        if (path.length() > 1024) {
            return false;
        }

        if (!VALID_PATH_PATTERN.matcher(path).matches()) {
            return false;
        }

        if (path.contains("//")) {
            return false;
        }

        if (path.contains("..")) {
            return false;
        }


        return true;
    }

    public static String normalizePathAsMinioKey(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        return path.strip().replace(" ", "+").replace("//", "/");
    }

    public static Breadcrumb breadcrumb(String path, String name) {
        return new Breadcrumb(path, name);
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Breadcrumb {
        private String path;
        private String name;
    }
}
