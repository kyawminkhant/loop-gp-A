package utils;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Flow-field particle background inspired by:
 * https://codepen.io/supah/pen/ExabJxB
 * Tuned to Loop warm cream / burgundy / green palette.
 */
public final class FlowFieldAmbient {

    private static final Random RAND = new Random();
    private static FlowFieldEngine activeEngine;

    private FlowFieldAmbient() {}

    /** Stops the shared animation timer when a data-heavy screen is shown. */
    public static void stopActive() {
        if (activeEngine != null) {
            activeEngine.stop();
            activeEngine = null;
        }
    }

    public static final class Handle {
        private final Pane layer;
        private final FlowFieldEngine engine;

        private Handle(Pane layer, FlowFieldEngine engine) {
            this.layer = layer;
            this.engine = engine;
        }

        public Pane getLayer() { return layer; }
    }

    public static Handle create(StackPane host) {
        // Stop any previous engine so we never run two timers (causes stutter)
        stopActive();

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(host.widthProperty());
        canvas.heightProperty().bind(host.heightProperty());

        Pane layer = new Pane(canvas);
        layer.setMouseTransparent(true);
        layer.prefWidthProperty().bind(host.widthProperty());
        layer.prefHeightProperty().bind(host.heightProperty());

        FlowFieldEngine engine = new FlowFieldEngine(canvas);
        activeEngine = engine;
        engine.start();
        return new Handle(layer, engine);
    }

    private static final class FlowFieldEngine {
        private final Canvas canvas;
        private final GraphicsContext gc;
        private final List<Particle> particles = new ArrayList<>();
        private final PerlinNoise noise = new PerlinNoise(RAND.nextInt());
        private AnimationTimer timer;

        private final double noiseScale = 0.0065;
        private double angle = Math.toRadians(-90);
        private final double tailFadeAlpha = 0.06;
        private long frame;
        private boolean primed;

        // Fixed Loop palette — no click recolor (keeps animation smooth)
        private final Color warmColor = Color.hsb(14, 0.55, 0.46, 0.38);
        private final Color coolColor = Color.hsb(128, 0.42, 0.38, 0.36);

        FlowFieldEngine(Canvas canvas) {
            this.canvas = canvas;
            this.gc = canvas.getGraphicsContext2D();
        }

        void start() {
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (activeEngine != FlowFieldEngine.this) {
                        stop();
                        return;
                    }
                    double w = canvas.getWidth();
                    double h = canvas.getHeight();
                    if (w < 2 || h < 2) return;

                    ensureParticles(w, h);
                    drawFrame(w, h);
                    frame++;
                }
            };
            timer.start();
        }

        void stop() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        private void ensureParticles(double w, double h) {
            // Slightly fewer particles = smoother FPS on JavaFX Canvas
            int target = (int) Math.max(220, Math.min(380, w / 2.8));
            while (particles.size() < target) {
                Particle p = new Particle(RAND.nextDouble() * w, RAND.nextDouble() * h);
                p.assign(warmColor, coolColor);
                particles.add(p);
            }
            while (particles.size() > target) {
                particles.remove(particles.size() - 1);
            }
        }

        private void drawFrame(double w, double h) {
            if (!primed) {
                gc.setGlobalAlpha(1.0);
                gc.setFill(Color.rgb(247, 236, 216));
                gc.fillRect(0, 0, w, h);
                primed = true;
            }

            // Soft cream trail fade
            gc.setGlobalAlpha(tailFadeAlpha);
            gc.setFill(Color.rgb(247, 236, 216));
            gc.fillRect(0, 0, w, h);
            gc.setGlobalAlpha(1.0);

            double t = frame * noiseScale * 0.45;
            gc.setLineWidth(1.05);

            // Two-pass draw by color → fewer setStroke calls (smoother)
            gc.setStroke(warmColor);
            for (Particle p : particles) {
                if (!p.warm) continue;
                p.update(noise, noiseScale, angle, t, w, h);
                gc.strokeLine(p.x, p.y, p.lx, p.ly);
                p.updatePrev();
            }

            gc.setStroke(coolColor);
            for (Particle p : particles) {
                if (p.warm) continue;
                p.update(noise, noiseScale, angle, t, w, h);
                gc.strokeLine(p.x, p.y, p.lx, p.ly);
                p.updatePrev();
            }

            // Very gentle continuous drift of the field angle
            angle += 0.00012;
        }
    }

    private static final class Particle {
        double x, y, lx, ly, vx, vy;
        double maxSpeed;
        boolean warm;

        Particle(double x, double y) {
            this.x = this.lx = x;
            this.y = this.ly = y;
        }

        void assign(Color warmColor, Color coolColor) {
            warm = RAND.nextBoolean();
            maxSpeed = warm ? 1.15 : 0.9;
        }

        void update(PerlinNoise noise, double noiseScale, double baseAngle, double t,
                    double width, double height) {
            double n = noise.noise(x * noiseScale, y * noiseScale, t);
            double a = n * Math.PI * 0.5 + baseAngle;

            // Weaker steering = calmer motion
            vx += Math.cos(a) * 0.35;
            vy += Math.sin(a) * 0.35;

            double speed = Math.hypot(vx, vy);
            if (speed > maxSpeed && speed > 0) {
                vx = vx / speed * maxSpeed;
                vy = vy / speed * maxSpeed;
            }

            x += vx;
            y += vy;

            if (x < 0) { x = width; lx = x; ly = y; }
            else if (x > width) { x = 0; lx = x; ly = y; }
            if (y < 0) { y = height; lx = x; ly = y; }
            else if (y > height) { y = 0; lx = x; ly = y; }
        }

        void updatePrev() {
            lx = x;
            ly = y;
        }
    }

    private static final class PerlinNoise {
        private final int[] perm = new int[512];

        PerlinNoise(int seed) {
            int[] p = new int[256];
            for (int i = 0; i < 256; i++) p[i] = i;
            Random r = new Random(seed);
            for (int i = 255; i > 0; i--) {
                int j = r.nextInt(i + 1);
                int tmp = p[i];
                p[i] = p[j];
                p[j] = tmp;
            }
            for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
        }

        double noise(double x, double y, double z) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);
            double u = fade(x);
            double v = fade(y);
            double w = fade(z);
            int A = perm[X] + Y;
            int AA = perm[A] + Z;
            int AB = perm[A + 1] + Z;
            int B = perm[X + 1] + Y;
            int BA = perm[B] + Z;
            int BB = perm[B + 1] + Z;

            return lerp(w,
                    lerp(v,
                            lerp(u, grad(perm[AA], x, y, z), grad(perm[BA], x - 1, y, z)),
                            lerp(u, grad(perm[AB], x, y - 1, z), grad(perm[BB], x - 1, y - 1, z))),
                    lerp(v,
                            lerp(u, grad(perm[AA + 1], x, y, z - 1), grad(perm[BA + 1], x - 1, y, z - 1)),
                            lerp(u, grad(perm[AB + 1], x, y - 1, z - 1), grad(perm[BB + 1], x - 1, y - 1, z - 1))));
        }

        private static double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        private static double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        private static double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
}
