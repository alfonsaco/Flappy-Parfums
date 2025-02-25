package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class Juego extends SurfaceView implements SurfaceHolder.Callback {
    private BucleJuego bucle;

    // Personaje
    private Bitmap[] framesPersonaje;
    private int frameIndex = 0; // Frame actual
    private long ultimoCambioFrame = 0; // Para temporizar
    private final int DURACION_FRAME = 100; // ms por frame
    private float posPersonajeY;
    private float velY = 0;
    private float GRAVEDAD = 3f;
    private float SALTO = -30;

    // Suelo
    private Bitmap suelo;
    private float posSuelo1;     // X del primer suelo
    private float posSuelo2;     // X del segundo suelo
    private float velSuelo = 7f; // Velocidad de desplazamiento del suelo
    private float sueloY;


    private ArrayList<Tuberia> tuberias;
    private float TIEMPO_ENTRE_TUBERIAS = 2500;
    private long ultimoSpawn = 0;
    private boolean gameOver = false;

    private int personajeAncho;
    private int personajeAlto;
    private Rect rectPersonaje;

    private int score = 0;

    private float factorVelocidad;

    public Juego(Context context) {
        super(context);
        getHolder().addCallback(this);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float densidadPantalla = metrics.density;  // Factor de densidad (dpi base = 160)
        int anchoPantalla = metrics.widthPixels;   // Ancho de pantalla en píxeles
        int altoPantalla = metrics.heightPixels;   // Alto de pantalla en píxeles

        factorVelocidad = (densidadPantalla + anchoPantalla / 2000f) / 4f;

        // Aplicar a variables del juego
        velSuelo = 7 * factorVelocidad;
        GRAVEDAD = 3f * factorVelocidad;
        SALTO = -30 * factorVelocidad;
        TIEMPO_ENTRE_TUBERIAS = (int) (2500 / factorVelocidad);

        // Hacer transparente el fondo del SurfaceView si deseas ver el fondo del layout
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        framesPersonaje = new Bitmap[4];
        framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
        framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
        framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
        framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);

        // Escalar cada frame (por ejemplo, a 1/10 de su tamaño original)
        for (int i=0; i<framesPersonaje.length; i++) {
            framesPersonaje[i] = Bitmap.createScaledBitmap(framesPersonaje[i],
                    framesPersonaje[i].getWidth() / 11,
                    framesPersonaje[i].getHeight() / 11,
                    true);
        }
        personajeAncho = framesPersonaje[0].getWidth();
        personajeAlto = framesPersonaje[0].getHeight();


        suelo = BitmapFactory.decodeResource(getResources(), R.drawable.suelo);

        // Iniciar posiciones
        posPersonajeY = 0;
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth(); // El segundo suelo va a continuación del primero

        tuberias = new ArrayList<>();
        rectPersonaje = new Rect();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int pantallaAncho = getWidth();
        int pantallaAlto = getHeight();

        // Escalar el suelo al ancho de pantalla manteniendo proporción
        int anchoOriginal = suelo.getWidth();
        int altoOriginal  = suelo.getHeight();
        float factorEscala = (float)pantallaAncho / (float)anchoOriginal;

        int nuevoAncho = pantallaAncho;
        int nuevoAlto  = (int)(altoOriginal * factorEscala);

        // Reemplazamos 'suelo' por la versión escalada
        suelo = Bitmap.createScaledBitmap(suelo, nuevoAncho, nuevoAlto, true);

        // Ajustar Y del suelo según su nueva altura
        sueloY = pantallaAlto - suelo.getHeight();

        // Ajustar posSuelo1 y posSuelo2 con el nuevo ancho
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth(); // El segundo suelo va al final del primero

        // Posicionar el personaje en el centro vertical
        posPersonajeY = (pantallaAlto - personajeAlto) / 2f;

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

    public void actualizar() {
        if (gameOver) {
            return;
        }

        // Gravedad
        velY += GRAVEDAD;
        posPersonajeY += velY;



        long ahora = System.currentTimeMillis();
        if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
            frameIndex = (frameIndex + 1) % framesPersonaje.length;
            ultimoCambioFrame = ahora;
        }

        // Si quieres que también pierda al tocar el techo:
        if (posPersonajeY <= 0) {
            posPersonajeY = 0;
            gameOver = true;
        }


        float limiteInferior = getHeight() - suelo.getHeight() - personajeAlto;
        if (posPersonajeY >= limiteInferior) {
            posPersonajeY = limiteInferior;
            gameOver = true;
        }

        //Movimiento del suelo
        posSuelo1 -= velSuelo;
        posSuelo2 -= velSuelo;
        if (posSuelo1 + suelo.getWidth() < 0) {
            posSuelo1 = posSuelo2 + suelo.getWidth();
        }
        if (posSuelo2 + suelo.getWidth() < 0) {
            posSuelo2 = posSuelo1 + suelo.getWidth();
        }

        //Generar las tuberias y actualizarlas
        generarTuberias();
        actualizarTuberias();
        chequearColisiones();

        for (Tuberia t : tuberias) {
            t.setVelocidad(velSuelo); // Ajusta la velocidad de desplazamiento
            t.actualizar();
        }


    }

    public void renderizar(Canvas canvas) {
        if (canvas == null) return;

        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        //dibujar el personaje
        float personajeX = 100;
        Bitmap frameActual = framesPersonaje[frameIndex];
        canvas.drawBitmap(frameActual, personajeX, posPersonajeY, null);

        for (Tuberia t : tuberias) {
            t.dibujar(canvas);
        }

        //dibujar el suelo
        renderizarSuelo(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(70);

        // Dibuja en la parte superior izquierda
        canvas.drawText( ""+score,500 , 300, paint);

    }

    private void generarTuberias() {
        long ahora = SystemClock.uptimeMillis();
        if (ahora - ultimoSpawn > TIEMPO_ENTRE_TUBERIAS) {

            // Aumenta el valor del gap, por ejemplo a 350
            Tuberia t = new Tuberia(
                    getContext(),
                    getWidth(),
                    sueloY,    // groundY
                    velSuelo,
                    350
            );
            tuberias.add(t);
            ultimoSpawn = ahora;
        }
    }


    private void actualizarTuberias() {
        for (int i = 0; i < tuberias.size(); i++) {
            Tuberia t = tuberias.get(i);
            t.actualizar();
            if (t.fueraDePantalla()) {
                tuberias.remove(i);
                i--;
            }
        }
    }

    private void chequearColisiones() {
        // Crear rect del personaje
        float personajeX = 100;
        rectPersonaje.set(
                (int) personajeX,
                (int) posPersonajeY,
                (int) (personajeX + personajeAncho),
                (int) (posPersonajeY + personajeAlto)
        );

        // Revisar colisión con tuberías
        for (Tuberia t : tuberias) {
            if (t.colisionaCon(rectPersonaje)) {
                gameOver = true;
                break;
            }
        }
        for (Tuberia t : tuberias) {
            // Si la tubería NO sumó punto todavía, y ya quedó detrás del personaje
            float tuberiaXFinal = t.getX() + t.getAncho();
            if (!t.isPuntoSumado() && tuberiaXFinal < personajeX) {
                // El personaje cruzó la tubería
                score++;
                t.setPuntoSumado(true);
            }
        }

    }



    private void renderizarSuelo(Canvas canvas) {
        canvas.drawBitmap(suelo, posSuelo1, sueloY, null);
        canvas.drawBitmap(suelo, posSuelo2, sueloY, null);
    }

    // Detectar toque en pantalla para hacer saltar al personaje
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            velY = SALTO; // El personaje salta
            return true;
        }
        return super.onTouchEvent(event);
    }
}