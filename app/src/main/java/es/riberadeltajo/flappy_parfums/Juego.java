package es.riberadeltajo.flappy_parfums;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class Juego extends SurfaceView implements SurfaceHolder.Callback {
    private BucleJuego bucle;
    // Personaje
    private Bitmap[] framesPersonaje;
    private Bitmap gameOverBitmap;
    private int frameIndex = 0;
    private long ultimoCambioFrame = 0;
    private final int DURACION_FRAME = 100;
    private float posPersonajeY;
    private float velY = 0;
    private final float GRAVEDAD = 3f;
    private final float SALTO = -30;
    private int score = 0;
    private int maxTuberias = 15; // Número máximo de tuberías para ganar
    private boolean gano = false;

    // Suelo
    private Bitmap suelo;
    private float posSuelo1;
    private float posSuelo2;
    private final float velSuelo = 7f;
    private float sueloY;

    private ArrayList<Tuberia> tuberias;
    private final float TIEMPO_ENTRE_TUBERIAS = 600f;
    private boolean gameOver = false;

    private int personajeAncho;
    private int personajeAlto;
    private Rect rectPersonaje;

    // Variables para el power-up
    private int totalTuberiasGeneradas = 0;
    private int invisibilidadTuberiasRestantes = 0; // Tuberías con invisibilidad activa

    // Variables de estado del juego
    // gameStarted se activa con el primer click; el suelo se mueve desde el inicio, salvo cuando muere.
    private boolean gameStarted = false;
    // Para que el sonido de muerte se reproduzca solo una vez.
    private boolean deathSoundPlayed = false;

    // Imagen que se mostrará al inicio
    private Bitmap imagenHola;

    // Variable para almacenar el AnimatorSet de la animación de "volar"
    private AnimatorSet animSet;

    public Juego(Context context, int idPersonaje) {
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        establecerPersonaje(idPersonaje);

        // Escalar los frames del personaje
        for (int i = 0; i < framesPersonaje.length; i++) {
            framesPersonaje[i] = Bitmap.createScaledBitmap(
                    framesPersonaje[i],
                    framesPersonaje[i].getWidth() / 13,
                    framesPersonaje[i].getHeight() / 13,
                    true);
        }
        personajeAncho = framesPersonaje[0].getWidth();
        personajeAlto = framesPersonaje[0].getHeight();

        suelo = BitmapFactory.decodeResource(getResources(), R.drawable.suelo);
        // La posición vertical se ajustará en surfaceCreated
        posPersonajeY = 0;

        tuberias = new ArrayList<>();
        rectPersonaje = new Rect();

        imagenHola = BitmapFactory.decodeResource(getResources(), R.drawable.message);

        gameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
    }

    // Función para establecer el personaje según el id
    private void establecerPersonaje(int idPersonaje) {
        if(idPersonaje == R.drawable.personaje_phantom) {
            framesPersonaje = new Bitmap[4];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);
        } else if(idPersonaje == R.drawable.personaje_azzaro) {
            framesPersonaje = new Bitmap[5];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro4);
            framesPersonaje[4] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro5);
        } else if(idPersonaje == R.drawable.personaje_stronger) {
            framesPersonaje = new Bitmap[4];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.stronger1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.stronger2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.stronger3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.stronger4);
        } else {
            framesPersonaje = new Bitmap[4];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);
        }
    }

    // Getters y Setters para la propiedad que animaremos
    public float getPosPersonajeY() {
        return posPersonajeY;
    }
    public void setPosPersonajeY(float pos) {
        this.posPersonajeY = pos;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int pantallaAncho = getWidth();
        int pantallaAlto = getHeight();

        // Escalar el suelo
        int anchoOriginal = suelo.getWidth();
        int altoOriginal = suelo.getHeight();
        float factorEscala = (float) pantallaAncho / anchoOriginal;
        int nuevoAncho = pantallaAncho;
        int nuevoAlto = (int) (altoOriginal * factorEscala);
        suelo = Bitmap.createScaledBitmap(suelo, nuevoAncho, nuevoAlto, true);
        sueloY = pantallaAlto - suelo.getHeight();
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth();

        // Posicionar al personaje en el centro vertical desde el inicio
        posPersonajeY = (pantallaAlto - personajeAlto) / 2f;

        // Inicia una animación cíclica para darle efecto de "volar" en espera
        animSet = new AnimatorSet();
        ObjectAnimator volar = ObjectAnimator.ofFloat(this, "posPersonajeY", posPersonajeY - 40, posPersonajeY);
        volar.setDuration(400);
        volar.setRepeatCount(ObjectAnimator.INFINITE);
        volar.setRepeatMode(ObjectAnimator.REVERSE);
        animSet.play(volar);
        animSet.start();

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
            } catch (InterruptedException e) { }
        }
    }

    public void actualizar() {
        // Actualizamos el movimiento del suelo solo si el juego no ha terminado
        if (!gameOver) {
            posSuelo1 -= velSuelo;
            posSuelo2 -= velSuelo;
            if (posSuelo1 + suelo.getWidth() < 0) {
                posSuelo1 = posSuelo2 + suelo.getWidth();
            }
            if (posSuelo2 + suelo.getWidth() < 0) {
                posSuelo2 = posSuelo1 + suelo.getWidth();
            }
        }

        // Si el juego no ha empezado, solo actualizamos los frames
        if (!gameStarted) {
            long ahora = System.currentTimeMillis();
            if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
                frameIndex = (frameIndex + 1) % framesPersonaje.length;
                ultimoCambioFrame = ahora;
            }
            return;
        }

        // Si el juego ha terminado, reproducimos el sonido de muerte solo si no se ganó
        if (gameOver) {
            if (!gano && !deathSoundPlayed) {
                reproducirAudio(R.raw.morir);
                deathSoundPlayed = true;
            }
            return;
        }

        // Actualizar física del personaje
        velY += GRAVEDAD;
        posPersonajeY += velY;

        long ahora = System.currentTimeMillis();
        if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
            frameIndex = (frameIndex + 1) % framesPersonaje.length;
            ultimoCambioFrame = ahora;
        }

        // Límites del personaje
        if (posPersonajeY <= 0) {
            posPersonajeY = 0;
            gameOver = true;
        }
        float limiteInferior = getHeight() - suelo.getHeight() - personajeAlto;
        if (posPersonajeY >= limiteInferior) {
            posPersonajeY = limiteInferior;
            gameOver = true;
        }

        // Actualizar rectángulo del personaje (posición fija en X)
        float personajeX = 100;
        rectPersonaje.set((int) personajeX, (int) posPersonajeY,
                (int) (personajeX + personajeAncho), (int) (posPersonajeY + personajeAlto));

        // Procesar tuberías y colisiones
        generarTuberias();
        actualizarTuberias();
        chequearColisiones();
        chequearPowerUp();
        chequearPasoTuberias();

        for (Tuberia t : tuberias) {
            t.setVelocidad(velSuelo);
            t.actualizar();
        }
    }

    public void renderizar(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // Si el juego no ha empezado, mostramos la imagen de inicio
        if (!gameStarted) {
            float personajeX = 100;
            Bitmap frameActual = framesPersonaje[frameIndex];
            canvas.drawBitmap(frameActual, personajeX, posPersonajeY, null);
            float holaX = (getWidth() - imagenHola.getWidth()) / 2f;
            float holaY = (getHeight() - imagenHola.getHeight()) / 2f;
            canvas.drawBitmap(imagenHola, holaX, holaY, null);
            renderizarSuelo(canvas);
            return;
        }

        // Renderizado normal del juego
        float personajeX = 100;
        Paint paint = new Paint();
        for (Tuberia t : tuberias) {
            t.dibujar(canvas);
        }
        renderizarSuelo(canvas);

        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.numbers);
        paint.setTypeface(typeface);
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setShadowLayer(1, 10, 10, Color.BLACK);
        canvas.drawText("" + score, 500, 350, paint);

        Bitmap frameActual = framesPersonaje[frameIndex];
        Paint personajePaint = new Paint();
        if (invisibilidadTuberiasRestantes > 0) {
            personajePaint.setAlpha(100);
        } else {
            personajePaint.setAlpha(255);
        }
        canvas.drawBitmap(frameActual, personajeX, posPersonajeY, personajePaint);

        // Si el juego ha terminado, mostramos el mensaje de victoria o la imagen de game over
        if (gameOver) {
            if (gano) {
                String mensaje = "¡Ganaste!";
                paint.setTextSize(100);
                float textWidth = paint.measureText(mensaje);
                float x = (getWidth() - textWidth) / 2f;
                float y = (getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
                canvas.drawText(mensaje, x, y, paint);
            } else {
                float x = (getWidth() - gameOverBitmap.getWidth()) / 2f;
                float y = (getHeight() - gameOverBitmap.getHeight()) / 2f;
                canvas.drawBitmap(gameOverBitmap, x, y, null);
            }
        }
    }

    private void generarTuberias() {
        if (score >= maxTuberias - 2) return;
        float anchoPantalla = getWidth();
        if (tuberias.isEmpty()) {
            totalTuberiasGeneradas++;
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
        if (invisibilidadTuberiasRestantes > 0) return;
        for (Tuberia t : tuberias) {
            if (t.colisionaCon(rectPersonaje)) {
                if (!gameOver) {
                    gameOver = true;
                }
                break;
            }
        }
    }

    private void chequearPowerUp() {
        for (Tuberia t : tuberias) {
            if (t.hasPowerUp() && !t.isPowerUpCollected() && Rect.intersects(rectPersonaje, t.getRectPowerUp())) {
                t.setPowerUpCollected(true);
                invisibilidadTuberiasRestantes = 4;
            }
        }
    }

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

    // El primer click inicia el juego y actúa como salto; posteriormente, cada toque hace saltar
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!gameStarted) {
                gameStarted = true;
                // Paramos la animación de volar una vez que se haya dado el primer Click
                if (animSet != null && animSet.isRunning()) {
                    animSet.cancel();
                }
                velY = SALTO;
                reproducirAudio(R.raw.spray);
            } else if (!gameOver) {
                velY = SALTO;
                reproducirAudio(R.raw.spray);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void reproducirAudio(int idAudio) {
        MediaPlayer audio = MediaPlayer.create(getContext(), idAudio);
        if (audio != null) {
            audio.setOnCompletionListener(mp -> mp.release());
            audio.start();
        }
    }
}
