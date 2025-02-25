package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Juego extends SurfaceView implements SurfaceHolder.Callback {


    private BucleJuego bucle;

    // Personaje
    private Bitmap personaje;
    private float posPersonajeY;
    private float velY = 0;
    private final float GRAVEDAD = 1.5f;
    private final float SALTO = -20;

    // Suelo
    private Bitmap suelo;
    private float posSuelo1;     // X del primer suelo
    private float posSuelo2;     // X del segundo suelo
    private float velSuelo = 7f; // Velocidad de desplazamiento del suelo
    private float sueloY;        // Posición Y donde se dibuja el suelo

    public Juego(Context context) {
        super(context);
        getHolder().addCallback(this);

        // Hacer transparente el fondo del SurfaceView si deseas ver el fondo del layout
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        // Cargar recursos
        personaje = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
        // Escalar personaje (por ejemplo, a la mitad)
        personaje = Bitmap.createScaledBitmap(personaje,
                personaje.getWidth() / 8,
                personaje.getHeight() / 8,
                true);

        suelo = BitmapFactory.decodeResource(getResources(), R.drawable.suelo);

        // Iniciar posiciones
        posPersonajeY = 0;
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth(); // El segundo suelo va a continuación del primero
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Cuando la superficie esté lista, calcular posición inicial del personaje en el centro vertical
        int pantallaAlto = getHeight();
        posPersonajeY = (pantallaAlto - personaje.getHeight()) / 2f;

        // Posicionar el suelo en la parte inferior
        sueloY = getHeight() - suelo.getHeight();

        // Iniciar el bucle
        bucle = new BucleJuego(holder, this);
        bucle.setEnEjecucion(true);
        bucle.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Parar el bucle
        boolean retry = true;
        bucle.setEnEjecucion(false);
        while (retry) {
            try {
                bucle.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }

    // Lógica de actualización: se llama cada frame en el hilo
    public void actualizar() {
        // 1. Actualizar física del personaje (gravedad + salto)
        velY += GRAVEDAD;
        posPersonajeY += velY;

        // Evitar que salga por abajo
        float limiteInferior = getHeight() - personaje.getHeight();
        if (posPersonajeY > limiteInferior) {
            posPersonajeY = limiteInferior;
            velY = 0;
        }
        // Evitar que salga por arriba
        if (posPersonajeY < 0) {
            posPersonajeY = 0;
            velY = 0;
        }

        // 2. Mover suelos (scroll infinito)
        posSuelo1 -= velSuelo;
        posSuelo2 -= velSuelo;
        // Cuando el primer suelo salga de la pantalla, lo ponemos detrás del segundo
        if (posSuelo1 + suelo.getWidth() < 0) {
            posSuelo1 = posSuelo2 + suelo.getWidth();
        }
        // Y viceversa
        if (posSuelo2 + suelo.getWidth() < 0) {
            posSuelo2 = posSuelo1 + suelo.getWidth();
        }
    }

    public void renderizar(Canvas canvas) {
        if (canvas == null) return;

        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        // DIBUJAR PERSONAJE
        float personajeX = 100; // Ubicación en X
        canvas.drawBitmap(personaje, personajeX, posPersonajeY, null);

        // DIBUJAR SUELO (dos copias para scroll infinito)
        renderizarSuelo(canvas);
    }

    private void renderizarSuelo(Canvas canvas) {
        canvas.drawBitmap(suelo, posSuelo1, sueloY, null);
        canvas.drawBitmap(suelo, posSuelo2, sueloY, null);
    }

    // Detectar toque en pantalla para hacer saltar al personaje
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            velY = SALTO; // El personaje salta
            return true;
        }
        return super.onTouchEvent(event);
    }
}