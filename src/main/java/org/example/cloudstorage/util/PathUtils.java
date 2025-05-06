package org.example.cloudstorage.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class PathUtils {

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
     * @param path        file/directory path; '/' in the end doesn't matter
     * @param isDir       to mark the path as file or directory
     * @param parentStart from which parent to start (e.g.,
     *                    if equals 0 then the returned path will be 0/1/2/n/)
     *                    if equals 1 then the returned path will be 1/2/n/)
     * @return breadcrumb which contains path and name
     * @throws IllegalArgumentException if path is invalid or path and parentStart are not compatible
     */
    public static Breadcrumb constructBreadcrumb(String path, boolean isDir, int parentStart) {
        if (!isPathValid(path))
            throw new IllegalArgumentException("Invalid path: " + path);

        try {
            Path file = Paths.get(path);
            file = file.subpath(parentStart, file.getNameCount());

            return new Breadcrumb(
                    (file.getParent() == null ? "/" : (file.getParent().toFile() + "/")).replace("\\", "/"),
                    isDir ? file.getFileName() + "/" : file.getFileName().toString()
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

        Pattern validPathPattern = Pattern.compile("^[a-zA-Z0-9!\\-_.*'()/]+$");

        if (!validPathPattern.matcher(path).matches()) {
            return false;
        }

        if (path.contains("//")) {
            return false;
        }

        return true;
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
