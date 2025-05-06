package org.example.cloudstorage.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

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
     * @param path        file/directory path; should be valid for an expected result; '/' in the end doesn't matter
     * @param isDir       to mark the path as file or directory
     * @param parentStart from which parent to start (e.g.,
     *                    if equals 0 then the returned path will be 0/1/2/n/)
     *                    if equals 1 then the returned path will be 1/2/n/)
     * @return breadcrumb which contains path and name
     */
    public static Breadcrumb constructBreadcrumb(String path, boolean isDir, int parentStart) {
        if (path.startsWith("/")) path = path.substring(1);
        Path file = Paths.get(path);
        file = file.subpath(parentStart, file.getNameCount());
        return new Breadcrumb(
                (file.getParent() == null ? "/" : (file.getParent().toFile() + "/")).replace("\\", "/"),
                isDir ? file.getFileName() + "/" : file.getFileName().toString()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class Breadcrumb {
        private String path;
        private String name;
    }
}
