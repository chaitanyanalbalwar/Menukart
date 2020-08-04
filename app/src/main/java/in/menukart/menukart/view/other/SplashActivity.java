package in.menukart.menukart.view.other;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.bitmap.CenterInside;

import java.io.IOException;
import java.io.InputStream;

import in.menukart.menukart.R;


public class SplashActivity extends AppCompatActivity {

    VideoView videoHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

       /* try {

            videoHolder = findViewById(R.id.videiView);
           // setContentView(videoHolder);
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
            videoHolder.setVideoURI(video);

            videoHolder.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    jump();
                }
            });
            videoHolder.start();

        }catch (Exception e){
            e.printStackTrace();
        }*/

       /* InputStream stream = null;
        try {
            stream = getAssets().open("piggy.gif");
        } catch (IOException e) {
            e.printStackTrace();
        }
        GifWebView view = new GifWebView(this, "file:///android_asset    /piggy.gif");

        setContentView(view);*/

    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        jump();
        return true;
    }

    private void jump() {
        if (isFinishing())
            return;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }*/
}