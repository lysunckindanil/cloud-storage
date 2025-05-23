package org.example.cloudstorage.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class PathUtils {

    private static final Pattern VALID_PATH_PATTERN =
            Pattern.compile("^[a-zA-Zа-яА-Я0-9!\\-_.*'()/+ ]+$");

    public static boolean isPathValid(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (path.isEmpty() || path.equals("/"))
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

        return true;
    }

    /**
     * <h3>Returns breadcrumb for file or directory path</h3>
     *
     * <h6>Name conventions</h6>
     * <li>If a path leads to a directory => name = 'folder_name/'</li>
     * <li>If a path leads to a file => name = 'file_name'</li>
     *
     * <h6>Path conventions</h6>
     * <li>If a path is root => path = '/'</li>
     * <li>path never starts with '/' (despite root directory) and always ends with '/'</li>
     *
     * <h6>Commons</h6>
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

            if (file.getNameCount() == parentStart) {
                return new Breadcrumb("/", "/");
            }

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

    /**
     * <h3>Normalizes path as minio compatible</h3>
     * <h6>Examples</h6>
     * <ul>
     * <li>{@code normalizePathMinioCompatible("/path") -> "path"}</li>
     * <li>{@code normalizePathMinioCompatible("//path//file") -> "path/file"}</li>
     * <li>{@code normalizePathMinioCompatible("path/name of file") -> "path/name+of+file"}</li>
     * </ul>
     *
     * @param path path to normalize
     * @return normalized path
     */
    public static String normalizePathMinioCompatible(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        return path.strip().replace(" ", "+").replace("//", "/");
    }

    /**
     * <h3>Returns parent at given place from the end</h3>
     * <h6>Examples</h6>
     * <ul>
     * <li>{@code getParentFromEndAtN("n1/n2/n3/", 0) -> "n3"}</li>
     * <li>{@code getParentFromEndAtN("n1/n2/n3/", 1) -> "n2"}</li>
     * <li>{@code getParentFromEndAtN("n1/n2/n3/", 2) -> "n1"}</li>
     * <li>{@code getParentFromEndAtN("n1/n2/n3", 0) -> "n1"}</li>
     * <li>{@code getParentFromEndAtN("n1,n2,n3/", 3) -> IllegalArgumentException}</li>
     * </ul>
     *
     * @param path directory where to search parent
     * @param n    number of parent from the end
     * @return parent name at given place
     * @throws IllegalArgumentException if path and n are not compatible
     */
    public static String getParentFromEndAtN(String path, int n) {
        Path p = Paths.get(path);
        if (n >= p.getNameCount())
            throw new IllegalArgumentException("N must be less than parent count: " + p.getNameCount());

        return p.subpath(p.getNameCount() - n - 1, p.getNameCount() - n).toString();
    }

    /**
     * <h3>Returns nested directories of the path</h3>
     * <h6>Examples</h6>
     * <ul>
     * <li>{@code getNestedDirectories("", "n1/n2/n3/") -> {"n1/n2/","n1/"}}</li>
     * <li>{@code getNestedDirectories("n1/", "n1/n2/n3/") -> "n1/n2"}</li>
     * </ul>
     *
     * @param path     directory where to search nested dirs
     * @param basePath to skip dirs inside basePath
     * @return list of nested dirs
     */
    public static List<String> getNestedDirectories(String basePath, String path) {
        if (!path.contains("/"))
            return List.of();

        List<String> result = new ArrayList<>();
        String[] dirNames = path.substring(0, path.lastIndexOf("/")).split("/");
        StringJoiner nestedDirs = new StringJoiner("/");
        for (String dir : dirNames) {
            nestedDirs.add(dir);
            result.add(basePath + nestedDirs + "/");
        }
        return result;
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
