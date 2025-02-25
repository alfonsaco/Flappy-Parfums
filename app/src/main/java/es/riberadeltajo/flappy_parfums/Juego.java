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
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;


public class Juego extends SurfaceView implements SurfaceHolder.Callback {
    private BucleJuego bucle;
    // Personaje
    private Bitmap[] framesPersonaje;
    private int frameIndex = 0;
    private long ultimoCambioFrame = 0;
    private final int DURACION_FRAME = 100;
    private float posPersonajeY;
    private float velY = 0;
    private float GRAVEDAD = 3f;
    private float SALTO = -30;
    private int score = 0;
    private int maxTuberias = 15; // Número máximo de tuberías para ganar
    private boolean gano = false;

    // Suelo
    private Bitmap suelo;
    private float posSuelo1;
    private float posSuelo2;
    private float velSuelo = 7f;
    private float sueloY;

    private ArrayList<Tuberia> tuberias;
    private float TIEMPO_ENTRE_TUBERIAS = 600f;
    private boolean gameOver = false;

    private int personajeAncho;
    private int personajeAlto;
    private Rect rectPersonaje;

    // Nuevas variables para el power-up
    private int totalTuberiasGeneradas = 0;
    private int invisibilidadTuberiasRestantes = 0; // Cantidad de tuberías restantes con invisibilidad

    public Juego(Context context) {
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        framesPersonaje = new Bitmap[4];
        framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
        framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
        framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
        framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);

        for (int i = 0; i < framesPersonaje.length; i++) {
            framesPersonaje[i] = Bitmap.createScaledBitmap(
                    framesPersonaje[i],
                    framesPersonaje[i].getWidth() / 11,
                    framesPersonaje[i].getHeight() / 11,
                    true);
        }
        personajeAncho = framesPersonaje[0].getWidth();
        personajeAlto = framesPersonaje[0].getHeight();

        suelo = BitmapFactory.decodeResource(getResources(), R.drawable.suelo);
        posPersonajeY = 0;
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth();
        tuberias = new ArrayList<>();
        rectPersonaje = new Rect();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int pantallaAncho = getWidth();
        int pantallaAlto = getHeight();

        int anchoOriginal = suelo.getWidth();
        int altoOriginal = suelo.getHeight();
        float factorEscala = (float) pantallaAncho / (float) anchoOriginal;
        int nuevoAncho = pantallaAncho;
        int nuevoAlto = (int) (altoOriginal * factorEscala);
        suelo = Bitmap.createScaledBitmap(suelo, nuevoAncho, nuevoAlto, true);
        sueloY = pantallaAlto - suelo.getHeight();
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth();
        posPersonajeY = (pantallaAlto - personajeAlto) / 2f;

        bucle = new BucleJuego(holder, this);
        bucle.setEnEjecucion(true);
        bucle.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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
        if (gameOver) return;

        // Actualizar física del personaje
        velY += GRAVEDAD;
        posPersonajeY += velY;

        long ahora = System.currentTimeMillis();
        if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
            frameIndex = (frameIndex + 1) % framesPersonaje.length;
            ultimoCambioFrame = ahora;
        }

        /*
            MUERTES DEL PERSONAJE
         */
        // Muerte por tocar el techo
        if (posPersonajeY <= 0) {
            posPersonajeY = 0;
            gameOver = true;
            reproducirAudio(R.raw.morir);
        }
        // Muerte por tocar el suelo
        float limiteInferior = getHeight() - suelo.getHeight() - personajeAlto;
        if (posPersonajeY >= limiteInferior) {
            posPersonajeY = limiteInferior;
            gameOver = true;
            reproducirAudio(R.raw.morir);
        }

        // Movimiento del suelo
        posSuelo1 -= velSuelo;
        posSuelo2 -= velSuelo;
        if (posSuelo1 + suelo.getWidth() < 0) {
            posSuelo1 = posSuelo2 + suelo.getWidth();
        }
        if (posSuelo2 + suelo.getWidth() < 0) {
            posSuelo2 = posSuelo1 + suelo.getWidth();
        }

        // Actualizar rectángulo del personaje
        float personajeX = 100;
        rectPersonaje.set(
                (int) personajeX,
                (int) posPersonajeY,
                (int) (personajeX + personajeAncho),
                (int) (posPersonajeY + personajeAlto)
        );

        generarTuberias();
        actualizarTuberias();
        chequearColisiones();
        chequearPowerUp(); // Comprobar si se recoge el power-up
        chequearPasoTuberias();

        for (Tuberia t : tuberias) {
            t.setVelocidad(velSuelo);
            t.actualizar();
        }
    }

    public void renderizar(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        float personajeX = 100;
        Paint paint = new Paint();

        // Dibujar las tuberías y el suelo normalmente
        for (Tuberia t : tuberias) {
            t.dibujar(canvas);
        }
        renderizarSuelo(canvas);

        // Cambiar la fuente del contador de puntos
        Typeface typeface= ResourcesCompat.getFont(getContext(), R.font.numbers);
        paint.setTypeface(typeface);

        // Mostrar el puntaje
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        // Sombra de la fuente
        paint.setShadowLayer(1, 10, 10, Color.BLACK);
        canvas.drawText("" + score, 500, 350, paint);

        // Dibujar el personaje con opacidad reducida si el power-up está activo
        Bitmap frameActual = framesPersonaje[frameIndex];
        Paint personajePaint = new Paint();
        if (invisibilidadTuberiasRestantes > 0) {
            // Valor alfa menor para simular invisibilidad (0 = transparente, 255 = opaco)
            personajePaint.setAlpha(100);
        } else {
            personajePaint.setAlpha(255);
        }
        canvas.drawBitmap(frameActual, personajeX, posPersonajeY, personajePaint);

        // Mensaje de victoria
        if (gameOver && gano) {
            paint.setTextSize(100);
            canvas.drawText("¡Ganaste!", getWidth() / 2 - 150, getHeight() / 2, paint);
        }
    }


    private void generarTuberias() {
        // Si ya se alcanzó el puntaje para ganar, no se generan más tuberías
        if (score >= maxTuberias - 2) return;

        float anchoPantalla = getWidth();
        if (tuberias.isEmpty()) {
            totalTuberiasGeneradas++;
            // Solo la décima tubería tendrá el power-up
            boolean esPowerUp = (totalTuberiasGeneradas == 10);
            tuberias.add(new Tuberia(getContext(), anchoPantalla + TIEMPO_ENTRE_TUBERIAS, sueloY, velSuelo, 350, esPowerUp));
            return;
        }
        Tuberia ultimaTuberia = tuberias.get(tuberias.size() - 1);
        if (ultimaTuberia.getX() + ultimaTuberia.getAncho() < anchoPantalla) {
            totalTuberiasGeneradas++;
            boolean esPowerUp = (totalTuberiasGeneradas == 10);
            float nuevaPosX = ultimaTuberia.getX() + ultimaTuberia.getAncho() + TIEMPO_ENTRE_TUBERIAS;
            tuberias.add(new Tuberia(getContext(), nuevaPosX, sueloY, velSuelo, 350, esPowerUp));
        }
    }

    private void actualizarTuberias() {
        for (int i = tuberias.size() - 1; i >= 0; i--) {
            Tuberia t = tuberias.get(i);
            t.actualizar();
            if (t.fueraDePantalla()) {
                tuberias.remove(i);
            }
        }
    }

    private void chequearColisiones() {
        // Si el power-up de invisibilidad está activo, se ignoran las colisiones con las tuberías.
        if (invisibilidadTuberiasRestantes > 0) return;

        // Crear el rectángulo del personaje (ya se actualizó en actualizar())
        // Muerte por chocar con la tubería
        for (Tuberia t : tuberias) {
            if (t.colisionaCon(rectPersonaje)) {
                gameOver = true;
                reproducirAudio(R.raw.morir);
                break;
            }
        }
    }


    // Comprueba si el personaje recoge el power-up en la décima tubería
    private void chequearPowerUp() {
        for (Tuberia t : tuberias) {
            if (t.hasPowerUp() && !t.isPowerUpCollected() && Rect.intersects(rectPersonaje, t.getRectPowerUp())) {
                t.setPowerUpCollected(true);
                invisibilidadTuberiasRestantes = 4; // Activa invisibilidad durante las 3 tuberías siguientes
            }
        }
    }

    // Al pasar una tubería, se incrementa el puntaje y se descuenta el contador de invisibilidad si está activo
    private void chequearPasoTuberias() {
        float personajeX = 100;
        for (Tuberia t : tuberias) {
            float tuberiaXFinal = t.getX() + t.getAncho();
            if (!t.isPuntoSumado() && tuberiaXFinal < personajeX) {
                score++;
                t.setPuntoSumado(true);
                reproducirAudio(R.raw.point);

                if (invisibilidadTuberiasRestantes > 0) {
                    invisibilidadTuberiasRestantes--;
                }
                if (score >= maxTuberias) {
                    gano = true;
                    gameOver = true;
                }
                break;
            }
        }
    }

    private void renderizarSuelo(Canvas canvas) {
        canvas.drawBitmap(suelo, posSuelo1, sueloY, null);
        canvas.drawBitmap(suelo, posSuelo2, sueloY, null);
    }

    // Método para que al pulsar en la pantalla, la colonia salte
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            velY = SALTO;
            reproducirAudio(R.raw.spray);

            return true;
        }
        return super.onTouchEvent(event);
    }

    // Método para reproducir audio
    public void reproducirAudio(int idAudio) {
        MediaPlayer audio=MediaPlayer.create(getContext(), idAudio);
        if(audio != null) {
            audio.start();
        }
    }
}
