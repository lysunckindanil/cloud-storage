package org.example.cloudstorage.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.example.cloudstorage.util.PathUtils.breadcrumb;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathUtilsTest {

    private static Stream<Arguments> constructBreadcrumbTestData() {
        return Stream.of(
                Arguments.of("/folder", true, 0, breadcrumb("/", "folder/")),
                Arguments.of("folder", true, 0, breadcrumb("/", "folder/")),
                Arguments.of("/folder/", true, 0, breadcrumb("/", "folder/")),
                Arguments.of("folder/", true, 0, breadcrumb("/", "folder/")),
                Arguments.of("folder/folder2", true, 0, breadcrumb("folder/", "folder2/")),
                Arguments.of("/folder/folder2", true, 0, breadcrumb("folder/", "folder2/")),
                Arguments.of("/file", false, 0, breadcrumb("/", "file")),
                Arguments.of("/file/", false, 0, breadcrumb("/", "file")),
                Arguments.of("file", false, 0, breadcrumb("/", "file")),
                Arguments.of("file/", false, 0, breadcrumb("/", "file")),
                Arguments.of("folder/file", false, 0, breadcrumb("folder/", "file")),
                Arguments.of("folder/file/", false, 0, breadcrumb("folder/", "file")),
                Arguments.of("/folder/file/", false, 0, breadcrumb("folder/", "file")),
                Arguments.of("/folder/file/", false, 1, breadcrumb("/", "file")),
                Arguments.of("/folder/file", false, 1, breadcrumb("/", "file")),
                Arguments.of("folder/file", false, 1, breadcrumb("/", "file")),
                Arguments.of("folder/file/", false, 1, breadcrumb("/", "file")),
                Arguments.of("/folder/file", false, 1, breadcrumb("/", "file")),
                Arguments.of("/folder/folder", true, 1, breadcrumb("/", "folder/")),
                Arguments.of("/folder/folder/", true, 1, breadcrumb("/", "folder/")),
                Arguments.of("folder/folder/", true, 1, breadcrumb("/", "folder/")),
                Arguments.of("folder/folder/file", false, 2, breadcrumb("/", "file")),
                Arguments.of("folder/folder/file/", false, 2, breadcrumb("/", "file")),
                Arguments.of("/folder/folder/file/", false, 2, breadcrumb("/", "file"))
        );
    }


    @MethodSource("constructBreadcrumbTestData")
    @ParameterizedTest
    void constructBreadcrumb(String path, boolean isDir, int parentStart, PathUtils.Breadcrumb result_breadcrumb) {
        assertEquals(result_breadcrumb, PathUtils.constructBreadcrumb(path, isDir, parentStart));
    }


    private static Stream<Arguments> constructBreadcrumbTestInvalidData() {
        return Stream.of(
                Arguments.of("/", true, 0),
                Arguments.of("/", false, 0),
                Arguments.of("/folder/", true, 1),
                Arguments.of("/folder", true, 1),
                Arguments.of("folder/", true, 1),
                Arguments.of("/folder/folder", true, 2),
                Arguments.of("folder/folder", true, 2),
                Arguments.of("folder/folder/", true, 2),
                Arguments.of("folder/folder/", true, 2),
                Arguments.of("folder/folder/", true, 2)
        );
    }

    @MethodSource("constructBreadcrumbTestInvalidData")
    @ParameterizedTest
    void constructBreadcrumb_ThrowsError(String path, boolean isDir, int parentStart) {
        assertThrows(IllegalArgumentException.class, () -> PathUtils.constructBreadcrumb(path, isDir, parentStart));
    }


    private static Stream<Arguments> getParentFromEndAtNTestData() {
        return Stream.of(
                Arguments.of("/n1/n2/n3", 0, "n3"),
                Arguments.of("/n1/n2/n3", 1, "n2"),
                Arguments.of("/n1/n2/n3", 2, "n1")
        );
    }

    @ParameterizedTest
    @MethodSource("getParentFromEndAtNTestData")
    void getParentFromEndAtN(String input, int arg, String expected) {
        String actual = PathUtils.getParentFromEndAtN(input, arg);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> getParentFromEndAtN_OutOfBonds_ThrowsException() {
        return Stream.of(
                Arguments.of("/n1/n2/n3", 3),
                Arguments.of("/n1/n2/n3", 4),
                Arguments.of("/n1", 1),
                Arguments.of("n1", 1),
                Arguments.of("n1", 2)
        );
    }

    @MethodSource("getParentFromEndAtN_OutOfBonds_ThrowsException")
    @ParameterizedTest
    void getParentFromEndAtN_OutOfBonds_ThrowsException(String input, int arg) {
        assertThrows(IllegalArgumentException.class, () -> PathUtils.getParentFromEndAtN(input, arg));
    }
}