package Utilities;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class SharedFile implements Serializable {
    private String path;
    private String owner;
    private String lastModifier;
    private LocalDateTime created;
    private LocalDateTime modified;
    private List<String> collaborators;
    private boolean readonly;
    private long size; // TODO: Use.

    public SharedFile(String owner, String path, LocalDateTime modified, boolean readonly) {
        this.owner = owner;
        this.path = path;
        this.lastModifier = owner;

        this.modified = modified;
        this.created = LocalDateTime.now();
        this.collaborators = new LinkedList<>();
        this.readonly = readonly;
    }

    public SharedFile(String owner, String path, LocalDateTime modified) {
        this(owner, path, modified, false);
    }

    public SharedFile(String owner, String path, boolean readonly) {
        this(owner, path, LocalDateTime.now(), readonly);
    }

    public SharedFile(String owner, Path path, boolean readonly) {
        this(owner, path.toString(), LocalDateTime.now(), readonly);
    }

    public SharedFile(String owner, Path path) {
        this(owner, path.toString(), LocalDateTime.now(), false);
    }

    public SharedFile(Path path) {
        this(path.getRoot().toString(), path.toString(), LocalDateTime.now(), false);
    }

    public SharedFile() {}


    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLastModifier() {
        return lastModifier;
    }
    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public LocalDateTime getCreated() {
        return created;
    }
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }
    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public List<String> getCollaborators() {
        return List.copyOf(collaborators);
    }
    public void addCollaborator(String username) {
        this.collaborators.add(username);
    }
    public boolean removeCollaborator(String username) {
        return this.collaborators.remove(username);
    }
    public void clearCollaborators() {
        this.collaborators.clear();
    }

    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
