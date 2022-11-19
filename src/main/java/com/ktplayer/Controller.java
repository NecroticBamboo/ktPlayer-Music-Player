package com.ktplayer;

import com.jfoenix.controls.JFXSlider;
import com.mpatric.mp3agic.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer.Status;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ListIterator;


public class Controller {


    @FXML
    private AnchorPane window;

    @FXML
    private AnchorPane playlistNode;

    @FXML
    private Pane showPlaylist;
    @FXML
    private Pane exit;
    @FXML
    private Pane minimize;
    @FXML
    private Pane imagePane;

    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> idColumn;
    @FXML
    private TableColumn<Song, String> artistNameColumn;
    @FXML
    private TableColumn<Song, String> songNameColumn;
    @FXML
    private TableColumn<Song, String> durationColumn;
    @FXML
    public TableColumn<Song, String> lengthColumn;
    @FXML
    public TableColumn<Song, String> albumColumn;

    @FXML
    private Label artistName;
    @FXML
    private Label albumName;
    @FXML
    private Label songName;
    @FXML
    private Label totalDuration;
    @FXML
    private Label currentDuration;
    @FXML
    private Label volumeValue;
    @FXML
    private Label songsCounter;

    @FXML
    private JFXSlider songSlider;
    @FXML
    private Slider volumeSlider;

    // Everything related to rate
    @FXML
    private Slider rateSlider;
    @FXML
    private Label rateValue;

    // Everything related to stamps
    @FXML
    public ImageView aPointButton;
    @FXML
    public Label aPointStamp;
    private double aPointStampValue = 0.0;
    @FXML
    public ImageView bPointButton;
    @FXML
    public Label bPointStamp;
    private double bPointStampValue = 0.0;
    @FXML
    public Button resetButton;

    @FXML
    private ImageView folderChooser;

    @FXML
    private ImageView playButton;
    @FXML
    private ImageView pauseButton;
    @FXML
    private ImageView nextSongButton;
    @FXML
    private ImageView previousSongButton;
    @FXML
    private ImageView muteIcon;
    @FXML
    private ImageView volumeIcon;
    @FXML
    private ToggleButton autoPlayIcon;


    @FXML
    private Stage stage;

    private Main main;

    private List<MediaPlayer> players;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private boolean isAutoplay;
    private double volume = 10;
    private double rate;
    private String path;

    private double xOffset = 0;
    private double yOffset = 0;

    private FadeTransition fadeIn = new FadeTransition();
    private FadeTransition fadeOut = new FadeTransition();


    public Controller() {
        players = new ArrayList<>();
        songSlider = new JFXSlider();
        rateSlider = new JFXSlider();
        isAutoplay = false;
        volume = 0.1;
        rate = 1;
        stage = Main.getStage();
        stage.getIcons().add(new Image(ClassLoader.getSystemResource("images/logo.png").toExternalForm()));
    }

    @FXML
    private void initialize() throws Exception {

        window.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            }
        });

        window.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }
        });

        autoPlayIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isAutoplay) {
                    autoPlayIcon.setSelected(false);
                    isAutoplay = false;
                }
                else if(!isAutoplay) {
                    autoPlayIcon.setSelected(true);
                    isAutoplay = true;
                }
            }
        });

        showPlaylist.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(playlistNode.isVisible() == true) {
                    hideTransition(playlistNode);
                }
                else {
                    showTransition(playlistNode);
                }
            }
        });

        minimize.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setIconified(true);
            }
        });

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.exit(0);
            }
        });

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().artistNameProperty());
        songNameColumn.setCellValueFactory(cellData -> cellData.getValue().songNameProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        lengthColumn.setCellValueFactory(cellData -> cellData.getValue().lengthProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());

        showSongInfo(null);

        songTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSongInfo(newValue));

        folderChooser.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DirectoryChooser chooser = new DirectoryChooser();
                File selectedDirectory = chooser.showDialog(stage);
                if(selectedDirectory == null) {
                    System.out.println("No directory selected!");
                }
                else {

                    try {
                        if(!(players.isEmpty())) {
                            players.clear();
                            System.out.println("new array list");
                        }
                        songTable.setItems(songsUrls(selectedDirectory));

                        songTable.setOnMouseClicked((MouseEvent e) -> {

                            if((e.getClickCount() > 0) && (e.getClickCount() < 2)) {
                                try {

                                    // When going back to mediaview to select a song, make sure the media player is reset to that song's position
                                    // System.out.println(next_player.getMedia().getMetadata());
                                    takeCare();
                                }
                                catch (Exception ex) {
                                    try {
                                        throw ex;
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                }
            }
        });
    }

    public void showSongInfo(Song song) {
        if(song != null) {
            artistName.setText(song.getArtistName());
            songName.setText(song.getSongName());
            albumName.setText(song.getAlbum());
        }
        else {
            artistName.setText("-");
            songName.setText("-");
            albumName.setText("-");
        }

    }

    public ObservableList<Song> songsUrls(File dir)   throws Exception{
        ObservableList<Song> songData = FXCollections.observableArrayList();
        File[] files = dir.listFiles();
        String name;
        int i = 0;
        for(File file : files) {
            if(file.isFile()) {
                name = file.getName();
                /*if(name.endsWith("jpg")) {
                    path = file.getAbsolutePath();
                }*/
                if(name.endsWith("mp3") || name.endsWith("wav")) {
                    try {
                        i++;
                        Mp3File mp3 = new Mp3File(file.getPath());
                        ID3v2 tag = mp3.getId3v2Tag();
                        Song song = new Song(String.valueOf(i), tag.getArtist(), tag.getTitle(), kbToMb(file.length()), Duration.seconds(mp3.getLengthInSeconds()),tag.getAlbum(), file.getAbsolutePath());
                        players.add(createPlayer(file.getAbsolutePath()));
                        songData.add(song);
                    }
                    catch(IOException e) {e.printStackTrace();}
                }
            }
        }
        setImage();
        i = 0;
        System.out.println(players.size());
        songsCounter.setText("");
        songsCounter.setText("Songs: " + players.size());
        return  songData;
    }

    public void playPauseSong(Song song) throws Exception{


        if(song != null) {
            File file = new File(song.getUrl());
            String path = file.getAbsolutePath();
            path.replace("\\", "/");

            if((mediaView != null) && (mediaPlayer != null)) {
                volume = mediaView.getMediaPlayer().getVolume();
                mediaView.getMediaPlayer().stop();
                mediaView = null;
                mediaPlayer = null;
            }


            Media media = new Media(new File(path).toURI().toString());

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.stop();
            mediaPlayer.setAutoPlay(false);

            mediaView = new MediaView(mediaPlayer);
            pauseIcon();
            mediaView = new MediaView(players.get(Integer.parseInt(song.getId()) - 1));


            volumeValue.setText(String.valueOf((int)volumeSlider.getValue()));
            volumeSlider.setValue(volume * 100);
            mediaView.getMediaPlayer().setVolume(volume);

            rateValue.setText(String.valueOf((int) rateSlider.getValue()/100));
            rateSlider.setValue(rate*100);
            mediaView.getMediaPlayer().setRate(rate);

            mediaView.getMediaPlayer().seek(Duration.ZERO);
            updateSliderPosition(Duration.ZERO);

            updateValues();
            mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
                @Override
                public void changed(ObservableValue<? extends MediaPlayer> observable, MediaPlayer oldValue, MediaPlayer newValue) {
                    try {
                        setCurrentlyPlayer(newValue);
                        updateValues();
                    }
                    catch(IOException e) {
                        System.out.println(e);
                    }
                    catch(UnsupportedTagException e) { System.out.println(e);}
                    catch(InvalidDataException e) { System.out.println(e);}
                }
            });


            playButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaView.getMediaPlayer().play();
                    playIcon();
                    updateValues();
                    pauseSong();

                    ListIterator players_it = players.listIterator();


                    while(players_it.hasNext()){
                        final MediaPlayer player = (MediaPlayer) players_it.next();

                        mediaPlayer = player;
                        final MediaPlayer next_player = players_it.hasNext() ? (MediaPlayer) players_it.next() : player;

                        mediaPlayer.setOnEndOfMedia(new Runnable() {



                            @Override
                            public void run() {

                                mediaView.getMediaPlayer().stop();
                                mediaView.getMediaPlayer().seek(isABInUse() ? Duration.seconds(aPointStampValue): Duration.ZERO);
                                if(isAutoplay || isABInUse()) {
                                    repeatSongs();
                                    return;
                                }
                                mediaPlayer = next_player;
                                mediaView.setMediaPlayer(mediaPlayer);
                                mediaView.getMediaPlayer().seek(Duration.ZERO);
                                updateSliderPosition(Duration.ZERO);
                                songSlider.setValue(0);

                                aPointStamp.setText("00:00");
                                aPointStampValue = 0.0;
                                bPointStamp.setText("00:00");
                                bPointStampValue=0.0;

                                updateValues();
                                mediaPlayer.setVolume(volume);
                                mediaPlayer.setRate(rate);
                                mediaPlayer.play();
                                playIcon();

                            }
                        });

                        pauseSong();

                    }


                }
            });

            nextSongButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                    if(isABInUse()){
                        seekAndUpdate(Duration.seconds(bPointStampValue));
                        return;
                    }

                    mediaView.getMediaPlayer().stop();
                    updateSliderPosition(Duration.ZERO);
                    songSlider.setValue(0);

                     MediaPlayer next_player = players.get(players.indexOf(mediaView.getMediaPlayer())+1 <  players.size() ? players.indexOf(mediaView.getMediaPlayer())+1 : players.indexOf(mediaView.getMediaPlayer()));


                    mediaPlayer = next_player;
                    System.out.println(mediaPlayer.getMedia().getMetadata());
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaView.getMediaPlayer().seek(Duration.ZERO);
                    updateSliderPosition(Duration.ZERO);
                    songSlider.setValue(0);
                    updateValues();
                    mediaPlayer.setVolume(volume);
                    mediaPlayer.setRate(rate);
                    mediaPlayer.play();
                    playIcon();

                    //add enum to state when theres a stop caused by forward or backward skipping
                }
            });

            previousSongButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
//                    seekAndUpdate(Duration.ZERO);

                    if(isABInUse()){
                        seekAndUpdate(Duration.ZERO);
                        return;
                    }

                    mediaView.getMediaPlayer().stop();
                    updateSliderPosition(Duration.ZERO);
                    songSlider.setValue(0);

                    MediaPlayer prev_player;
                    if(mediaView.getMediaPlayer().getCurrentTime().toSeconds() <=5) {
                       prev_player = players.get(players.indexOf(mediaView.getMediaPlayer())-1 >=  0 ? players.indexOf(mediaView.getMediaPlayer())-1 : players.indexOf(mediaView.getMediaPlayer()));

                    } else {
                        prev_player = players.get(players.indexOf(mediaView.getMediaPlayer()));
                    }


                    //when going back to mediaview to select a song, make sure the media player is reset to that song's position
                    //System.out.println(next_player.getMedia().getMetadata());


                    mediaPlayer = prev_player;
                    System.out.println(mediaPlayer.getMedia().getMetadata());
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaView.getMediaPlayer().seek(Duration.ZERO);
                    updateSliderPosition(Duration.ZERO);
                    songSlider.setValue(0);
                    updateValues();
                    mediaPlayer.setVolume(volume);
                    mediaPlayer.setRate(rate);
                    mediaPlayer.play();
                    playIcon();
                }
            });

            aPointButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    double aVal = mediaView.getMediaPlayer().getCurrentTime().toSeconds();

                    if(aVal > bPointStampValue && bPointStampValue!=0.0){
                        setPointA(bPointStampValue);
                        setPointB(aVal);
                    } else {
                        setPointA(aVal);
                    }

                }
            });

            bPointButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    double bVal = mediaView.getMediaPlayer().getCurrentTime().toSeconds();
                    if(bVal < aPointStampValue && aPointStampValue!=0){
                        setPointB(aPointStampValue);
                        setPointA(bVal);
                    } else {
                        setPointB(bVal);
                    }
                }
            });

            resetButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    resetABPoints();
                }
            });

            songSlider.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Bounds b1 = songSlider.getLayoutBounds();
                    double mouseX = event.getX();
                    double percent = (((b1.getMinX() + mouseX) * 100) / (b1.getMaxX() - b1.getMinX()));
                    songSlider.setValue((percent) / 100);
                    seekAndUpdate(new Duration(mediaView.getMediaPlayer().getTotalDuration().toMillis() * percent / 100));
                    songSlider.setValueFactory(slider ->
                            Bindings.createStringBinding(
                                    () -> (secToMin((long) mediaView.getMediaPlayer().getCurrentTime().add(mediaView.getMediaPlayer().getStartTime()).toSeconds())),
                                    songSlider.valueProperty()
                            )
                    );
                }
            });

        }
        else {
            if(pauseButton.isVisible()) {
                if ((mediaPlayer != null) && (mediaView != null)) {
                    mediaPlayer = mediaView.getMediaPlayer();
                    mediaPlayer.stop();
                    mediaView = null;
                    mediaPlayer = null;
                }
                pauseIcon();
            }
            System.out.println("Song does not exist!");
        }
    }

    private void setPointA(double aVal) {
        aPointStampValue = aVal;
        aPointStamp.setText(secToMin((long) aPointStampValue));
    }

    private void setPointB(double bVal) {
        bPointStampValue = bVal;
        bPointStamp.setText(secToMin((long) bPointStampValue));
        if(aPointStampValue != 0.0){
            if (mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                mediaView.getMediaPlayer().pause();
                pauseIcon();
            }

            mediaView.getMediaPlayer().setStartTime(Duration.seconds(aPointStampValue));
            mediaView.getMediaPlayer().setStopTime(Duration.seconds(bPointStampValue));
            seekAndUpdate(Duration.ZERO);

            updateSliderPosition(Duration.ZERO);
            autoPlayIcon.setSelected(true);
            isAutoplay = true;
        }
    }

    private boolean isABInUse(){
        return aPointStampValue != 0.0 && bPointStampValue != 0.0;
    }

    private void resetABPoints() {
        aPointStamp.setText("00:00");
        aPointStampValue = 0.0;
        bPointStamp.setText("00:00");
        bPointStampValue = 0.0;

        mediaView.getMediaPlayer().setStartTime(Duration.ZERO);
        mediaView.getMediaPlayer().setStopTime(Duration.ZERO);
    }

    public void setMain(Main main) {
        this.main = main;
    }


    public String kbToMb(long length) {
        Long l = length;
        double d = l.doubleValue();
        DecimalFormat df = new DecimalFormat("#.00");
        String form = df.format((d/1024)/1024);
        return form + "Mb";
    }

    public static String secToMin(long sec) {
        Long s = sec;
        String time = null;
        if((s%60) < 10) {
            time = s/60 + ":0" + s%60;
        }
        else {
            time = s/60 + ":" + s%60;
        }
        return time;
    }

    public MediaPlayer createPlayer(String url) {
        url.replace("\\", "/");
        final Media media = new Media(new File(url).toURI().toString());
        final MediaPlayer player = new MediaPlayer(media);
        System.out.println("+++++ " + url);
        return player;
    }

    public Media createMedia(String url) {
        url.replace("\\", "/");
        final Media media = new Media(new File(url).toURI().toString());
        return media;
    }

    public void playIcon() {
        playButton.setVisible(false);
        playButton.setDisable(true);
        pauseButton.setVisible(true);
        pauseButton.setDisable(false);
    }

    public void pauseIcon() {
        pauseButton.setVisible(false);
        pauseButton.setDisable(true);
        playButton.setVisible(true);
        playButton.setDisable(false);
    }

    public void setCurrentlyPlayer(MediaPlayer player) throws InvalidDataException, IOException, UnsupportedTagException {
        String source = player.getMedia().getSource();
        source = source.replace("/","\\");
        source = source.replaceAll("%20", " ");
        source = source.replaceAll("%5B", "[");
        source = source.replaceAll("%5D", "]");
        source = source.substring(6,source.length());
        System.out.println(source + " +++");
        Mp3File mp3 = new Mp3File(source);
        ID3v2 tag = mp3.getId3v2Tag();
        artistName.setText(tag.getArtist());
        songName.setText(tag.getTitle());
        albumName.setText(tag.getAlbum());
    }

    public void takeCare() throws Exception {
        if(songTable.getSelectionModel().getSelectedItem() != null) {
            Song song = songTable.getSelectionModel().getSelectedItem();
            playPauseSong(song);
        }
        else {
            System.out.println("null");
        }
    }

    private void seekAndUpdate(Duration duration) {
        final MediaPlayer player = players.get(players.indexOf(mediaView.getMediaPlayer()));

        player.seek(player.getStartTime().add(duration));
    }

    private void updateValues() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            final MediaPlayer player = mediaView.getMediaPlayer();
                            if((player.getStatus() != Status.PAUSED) && (player.getStatus() != Status.STOPPED) && (player.getStatus() != Status.READY)) {
                                double tduration = player.getTotalDuration().toSeconds();
                                totalDuration.setText(secToMin((long) tduration));
                                currentDuration.setText(secToMin((long) player.getCurrentTime().toSeconds()));
                                updateSliderPosition(player.getCurrentTime().subtract(player.getStartTime()));
                                volumeHandler();

                                rateHandler();
                            }
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
                while(!players.isEmpty());
            }
        });
        thread.start();
    }

    private void updateSliderPosition(Duration currentTime) {
        final MediaPlayer player = mediaView.getMediaPlayer();
        final Duration totalDuration = player.getTotalDuration();
        if((totalDuration == null) || (currentTime == null)) {
            songSlider.setValue(0);
        }
        else {
            songSlider.setValue((currentTime.toMillis() / totalDuration.toMillis()) * 100);
        }
    }

    private void volumeHandler() {
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100);
                volumeValue.setText(String.valueOf((int)volumeSlider.getValue()));
                volume = mediaView.getMediaPlayer().getVolume();
                volumeIconChanger();
            }
        });
    }

    private void volumeIconChanger() {
        if(volumeSlider.getValue() == 0) {
            muteIcon.setVisible(true);
            volumeIcon.setVisible(false);
        }
        else {
            muteIcon.setVisible(false);
            volumeIcon.setVisible(true);
        }
    }


    private void rateHandler() {
        rateSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double value = rateSlider.getValue()/100;
                mediaView.getMediaPlayer().setRate(value);
                rateValue.setText(String.format("%.2f", value));
                rate = mediaView.getMediaPlayer().getRate();

             if (rate == 0.0){
                    mediaView.getMediaPlayer().setRate(0.1);
                   rateValue.setText("1");
                   rate = 0.1;
              }

            }
        });

    }

    private void transitionOperation(AnchorPane anchorPane, FadeTransition fadeTransition, boolean isShowing) {
    	fadeTransition.setNode(anchorPane);
        fadeTransition.setDuration(Duration.millis(1000));
        fadeTransition.setFromValue(isShowing ? 0.0 : 1.0);
        fadeTransition.setToValue(isShowing ? 1.0 : 0.0);
        anchorPane.setVisible(isShowing);
        fadeTransition.play();
    }

    private void showTransition(AnchorPane anchorPane) {
        transitionOperation(anchorPane, fadeIn, true);
    }

    private void hideTransition(AnchorPane anchorPane) {
        transitionOperation(anchorPane, fadeOut, false);
    }

    private void setImage() throws Exception {
        String path = "";
        path = path.replace("\\", "/");
        path = path.replace(" ", "%20");
        //path = "file:/" + path;
        path = ClassLoader.getSystemResource("images/Question.PNG").toExternalForm();
        System.out.println(path);

        imagePane.setStyle("-fx-background-image: url(\"" + path + "\"); " +
                "-fx-background-position: center center; " +
                "-fx-background-repeat: stretch;");

    }

    private void repeatSongs(){
        try{
            mediaView.getMediaPlayer().setOnRepeat(new Runnable() {
                @Override
                public void run() {
                    mediaView.getMediaPlayer().seek(isABInUse() ? Duration.seconds(aPointStampValue):Duration.ZERO);
                }
            });
            if(isAutoplay && isABInUse()) {
                //don't do this
                TimeUnit.MILLISECONDS.sleep(150);

                mediaView.getMediaPlayer().play();
            } else if(!isAutoplay && isABInUse()){
                pauseIcon();
            }
        } catch (Exception e){

        }

    }

    private void pauseSong() {
        mediaView.getMediaPlayer().setAutoPlay(true);
        pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaView.getMediaPlayer().pause();
                    pauseIcon();
                }
            }
        });
    }
}

