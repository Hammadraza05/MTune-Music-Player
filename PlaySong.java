package com.maddy.tech.mtunes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.maddy.tech.mtunes.databinding.ActivityMainBinding;
import com.maddy.tech.mtunes.databinding.ActivityPlaySongBinding;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler;
    private Runnable updateSeekRunnable;
    private TextView textView;
    private ImageView play, previous, next;
    private ArrayList<File> songs;
    private int position;
    private String textContent;
    private ActivityPlaySongBinding playSongBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playSongBinding = DataBindingUtil.setContentView(this,R.layout.activity_play_song);

        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        ArrayList<String> songPaths = bundle.getStringArrayList("songList");
        songs = new ArrayList<>();
        for (String path : songPaths) {
            songs.add(new File(path));
        }
        textContent = intent.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);
        position = intent.getIntExtra("position", 0);
        Uri uri = Uri.parse(songs.get(position).toString());

        mediaPlayer = MediaPlayer.create(this, uri);

        if (mediaPlayer != null) {
            mediaPlayer.start();
            play.setImageResource(R.drawable.pause); // Set the initial image to pause
            seekBar.setMax(mediaPlayer.getDuration());
            setupSeekBar();
            setupButtons();
        } else {
            Log.e("PlaySong", "MediaPlayer is null");
        }
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Optional: update UI or other things
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: handle touch start
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.seekTo(seekBar.getProgress());
                    } catch (IllegalStateException e) {
                        Log.e("PlaySong", "Error seeking MediaPlayer", e);
                    }
                }
            }
        });

        handler = new Handler(Looper.getMainLooper());
        updateSeekRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    try {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        handler.postDelayed(this, 800);
                    } catch (IllegalStateException e) {
                        Log.e("PlaySong", "Error updating SeekBar", e);
                    }
                }
            }
        };
        handler.post(updateSeekRunnable);
    }

    private void setupButtons() {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.play); // Change to play image
                } else {
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause); // Change to pause image
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != 0) {
                    position = position - 1;
                } else {
                    position = songs.size() - 1;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause); // Ensure the image is pause when playing
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName();
                textView.setText(textContent);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != songs.size() - 1) {
                    position = position + 1;
                } else {
                    position = 0;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause); // Ensure the image is pause when playing
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName();
                textView.setText(textContent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacks(updateSeekRunnable);
        }
        super.onDestroy();
    }
}
