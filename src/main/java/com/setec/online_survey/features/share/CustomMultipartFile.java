package com.setec.online_survey.features.share;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;

public class CustomMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;

    public CustomMultipartFile(byte[] content, String name) {
        this.content = content;
        this.name = name;
    }

    @Override public String getName() { return name; }
    @Override public String getOriginalFilename() { return name; }
    @Override public String getContentType() { return "image/png"; }
    @Override public boolean isEmpty() { return content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override public byte[] getBytes() { return content; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(File dest) { /* Not needed */ }
}