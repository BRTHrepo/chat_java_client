package com.chatapp.core.model;

public class MediaInfo {
    private String media_type; // "audio", "image", "video"
    private String file_path;
    private long file_size;

    // Getters and Setters

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
               "media_type='" + media_type + '\'' +
               ", file_path='" + file_path + '\'' +
               ", file_size=" + file_size +
               '}';
    }
}
