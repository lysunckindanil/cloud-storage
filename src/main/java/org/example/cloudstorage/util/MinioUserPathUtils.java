package org.example.cloudstorage.util;

import org.example.cloudstorage.entity.User;

public class MinioUserPathUtils {
    public static final String MINIO_USER_PREFIX = "user-%d-files/";

    public static String constructPath(String path, User user) {
        if (path.isEmpty()) return MINIO_USER_PREFIX.formatted(user.getId());
        return PathUtils.normalizePathMinioCompatible(MINIO_USER_PREFIX.formatted(user.getId()) + path);
    }
}
