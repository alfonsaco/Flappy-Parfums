package es.riberadeltajo.flappy_parfums;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.material.tabs.TabLayout;

public class BucleJuego extends Thread {

    private final static int MAX_FPS = 30;
    private final static int TIEMPO_FRAME = 1000 / MAX_FPS;

    private Juego juego;
    private SurfaceHolder surfaceHolder;

    private boolean enEjecucion = false;

    public BucleJuego(SurfaceHolder sh, Juego s) {
        this.juego = s;
        this.surfaceHolder = sh;
    }

    public void setEnEjecucion(boolean enEjecucion) {
        this.enEjecucion = enEjecucion;
    }

    @Override
    public void run() {
        long inicioFrame, tiempoDiferencia, tiempoDormir;
        Canvas canvas = null;

        while (enEjecucion) {
            inicioFrame = System.currentTimeMillis();

            try {
                // Bloqueamos el Canvas
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    // Sincronizamos para evitar problemas de concurrencia
                    synchronized (surfaceHolder) {
                        // Actualizamos la lógica del juego
                        juego.actualizar();
                        // Renderizamos
                        juego.renderizar(canvas);
                    }
                }
            } finally {
                // Liberamos el Canvas
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            // Calculamos cuánto tardó el frame
            tiempoDiferencia = System.currentTimeMillis() - inicioFrame;
            // Cuánto nos sobra para llegar a los ~16 ms (60 FPS)
            tiempoDormir = TIEMPO_FRAME - tiempoDiferencia;

            // Si aún sobra tiempo (vamos por encima de 60 FPS),
            // dormimos el hilo para no forzar CPU/GPU
            if (tiempoDormir > 0) {
                try {
                    Thread.sleep(tiempoDormir);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fin() {
        enEjecucion = false;
    }
}
