package edu.diploma.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
public class FileContent {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File file;

    @Lob
    private byte[] content;

    public FileContent() {
    }

    public FileContent(File file) {
        this.file = file;
    }

    public FileContent(File file, byte[] content) {
        this.file = file;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileContent that = (FileContent) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }
}
