package es.riberadeltajo.flappy_parfums;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.material.tabs.TabLayout;

public class BucleJuego extends Thread {

    private JuegoActivity juego;
    private SurfaceHolder surfaceHolder;
    public boolean JuegoEnEjecucion=true;
    public final static int MAX_FPS=30;
    public final static int TIEMPO_FRAME = 1000 / MAX_FPS;
    private final static int MAX_FRAMES_SALTADOS = 5;

    BucleJuego(SurfaceHolder sh, JuegoActivity s){
        juego=s;
        surfaceHolder =sh;
    }
    public void run(){
        Canvas canvas;
        Log.d(TAG,"Comienza el bucle");

        long tiempoComienzo;
        long tiempoDiferencia;
        int tiempoDormir;
        int framesASaltar;
        tiempoDormir=0;

        while(JuegoEnEjecucion) {
            canvas=null;
            try {
                canvas= this.surfaceHolder.lockCanvas();

                synchronized (surfaceHolder) {
                    tiempoComienzo = System.currentTimeMillis();
                    framesASaltar=0;
                    //juego.actualizar();
                    //juego.renderizar(canvas);
                    tiempoDiferencia= System.currentTimeMillis() - tiempoComienzo;
                    tiempoDormir = (int) (TIEMPO_FRAME-tiempoDiferencia);

                    if (tiempoDormir>0){
                        try{
                            Thread.sleep(tiempoDormir);
                        }catch (InterruptedException e){}
                    }

                    while (tiempoDormir<0 && framesASaltar > MAX_FRAMES_SALTADOS ){
                        //juego.actualizar();
                        tiempoDormir += TIEMPO_FRAME;
                        framesASaltar++;
                    }
                }

            } finally {
                if (canvas!=null){
                    surfaceHolder.unlockCanvasAndPost(canvas);

                }
            } Log.d(TAG,"nueva iteración");

        }
    }

    // Se para el juego
    public void fin(){JuegoEnEjecucion=false;}
}
