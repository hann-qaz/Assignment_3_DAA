package benchmark;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Simple Java2D chart: no plotting packages or downloads are needed. */
public class PlotWriter {
    private static final Color[] COLORS = {new Color(20, 90, 190), new Color(210, 80, 20),
            new Color(20, 140, 90), new Color(140, 60, 180), new Color(150, 115, 0),
            new Color(200, 40, 100), new Color(30, 135, 150)};

    public static void write(List<Benchmark.Row> rows, boolean byVertices) throws Exception {
        BufferedImage image = new BufferedImage(1200, 760, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, 1200, 760);
        g.setColor(new Color(25, 35, 50)); g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("Execution time vs. " + (byVertices ? "vertices" : "edges"), 90, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("Mean of 5 runs | seed 42 | logarithmic axes | instrumented algorithms", 90, 73);
        double minTime = rows.stream().mapToDouble(Benchmark.Row::averageMs).min().orElseThrow();
        double maxTime = rows.stream().mapToDouble(Benchmark.Row::averageMs).max().orElseThrow();
        double low = Math.floor(Math.log10(Math.max(minTime, 0.000001)));
        double high = Math.ceil(Math.log10(maxTime));
        if (high <= low) high = low + 1;
        int left = 110, right = 830, top = 115, bottom = 640;
        int[] ticks = byVertices ? new int[]{10, 100, 1000} : new int[]{30, 500, 5000};
        for (double exponent = low; exponent <= high; exponent++) {
            int y = (int) (bottom - (exponent - low) / (high - low) * (bottom - top));
            g.setColor(new Color(225, 230, 236)); g.drawLine(left, y, right, y);
            g.setColor(Color.DARK_GRAY); g.drawString(String.format(java.util.Locale.US, "%.6f", Math.pow(10, exponent)), 15, y + 5);
        }
        for (int tick : ticks) {
            int x = x(tick, ticks, left, right);
            g.setColor(new Color(225, 230, 236)); g.drawLine(x, top, x, bottom);
            g.setColor(Color.DARK_GRAY); g.drawString(Integer.toString(tick), x - 12, bottom + 26);
        }
        g.setColor(Color.DARK_GRAY); g.drawLine(left, bottom, right, bottom); g.drawLine(left, top, left, bottom);
        g.drawString("Mean time (ms)", 15, 100);
        g.drawString(byVertices ? "Vertices (V)" : "Edges (E)", 410, 706);
        List<String> series = rows.stream().map(r -> r.algorithm() + " / " + r.kind()).distinct().toList();
        for (int i = 0; i < series.size(); i++) {
            String name = series.get(i);
            List<Benchmark.Row> points = rows.stream().filter(r -> (r.algorithm() + " / " + r.kind()).equals(name)).toList();
            g.setColor(COLORS[i]); g.setStroke(new BasicStroke(2.5f));
            int previousX = -1, previousY = -1;
            for (Benchmark.Row point : points) {
                int px = x(byVertices ? point.vertices() : point.edges(), ticks, left, right);
                int py = (int) (bottom - (Math.log10(Math.max(point.averageMs(), 0.000001)) - low) / (high - low) * (bottom - top));
                if (previousX >= 0) g.draw(new Line2D.Double(previousX, previousY, px, py));
                g.fillOval(px - 5, py - 5, 10, 10);
                previousX = px; previousY = py;
            }
            int legendY = 145 + i * 38;
            g.drawLine(865, legendY, 895, legendY);
            g.setColor(Color.DARK_GRAY); g.drawString(name, 905, legendY + 5);
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("V and E increase together; these plots do not isolate their individual effects.", 110, 744);
        g.dispose();
        Files.createDirectories(Path.of("results/plots"));
        ImageIO.write(image, "png", Path.of("results/plots/time_vs_" + (byVertices ? "vertices" : "edges") + ".png").toFile());
    }

    private static int x(int value, int[] ticks, int left, int right) {
        return (int) (left + (Math.log10(value) - Math.log10(ticks[0]))
                / (Math.log10(ticks[2]) - Math.log10(ticks[0])) * (right - left));
    }
}
