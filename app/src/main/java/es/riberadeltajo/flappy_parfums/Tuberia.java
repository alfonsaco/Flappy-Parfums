package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

public class Tuberia {
    // Bitmaps para dibujar las tuberías
    private Bitmap tuberiaArribaOriginal;
    private Bitmap tuberiaAbajoOriginal;
    private Bitmap tuberiaArribaEscalada;
    private Bitmap tuberiaAbajoEscalada;

    // Variables para las colisiones y localización de las tuberías
    private float x;
    private float topHeight;
    private float bottomHeight;
    private float gap;
    private float topHeightEscalada;
    private float bottomHeightEscalada;
    private float gapEscalado;
    private float groundY;
    private float velocidad;

    // Rectángulos de colisión que tendrán las tuberías
    private Rect rectArriba;
    private Rect rectAbajo;

    private boolean puntoSumado = false;
    private static final float FACTOR_ESCALA = 1.3f;
    private static final Random random = new Random();

    // Nuevas variables para el power-up
    // Estos son objetos que el usuario puede obtener para recibir algún efecto, como el de invisivilidad
    private boolean powerUp = false;
    private Bitmap imagenPowerUp;
    private Rect rectPowerUp;
    private boolean powerUpCollected = false;

    // Constructor
    public Tuberia(Context context, float pantallaAncho, float groundY, float velocidad, float gap, boolean powerUp) {
        // Bitmap de las tuberías
        tuberiaArribaOriginal = BitmapFactory.decodeResource(context.getResources(), R.drawable.inverspipered);
        tuberiaAbajoOriginal  = BitmapFactory.decodeResource(context.getResources(), R.drawable.pipered);

        this.velocidad = velocidad;
        this.gap = gap;
        this.groundY = groundY;
        x = pantallaAncho;

        float minTop = 400;
        float maxTop = groundY - gap - 400;

        if (maxTop < minTop) {
            maxTop = minTop + 50;
        }
        topHeight = minTop + random.nextFloat() * (maxTop - minTop);
        bottomHeight = groundY - (topHeight + gap);
        if (bottomHeight < 0) {
            bottomHeight = 0;
        }

        float anchoOriginal = tuberiaArribaOriginal.getWidth();
        float anchoEscalado = anchoOriginal * FACTOR_ESCALA;
        topHeightEscalada = topHeight * FACTOR_ESCALA;
        bottomHeightEscalada = bottomHeight * FACTOR_ESCALA;
        gapEscalado = gap * FACTOR_ESCALA;

        if (anchoEscalado <= 0 || topHeightEscalada <= 0 || bottomHeightEscalada <= 0) {
            tuberiaArribaEscalada = tuberiaArribaOriginal;
            tuberiaAbajoEscalada = tuberiaAbajoOriginal;
        } else {
            tuberiaArribaEscalada = Bitmap.createScaledBitmap(
                    tuberiaArribaOriginal,
                    (int) anchoEscalado,
                    (int) topHeightEscalada,
                    true);
            tuberiaAbajoEscalada = Bitmap.createScaledBitmap(
                    tuberiaAbajoOriginal,
                    (int) anchoEscalado,
                    (int) bottomHeightEscalada,
                    true);
        }
        rectArriba = new Rect();
        rectAbajo = new Rect();

        // Se crea el power-up en las tuberías en cuestión
        this.powerUp = powerUp;
        if (powerUp) {
            imagenPowerUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.invisibilidad);

            int tubeWidth = tuberiaArribaEscalada.getWidth();
            int scaledWidth = tubeWidth / 2; // Por ejemplo, la mitad del ancho de la tubería
            int scaledHeight = (int) (imagenPowerUp.getHeight() * ((float) scaledWidth / imagenPowerUp.getWidth()));
            imagenPowerUp = Bitmap.createScaledBitmap(imagenPowerUp, scaledWidth, scaledHeight, true);

            int powerUpX = (int) (x + tubeWidth / 2 - scaledWidth / 2);
            int powerUpY = (int) (tuberiaArribaEscalada.getHeight() + (gapEscalado / 2) - (scaledHeight / 2));
            rectPowerUp = new Rect(powerUpX, powerUpY, powerUpX + scaledWidth, powerUpY + scaledHeight);
        }
    }

    // Getters y Setters
    public boolean isPuntoSumado() {
        return puntoSumado;
    }
    public void setPuntoSumado(boolean puntoSumado) {
        this.puntoSumado = puntoSumado;
    }
    public float getX() {
        return x;
    }
    public float getAncho() {
        return tuberiaArribaEscalada.getWidth();
    }
    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    // Getters y setters para el power-up
    public boolean hasPowerUp() {
        return powerUp;
    }
    public boolean isPowerUpCollected() {
        return powerUpCollected;
    }
    public void setPowerUpCollected(boolean collected) {
        powerUpCollected = collected;
    }
    public Rect getRectPowerUp() {
        return rectPowerUp;
    }

    // Método actualizar. Mueve las tuberías a la izquierda
    public void actualizar() {
        x -= velocidad;

        // Obtenemos la altura y anchura de ambas tuberías (arriba y abajo)
        float anchoArriba = tuberiaArribaEscalada.getWidth();
        float altoArriba = tuberiaArribaEscalada.getHeight();
        float anchoAbajo = tuberiaAbajoEscalada.getWidth();
        float altoAbajo = tuberiaAbajoEscalada.getHeight();
        float posAbajo = altoArriba + gapEscalado;

        // Al igual que las tuberías, se mueven los rectángulos de colisión con estas
        rectArriba.set((int) x, 0, (int) (x + anchoArriba), (int) altoArriba);
        rectAbajo.set((int) x, (int) posAbajo, (int) (x + anchoAbajo), (int) (posAbajo + altoAbajo));

        // Se mueve también el powerUp, siempre que no haya sido cogido por el jugador
        if (powerUp && !powerUpCollected && rectPowerUp != null) {
            int tubeWidth = tuberiaArribaEscalada.getWidth();
            int scaledWidth = rectPowerUp.width();
            int powerUpX = (int) (x + tubeWidth / 2 - scaledWidth / 2);

            rectPowerUp.left = powerUpX;
            rectPowerUp.right = powerUpX + scaledWidth;
        }
    }

    // Dibujar las tuberías y el power-up
    public void dibujar(Canvas canvas) {
        canvas.drawBitmap(tuberiaArribaEscalada, x, 0, null);
        float posAbajo = tuberiaArribaEscalada.getHeight() + gapEscalado;
        canvas.drawBitmap(tuberiaAbajoEscalada, x, posAbajo, null);

        // Se dibuja el power-up
        if (powerUp && !powerUpCollected && imagenPowerUp != null && rectPowerUp != null) {
            canvas.drawBitmap(imagenPowerUp, rectPowerUp.left, rectPowerUp.top, null);
        }
    }

    // Si la tubería esta fuera de la pantalla, se elimina
    public boolean fueraDePantalla() {
        float anchoTuberia = tuberiaArribaEscalada.getWidth();
        return (x + anchoTuberia < 0);
    }

    // Se verifica si la tubería ha colisionado con el personaje o no
    public boolean colisionaCon(Rect rectPersonaje) {
        return Rect.intersects(rectPersonaje, rectArriba) || Rect.intersects(rectPersonaje, rectAbajo);
    }
}

