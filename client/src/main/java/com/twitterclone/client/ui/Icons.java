package com.twitterclone.client.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders bundled <a href="https://lucide.dev">Lucide</a> SVG icons (in
 * {@code /icons/*.svg}) as JavaFX nodes. Lucide icons are stroke-based drawings
 * on a 24×24 grid, so each SVG element is parsed into the equivalent JavaFX
 * {@link Shape} and styled entirely through CSS (see {@code .lucide-icon} and
 * the {@code icon-*} colour classes), which keeps them theme-aware (they follow
 * the light/dark palette automatically).
 *
 * The parsed geometry is cached per icon name; a fresh (lightweight) set of
 * shapes is built for each call because a JavaFX node can only live in one
 * parent.
 */
public final class Icons {

    private static final double VIEWBOX = 24.0;
    private static final Map<String, List<ShapeDesc>> CACHE = new ConcurrentHashMap<>();

    private Icons() {
    }

    /** Icon in the default (primary text) colour. */
    public static Node create(String name, double size) {
        return create(name, size, "icon-default");
    }

    /**
     * Builds an icon node of the given pixel size. {@code colorClass} is one of
     * the {@code icon-*} CSS classes (e.g. {@code icon-muted}, {@code icon-like})
     * that determines the stroke/fill colour.
     */
    public static Node create(String name, double size, String colorClass) {
        Group group = new Group();
        for (ShapeDesc desc : load(name)) {
            Shape shape = desc.build();
            shape.getStyleClass().addAll("lucide-icon", colorClass);
            group.getChildren().add(shape);
        }
        double factor = size / VIEWBOX;
        group.getTransforms().add(new Scale(factor, factor));

        StackPane pane = new StackPane(group);
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
        // Let clicks fall through to an enclosing button/row so the icon never
        // becomes the mouse-event target.
        pane.setMouseTransparent(true);
        return pane;
    }

    // ---- parsing ----

    private static List<ShapeDesc> load(String name) {
        return CACHE.computeIfAbsent(name, Icons::parse);
    }

    private static List<ShapeDesc> parse(String name) {
        List<ShapeDesc> shapes = new ArrayList<>();
        try (InputStream in = Icons.class.getResourceAsStream("/icons/" + name + ".svg")) {
            if (in == null) {
                System.err.println("Icon not found: " + name);
                return shapes;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            NodeList children = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (!(children.item(i) instanceof Element el)) {
                    continue;
                }
                switch (el.getTagName()) {
                    case "path" -> shapes.add(new PathDesc(el.getAttribute("d")));
                    case "circle" -> shapes.add(new CircleDesc(d(el, "cx"), d(el, "cy"), d(el, "r")));
                    case "line" -> shapes.add(new LineDesc(d(el, "x1"), d(el, "y1"), d(el, "x2"), d(el, "y2")));
                    case "rect" -> shapes.add(new RectDesc(
                            d(el, "x"), d(el, "y"), d(el, "width"), d(el, "height"), d(el, "rx"), d(el, "ry")));
                    case "ellipse" -> shapes.add(new EllipseDesc(d(el, "cx"), d(el, "cy"), d(el, "rx"), d(el, "ry")));
                    case "polyline" -> shapes.add(new PolyDesc(false, points(el)));
                    case "polygon" -> shapes.add(new PolyDesc(true, points(el)));
                    default -> { /* ignore unsupported elements */ }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse icon " + name + ": " + e.getMessage());
        }
        return shapes;
    }

    private static double d(Element el, String attr) {
        String v = el.getAttribute(attr);
        return v.isBlank() ? 0.0 : Double.parseDouble(v);
    }

    private static List<Double> points(Element el) {
        List<Double> out = new ArrayList<>();
        String raw = el.getAttribute("points").trim();
        if (!raw.isEmpty()) {
            for (String tok : raw.split("[\\s,]+")) {
                out.add(Double.parseDouble(tok));
            }
        }
        return out;
    }

    // ---- geometry descriptors (immutable, cached; build a fresh Shape on demand) ----

    private interface ShapeDesc {
        Shape build();
    }

    private record PathDesc(String d) implements ShapeDesc {
        public Shape build() {
            SVGPath p = new SVGPath();
            p.setContent(d);
            return p;
        }
    }

    private record CircleDesc(double cx, double cy, double r) implements ShapeDesc {
        public Shape build() {
            return new Circle(cx, cy, r);
        }
    }

    private record LineDesc(double x1, double y1, double x2, double y2) implements ShapeDesc {
        public Shape build() {
            return new Line(x1, y1, x2, y2);
        }
    }

    private record RectDesc(double x, double y, double w, double h, double rx, double ry) implements ShapeDesc {
        public Shape build() {
            Rectangle rect = new Rectangle(x, y, w, h);
            double arcX = rx > 0 ? rx : ry;
            double arcY = ry > 0 ? ry : rx;
            rect.setArcWidth(arcX * 2);
            rect.setArcHeight(arcY * 2);
            return rect;
        }
    }

    private record EllipseDesc(double cx, double cy, double rx, double ry) implements ShapeDesc {
        public Shape build() {
            return new Ellipse(cx, cy, rx, ry);
        }
    }

    private record PolyDesc(boolean closed, List<Double> points) implements ShapeDesc {
        public Shape build() {
            if (closed) {
                Polygon poly = new Polygon();
                poly.getPoints().addAll(points);
                return poly;
            }
            Polyline poly = new Polyline();
            poly.getPoints().addAll(points);
            return poly;
        }
    }
}
