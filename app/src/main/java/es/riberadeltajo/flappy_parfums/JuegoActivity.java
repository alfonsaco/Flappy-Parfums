package es.riberadeltajo.flappy_parfums;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class JuegoActivity extends AppCompatActivity {

    private Juego juego;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_juego);

        ConstraintLayout layoutPrincipal = findViewById(R.id.main);

        // Se obtiene el personaje elegido
        SharedPreferences sp = getSharedPreferences("MisPuntuaciones", MODE_PRIVATE);
        int personaje = sp.getInt("personajeSeleccionado", R.drawable.personaje_phantom);


        // Crea e inserta el SurfaceView "Juego". Se le pasa como parámetro el personaje, para
        // poder utilizarlo en "Juego"
        juego = new Juego(this, personaje);
        FrameLayout contenedor = new FrameLayout(this);
        contenedor.addView(juego);
        layoutPrincipal.addView(contenedor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Mostrar la transición también al volver a la MainActivity
    @Override
    public void finish () {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
