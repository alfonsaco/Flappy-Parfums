package es.riberadeltajo.flappy_parfums;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // boton de prueba
    private ImageView btnJugar;
    private ImageView btnPersonaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnJugar=findViewById(R.id.btnJugar);
        btnPersonaje=findViewById(R.id.btnPersonaje);

        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, JuegoActivity.class);
                startActivity(intent);
                // Transición entre las actividades
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // Comenzaar animación del botón
                animarBoton(btnJugar);
            }
        });

        btnPersonaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animación botón
                animarBoton(btnPersonaje);
            }
        });

        animarColonia();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void animarColonia() {
        // Agregar frames del personaje
        AnimationDrawable animacionColonia;
        ImageView personaje=(ImageView) findViewById(R.id.imgPersonaje);
        personaje.setBackgroundResource(R.drawable.personaje_phantom);

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

    private void animarBoton(ImageView idBoton) {
        // Animación de Click en el botón
        AnimatorSet set=new AnimatorSet();
        ObjectAnimator pulsarBoton=ObjectAnimator.ofFloat(idBoton, "translationY", 10, 0);
        pulsarBoton.setDuration(100);

        set.play(pulsarBoton);
        set.start();
    }
}