package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

public class Tuberia {

    // Imágenes de tubería de arriba y de abajo
    private Bitmap tuberiaArriba;
    private Bitmap tuberiaAbajo;

    private float x;       // posición X de la tubería
    private float ancho;   // ancho de la tubería
    private float topHeight;    // altura de la tubería superior
    private float bottomHeight; // altura de la tubería inferior

    private float gap; // espacio entre tubería de arriba y abajo
    private float groundY; // posición Y donde empieza el suelo
    private float velocidad;

    private Rect rectArriba;
    private Rect rectAbajo;
    private static final float FACTOR_ESCALA = 1.3f;
    private boolean puntoSumado = false;
    private float velocidadTuberia;

    private static final Random random = new Random();

    public Tuberia(Context context,
                   float pantallaAncho,
                   float groundY,   // parte de arriba del suelo
                   float velocidad, // velocidad de scroll
                   float gap) {
        // Cargar imágenes (asegúrate de que pipe_top / pipe_bottom están en res/drawable)
        tuberiaArriba = BitmapFactory.decodeResource(context.getResources(), R.drawable.inverspipered);
        tuberiaAbajo  = BitmapFactory.decodeResource(context.getResources(), R.drawable.pipered);

        // Ajustar ancho si deseas, o dejarlo así
        ancho = tuberiaArriba.getWidth();
        this.velocidad = velocidad;
        this.gap = gap;
        this.groundY = groundY;

        // Inicia a la derecha de la pantalla
        x = pantallaAncho;


        float minTop = 400;
        float maxTop = groundY - gap - 400;
        if (maxTop < minTop) {
            // Si el gap es muy grande, ajusta para no tener problema
            maxTop = minTop + 50;
        }

        topHeight = minTop + random.nextFloat() * (maxTop - minTop);
        bottomHeight = groundY - (topHeight + gap);

        // Rects para colisión
        rectArriba = new Rect();
        rectAbajo  = new Rect();

        this.puntoSumado = false;
    }

    public void actualizar() {
        // Mover tubería a la izquierda
        x -= velocidad;
        float anchoEscalado       = ancho        * FACTOR_ESCALA;
        float topHeightEscalada   = topHeight    * FACTOR_ESCALA;
        float bottomHeightEscalada= bottomHeight * FACTOR_ESCALA;
        float posAbajoEscalada = topHeightEscalada + (gap * FACTOR_ESCALA);


        // Rect arriba: va de Y=0 a Y=topHeight
        rectArriba.set(
                (int)x,
                0,
                (int)(x + anchoEscalado),
                (int)topHeightEscalada
        );

        rectAbajo.set(
                (int)x,
                (int)posAbajoEscalada,
                (int)(x + anchoEscalado),
                (int)(posAbajoEscalada + bottomHeightEscalada)
        );
    }

    public void dibujar(Canvas canvas) {
        float anchoEscalado       = ancho        * FACTOR_ESCALA;
        float topHeightEscalada   = topHeight    * FACTOR_ESCALA;
        float bottomHeightEscalada= bottomHeight * FACTOR_ESCALA;

        // Escalar tubería superior
        Bitmap arribaEscalada = Bitmap.createScaledBitmap(
                tuberiaArriba,
                (int)anchoEscalado,
                (int)topHeightEscalada,
                true
        );

        // Escalar tubería inferior
        Bitmap abajoEscalada = Bitmap.createScaledBitmap(
                tuberiaAbajo,
                (int)anchoEscalado,
                (int)bottomHeightEscalada,
                true
        );

        // Dibujar la tubería superior en Y=0
        canvas.drawBitmap(arribaEscalada, x, 0, null);

        // Dibujar la tubería inferior justo después del hueco escalado
        float posAbajoEscalada = topHeightEscalada + (gap * FACTOR_ESCALA);
        canvas.drawBitmap(abajoEscalada, x, posAbajoEscalada, null);
    }

    public boolean fueraDePantalla() {
        float anchoEscalado = ancho * FACTOR_ESCALA;
        return (x + anchoEscalado < 0);
    }

    public boolean colisionaCon(Rect rectPersonaje) {
        return Rect.intersects(rectPersonaje, rectArriba)
                || Rect.intersects(rectPersonaje, rectAbajo);
    }
    public boolean isPuntoSumado() {
        return puntoSumado;
    }

    public void setPuntoSumado(boolean puntoSumado) {
        this.puntoSumado = puntoSumado;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getAncho() {
        return ancho;
    }
    public void setVelocidad(float velocidad) {
        this.velocidadTuberia = velocidad;
    }



}
