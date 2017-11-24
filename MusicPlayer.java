package musicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MusicPlayer extends Application
{
    private MediaPlayer player;
    private Media media;
    private ArrayList<File> songs;
    TextField tfSong;
    Button btnPlay;
    Button btnPause;
    Button btnStop;
    Label lblRepeat;
    CheckBox cbRepeat;
    ListView<String> lvSongs;
    int count = 0;

    public static void main(String[] args) {
        MusicPlayer.launch((String[])args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music");
        
        
        showAlert(null, "Select a Directory");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File musicDir = directoryChooser.showDialog(primaryStage);
        if (musicDir == null) {
            System.exit(0);
        }
        songs = new ArrayList<File>(Arrays.asList(musicDir.listFiles()));
        pullDirectories(songs);
        
        
        tfSong = new TextField();
        btnPlay = new Button("Play");
        btnPause = new Button("||");
        btnStop = new Button("Stop");
        lblRepeat = new Label("Repeat: ");
        cbRepeat = new CheckBox();
        lvSongs = new ListView<>();
        
        tfSong.setEditable(false);
        
        
        for(int i = 0; i < songs.size(); i++) {
            if (isValidFormat(songs.get(i))) {
                lvSongs.getItems().add(songs.get(i).getName());
            } else {
                songs.remove(i);
                --i;
            }
        }
        
        Collections.sort(lvSongs.getItems());
        
        //create layouts
        BorderPane rootLayout = new BorderPane();
        GridPane controlLayout = new GridPane();
        for(int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20.0);
            controlLayout.getColumnConstraints().add(col);
        }
        for(int i = 0; i < 5; i++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(20.0);
            controlLayout.getRowConstraints().add(row);
        }
        controlLayout.getChildren().addAll(tfSong, btnPlay, btnPause, btnStop, lblRepeat, cbRepeat, lvSongs);
        GridPane.setHalignment(tfSong, HPos.CENTER);
        GridPane.setConstraints(btnPlay, 3, 3, 2, 1);
        GridPane.setConstraints(btnPause, 2, 3);
        GridPane.setConstraints(btnStop, 0, 3, 2, 1);
        GridPane.setConstraints(lblRepeat, 1, 0, 3, 1);
        GridPane.setHalignment(lblRepeat, HPos.RIGHT);
        GridPane.setConstraints(cbRepeat, 4, 0);
        rootLayout.setTop(tfSong);
        rootLayout.setLeft(lvSongs);
        rootLayout.setCenter(controlLayout);
        controlLayout.setStyle("-fx-background-color: linear-gradient(#00F 0%, #004 100%);");
        
        btnPlay.setOnAction(event -> {
            if (player != null)
            {
                if (player.getStatus().equals(MediaPlayer.Status.PAUSED))
                {
                    player.play();
                    return;
                }
                if (player.getStatus().equals(MediaPlayer.Status.PLAYING) || player.getStatus().equals(MediaPlayer.Status.READY))
                {
                    player.stop();
                    if (!cbRepeat.isSelected()) {
                    		//play next song if repeat is not selected
                        ++count;
                    }
                    if (count >= songs.size()) {
                        count = 0;
                    }
                }
            }
            playSong(songs.get(count));
            player.setOnEndOfMedia(() -> {
                btnPlay.fire();
            }
            );
        }
        );
        
        btnPause.setOnAction(event -> {
            if (player != null) {
                if (player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        }
        );
        
        btnStop.setOnAction(event -> {
        		if(player != null && player.getStatus().equals(MediaPlayer.Status.PLAYING))
        		{
        			player.stop();
        			tfSong.setText("");
        		}
        });
        
        lvSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String songName = (String)lvSongs.getSelectionModel().getSelectedItem();
                playSong(songName);
                player.setOnEndOfMedia(() -> {
                    if (cbRepeat.isSelected())
                    {
                        playSong(songName);
                    }
                }
                );
            }
        }
        );
        
        Scene scene = new Scene(rootLayout, 450, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void pullDirectories(ArrayList<File> files)
    {
        for (int i = 0; i < files.size(); i++)
        {
            if (files.get(i).isDirectory())
            {
                pullDirectories(files, files.get(i));
                files.remove(files.get(i));
                --i;
            }
        }
    }

    private void pullDirectories(ArrayList<File> files, File dir)
    {
        File[] fileArray = dir.listFiles();
        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].isDirectory()) {
                pullDirectories(files, fileArray[i]);
                files.remove(fileArray[i]);
            } else {
                files.add(fileArray[i]);
            }
        }
    }

    private void playSong(File song)
    {
    		if(player != null && player.getStatus().equals(MediaPlayer.Status.PLAYING)) player.stop();
        tfSong.setText(song.getName());
        media = new Media(song.toURI().toString());
        player = new MediaPlayer(media);
        player.play();
    }

    private void playSong(String songName)
    {
        for (int i = 0; i < songs.size(); i++)
        {
        	if (songs.get(i).getName().equals(songName)) 
        	{
        		if (player != null && player.getStatus().equals(MediaPlayer.Status.PLAYING))
        		{
                player.stop();
            }
                playSong(songs.get(i));
            }
        }
    }

    private boolean isValidFormat(File file) {
        if (!file.getName().endsWith(".mp3") && !file.getName().endsWith(".wav")) {
            return false;
        }
        return true;
    }
    
    private void showAlert(String title, String message)
	{
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}