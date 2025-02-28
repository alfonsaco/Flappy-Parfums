package es.riberadeltajo.flappy_parfums;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private Bitmap dialogoScoreBitmap; // Recuadro de score (para cuando se pierde)
    private Bitmap restartBitmap;      // Botón de reinicio (restart.png)
    private Bitmap menuBitmap;         // Botón para ir al menú (menu.png)

    private int frameIndex = 0;
    private long ultimoCambioFrame = 0;
    private final int DURACION_FRAME = 100;
    private float posPersonajeY;
    private float velY = 0;
    private final float GRAVEDAD = 3f;
    private final float SALTO = -30;
    private int score = 0;
    private int maxTuberias = 15;
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

    // Power-up
    private int totalTuberiasGeneradas = 0;
    private int invisibilidadTuberiasRestantes = 0;

    // Estado del juego
    private boolean gameStarted = false;
    private boolean deathSoundPlayed = false;

    // Imagen de inicio
    private Bitmap imagenHola;

    // Animación "volar"
    private AnimatorSet animSet;

    // SharedPreferences para la mejor puntuación
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MisPuntuaciones";
    private static final String KEY_BEST_SCORE = "mejorPuntuacion";
    private int bestScore = 0;

    public Juego(Context context, int idPersonaje) {
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        establecerPersonaje(idPersonaje);

        // Escalar frames del personaje
        for (int i = 0; i < framesPersonaje.length; i++) {
            framesPersonaje[i] = Bitmap.createScaledBitmap(
                    framesPersonaje[i],
                    framesPersonaje[i].getWidth() / 13,
                    framesPersonaje[i].getHeight() / 13,
                    true
            );
        }
        personajeAncho = framesPersonaje[0].getWidth();
        personajeAlto = framesPersonaje[0].getHeight();

        suelo = BitmapFactory.decodeResource(getResources(), R.drawable.suelo);
        posPersonajeY = 0;

        tuberias = new ArrayList<>();
        rectPersonaje = new Rect();

        imagenHola = BitmapFactory.decodeResource(getResources(), R.drawable.message);

        gameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
        dialogoScoreBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dialogo_score);
        restartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.restart);
        menuBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.menu);

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0);
    }

    private void establecerPersonaje(int idPersonaje) {
        if (idPersonaje == R.drawable.personaje_phantom) {
            framesPersonaje = new Bitmap[4];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.phantom4);
        } else if (idPersonaje == R.drawable.personaje_azzaro) {
            framesPersonaje = new Bitmap[5];
            framesPersonaje[0] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro1);
            framesPersonaje[1] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro2);
            framesPersonaje[2] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro3);
            framesPersonaje[3] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro4);
            framesPersonaje[4] = BitmapFactory.decodeResource(getResources(), R.drawable.azzaro5);
        } else if (idPersonaje == R.drawable.personaje_stronger) {
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int pantallaAncho = getWidth();
        int pantallaAlto = getHeight();
        int anchoOriginal = suelo.getWidth();
        int altoOriginal = suelo.getHeight();
        float factorEscala = (float) pantallaAncho / anchoOriginal;
        int nuevoAncho = pantallaAncho;
        int nuevoAlto = (int) (altoOriginal * factorEscala);
        suelo = Bitmap.createScaledBitmap(suelo, nuevoAncho, nuevoAlto, true);
        sueloY = pantallaAlto - suelo.getHeight();
        posSuelo1 = 0;
        posSuelo2 = suelo.getWidth();
        posPersonajeY = (pantallaAlto - personajeAlto) / 2f;

        animSet = new AnimatorSet();
        @SuppressLint("ObjectAnimatorBinding")
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

        if (!gameStarted) {
            long ahora = System.currentTimeMillis();
            if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
                frameIndex = (frameIndex + 1) % framesPersonaje.length;
                ultimoCambioFrame = ahora;
            }
            return;
        }

        if (gameOver) {
            if (!gano && !deathSoundPlayed) {
                reproducirAudio(R.raw.morir);
                deathSoundPlayed = true;
            }
            return;
        }

        velY += GRAVEDAD;
        posPersonajeY += velY;

        long ahora = System.currentTimeMillis();
        if (ahora - ultimoCambioFrame >= DURACION_FRAME) {
            frameIndex = (frameIndex + 1) % framesPersonaje.length;
            ultimoCambioFrame = ahora;
        }

        if (posPersonajeY <= 0) {
            posPersonajeY = 0;
            gameOver = true;
        }
        float limiteInferior = getHeight() - suelo.getHeight() - personajeAlto;
        if (posPersonajeY >= limiteInferior) {
            posPersonajeY = limiteInferior;
            gameOver = true;
        }

        float personajeX = 100;
        rectPersonaje.set((int) personajeX, (int) posPersonajeY,
                (int) (personajeX + personajeAncho), (int) (posPersonajeY + personajeAlto));

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

        float personajeX = 100;
        for (Tuberia t : tuberias) {
            t.dibujar(canvas);
        }
        renderizarSuelo(canvas);

        if (!gameOver) {
            Paint paint = new Paint();
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.numbers);
            paint.setTypeface(typeface);
            paint.setColor(Color.WHITE);
            paint.setTextSize(120);
            paint.setShadowLayer(1, 10, 10, Color.BLACK);
            canvas.drawText("" + score, 500, 350, paint);
        }

        Bitmap frameActual = framesPersonaje[frameIndex];
        Paint personajePaint = new Paint();
        if (invisibilidadTuberiasRestantes > 0) {
            personajePaint.setAlpha(100);
        } else {
            personajePaint.setAlpha(255);
        }
        canvas.drawBitmap(frameActual, personajeX, posPersonajeY, personajePaint);

        if (gameOver) {
            // Actualizamos bestScore siempre (tanto si se gana como si se pierde)
            if (score > bestScore) {
                bestScore = score;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_BEST_SCORE, bestScore);
                editor.apply();
            }
            if (gano) {
                // Rama de victoria: dibujar mensaje y, debajo, el botón de menú
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setTextSize(100);
                paint.setShadowLayer(1, 10, 10, Color.BLACK);
                paint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.numbers));
                String mensaje = "¡Ganaste!";
                float textWidth = paint.measureText(mensaje);
                float x = (getWidth() - textWidth) / 2f;
                float y = (getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
                canvas.drawText(mensaje, x, y, paint);

                // Dibujar botón de menú debajo del mensaje
                float menuMarginWin = 30f; // margen entre el mensaje y el botón
                float menuX = (getWidth() - menuBitmap.getWidth()) / 2f;
                float menuY = y + 100f + menuMarginWin; // 100f: altura aproximada del mensaje
                canvas.drawBitmap(menuBitmap, menuX, menuY, null);
            } else {
                // Rama de derrota: dibujar recuadro, puntuación actual, bestScore y los botones restart y menú
                float margenEntreImagenes = 20f;
                float alturaTotal = gameOverBitmap.getHeight() + margenEntreImagenes + dialogoScoreBitmap.getHeight();
                float bloqueY = (getHeight() - alturaTotal) / 2f - 100f;
                float gameOverX = (getWidth() - gameOverBitmap.getWidth()) / 2f;
                canvas.drawBitmap(gameOverBitmap, gameOverX, bloqueY, null);
                float dialogoX = (getWidth() - dialogoScoreBitmap.getWidth()) / 2f;
                float dialogoY = bloqueY + gameOverBitmap.getHeight() + margenEntreImagenes;
                canvas.drawBitmap(dialogoScoreBitmap, dialogoX, dialogoY, null);

                Paint paintScore = new Paint();
                paintScore.setColor(Color.WHITE);
                paintScore.setTextSize(55);
                paintScore.setShadowLayer(1, 4, 4, Color.BLACK);
                paintScore.setTypeface(ResourcesCompat.getFont(getContext(), R.font.numbers));
                String finalScore = String.valueOf(score);
                float offsetXScore = 500f;
                float offsetYScore = 110f;
                float scoreCenterX = dialogoX + offsetXScore;
                float scoreCenterY = dialogoY + offsetYScore;
                float textWidthScore = paintScore.measureText(finalScore);
                float textXScore = scoreCenterX - (textWidthScore / 2f);
                float textYScore = scoreCenterY - ((paintScore.descent() + paintScore.ascent()) / 2f);
                canvas.drawText(finalScore, textXScore, textYScore, paintScore);

                Paint paintBest = new Paint();
                paintBest.setColor(Color.WHITE);
                paintBest.setTextSize(55);
                paintBest.setShadowLayer(1, 4, 4, Color.BLACK);
                paintBest.setTypeface(ResourcesCompat.getFont(getContext(), R.font.numbers));
                String finalBest = String.valueOf(bestScore);
                float offsetXBest = 500f;
                float offsetYBest = 230f;
                float bestCenterX = dialogoX + offsetXBest;
                float bestCenterY = dialogoY + offsetYBest;
                float textWidthBest = paintBest.measureText(finalBest);
                float textXBest = bestCenterX - (textWidthBest / 2f);
                float textYBest = bestCenterY - ((paintBest.descent() + paintBest.ascent()) / 2f);
                canvas.drawText(finalBest, textXBest, textYBest, paintBest);

                // Dibujar el botón de reinicio (restart.png)
                float offsetRestart = 30f;
                float verticalOffset = 50f;
                float restartX = dialogoX + (dialogoScoreBitmap.getWidth() - restartBitmap.getWidth()) / 2f - offsetRestart;
                float restartY = dialogoY + (dialogoScoreBitmap.getHeight() - restartBitmap.getHeight()) / 2f - verticalOffset;
                canvas.drawBitmap(restartBitmap, restartX, restartY, null);

                // Dibujar el botón "ir al menú" debajo del de reinicio
                float menuMargin = 20f;
                float offsetMenu = 30f;
                float menuX = dialogoX + (dialogoScoreBitmap.getWidth() - menuBitmap.getWidth()) / 2f - offsetMenu;
                float menuY = restartY + restartBitmap.getHeight() + menuMargin;
                canvas.drawBitmap(menuBitmap, menuX, menuY, null);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                // Si se ganó, solo se activa el botón de menú
                if (gano) {
                    float menuMarginWin = 30f;
                    Paint paint = new Paint();
                    paint.setTextSize(100);
                    paint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.numbers));
                    float mensajeWidth = paint.measureText("¡Ganaste!");
                    float mensajeX = (getWidth() - mensajeWidth) / 2f;
                    float mensajeY = (getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
                    // Ubicar el botón de menú debajo del mensaje
                    float menuX = (getWidth() - menuBitmap.getWidth()) / 2f;
                    float menuY = mensajeY + 100f + menuMarginWin;
                    Rect menuRect = new Rect(
                            (int) menuX,
                            (int) menuY,
                            (int) (menuX + menuBitmap.getWidth()),
                            (int) (menuY + menuBitmap.getHeight())
                    );
                    float touchX = event.getX();
                    float touchY = event.getY();
                    if (menuRect.contains((int) touchX, (int) touchY)) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                        ((android.app.Activity)getContext()).finish();
                        return true;
                    }
                    return true;
                }
                // Si se perdió, comprobar ambos botones: restart y menú
                else {
                    float margenEntreImagenes = 20f;
                    float alturaTotal = gameOverBitmap.getHeight() + margenEntreImagenes + dialogoScoreBitmap.getHeight();
                    float bloqueY = (getHeight() - alturaTotal) / 2f - 100f;
                    float dialogoX = (getWidth() - dialogoScoreBitmap.getWidth()) / 2f;
                    float dialogoY = bloqueY + gameOverBitmap.getHeight() + margenEntreImagenes;

                    float offsetRestart = 30f;
                    float verticalOffset = 50f;
                    float restartX = dialogoX + (dialogoScoreBitmap.getWidth() - restartBitmap.getWidth()) / 2f - offsetRestart;
                    float restartY = dialogoY + (dialogoScoreBitmap.getHeight() - restartBitmap.getHeight()) / 2f - verticalOffset;
                    Rect restartRect = new Rect(
                            (int) restartX,
                            (int) restartY,
                            (int) (restartX + restartBitmap.getWidth()),
                            (int) (restartY + restartBitmap.getHeight())
                    );

                    float menuMargin = 20f;
                    float offsetMenu = 30f;
                    float menuX = dialogoX + (dialogoScoreBitmap.getWidth() - menuBitmap.getWidth()) / 2f - offsetMenu;
                    float menuY = restartY + restartBitmap.getHeight() + menuMargin;
                    Rect menuRect = new Rect(
                            (int) menuX,
                            (int) menuY,
                            (int) (menuX + menuBitmap.getWidth()),
                            (int) (menuY + menuBitmap.getHeight())
                    );

                    float touchX = event.getX();
                    float touchY = event.getY();
                    if (restartRect.contains((int) touchX, (int) touchY)) {
                        reiniciarPartida();
                        return true;
                    } else if (menuRect.contains((int) touchX, (int) touchY)) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                        ((android.app.Activity)getContext()).finish();
                        return true;
                    }
                    return true;
                }
            } else if (!gameStarted) {
                gameStarted = true;
                if (animSet != null && animSet.isRunning()) {
                    animSet.cancel();
                }
                velY = SALTO;
                reproducirAudio(R.raw.spray);
            } else {
                velY = SALTO;
                reproducirAudio(R.raw.spray);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void reiniciarPartida() {
        score = 0;
        gano = false;
        gameOver = false;
        gameStarted = false;
        deathSoundPlayed = false;
        totalTuberiasGeneradas = 0;
        invisibilidadTuberiasRestantes = 0;
        tuberias.clear();

        posPersonajeY = (getHeight() - personajeAlto) / 2f;
        velY = 0;

        if (animSet != null) {
            animSet.cancel();
        }
        animSet = new AnimatorSet();
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator volar = ObjectAnimator.ofFloat(this, "posPersonajeY", posPersonajeY - 40, posPersonajeY);
        volar.setDuration(400);
        volar.setRepeatCount(ObjectAnimator.INFINITE);
        volar.setRepeatMode(ObjectAnimator.REVERSE);
        animSet.play(volar);
        animSet.start();
    }

    public void reproducirAudio(int idAudio) {
        MediaPlayer audio = MediaPlayer.create(getContext(), idAudio);
        if (audio != null) {
            audio.setOnCompletionListener(MediaPlayer::release);
            audio.start();
        }
    }
}
