package es.riberadeltajo.flappy_parfums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

public class Tuberia {

    // Bitmaps originales
    private Bitmap tuberiaArribaOriginal;
    private Bitmap tuberiaAbajoOriginal;

    // Bitmaps escalados (se crean una sola vez)
    private Bitmap tuberiaArribaEscalada;
    private Bitmap tuberiaAbajoEscalada;

    // Posición X de la tubería
    private float x;

    // Alturas originales (sin escalar)
    private float topHeight;
    private float bottomHeight;
    // Hueco original entre la parte superior e inferior
    private float gap;

    // Alturas escaladas
    private float topHeightEscalada;
    private float bottomHeightEscalada;
    private float gapEscalado;

    // Parámetros de posicionamiento
    private float groundY;   // Y donde comienza el suelo (limite inferior del área jugable)
    private float velocidad; // Velocidad de desplazamiento

    // Rectángulos para colisiones
    private Rect rectArriba;
    private Rect rectAbajo;

    // Factor de escalado (puedes ajustarlo según la calidad de tus assets)
    private static final float FACTOR_ESCALA = 1.3f;

    // Control para sumar puntos una sola vez
    private boolean puntoSumado = false;

    // Generador aleatorio para la altura de la tubería superior
    private static final Random random = new Random();

    /**
     * Constructor de Tuberia.
     *
     * @param context       Contexto para cargar recursos.
     * @param pantallaAncho Ancho de la pantalla (para posicionarla inicialmente a la derecha).
     * @param groundY       Y donde comienza el suelo.
     * @param velocidad     Velocidad de desplazamiento horizontal.
     * @param gap           Hueco vertical (en píxeles, sin escalar) entre tubería superior e inferior.
     */
    public Tuberia(Context context,
                   float pantallaAncho,
                   float groundY,
                   float velocidad,
                   float gap) {

        // Cargar los bitmaps originales
        tuberiaArribaOriginal = BitmapFactory.decodeResource(context.getResources(), R.drawable.inverspipered);
        tuberiaAbajoOriginal  = BitmapFactory.decodeResource(context.getResources(), R.drawable.pipered);

        this.velocidad = velocidad;
        this.gap = gap;
        this.groundY = groundY;
        // La tubería se posiciona inicialmente a la derecha de la pantalla
        x = pantallaAncho;

        /*
         * Calcula la altura superior (topHeight) de forma aleatoria.
         * IMPORTANTE: Asegúrate de que groundY sea lo suficientemente alto para que
         * se pueda acomodar un topHeight + gap + bottomHeight.
         */
        float minTop = 400;
        float maxTop = groundY - gap - 400; // 400: margen para el suelo
        if (maxTop < minTop) {
            maxTop = minTop + 50;
        }
        topHeight = minTop + random.nextFloat() * (maxTop - minTop);

        // Calcula bottomHeight para que la suma (topHeight + gap + bottomHeight) sea igual a groundY
        bottomHeight = groundY - (topHeight + gap);
        if (bottomHeight < 0) {
            bottomHeight = 0; // Evita valores negativos
        }

        // Escala las dimensiones usando FACTOR_ESCALA
        // También escalamos el gap para que se vea proporcional
        float anchoOriginal = tuberiaArribaOriginal.getWidth();
        float anchoEscalado = anchoOriginal * FACTOR_ESCALA;
        topHeightEscalada    = topHeight    * FACTOR_ESCALA;
        bottomHeightEscalada = bottomHeight * FACTOR_ESCALA;
        gapEscalado          = gap          * FACTOR_ESCALA;

        /*
         * Si los valores escalados son muy pequeños, puede que la imagen se pixele.
         * Para evitarlo, asegúrate de usar imágenes originales de buena resolución
         * o ajusta FACTOR_ESCALA a un valor menor.
         */
        if (anchoEscalado <= 0 || topHeightEscalada <= 0 || bottomHeightEscalada <= 0) {
            // Si algo falla, usamos las imágenes originales (aunque esto podría romper la lógica visual)
            tuberiaArribaEscalada = tuberiaArribaOriginal;
            tuberiaAbajoEscalada  = tuberiaAbajoOriginal;
        } else {
            tuberiaArribaEscalada = Bitmap.createScaledBitmap(
                    tuberiaArribaOriginal,
                    (int) anchoEscalado,
                    (int) topHeightEscalada,
                    true
            );
            tuberiaAbajoEscalada = Bitmap.createScaledBitmap(
                    tuberiaAbajoOriginal,
                    (int) anchoEscalado,
                    (int) bottomHeightEscalada,
                    true
            );
        }

        // Inicializar rectángulos para las colisiones
        rectArriba = new Rect();
        rectAbajo  = new Rect();
    }

    // Actualizando y subiendo cambios


    /**
     * Actualiza la posición de la tubería y sus rectángulos de colisión.
     */
    public void actualizar() {
        // Mueve la tubería hacia la izquierda
        x -= velocidad;

        // Dimensiones de los bitmaps escalados
        float anchoArriba = tuberiaArribaEscalada.getWidth();
        float altoArriba  = tuberiaArribaEscalada.getHeight();
        float anchoAbajo  = tuberiaAbajoEscalada.getWidth();
        float altoAbajo   = tuberiaAbajoEscalada.getHeight();

        // La parte inferior se posiciona debajo de la superior dejando el gap escalado
        float posAbajo = altoArriba + gapEscalado;

        // Actualiza rectángulo de la tubería superior
        rectArriba.set(
                (int) x,
                0,
                (int) (x + anchoArriba),
                (int) altoArriba
        );

        // Actualiza rectángulo de la tubería inferior
        rectAbajo.set(
                (int) x,
                (int) posAbajo,
                (int) (x + anchoAbajo),
                (int) (posAbajo + altoAbajo)
        );
    }

    /**
     * Dibuja la tubería en el canvas, dejando un hueco entre la parte superior e inferior.
     */
    public void dibujar(Canvas canvas) {
        // Dibuja la tubería superior en Y = 0
        canvas.drawBitmap(tuberiaArribaEscalada, x, 0, null);
        // Dibuja la tubería inferior, dejando el hueco escalado
        float posAbajo = tuberiaArribaEscalada.getHeight() + gapEscalado;
        canvas.drawBitmap(tuberiaAbajoEscalada, x, posAbajo, null);
    }

    /**
     * Devuelve true si la tubería ya salió completamente de la pantalla.
     */
    public boolean fueraDePantalla() {
        float anchoTuberia = tuberiaArribaEscalada.getWidth();
        return (x + anchoTuberia < 0);
    }

    /**
     * Comprueba si hay colisión con el rectángulo del personaje.
     */
    public boolean colisionaCon(Rect rectPersonaje) {
        return Rect.intersects(rectPersonaje, rectArriba) ||
                Rect.intersects(rectPersonaje, rectAbajo);
    }

    // GETTERS Y SETTERS

    public boolean isPuntoSumado() {
        return puntoSumado;
    }

    public void setPuntoSumado(boolean puntoSumado) {
        this.puntoSumado = puntoSumado;
    }

    public float getX() {
        return x;
    }

    /**
     * Retorna el ancho (ya escalado) de la tubería, útil para calcular la posición de la siguiente.
     */
    public float getAncho() {
        return tuberiaArribaEscalada.getWidth();
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }
}
