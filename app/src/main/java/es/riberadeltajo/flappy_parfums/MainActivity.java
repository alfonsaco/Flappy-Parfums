package es.riberadeltajo.flappy_parfums;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // boton de prueba
    private ImageView btnJugar;
    private ImageView btnPersonaje;

    int personajeElegido=R.drawable.personaje_phantom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnJugar=findViewById(R.id.btnJugar);
        btnPersonaje=findViewById(R.id.btnPersonaje);

        // Canción menú
        reproducirAudio(R.raw.menu_theme1);

        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, JuegoActivity.class);
                intent.putExtra("personaje", personajeElegido);
                startActivity(intent);

                // Transición entre las actividades
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // Comenzaar animación del botón
                animarBoton(btnJugar);
                reproducirAudio(R.raw.start);
            }
        });

        btnPersonaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animación botón
                animarBoton(btnPersonaje);
                reproducirAudio(R.raw.start);

                mostrarDialogoPersonajes();
            }
        });

        animarColonia(personajeElegido);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void animarColonia(int idPersonaje) {
        // Agregar frames del personaje
        AnimationDrawable animacionColonia;
        ImageView personaje=(ImageView) findViewById(R.id.imgPersonaje);
        personaje.setBackgroundResource(idPersonaje);

        animacionColonia=(AnimationDrawable) personaje.getBackground();
        animacionColonia.start();

        // Agregar efectos al personaje
        AnimatorSet set=new AnimatorSet();
        ObjectAnimator volar=ObjectAnimator.ofFloat(personaje, "translationY", -20f, 20f);
        volar.setDuration(400);
        volar.setRepeatCount(ObjectAnimator.INFINITE);
        volar.setRepeatMode(ObjectAnimator.REVERSE);

        set.play(volar);
        set.start();
    }

    // Método para las animaciones del botón
    private void animarBoton(ImageView idBoton) {
        // Animación de Click en el botón
        AnimatorSet set=new AnimatorSet();
        ObjectAnimator pulsarBoton=ObjectAnimator.ofFloat(idBoton, "translationY", 10, 0);
        pulsarBoton.setDuration(100);

        set.play(pulsarBoton);
        set.start();
    }

    // Método para reproducir sonidos
    public void reproducirAudio(int idAudio) {
        MediaPlayer audio=MediaPlayer.create(this, idAudio);
        if(audio != null) {
            // Libera los recursos cuando termine el audio
            audio.setOnCompletionListener(mp -> {
                mp.release();
            });
            audio.start();
        }
    }

    private void mostrarDialogoPersonajes(){
        // Construir el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.seleccion_personaje, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        ImageView imagenPhantom=dialogView.findViewById(R.id.imagenPhantom);
        ImageView imgenAzzaro=dialogView.findViewById(R.id.imagenAzzaro);
        ImageView imagenStronger=dialogView.findViewById(R.id.imagenStronger);

        LinearLayout layoutPhantom=dialogView.findViewById(R.id.personajePhantomElegido);
        LinearLayout layoutAzzaro=dialogView.findViewById(R.id.personajeAzzaroElegido);
        LinearLayout layoutStronger=dialogView.findViewById(R.id.personajeStrongerElegido);

        layoutAzzaro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personajeElegido=R.drawable.personaje_azzaro;
                animarColonia(personajeElegido);
            }
        });
        layoutStronger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personajeElegido=R.drawable.personaje_stronger;
                animarColonia(personajeElegido);
            }
        });
        layoutPhantom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personajeElegido=R.drawable.personaje_phantom;
                animarColonia(personajeElegido);
            }
        });

        dialog.show();
    }

}