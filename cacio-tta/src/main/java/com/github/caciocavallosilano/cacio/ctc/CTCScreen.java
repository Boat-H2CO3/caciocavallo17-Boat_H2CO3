package com.github.caciocavallosilano.cacio.ctc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.List;

import com.github.caciocavallosilano.cacio.peer.WindowClippedGraphics;
import com.github.caciocavallosilano.cacio.peer.managed.FullScreenWindowFactory;
import com.github.caciocavallosilano.cacio.peer.managed.PlatformScreen;

public class CTCScreen implements PlatformScreen {

    private BufferedImage screenBuffer;

    private static volatile CTCScreen instance;

    public static CTCScreen getInstance() {
        if (instance == null) {
            synchronized (CTCScreen.class) {
                if (instance == null) {
                    instance = new CTCScreen();
                }
            }
        }
        return instance;
    }

    private CTCScreen() {
        Dimension d = FullScreenWindowFactory.getScreenDimension();
        screenBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public ColorModel getColorModel() {
        return screenBuffer.getColorModel();
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    @Override
    public Rectangle getBounds() {
        Dimension d = FullScreenWindowFactory.getScreenDimension();
        return new Rectangle(0, 0, d.width, d.height);
    }

    @Override
    public Graphics2D getClippedGraphics(Color fg, Color bg, Font f, List<Rectangle> clipRects) {
        Graphics2D g2d = (Graphics2D) screenBuffer.getGraphics();
        Area a = new Area(getBounds());
        if (clipRects != null && !clipRects.isEmpty()) {
            for (Rectangle clip : clipRects) {
                a.subtract(new Area(clip));
            }
        }
        g2d = new WindowClippedGraphics(g2d, a);
        return g2d;
    }

    int[] getRGBPixels(Rectangle bounds) {
        return screenBuffer.getRGB(bounds.x, bounds.y, bounds.width, bounds.height, null, 0, bounds.width);
    }

    private static int[] dataBufAux;

    public static int[] getCurrentScreenRGB() {
        if (instance.screenBuffer == null) {
            return null;
        } else {
            if (dataBufAux == null) {
                dataBufAux = new int[((int) FullScreenWindowFactory.getScreenDimension().getWidth()) * (int) FullScreenWindowFactory.getScreenDimension().getHeight()];
            }
            instance.screenBuffer.getRaster().getDataElements(0, 0,
                    (int) FullScreenWindowFactory.getScreenDimension().getWidth(),
                    (int) FullScreenWindowFactory.getScreenDimension().getHeight(),
                    dataBufAux);
            return dataBufAux;
        }
    }

    static {
        try {
            File currLibFile;
            for (String ldLib : System.getenv("H2CO3Launcher_NATIVEDIR").split(":")) {
                if (ldLib.isEmpty()) continue;
                currLibFile = new File(ldLib, "libh2co3_exec_awt.so");
                if (currLibFile.exists()) {
                    System.load(currLibFile.getAbsolutePath());
                    break;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}