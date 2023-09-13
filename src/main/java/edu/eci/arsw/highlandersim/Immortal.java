package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

	private ImmortalUpdateReportCallback updateCallback = null;

	private int health;

	private int defaultDamageValue;

	private final List<Immortal> immortalsPopulation;

	private final String name;

	private final Random r = new Random(System.currentTimeMillis());

	private AtomicBoolean pause;

	private AtomicInteger sleeped;

	private final Object childObject;

	private final Object chief;

	private final int siblings;

	private boolean alive;

	private AtomicBoolean end;

	public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
			ImmortalUpdateReportCallback ucb, Object chief, AtomicInteger sleeped, AtomicBoolean pause,
			Object childObject, int siblings, boolean alive, AtomicBoolean end) {
		super(name);
		this.updateCallback = ucb;
		this.name = name;
		this.immortalsPopulation = immortalsPopulation;
		this.health = health;
		this.defaultDamageValue = defaultDamageValue;
		this.childObject = childObject;
		this.chief = chief;
		this.siblings = siblings;
		this.sleeped = sleeped;
		this.alive = alive;
		this.end = end;
		this.pause = pause;

	}

	public void run() {

		while (alive && !end.get()) {
			while (pause.get()) {
				synchronized (sleeped) {
					sleeped.getAndAdd(1);
				}
				if (sleeped.get() < siblings) {
					synchronized (childObject) {
						try {
							childObject.wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					synchronized (chief) {
						chief.notify();
						sleeped.set(0);
					}
					synchronized (childObject) {
						try {
							childObject.wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}

			}
			Immortal im;

			int myIndex = immortalsPopulation.indexOf(this);

			int nextFighterIndex = r.nextInt(immortalsPopulation.size());

			// avoid self-fight
			if (nextFighterIndex == myIndex) {
				nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
			}

			im = immortalsPopulation.get(nextFighterIndex);

			this.fight(im);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void fight(Immortal i2) {

		if (i2.hashCode() > this.hashCode()) {
			synchronized (this) {
				synchronized (i2) {
					if (i2.getHealth() > 0) {
						i2.changeHealth(i2.getHealth() - defaultDamageValue);
						this.health += defaultDamageValue;
						updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
						if (i2.getHealth() == 0) {
							i2.setAlive(false);
						}
					} else {
						updateCallback.processReport(this + " says: " + i2 + "is already dead!\n");
					}
				}
			}
		} else {
			synchronized (i2) {
				synchronized (this) {
					if (i2.getHealth() > 0) {
						i2.changeHealth(i2.getHealth() - defaultDamageValue);
						this.health += defaultDamageValue;
						updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
					} else {
						updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
					}
				}
			}
		}
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void changeHealth(int v) {
		health = v;
	}

	public int getHealth() {
		return health;
	}

	@Override
	public String toString() {

		return name + "[" + health + "]";
	}

}
