package com.lucidplugins.inferno;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import com.lucidplugins.inferno.displaymodes.InfernoNamingDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoPrayerDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoSafespotDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoWaveDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode;

@ConfigGroup(value="inferno")
public interface InfernoConfig
extends Config {
    @ConfigItem(name="Api", description="Configure your api  here", position=0, keyName="api")
    default public boolean api() {
        return false;
    }

    @ConfigItem(name="Api Key", keyName="apiKey", description="Enter your api key here", position=0, section="api")
    default public String apiKey() {
        return "SB-";
    }

    @ConfigItem(name="Api Name", keyName="apiName", description="Enter your api name here", position=0, section="api")
    default public String apiName() {
        return "none";
    }

    @ConfigItem(name="Mirror Mode Compatibility?", keyName="mirrorMode", description="Should we show the overlay on Mirror Mode?", position=0)
    default public boolean mirrorMode() {
        return false;
    }

    @ConfigItem(name="Prayer", description="Configuration options forPprayer", position=0, keyName="PrayerSection")
    default public boolean prayerSection() {
        return false;
    }

    @ConfigItem(name="Safespots", description="Configuration options for Safespots", position=1, keyName="SafespotsSection")
    default public boolean safespotsSection() {
        return false;
    }

    @ConfigItem(name="Waves", description="Configuration options for Waves", position=2, keyName="WavesSection")
    default public boolean wavesSection() {
        return false;
    }

    @ConfigItem(name="ExtraSection", description="Configuration options for Extras", position=3, keyName="ExtraSection")
    default public boolean extraSection() {
        return false;
    }

    @ConfigItem(name="Nibblers", description="Configuration options for Nibblers", position=4, keyName="NibblersSection")
    default public boolean nibblersSection() {
        return false;
    }

    @ConfigItem(name="Bats", description="Configuration options for Bats", position=5, keyName="BatsSection")
    default public boolean batsSection() {
        return false;
    }

    @ConfigItem(name="Blobs", description="Configuration options for Blobs", position=6, keyName="BlobsSection")
    default public boolean blobsSection() {
        return false;
    }

    @ConfigItem(name="Meleers", description="Configuration options for Meleers", position=7, keyName="MeleersSection")
    default public boolean meleersSection() {
        return false;
    }

    @ConfigItem(name="Rangers", description="Configuration options for Rangers", position=8, keyName="RangersSection")
    default public boolean rangersSection() {
        return false;
    }

    @ConfigItem(name="Magers", description="Configuration options for Magers", position=9, keyName="MagersSection")
    default public boolean magersSection() {
        return false;
    }

    @ConfigItem(name="Jad", description="Configuration options for Jad", position=10, keyName="JadSection")
    default public boolean jadSection() {
        return false;
    }

    @ConfigItem(name="Jad Healers", description="Configuration options for Jad Healers", position=11, keyName="JadHealersSection")
    default public boolean jadHealersSection() {
        return false;
    }

    @ConfigItem(name="Zuk", description="Configuration options for  Zuk", position=12, keyName="ZukSection")
    default public boolean zukSection() {
        return false;
    }

    @ConfigItem(name="Zuk Healers", description="Configuration options for Zuk Healers", position=13, keyName="ZukHealersSection")
    default public boolean zukHealersSection() {
        return false;
    }

    @ConfigItem(position=0, keyName="prayerDisplayMode", name="Prayer Display Mode", description="Display prayer indicator in the prayer tab or in the bottom right corner of the screen", section="PrayerSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoPrayerDisplayMode prayerDisplayMode() {
        return com.lucidplugins.inferno.displaymodes.InfernoPrayerDisplayMode.BOTH;
    }

    @ConfigItem(position=1, keyName="indicateWhenPrayingCorrectly", name="Indicate When Praying Correctly", description="Indicate the correct prayer, even if you are already praying that prayer", section="PrayerSection")
    default public boolean indicateWhenPrayingCorrectly() {
        return true;
    }

    @ConfigItem(position=2, keyName="descendingBoxes", name="Descending Boxes", description="Draws timing boxes above the prayer icons, as if you were playing Piano Tiles", section="PrayerSection")
    default public boolean descendingBoxes() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateNonPriorityDescendingBoxes", name="Indicate Non-Priority Boxes", description="Render descending boxes for prayers that are not the priority prayer for that tick", section="PrayerSection")
    default public boolean indicateNonPriorityDescendingBoxes() {
        return true;
    }

    @ConfigItem(position=4, keyName="alwaysShowPrayerHelper", name="Always Show Prayer Helper", description="Render prayer helper at all time, even when other inventory tabs are open.", section="PrayerSection")
    default public boolean alwaysShowPrayerHelper() {
        return false;
    }

    @ConfigItem(position=4, keyName="safespotDisplayMode", name="Tile Safespots", description="Indicate safespots on the ground: safespot (white), pray melee (red), pray range (green), pray magic (blue) and combinations of those", section="SafespotsSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoSafespotDisplayMode safespotDisplayMode() {
        return com.lucidplugins.inferno.displaymodes.InfernoSafespotDisplayMode.AREA;
    }

    @ConfigItem(position=5, keyName="safespotsCheckSize", name="Tile Safespots Check Size", description="The size of the area around the player that should be checked for safespots (SIZE x SIZE area)", section="SafespotsSection")
    default public int safespotsCheckSize() {
        return 6;
    }

    @ConfigItem(position=6, keyName="indicateNonSafespotted", name="Non-safespotted NPC's Overlay", description="Red overlay for NPC's that can attack you", section="SafespotsSection")
    default public boolean indicateNonSafespotted() {
        return false;
    }

    @ConfigItem(position=7, keyName="indicateTemporarySafespotted", name="Temporary safespotted NPC's Overlay", description="Orange overlay for NPC's that have to move to attack you", section="SafespotsSection")
    default public boolean indicateTemporarySafespotted() {
        return false;
    }

    @ConfigItem(position=8, keyName="indicateSafespotted", name="Safespotted NPC's Overlay", description="Green overlay for NPC's that are safespotted (can't attack you)", section="SafespotsSection")
    default public boolean indicateSafespotted() {
        return false;
    }

    @ConfigItem(position=0, keyName="waveDisplay", name="Wave Display", description="Shows monsters that will spawn on the selected wave(s).", section="WavesSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoWaveDisplayMode waveDisplay() {
        return com.lucidplugins.inferno.displaymodes.InfernoWaveDisplayMode.BOTH;
    }

    @ConfigItem(position=1, keyName="npcNaming", name="NPC Naming", description="Simple (ex: Bat) or Complex (ex: Jal-MejRah) NPC naming", section="WavesSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoNamingDisplayMode npcNaming() {
        return com.lucidplugins.inferno.displaymodes.InfernoNamingDisplayMode.SIMPLE;
    }

    @ConfigItem(position=2, keyName="npcLevels", name="NPC Levels", description="Show the combat level of the NPC next to their name", section="WavesSection")
    default public boolean npcLevels() {
        return false;
    }

    @ConfigItem(position=3, keyName="getWaveOverlayHeaderColor", name="Wave Header", description="Color for Wave Header", section="WavesSection")
    default public Color getWaveOverlayHeaderColor() {
        return Color.ORANGE;
    }

    @ConfigItem(position=4, keyName="getWaveTextColor", name="Wave Text Color", description="Color for Wave Texts", section="WavesSection")
    default public Color getWaveTextColor() {
        return Color.WHITE;
    }

    @ConfigItem(position=0, keyName="indicateObstacles", name="Obstacles", description="Indicate obstacles that NPC's cannot pass through", section="ExtraSection")
    default public boolean indicateObstacles() {
        return false;
    }

    @ConfigItem(position=1, keyName="spawnTimerInfobox", name="Spawn Timer Infobox", description="Display an Infobox that times spawn sets during Zuk fight.", section="ExtraSection")
    default public boolean spawnTimerInfobox() {
        return false;
    }

    @ConfigItem(position=0, keyName="indicateNibblers", name="Indicate Nibblers", description="Indicate's nibblers that are alive", section="NibblersSection")
    default public boolean indicateNibblers() {
        return true;
    }

    @ConfigItem(position=1, keyName="hideJalNibDeath", name="Hide On Death", description="Hide Nibblers on death animation", section="NibblersSection")
    default public boolean hideNibblerDeath() {
        return false;
    }

    @ConfigItem(position=2, keyName="indicateCentralNibbler", name="Indicate Central Nibbler", description="Indicate the most central nibbler. If multiple nibblers will freeze the same amount of other nibblers, the nibbler closest to the player's location is chosen.", section="NibblersSection")
    default public boolean indicateCentralNibbler() {
        return true;
    }

    @ConfigItem(position=0, keyName="prayerBat", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="BatsSection")
    default public boolean prayerBat() {
        return true;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcBat", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="BatsSection")
    default public boolean ticksOnNpcBat() {
        return true;
    }

    @ConfigItem(position=2, keyName="safespotsBat", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="BatsSection")
    default public boolean safespotsBat() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateNpcPositionBat", name="Indicate Main Tile", description="Indicate the main tile for multi-tile NPC's. This tile is used for and pathfinding.", section="BatsSection")
    default public boolean indicateNpcPositionBat() {
        return false;
    }

    @ConfigItem(position=4, keyName="hideJalMejRahDeath", name="Hide On Death", description="Hide Jal-MejRah on death animation", section="BatsSection")
    default public boolean hideBatDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="prayerBlob", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="BlobsSection")
    default public boolean prayerBlob() {
        return true;
    }

    @ConfigItem(position=1, keyName="indicateBlobDetectionTick", name="Indicate Blob Dection Tick", description="Show a prayer indicator (default: magic) for the tick on which the blob will detect prayer", section="BlobsSection")
    default public boolean indicateBlobDetectionTick() {
        return true;
    }

    @ConfigItem(position=2, keyName="ticksOnNpcBlob", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="BlobsSection")
    default public boolean ticksOnNpcBlob() {
        return true;
    }

    @ConfigItem(position=3, keyName="safespotsBlob", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="BlobsSection")
    default public boolean safespotsBlob() {
        return true;
    }

    @ConfigItem(position=4, keyName="indicateNpcPositionBlob", name="Indicate Main Tile", description="Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", section="BlobsSection")
    default public boolean indicateNpcPositionBlob() {
        return false;
    }

    @ConfigItem(position=5, keyName="hideJalAkDeath", name="Hide Blob On Death", description="Hide Jal-Ak on death animation", section="BlobsSection")
    default public boolean hideBlobDeath() {
        return false;
    }

    @ConfigItem(position=6, keyName="hideJalAkRekXilDeath", name="Hide Small Range Blob On Death", description="Hide Jal-AkRek-Xil on death animation", section="BlobsSection")
    default public boolean hideBlobSmallRangedDeath() {
        return false;
    }

    @ConfigItem(position=7, keyName="hideJalAkRekMejDeath", name="Hide Small Magic Blob On Death", description="Hide Jal-AkRek-Mej on death animation", section="BlobsSection")
    default public boolean hideBlobSmallMagicDeath() {
        return false;
    }

    @ConfigItem(position=8, keyName="hideJalAkRekKetDeath", name="Hide Small Melee Blob On Death", description="Hide Jal-AkRek-Ket on death animation", section="BlobsSection")
    default public boolean hideBlobSmallMeleeDeath() {
        return false;
    }

    @ConfigItem(position=9, keyName="indicateBlobDeathLocation", name="Indicate Blob Death Location", description="Indicate where blobs will spawn their split forms", section="BlobsSection")
    default public boolean indicateBlobDeathLocation() {
        return true;
    }

    @ConfigItem(position=10, keyName="blobDeathLocationColor", name="Blob Death Location Color", description="Color of the blob death location indicator", section="BlobsSection")
    default public Color getBlobDeathLocationColor() {
        return Color.CYAN;
    }

    @ConfigItem(position=11, keyName="blobDeathLocationFade", name="Blob Death Location Fade", description="Whether the blob death location indicator should fade out over time", section="BlobsSection")
    default public boolean blobDeathLocationFade() {
        return true;
    }

    @ConfigItem(position=12, keyName="blobDeathLocationDuration", name="Blob Death Location Duration", description="How many ticks the blob death location should remain visible", section="BlobsSection")
    @Range(min=1, max=50)
    default public int getBlobDeathLocationDuration() {
        return 10;
    }

    @ConfigItem(position=0, keyName="prayerMeleer", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="MeleersSection")
    default public boolean prayerMeleer() {
        return true;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcMeleer", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="MeleersSection")
    default public boolean ticksOnNpcMeleer() {
        return true;
    }

    @ConfigItem(position=2, keyName="safespotsMeleer", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="MeleersSection")
    default public boolean safespotsMeleer() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateNpcPositionMeleer", name="Indicate Main Tile", description="Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", section="MeleersSection")
    default public boolean indicateNpcPositionMeleer() {
        return false;
    }

    @ConfigItem(position=4, keyName="hideJalImKotDeath", name="Hide On Death", description="Hide Jal-ImKot on death animation", section="MeleersSection")
    default public boolean hideMeleerDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="ticksOnNpcMeleerDig", name="Ticks on NPC Dig", description="Draws the amount of ticks before a melee NPC digs", section="MeleersSection")
    default public boolean ticksOnNpcMeleerDig() {
        return true;
    }

    @ConfigItem(position=1, keyName="digTimerThreshold", name="Dig Timer Threshold", description="Number of ticks before dig timer appears", section="MeleersSection")
    @Range(min=1, max=10)
    default public int digTimerThreshold() {
        return 5;
    }

    @ConfigItem(position=2, keyName="digTimerDangerThreshold", name="Dig Timer Danger Threshold", description="Number of ticks before dig timer turns red", section="MeleersSection")
    @Range(min=1, max=10)
    default public int digTimerDangerThreshold() {
        return 2;
    }

    @ConfigItem(position=3, keyName="meleeDigFontSize", name="Dig Timer Font Size", description="Font size for the dig timer", section="MeleersSection")
    @Range(min=12, max=64)
    default public int getMeleeDigFontSize() {
        return 14;
    }

    @ConfigItem(position=4, keyName="meleeDigSafeColor", name="Dig Timer Safe Color", description="Color for the dig timer when above danger threshold", section="MeleersSection")
    default public Color getMeleeDigSafeColor() {
        return Color.GREEN;
    }

    @ConfigItem(position=5, keyName="meleeDigDangerColor", name="Dig Timer Danger Color", description="Color for the dig timer when below danger threshold", section="MeleersSection")
    default public Color getMeleeDigDangerColor() {
        return Color.RED;
    }

    @ConfigItem(position=0, keyName="prayerRanger", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="RangersSection")
    default public boolean prayerRanger() {
        return true;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcRanger", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="RangersSection")
    default public boolean ticksOnNpcRanger() {
        return true;
    }

    @ConfigItem(position=2, keyName="safespotsRanger", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="RangersSection")
    default public boolean safespotsRanger() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateNpcPositionRanger", name="Indicate Main Tile", description="Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", section="RangersSection")
    default public boolean indicateNpcPositionRanger() {
        return false;
    }

    @ConfigItem(position=4, keyName="hideJalXilDeath", name="Hide On Death", description="Hide Jal-Xil on death animation", section="RangersSection")
    default public boolean hideRangerDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="prayerMage", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="MagersSection")
    default public boolean prayerMage() {
        return true;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcMage", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="MagersSection")
    default public boolean ticksOnNpcMage() {
        return true;
    }

    @ConfigItem(position=2, keyName="safespotsMage", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="MagersSection")
    default public boolean safespotsMage() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateNpcPositionMage", name="Indicate Main Tile", description="Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", section="MagersSection")
    default public boolean indicateNpcPositionMage() {
        return false;
    }

    @ConfigItem(position=4, keyName="hideJalZekDeath", name="Hide On Death", description="Hide Jal-Zek on death animation", section="MagersSection")
    default public boolean hideMagerDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="prayerHealersJad", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="JadHealersSection")
    default public boolean prayerHealerJad() {
        return false;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcHealersJad", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="JadHealersSection")
    default public boolean ticksOnNpcHealerJad() {
        return false;
    }

    @ConfigItem(position=2, keyName="safespotsHealersJad", name="Safespots", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="JadHealersSection")
    default public boolean safespotsHealerJad() {
        return true;
    }

    @ConfigItem(position=3, keyName="indicateActiveHealersJad", name="Indicate Active Healers", description="Indicate healers that are still healing Jad", section="JadHealersSection")
    default public boolean indicateActiveHealerJad() {
        return true;
    }

    @ConfigItem(position=4, keyName="hideYtHurKotDeath", name="Hide On Death", description="Hide Yt-HurKot on death animation", section="JadHealersSection")
    default public boolean hideHealerJadDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="prayerJad", name="Prayer Helper", description="Indicate the correct prayer when this NPC attacks", section="JadSection")
    default public boolean prayerJad() {
        return true;
    }

    @ConfigItem(position=1, keyName="ticksOnNpcJad", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="JadSection")
    default public boolean ticksOnNpcJad() {
        return true;
    }

    @ConfigItem(position=2, keyName="safespotsJad", name="Safespots (Melee Range Only)", description="Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="JadSection")
    default public boolean safespotsJad() {
        return true;
    }

    @ConfigItem(position=3, keyName="hideJalTokJadDeath", name="Hide On Death", description="Hide JalTok-Jad on death animation", section="JadSection")
    default public boolean hideJadDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="indicateActiveHealersZuk", name="Indicate Active Healers (UNTESTED)", description="Indicate healers that are still healing Zuk", section="ZukHealersSection")
    default public boolean indicateActiveHealerZuk() {
        return true;
    }

    @ConfigItem(position=1, keyName="hideJalMejJakDeath", name="Hide On Death", description="Hide Jal-MejJak on death animation", section="ZukHealersSection")
    default public boolean hideHealerZukDeath() {
        return false;
    }

    @ConfigItem(position=0, keyName="ticksOnNpcZuk", name="Ticks on NPC", description="Draws the amount of ticks before an NPC is going to attack on the NPC", section="ZukSection")
    default public boolean ticksOnNpcZuk() {
        return true;
    }

    @ConfigItem(position=1, keyName="safespotsZukShieldBeforeHealers", name="Safespots (Before Healers)", description="Indicate the zuk shield safespots. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect. Shield must go back and forth at least 1 time before the predict option will work.", section="ZukSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode safespotsZukShieldBeforeHealers() {
        return com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode.PREDICT;
    }

    @ConfigItem(position=2, keyName="safespotsZukShieldAfterHealers", name="Safespots (After Healers)", description="Indicate the zuk shield safespots. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", section="ZukSection")
    default public com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode safespotsZukShieldAfterHealers() {
        return com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode.LIVE;
    }

    @ConfigItem(position=3, keyName="hideTzKalZukDeath", name="Hide On Death", description="Hide TzKal-Zuk on death animation", section="ZukSection")
    default public boolean hideZukDeath() {
        return false;
    }

    @ConfigItem(position=4, keyName="ticksOnNpcZukShield", name="Ticks on Zuk Shield", description="Draws the amount of ticks before Zuk attacks on the floating shield", section="ZukSection")
    default public boolean ticksOnNpcZukShield() {
        return false;
    }

    public static enum FontStyle {
        BOLD("Bold", 1),
        ITALIC("Italic", 2),
        PLAIN("Plain", 0);

        private String name;
        private int font;

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this.name;
        }

        public int getFont() {
            return this.font;
        }

        private FontStyle(String name, int font) {
            this.name = name;
            this.font = font;
        }
    }
}
