package com.lucidplugins.inferno;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.PrayerInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.common.base.Strings;
import com.lucidplugins.api.utils.GameObjectUtils;
import com.lucidplugins.inferno.displaymodes.InfernoPrayerDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoSafespotDisplayMode;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfernoOverlay extends Overlay
{
	private static final int TICK_PIXEL_SIZE = 60;
	private static final int BOX_WIDTH = 10;
	private static final int BOX_HEIGHT = 5;

	private final InfernoPlugin plugin;
	private final InfernoConfig config;
	private final Client client;

	@Inject
	private InfernoOverlay(final Client client, final InfernoPlugin plugin, final InfernoConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(net.runelite.client.ui.overlay.OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Widget meleePrayerWidget = getWidget(Prayer.PROTECT_FROM_MELEE);
		final Widget rangePrayerWidget = getWidget(Prayer.PROTECT_FROM_MISSILES);
		final Widget magicPrayerWidget = getWidget(Prayer.PROTECT_FROM_MAGIC);

		if (config.indicateObstacles())
		{
			renderObstacles(graphics);
		}

		if (config.safespotDisplayMode() == InfernoSafespotDisplayMode.AREA)
		{
			renderAreaSafepots(graphics);
		}
		else if (config.safespotDisplayMode() == InfernoSafespotDisplayMode.INDIVIDUAL_TILES)
		{
			renderIndividualTilesSafespots(graphics);
		}

		if (config != null && config.indicateBlobDeathLocation() && plugin != null && plugin.getBlobDeathSpots() != null && !plugin.getBlobDeathSpots().isEmpty())
		{
			renderBlobDeathPoly(graphics);
		}

		for (InfernoNPC infernoNPC : plugin.getInfernoNpcs())
		{
			if (infernoNPC.getNpc().getConvexHull() != null)
			{
				if (config.indicateNonSafespotted() && plugin.isNormalSafespots(infernoNPC)
					&& infernoNPC.canAttack(client, client.getLocalPlayer().getWorldLocation()))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.RED);
				}
				if (config.indicateTemporarySafespotted() && plugin.isNormalSafespots(infernoNPC)
					&& infernoNPC.canMoveToAttack(client, client.getLocalPlayer().getWorldLocation(), plugin.getObstacles()))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.YELLOW);
				}
				if (config.indicateSafespotted() && plugin.isNormalSafespots(infernoNPC))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.GREEN);
				}
				if (config.indicateNibblers() && infernoNPC.getType() == InfernoNPC.Type.NIBBLER
					&& (!config.indicateCentralNibbler() || plugin.getCentralNibbler() != infernoNPC))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				}
				if (config.indicateCentralNibbler() && infernoNPC.getType() == InfernoNPC.Type.NIBBLER
					&& plugin.getCentralNibbler() == infernoNPC)
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.BLUE);
				}
				if (config.indicateActiveHealerJad() && infernoNPC.getType() == InfernoNPC.Type.HEALER_JAD
					&& infernoNPC.getNpc().getInteracting() != client.getLocalPlayer())
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				}
				if (config.indicateActiveHealerZuk() && infernoNPC.getType() == InfernoNPC.Type.HEALER_ZUK
					&& infernoNPC.getNpc().getInteracting() != client.getLocalPlayer())
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				}
			}

			if (plugin.isIndicateNpcPosition(infernoNPC))
			{
				renderNpcLocation(graphics, infernoNPC);
			}

			if (plugin.isTicksOnNpc(infernoNPC) && infernoNPC.getTicksTillNextAttack() > 0)
			{
				renderTicksOnNpc(graphics, infernoNPC, infernoNPC.getNpc());
			}

			if (config.ticksOnNpcZukShield() && infernoNPC.getType() == InfernoNPC.Type.ZUK && plugin.getZukShield() != null && infernoNPC.getTicksTillNextAttack() > 0)
			{
				renderTicksOnNpc(graphics, infernoNPC, plugin.getZukShield());
			}

			if (config.ticksOnNpcMeleerDig()
				&& infernoNPC.getType() == InfernoNPC.Type.MELEE
				&& infernoNPC.getIdleTicks() >= config.digTimerThreshold()
				&& infernoNPC.getTicksTillNextAttack() == 0) // don't clobber the attack timer
			{
				renderDigTimer(graphics, infernoNPC);
			}
		}

		var prayerWidgetHidden =
			meleePrayerWidget == null
				|| rangePrayerWidget == null
				|| magicPrayerWidget == null
				|| meleePrayerWidget.isHidden()
				|| rangePrayerWidget.isHidden()
				|| magicPrayerWidget.isHidden();

		if ((config.prayerDisplayMode() == InfernoPrayerDisplayMode.PRAYER_TAB
			|| config.prayerDisplayMode() == InfernoPrayerDisplayMode.BOTH)
			&& (!prayerWidgetHidden || config.alwaysShowPrayerHelper()))
		{
			renderPrayerIconOverlay(graphics);

			if (config.descendingBoxes())
			{
				renderDescendingBoxes(graphics);
			}
		}

		return null;
	}

	private void renderObstacles(Graphics2D graphics)
	{
		for (WorldPoint worldPoint : plugin.getObstacles())
		{
			final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

			if (localPoint == null)
			{
				continue;
			}

			final Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

			if (tilePoly == null)
			{
				continue;
			}

			OverlayUtil.renderPolygon(graphics, tilePoly, Color.BLUE);
		}
	}

	private void renderAreaSafepots(Graphics2D graphics)
	{
		if (plugin == null || client == null)
		{
			return;
		}

		plugin.getSafeSpotAreas().forEach((safeSpotId, safeSpots) -> {
			if (safeSpots == null)
			{
				return;
			}

			safeSpots.forEach(safeSpot -> {
				if (safeSpot == null)
				{
					return;
				}

				LocalPoint localPoint = LocalPoint.fromWorld(client, safeSpot);
				if (localPoint == null)
				{
					return;
				}

				Polygon poly = Perspective.getCanvasTileAreaPoly(client, localPoint, 3);
				if (poly != null)
				{
					renderAreaTilePolygon(graphics, poly, getSafespotColor(safeSpotId));
				}
			});
		});
	}

	private void renderDigTimer(Graphics2D g, InfernoNPC npc)
	{
		if (npc == null || npc.getNpc() == null || g == null || config == null || plugin == null)
		{
			return;
		}

		try
		{
			String tickString = String.valueOf(npc.getIdleTicks());
			int fontSize = Math.max(12, config.getMeleeDigFontSize()); // Ensure minimum readable size
			
			// Get the NPC's position and adjust for visibility
			Point canvasLocation = npc.getNpc().getCanvasTextLocation(g, tickString, npc.getNpc().getLogicalHeight() + 40);
			
			if (canvasLocation == null)
			{
				return;
			}

			// Determine color based on dig timer threshold
			Color digColor = npc.getIdleTicks() < config.digTimerDangerThreshold() 
				? config.getMeleeDigSafeColor() 
				: config.getMeleeDigDangerColor();

			// Draw with shadow for better visibility
			renderTextLocation(
				g,
				tickString,
				fontSize,
				Font.BOLD, // Use bold for better visibility
				digColor,
				canvasLocation,
				true, // Enable shadows
				0
			);
		}
		catch (Exception e)
		{
			// Silently handle any rendering errors
		}
	}

	private void renderBlobDeathPoly(Graphics2D graphics)
	{
		if (graphics == null || plugin == null || client == null || 
			!config.indicateBlobDeathLocation() || 
			plugin.getBlobDeathSpots() == null || plugin.getBlobDeathSpots().isEmpty())
		{
			return;
		}

		plugin.getBlobDeathSpots().forEach(blobDeathSpot -> {
			if (blobDeathSpot == null || blobDeathSpot.getLocation() == null)
			{
				return;
			}

			try
			{
				LocalPoint localPoint = LocalPoint.fromWorld(client, blobDeathSpot.getLocation());
				if (localPoint == null)
				{
					return;
				}

				Polygon area = Perspective.getCanvasTileAreaPoly(client, localPoint, 3);
				if (area != null)
				{
					Color color = config.getBlobDeathLocationColor();
					if (config.blobDeathLocationFade())
					{
						color = new Color(
							color.getRed(), 
							color.getGreen(), 
							color.getBlue(), 
							Math.min(255, Math.max(0, blobDeathSpot.getFillAlpha()))
						);
					}

					renderOutlinePolygon(graphics, area, color);

					String ticksText = String.valueOf(blobDeathSpot.getTicksUntilDone());
					Point textLocation = Perspective.getCanvasTextLocation(
						client, 
						graphics, 
						localPoint, 
						ticksText, 
						0
					);

					if (textLocation != null)
					{
						graphics.setFont(new Font("Arial", Font.BOLD, plugin.getTextSize()));
						renderTextLocation(
							graphics,
							ticksText,
							plugin.getTextSize(),
							Font.BOLD,
							color,
							textLocation,
							true,
							0
						);
					}
				}
			}
			catch (Exception ex)
			{
				// Ignore any rendering errors
			}
		});
	}

	private void renderIndividualTilesSafespots(Graphics2D graphics)
	{
		if (plugin == null || client == null)
		{
			return;
		}

		plugin.getSafeSpotMap().forEach((worldPoint, safeSpotId) -> {
			if (worldPoint == null)
			{
				return;
			}

			LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
			if (localPoint == null)
			{
				return;
			}

			Polygon poly = Perspective.getCanvasTileAreaPoly(client, localPoint, 3);
			if (poly != null)
			{
				renderAreaTilePolygon(graphics, poly, getSafespotColor(safeSpotId));
			}
		});
	}

	private void renderTicksOnNpc(Graphics2D graphics, InfernoNPC infernoNPC, NPC renderOnNPC)
	{
		final Color color = (infernoNPC.getTicksTillNextAttack() == 1
			|| (infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() == 4))
			? infernoNPC.getNextAttack().getCriticalColor() : infernoNPC.getNextAttack().getNormalColor();

		graphics.setFont(new Font("Arial", plugin.getFontStyle().getFont(), plugin.getTextSize()));

		final Point canvasPoint = renderOnNPC.getCanvasTextLocation(
			graphics, String.valueOf(infernoNPC.getTicksTillNextAttack()), 0);
		renderTextLocation(graphics, String.valueOf(infernoNPC.getTicksTillNextAttack()),
			plugin.getTextSize(), plugin.getFontStyle().getFont(), color, canvasPoint, false, 0);
	}

	private void renderNpcLocation(Graphics2D graphics, InfernoNPC infernoNPC)
	{
		final LocalPoint localPoint = LocalPoint.fromWorld(client, infernoNPC.getNpc().getWorldLocation());

		if (localPoint != null)
		{
			final Polygon tilePolygon = Perspective.getCanvasTilePoly(client, localPoint);

			if (tilePolygon != null)
			{
				OverlayUtil.renderPolygon(graphics, tilePolygon, Color.BLUE);
			}
		}
	}

	private Widget getWidget(Prayer prayer)
	{
		if (client == null)
		{
			return null;
		}

		int widgetId;
		switch (prayer)
		{
			case PROTECT_FROM_MELEE:
				return client.getWidget(541, 17);
			case PROTECT_FROM_MISSILES:
				return client.getWidget(541, 18);
			case PROTECT_FROM_MAGIC:
				return client.getWidget(541, 19);
			default:
				return null;
		}
	}

	private void renderDescendingBoxes(Graphics2D graphics)
	{
		for (Integer tick : plugin.getUpcomingAttacks().keySet())
		{
			final Map<InfernoNPC.Attack, Integer> attackPriority = plugin.getUpcomingAttacks().get(tick);
			int bestPriority = 999;
			InfernoNPC.Attack bestAttack = null;

			for (Map.Entry<InfernoNPC.Attack, Integer> attackEntry : attackPriority.entrySet())
			{
				if (attackEntry.getValue() < bestPriority)
				{
					bestAttack = attackEntry.getKey();
					bestPriority = attackEntry.getValue();
				}
			}

			for (InfernoNPC.Attack currentAttack : attackPriority.keySet())
			{
				//TODO: Config values for these colors
				final Color color = (tick == 1 && currentAttack == bestAttack) ? Color.RED : Color.ORANGE;
				final Widget prayerWidget = getWidget(currentAttack.getPrayer());

				int baseX = (int) prayerWidget.getBounds().getX();
				baseX += prayerWidget.getBounds().getWidth() / 2;
				baseX -= BOX_WIDTH / 2;

				int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BOX_HEIGHT;
				baseY += TICK_PIXEL_SIZE - ((plugin.getLastTick() + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

				final Rectangle boxRectangle = new Rectangle(BOX_WIDTH, BOX_HEIGHT);
				boxRectangle.translate(baseX, baseY);

				if (currentAttack == bestAttack)
				{
					renderFilledPolygon(graphics, boxRectangle, color);
				}
				else if (config.indicateNonPriorityDescendingBoxes())
				{
					renderOutlinePolygon(graphics, boxRectangle, color);
				}
			}
		}
	}

	private void renderPrayerIconOverlay(Graphics2D graphics)
	{
		if (plugin.getClosestAttack() != null && client != null)
		{
			InfernoNPC.Attack prayerForAttack = null;
			
			if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
			{
				prayerForAttack = InfernoNPC.Attack.MAGIC;
			}
			else if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
			{
				prayerForAttack = InfernoNPC.Attack.RANGED;
			}
			else if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE))
			{
				prayerForAttack = InfernoNPC.Attack.MELEE;
			}

			if (plugin.getClosestAttack() != prayerForAttack || config.indicateWhenPrayingCorrectly())
			{
				final Widget prayerWidget = getWidget(plugin.getClosestAttack().getPrayer());
				if (prayerWidget != null)
				{
					final Rectangle prayerRectangle = new Rectangle(
						(int) prayerWidget.getBounds().getWidth(),
						(int) prayerWidget.getBounds().getHeight()
					);
					prayerRectangle.translate(
						(int) prayerWidget.getBounds().getX(),
						(int) prayerWidget.getBounds().getY()
					);

					Color prayerColor = plugin.getClosestAttack() == prayerForAttack ? Color.GREEN : Color.RED;
					renderOutlinePolygon(graphics, prayerRectangle, prayerColor);
				}
			}
		}
	}

	private boolean edgeEqualsEdge(int[][] edge1, int[][] edge2, int toleranceSquared)
	{
		return (pointEqualsPoint(edge1[0], edge2[0], toleranceSquared) && pointEqualsPoint(edge1[1], edge2[1], toleranceSquared))
			|| (pointEqualsPoint(edge1[0], edge2[1], toleranceSquared) && pointEqualsPoint(edge1[1], edge2[0], toleranceSquared));
	}

	private boolean pointEqualsPoint(int[] point1, int[] point2, int toleranceSquared)
	{
		double distanceSquared = Math.pow(point1[0] - point2[0], 2) + Math.pow(point1[1] - point2[1], 2);

		return distanceSquared <= toleranceSquared;
	}

	private Color getSafespotColor(Integer safeSpotId) {
        switch (safeSpotId) {
            case 1:
                return Color.GREEN;  // Safe
            case 2:
                return Color.YELLOW; // Temporary safe
            case 3:
                return Color.RED;    // Not safe
            default:
                return Color.WHITE;  // Unknown
        }
    }

    private void renderAreaTilePolygon(Graphics2D graphics, Shape poly, Color color) {
        if (poly != null) {
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            graphics.fill(poly);
            graphics.setColor(color);
            graphics.draw(poly);
        }
    }

	public static void renderFullLine(Graphics2D graphics, int[][] line, Color color)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2));
		graphics.drawLine(line[0][0], line[0][1], line[1][0], line[1][1]);
		graphics.setStroke(originalStroke);
	}

	public static void renderDashedLine(Graphics2D graphics, int[][] line, Color color)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
		graphics.drawLine(line[0][0], line[0][1], line[1][0], line[1][1]);
		graphics.setStroke(originalStroke);
	}

	public static void renderOutlinePolygon(Graphics2D graphics, Shape poly, Color color)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(poly);
		graphics.setStroke(originalStroke);
	}

	public static void renderFilledPolygon(Graphics2D graphics, Shape poly, Color color)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(poly);
		graphics.fill(poly);
		graphics.setStroke(originalStroke);
	}

	public static void renderTextLocation(Graphics2D graphics, Point txtLoc, String text, Color color)
	{
		if (Strings.isNullOrEmpty(text))
		{
			return;
		}

		int x = txtLoc.getX();
		int y = txtLoc.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}

	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset)
	{
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX(),
				canvasPoint.getY() + yOffset);
			final Point canvasCenterPoint_shadow = new Point(
				canvasPoint.getX() + 1,
				canvasPoint.getY() + 1 + yOffset);
			if (shadows)
			{
				renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}
}
