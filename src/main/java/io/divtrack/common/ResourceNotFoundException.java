package io.divtrack.common;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " not found: " + id, 404);
    }
}
