package hardcore;

import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.content.Blocks.*;
import static mindustry.content.UnitTypes.*;
import static mindustry.Vars.*;

public class GameWork {

	public static final Block[] drills = {
			mechanicalDrill, pneumaticDrill, laserDrill, blastDrill, waterExtractor, oilExtractor, cultivator,
			cliffCrusher, plasmaBore, largePlasmaBore, impactDrill, eruptionDrill,
	};

	public static Block randomDrill() {
		return drills[Mathf.random(0, drills.length-1)];
	}
	
	public static void itemExplosion(Item item, int amount, float x, float y) {
		float explosiveness = 2f + item.explosiveness * amount * 1.53f;
		float flammability = item.flammability * amount / 1.9f;
		float power = item.charge * Mathf.pow(amount, 1.11f) * 160f;
		Damage.dynamicExplosion(x, y, flammability, explosiveness, power, amount*tilesize / 3f, true, item.flammability > Math.random(), Team.derelict, Fx.blastExplosion);
	}

	public static Player randomPlayer() {
		int count = Groups.player.size();
		if(count == 0) return null;
		return Groups.player.index(Mathf.random(0, count-1));
	}

	public static Building randomBuilding(Block block) {
		Player randomPlayer = GameWork.randomPlayer();
		if(randomPlayer == null) return null;
		if(randomPlayer.team() == null) return null;
		if(randomPlayer.team().data() == null) return null;
		return randomPlayer.team().data().getBuildings(block).random();
	}

	public static void unitStatusEffect(Unit unit, float durationMultiplier) {
		UnitType type = unit.type;
		StatusEffect effect = null;

		float duration = 0;
		float rad = 0;

		if(type == dagger) {	effect = StatusEffects.blasted;			duration = 1;	rad = 18;}
		if(type == mace) {		effect = StatusEffects.burning;			duration = 10; 	rad = 12;}
		if(type == fortress) {	effect = StatusEffects.blasted;			duration = 1; 	rad = 29;}
		if(type == scepter) {	effect = StatusEffects.wet;				duration = 40; 	rad = 42;}
		if(type == reign) {		effect = StatusEffects.blasted;			duration = 1; 	rad = 49;}

		if(type == nova) {		effect = StatusEffects.electrified; 	duration = 10; 	rad = 19;}
		if(type == pulsar) {	effect = StatusEffects.wet; 			duration = 20; 	rad = 14;}
		if(type == quasar) {	effect = StatusEffects.electrified; 	duration = 30; 	rad = 16;}
		if(type == vela) {		effect = StatusEffects.burning; 		duration = 40; 	rad = 22;}
		if(type == corvus) {	effect = StatusEffects.unmoving; 		duration = 25; 	rad = 57;}

		if(type == crawler) {	effect = StatusEffects.sporeSlowed; 	duration = 10; 	rad = 10;}
		if(type == atrax) {		effect = StatusEffects.tarred; 			duration = 20; 	rad = 26;}
		if(type == spiroct) {	effect = StatusEffects.sapped; 			duration = 30; 	rad = 16;}
		if(type == arkyid) {	effect = StatusEffects.sapped; 			duration = 40; 	rad = 34;}
		if(type == toxopid) {	effect = StatusEffects.sapped; 			duration = 50; 	rad = 58;}

		if(type == flare) {		effect = StatusEffects.burning; 		duration = 5; 	rad = 13;}
		if(type == horizon) {	effect = StatusEffects.freezing; 		duration = 10; 	rad = 26;}
		if(type == zenith) {	effect = StatusEffects.melting; 		duration = 10; 	rad = 30;}
		if(type == antumbra) {	effect = StatusEffects.freezing; 		duration = 15; 	rad = 32;}
		if(type == eclipse) {	effect = StatusEffects.freezing; 		duration = 20; 	rad = 42;}

		if(type == mono) {		effect = StatusEffects.unmoving; 		duration = 2; 	rad = 16;}
		if(type == poly) {		effect = StatusEffects.electrified; 	duration = 20; 	rad = 24;}
		if(type == mega) {		effect = StatusEffects.electrified; 	duration = 30; 	rad = 33;}
		if(type == quad) {		effect = StatusEffects.unmoving; 		duration = 5; 	rad = 17;}
		if(type == oct) {		effect = StatusEffects.unmoving; 		duration = 10; 	rad = 34;}

		if(type == risso) {		effect = StatusEffects.shocked; 		duration = 5; 	rad = 23;}
		if(type == minke) {		effect = StatusEffects.shocked; 		duration = 10; 	rad = 30;}
		if(type == bryde) {		effect = StatusEffects.freezing; 		duration = 10; 	rad = 33;}
		if(type == sei) {		effect = StatusEffects.shocked; 		duration = 20; 	rad = 35;}
		if(type == omura) {		effect = StatusEffects.shocked; 		duration = 25; 	rad = 62;}
		
		if(type == retusa) {	effect = StatusEffects.electrified; 	duration = 5; 	rad = 16;}
		if(type == oxynoe) {	effect = StatusEffects.freezing; 		duration = 10; 	rad = 24;}
		if(type == cyerce) {	effect = StatusEffects.electrified; 	duration = 15; 	rad = 27;}
		if(type == aegires) {	effect = StatusEffects.electrified; 	duration = 20; 	rad = 33;}
		if(type == navanax) {	effect = StatusEffects.melting; 		duration = 30; 	rad = 40;}

		if(type == stell) {		effect = StatusEffects.burning; 		duration = 5; 	rad = 19;}
		if(type == locus) {		effect = StatusEffects.melting; 		duration = 5; 	rad = 19;}
		if(type == precept) {	effect = StatusEffects.burning; 		duration = 10; 	rad = 24;}
		if(type == vanquish) {	effect = StatusEffects.melting; 		duration = 10; 	rad = 25;}
		if(type == conquer) {	effect = StatusEffects.melting; 		duration = 15; 	rad = 35;}

		if(type == merui) {		effect = StatusEffects.freezing; 		duration = 5; 	rad = 18;}
		if(type == cleroi) {	effect = StatusEffects.freezing; 		duration = 10; 	rad = 22;}
		if(type == anthicus) {	effect = StatusEffects.freezing; 		duration = 15; 	rad = 41;}
		if(type == tecta) {		effect = StatusEffects.freezing; 		duration = 20; 	rad = 42;}
		if(type == collaris) {	effect = StatusEffects.freezing; 		duration = 25; 	rad = 47;}

		if(type == elude) {		effect = StatusEffects.overdrive; 		duration = 10; 	rad = 9;}
		if(type == avert) {		effect = StatusEffects.sapped; 			duration = 20; 	rad = 20;}
		if(type == obviate) {	effect = StatusEffects.wet; 			duration = 30; 	rad = 33;}
		if(type == quell) {		effect = StatusEffects.sapped; 			duration = 40; 	rad = 49;}
		if(type == disrupt) {	effect = StatusEffects.electrified; duration = 50; 	rad = 87;}

		if(effect == null) return;
		if(duration == 0) return;
		
		duration *= 60;
		Damage.status(null, unit.x, unit.y, rad*tilesize, effect, duration*durationMultiplier, true, true);		
	}
	
	public static boolean hasNearFloor(Block floor, int x, int y) {
		return hasFloor(floor, x-1, y) 
				|| hasFloor(floor, x+1, y)
				|| hasFloor(floor, x, y-1)
				|| hasFloor(floor, x, y+1);
	}

	public static boolean hasFloor(Block floor, int x, int y) {
		Tile tile = world.tile(x, y);
		if(tile == null) return false;
		return tile.floor() == floor;
	}
}
