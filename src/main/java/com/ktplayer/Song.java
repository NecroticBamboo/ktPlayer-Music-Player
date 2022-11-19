package com.ktplayer;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.util.Duration;


public class Song {
    private StringProperty id;
    private StringProperty artistName;
    private StringProperty songName;
    private StringProperty length;
    private DoubleProperty duration;
    private StringProperty durationString;
    private StringProperty album;
    private StringProperty url;
    private Image image;

    public Song() {}

    public Song(String url) {
        this.url = new SimpleStringProperty(url);
    }

    public Song(String id, String artistName, String songName, String length, Duration duration, String album, String url) {
        this.id = new SimpleStringProperty(id);
        this.artistName = new SimpleStringProperty(artistName);
        this.songName = new SimpleStringProperty(songName);
        this.length = new SimpleStringProperty(length);
        this.duration = new SimpleDoubleProperty(duration.toSeconds());
        this.durationString = new SimpleStringProperty(Controller.secToMin((long) duration.toSeconds()));
        this.album = new SimpleStringProperty(album);
        this.url = new SimpleStringProperty(url);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getArtistName() {
        return artistName.get();
    }

    public StringProperty artistNameProperty() {
        return artistName;
    }

    public String getSongName() {
        return songName.get();
    }

    public StringProperty songNameProperty() {
        return songName;
    }

    public String getLength() {
        return length.get();
    }

    public StringProperty lengthProperty() {
        return length;
    }

    public StringProperty durationProperty() {
        return durationString;
    }

    public String getDuration() {
        return Duration.minutes(Duration.seconds(duration.get()).toMinutes()).toString();
    }

    public String getAlbum() {
        return album.get();
    }

    public StringProperty albumProperty() {
        return album;
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}

