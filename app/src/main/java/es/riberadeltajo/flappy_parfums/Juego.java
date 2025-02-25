package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Juego extends SurfaceView implements SurfaceHolder.Callback {


    private BucleJuego bucle;

    // Personaje
    private Bitmap[] framesPersonaje;
    private int frameIndex = 0; // Frame actual
    private long ultimoCambioFrame = 0; // Para temporizar
    private final int DURACION_FRAME = 100; // ms por frame
    private float posPersonajeY;
    private float velY = 0;
    private final float GRAVEDAD = 1.5f;
    private final float SALTO = -20;

    // Suelo
    private Bitmap suelo;
    private float posSuelo1;     // X del primer suelo
    private float posSuelo2;     // X del segundo suelo
    private float velSuelo = 7f; // Velocidad de desplazamiento del suelo
    private float sueloY;

    private int personajeAncho;
    private int personajeAlto;

    public Juego(Context context) {
        super(context);
        getHolder().addCallback(this);

        // Hacer transparente el fondo del SurfaceView si deseas ver el fondo del layout
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        framesPersonaje = new Bitmap[4];
        framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
        framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
        framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
        framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);

        // Escalar cada frame (por ejemplo, a 1/10 de su tamaño original)
        for (int i = 0; i < framesPersonaje.length; i++) {
            Bitmap original = framesPersonaje[i];
            int newWidth = original.getWidth() / 10;
            int newHeight = original.getHeight() / 10;
            framesPersonaje[i] = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
        }
        personajeAncho = framesPersonaje[0].getWidth();
        personajeAlto = framesPersonaje[0].getHeight();


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
        posPersonajeY = (pantallaAlto - personajeAlto) / 2f;

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

        long ahora = SystemClock.uptimeMillis();
        if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
            frameIndex = (frameIndex + 1) % framesPersonaje.length;
            ultimoCambioFrame = ahora;
        }

        velY += GRAVEDAD;
        posPersonajeY += velY;

        float limiteInferior = getHeight() - personajeAlto;
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

        float personajeX = 100;
        Bitmap frameActual = framesPersonaje[frameIndex];
        canvas.drawBitmap(frameActual, personajeX, posPersonajeY, null);

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