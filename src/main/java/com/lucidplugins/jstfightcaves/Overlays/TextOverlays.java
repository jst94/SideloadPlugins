package com.lucidplugins.jstfightcaves.Overlays;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.lucidplugins.jstfightcaves.CavesConfig;
import com.lucidplugins.jstfightcaves.CavesPlugin;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class TextOverlays
extends Overlay {
    @Inject
    public CavesConfig config;
    private final CavesPlugin plugin;
    private final Client client;

    @Inject
    public TextOverlays(CavesPlugin plugin, Client client, CavesConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config == null) {
            System.out.println("Config is null");
            return null;
        }
        float fontSize = 20.5f;
        Font customFont = new Font("Verdana", 0, (int)fontSize);
        for (NPC npc : this.client.getNpcs()) {
            Integer tickTimer = this.plugin.getNpcAttackTimers().get(npc.getIndex());
            if (tickTimer == null) continue;
            this.renderActorText(graphics, fontSize, tickTimer.toString(), (Actor)npc, Color.WHITE, 300);
        }
        return null;
    }

    private void renderActorText(Graphics2D graphics, float fontSize, String text, Actor actor, Color color, int zOffset) {
        if (actor == null || actor.getCanvasTilePoly() == null) {
            return;
        }
        Font customFont = new Font("Verdana", 1, (int)fontSize);
        graphics.setFont(customFont);
        Point textLocation = actor.getCanvasTextLocation(graphics, text, zOffset);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)text, (Color)color);
        }
    }

    private void renderTileObjectText(Graphics2D graphics, float fontSize, String text, TileObject tileObject, Color color, int zOffset) {
        if (tileObject == null || tileObject.getCanvasTilePoly() == null) {
            return;
        }
        Font currentFont = graphics.getFont();
        Font newFont = currentFont.deriveFont(fontSize);
        graphics.setFont(newFont);
        Point textLocation = Perspective.getCanvasTextLocation((Client)this.client, (Graphics2D)graphics, (LocalPoint)tileObject.getLocalLocation(), (String)text, (int)zOffset);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)text, (Color)color);
        }
    }

    private void renderTile(Graphics2D graphics, LocalPoint dest, Color color, double borderWidth, Color fillColor, boolean cornersOnly, int divisor) {
        if (dest == null) {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)dest);
        if (poly == null) {
            return;
        }
        if (cornersOnly) {
            TextOverlays.renderPolygonCorners(graphics, poly, color, fillColor, new BasicStroke((float)borderWidth), divisor);
        } else {
            OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)color, (Color)fillColor, (Stroke)new BasicStroke((float)borderWidth));
        }
    }

    public static void renderPolygonCorners(Graphics2D graphics, Polygon poly, Color color, Color fillColor, Stroke borderStroke, int divisor) {
        graphics.setColor(color);
        Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(borderStroke);
        for (int i = 0; i < poly.npoints; ++i) {
            int ptx = poly.xpoints[i];
            int pty = poly.ypoints[i];
            int prev = i - 1 < 0 ? poly.npoints - 1 : i - 1;
            int next = i + 1 > poly.npoints - 1 ? 0 : i + 1;
            int ptxN = (poly.xpoints[next] - ptx) / divisor + ptx;
            int ptyN = (poly.ypoints[next] - pty) / divisor + pty;
            int ptxP = (poly.xpoints[prev] - ptx) / divisor + ptx;
            int ptyP = (poly.ypoints[prev] - pty) / divisor + pty;
            graphics.drawLine(ptx, pty, ptxN, ptyN);
            graphics.drawLine(ptx, pty, ptxP, ptyP);
        }
        graphics.setColor(fillColor);
        graphics.fill(poly);
        graphics.setStroke(originalStroke);
    }

    private void renderTiles(Graphics2D graphics, Set<WorldPoint> worldPoints, Color color, double borderWidth, Color fillColor) {
        for (WorldPoint wp : worldPoints) {
            Polygon poly;
            LocalPoint lp = LocalPoint.fromWorld((Client)this.client, (int)wp.getX(), (int)wp.getY());
            if (lp == null || (poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)lp)) == null) continue;
            OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)color, (Color)fillColor, (Stroke)new BasicStroke((float)borderWidth));
        }
    }

    private void renderTiles(Graphics2D graphics, List<WorldPoint> worldPoints, Color color, double borderWidth, Color fillColor) {
        for (WorldPoint wp : worldPoints) {
            Polygon poly;
            LocalPoint lp = LocalPoint.fromWorld((Client)this.client, (int)wp.getX(), (int)wp.getY());
            if (lp == null || (poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)lp)) == null) continue;
            OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)color, (Color)fillColor, (Stroke)new BasicStroke((float)borderWidth));
        }
    }

    private void renderWorldAreaTiles(Graphics2D graphics, WorldArea worldArea, Color color, double borderWidth, Color fillColor) {
        if (worldArea == null) {
            return;
        }
        for (WorldPoint wp : worldArea.toWorldPointList()) {
            this.drawTile(graphics, wp, color, borderWidth, fillColor);
        }
    }

    private void drawLocalTile(Graphics2D graphics, LocalPoint point, Color fillColor, double borderWidth, Color borderColor) {
        if (point == null) {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)point);
        if (poly == null) {
            return;
        }
        graphics.setColor(fillColor);
        graphics.fill(poly);
        if (borderWidth > 0.0) {
            graphics.setColor(borderColor);
            graphics.setStroke(new BasicStroke((float)borderWidth));
            graphics.draw(poly);
        }
    }

    private void drawTile(Graphics2D graphics, WorldPoint point, Color fillColor, double borderWidth, Color borderColor) {
        if (point.getPlane() != this.client.getPlane()) {
            return;
        }
        LocalPoint lp = LocalPoint.fromWorld((Client)this.client, (WorldPoint)point);
        if (lp == null) {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)lp);
        if (poly == null) {
            return;
        }
        graphics.setColor(fillColor);
        graphics.fill(poly);
        if (borderWidth > 0.0) {
            graphics.setColor(borderColor);
            graphics.setStroke(new BasicStroke((float)borderWidth));
            graphics.draw(poly);
        }
    }

    private void renderWorldTextLocation(Graphics2D graphics, float fontSize, String text, WorldPoint worldPoint, Color color, int zOffset) {
        LocalPoint point = LocalPoint.fromWorld((Client)this.client, (WorldPoint)worldPoint);
        if (point == null) {
            return;
        }
        Font currentFont = graphics.getFont();
        Font newFont = currentFont.deriveFont(fontSize);
        graphics.setFont(newFont);
        Point textLocation = Perspective.getCanvasTextLocation((Client)this.client, (Graphics2D)graphics, (LocalPoint)point, (String)text, (int)zOffset);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)text, (Color)color);
        }
    }

    private void renderLocalTextLocation(Graphics2D graphics, String text, LocalPoint localPoint, Color color, int zOffset) {
        if (localPoint == null) {
            return;
        }
        Point textLocation = Perspective.getCanvasTextLocation((Client)this.client, (Graphics2D)graphics, (LocalPoint)localPoint, (String)text, (int)zOffset);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)text, (Color)color);
        }
    }
}
