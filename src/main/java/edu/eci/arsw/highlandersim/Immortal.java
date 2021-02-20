package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private AtomicBoolean trabajando,vivo;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue=defaultDamageValue;
        this.trabajando=new AtomicBoolean(false);
        this.vivo=new AtomicBoolean(true);
    }

    public void run() {
        this.trabajando.getAndSet(true);
        while (vivo.get()) {
            Immortal im;
            while(!trabajando.get()){
                synchronized (this){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }
            if (getHealth().get() <= 0){
                this.getVivo().getAndSet(false);
                getHealth().set(0);
                immortalsPopulation.remove(this);



            }
            synchronized (immortalsPopulation.get(nextFighterIndex)){
                im = immortalsPopulation.get(nextFighterIndex);
            }

            if(this.getVivo().get()) {
                this.fight(im);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
        boolean vA;
        synchronized (i2){
            vA = i2.getHealth().get() > 0;
            if (vA) {
                i2.getHealth().getAndAdd(- defaultDamageValue);
                this.health.getAndAdd(defaultDamageValue);
                updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
            } else {
                updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                return;
            }
        }




    }





    public AtomicInteger getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public AtomicBoolean isTrabajando() {
        return trabajando;
    }

    public void setTrabajando(boolean trabajando) {
        this.trabajando.getAndSet(trabajando);
    }

    public synchronized void despierta() {
        this.setTrabajando(true);
        this.notify();
    }

    public AtomicBoolean getVivo() {
        return vivo;
    }


}
