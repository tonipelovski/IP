package com.example.file;

import javax.persistence.*;

@Entity
@Table(name = "link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String linkCode = "";

    @OneToOne
    @JoinColumn(name="file_id", nullable=false)
    private File file;

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
