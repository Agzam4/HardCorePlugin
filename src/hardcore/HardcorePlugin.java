package hardcore;

import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.Damage;
import mindustry.entities.abilities.ArmorPlateAbility;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.maps.Maps.ShuffleMode;
import mindustry.mod.Plugin;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicBuild;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

import java.util.concurrent.TimeUnit;


public class HardcorePlugin extends Plugin {

	public static final String PLUGIN_NAME = "hardcore-plugin";
	public static final String VERSION = "v1.0";

	public ObjectMap<String, Team> playersTeams;
	public int updates = 0;
	public boolean needSetRules = true;
	/**
	 * 
	 */
	
    @Override
    public void init() {
    	playersTeams = new ObjectMap<>();
    	
    	maps.setShuffleMode(ShuffleMode.custom);
		
		Events.run(Trigger.update, () -> {
			updates++;
			
			if(updates%60 == 0) {
				int randomEvent = Mathf.random(0, 4);
				if(randomEvent == 0) updatePuddle();
				if(randomEvent == 1) updatePowerNode();
				if(randomEvent == 2) updateDrill();
				if(randomEvent == 3) updateMassDriver();
				if(randomEvent == 4) updateTraps();
			}
			
			if(needSetRules && updates > 60) {
				state.rules.unitAmmo = true;
				state.rules.reactorExplosions = true;
				state.rules.deconstructRefundMultiplier = 0;
				state.rules.pvp = true;
				state.rules.blockDamageMultiplier = 5;
				state.rules.blockHealthMultiplier = 7;
				state.rules.unitDamageMultiplier = .75f;
				state.rules.unitCap = 24;
				Call.setRules(state.rules);
				
				needSetRules = false;
			}
		});
		
		Events.on(WorldLoadEndEvent.class, e -> {
			needSetRules = true;
		});

		Events.on(DepositEvent.class, e -> {
			if(e.tile == null) return;
			if(e.item == null) return;
			
			GameWork.itemExplosion(e.item, e.amount, e.tile.x, e.tile.y);
		});
		
		Events.on(PayloadDropEvent.class, e -> {
			if(e.unit != null) {
				float size = e.unit.hitSize;
				Time.run(5, () -> {
					Damage.damage(Team.derelict, e.unit.x, e.unit.y, size, e.unit.maxHealth/4, true, false, true);
					Damage.damage(Team.derelict, e.unit.x, e.unit.y, size*2, e.unit.maxHealth/4, false, false, true);
	            });
			}
			if(e.build != null) {
				int size = e.build.block.size;
				Time.run(5, () -> {
					Damage.damage(Team.derelict, e.build.x, e.build.y, size*tilesize, e.build.maxHealth, true, false, true);
					Damage.damage(Team.derelict, e.build.x, e.build.y, size*2*tilesize, e.build.maxHealth, false, false, true);
	            });
			}
		});

		Events.on(UnitDamageEvent.class, e -> {
			if(e.unit != null) {
				if(Math.random()*e.unit.maxHealth > e.unit.health) {
					GameWork.unitStatusEffect(e.unit, 0.1f);
				}
			}
		});
		
		Events.on(UnitBulletDestroyEvent.class, e -> {
			if(e.unit != null) {
				GameWork.unitStatusEffect(e.unit, 1);
			}
		});

		Events.on(WorldLoadBeginEvent.class, e -> {
			world.setGenerating(true);
        });

		Events.on(WorldLoadEndEvent.class, e -> {
			traps = new Seq<>();
			
			for (int y = 0; y < world.height(); y++) {
				for (int x = 0; x < world.width(); x++) {
					Tile tile = world.tile(x, y);
					if(tile == null) continue;
					if(tile.block() == Blocks.crystalOrbs) {
						tile.setBlock(Blocks.air);
						traps.add(new Point2(x, y));
					}
					
					// coreBuildShockwave
					// 
					
//					Fx.coreBuildShockwave;
				}
			}
			world.setGenerating(false);
			
			stopGame();
        });
		
		Events.on(PlayerJoin.class, e -> {
			e.player.team(Team.derelict);
			if(e.player.unit() == null) return;
			e.player.unit().kill();
        });
		
		Events.on(GameOverEvent.class, e -> {
			long elapsed = System.nanoTime() - startTime;
			
			String hms = String.format("%02d:%02d:%02d", TimeUnit.NANOSECONDS.toHours(elapsed),
				    TimeUnit.NANOSECONDS.toMinutes(elapsed) % TimeUnit.HOURS.toMinutes(1),
				    TimeUnit.NANOSECONDS.toSeconds(elapsed) % TimeUnit.MINUTES.toSeconds(1));

			Call.announce("[gold]Раунд окончен: [lightgray]" + hms);
			Call.sendMessage("[gold]Раунд окончен: [lightgray]" + hms);
		});
		

//		Vars.netServer.admins.addChatFilter((player, text) -> {
//			
//		};
    }
    
    boolean isGameStarted = false;
    private void stopGame() {
    	isGameStarted = false;
		for (int i = 0; i < Groups.player.size(); i++) {
			Player player = Groups.player.index(i);
			if(player.admin) continue;
			player.team(Team.derelict);
			if(player.unit() == null) continue;
			player.unit().kill();
		}		
	}

    long startTime = 0;
    
    private boolean startGame() {
		for (int i = 0; i < Groups.player.size(); i++) {
			Player player = Groups.player.index(i);
			if(player.admin) continue;

			if(!playersTeams.containsKey(player.uuid())) return false;
		}
			
		for (int i = 0; i < Groups.player.size(); i++) {
			Player player = Groups.player.index(i);
//			if(player.admin) continue;
			if(!playersTeams.containsKey(player.uuid())) continue;
			player.team(playersTeams.get(player.uuid()));
		}
		
		Call.announce("[lime]Игра началась!");
		startTime = System.nanoTime();
    	isGameStarted = true;
		return true;
	}
    
	@Override
    public void registerServerCommands(CommandHandler handler){
    }
	
	@Override
	public void registerClientCommands(CommandHandler handler) {
		handler.<Player>register("start", "", (args, player) -> {
			if(player.admin) {
				player.sendMessage("" + startGame());
			}
		});
		handler.<Player>register("stop", "", (args, player) -> {
			if(player.admin) {
				stopGame();
				player.sendMessage("[gold]Готово!");
			}
		});
		handler.<Player>register("teams", "", (args, player) -> {
			if(player.admin) {
				for (int i = 0; i < Groups.player.size(); i++) {
					Player p = Groups.player.index(i);
					if(!playersTeams.containsKey(player.uuid())) {
						p.sendMessage("[red]У ВАС НЕТ КОМАНДЫ!");
						continue;
					}
					Team team = playersTeams.get(p.uuid());
					p.sendMessage("Вы в команде [#" + team.color.toString() + "]" + team.name);
				}
			}
		});
		handler.<Player>register("pt", "[add/remove/list] [uidd] [team]", "Сменить команду игрока", (args, player) -> {
			if(player.admin) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("list")) {
						for (String uidd : playersTeams.keys()) {
							PlayerInfo info = netServer.admins.getInfo(uidd);
							if(info == null) {
								player.sendMessage("[red]UIDD ненайдено");
								continue;
							}
							String name = info.plainLastName();
							Team team = playersTeams.get(uidd);
							player.sendMessage(name + ": [#" + team.color.toString() + "]" + uidd + " " + team.name);
						}
						return;
					}
				}
				if(args.length == 2) {
					if(args[0].equalsIgnoreCase("remove")) {
						String uidd = args[1];
						if(playersTeams.containsKey(uidd)) {
							playersTeams.remove(uidd);
							player.sendMessage("[green]UIDD удалено!");
						} else {
							player.sendMessage("[red]UIDD не найдено!");
						}
						return;
					}
				}
				if(args.length == 3) {
					if(args[0].equalsIgnoreCase("add")) {
						String uidd = args[1];
						String team = args[2];
						for (int i = 0; i < Team.baseTeams.length; i++) {
							if(Team.baseTeams[i].name.equalsIgnoreCase(team)) {
								playersTeams.put(uidd, Team.baseTeams[i]);
								player.sendMessage("[red]Добавлено!");
								return;
							}
						}
						player.sendMessage("[red]Команда ненайдена!");
						return;
					}
				}
				player.sendMessage("[red]Ошибка аргументов!");
			}
		});
	}

	public void test() {
		
		UnitTypes.oct.stats.remove(Stat.range);
//		UnitTypes.oct.maxRange
		UnitTypes.oct.stats.add(Stat.range, (int)(7000 / tilesize), StatUnit.blocks);
	}
	
	Seq<Point2> traps;
	
	private void updateTraps() {
		for (int i = 0; i < traps.size; i++) {
			Point2 trap = traps.get(i);
			Tile tile = world.tile(trap.x, trap.y);
			if(tile == null) continue;
			
			if(tile.build != null) {
				Call.logicExplosion(Team.derelict, trap.x*tilesize, trap.y*tilesize, 10*tilesize, 100_000, true, true, false);
				Call.effect(Fx.impactReactorExplosion, trap.x*tilesize, trap.y*tilesize, 10, Color.white);
				traps.remove(i);
				return;
			}
			
			for (int j = 0; j < Groups.unit.size(); j++) {
				Unit unit = Groups.unit.index(j);
				if(unit == null) continue;
				if(unit.isFlying()) continue;
				if(unit.tileX() == trap.x && unit.tileY() == trap.y) {
					Call.logicExplosion(Team.derelict, trap.x*tilesize, trap.y*tilesize, 10*tilesize, 100_000, true, true, false);
					Call.effect(Fx.impactReactorExplosion, trap.x*tilesize, trap.y*tilesize, 10, Color.white);
					traps.remove(i);
					return;
				}
			}
		}
	}

	
	private void updateMassDriver() {
		Building building = GameWork.randomBuilding(Blocks.massDriver);
		if(building == null) return;
		if(building.items == null) return;
		for (int i = 0; i < content.items().size; i++) {
			Item item = content.items().get(i);
			GameWork.itemExplosion(item, building.items.get(item), building.x, building.y);
		}
	}

	private void updateDrill() {
		Building building = GameWork.randomBuilding(GameWork.randomDrill());
		if(building == null) return;
		if(building.liquids == null) return;
		if(building.liquids.currentAmount() > 0) return;
		if(building.items == null) return;
		Item item = building.items.first();
		if(item == null) return;

		GameWork.itemExplosion(item, building.items.get(item), building.x, building.y);
	}

	private void updatePowerNode() {
		Building building = GameWork.randomBuilding(Blocks.powerNode);
		if(building == null) return;
		
		Seq<Building> links = new Seq<>();
		building.getPowerConnections(links);

		if(building.power() == null) return;
		if(building.power().graph == null) return;
		
		int con = links.size;
		if(con > 5) {
			con -= 5;
			con -= Mathf.random(5);
			if(con > 0 && building.power().graph.getPowerProduced() > 0) {
				for (int i = 0; i < con; i++) {
					Call.createBullet(Bullets.fireball, Team.derelict, building.x, building.y, Mathf.random(360f), Bullets.fireball.damage, 1, 1);
				}
				return;
			}
		}
	}

	private void updatePuddle() {
		Groups.puddle.each(e -> {
			int x = e.tileX();
			int y = e.tileY();
			if(e.liquid == Liquids.water) {
				if(e.tile != null) {
					if(GameWork.hasNearFloor(Blocks.slag, x, y)) {
						e.tile.setFloorNet(Blocks.stone, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.cryofluid, x, y)) {
						e.tile.setFloorNet(Blocks.ice, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.tar, x, y)) {
						e.tile.setFloorNet(Blocks.tar, e.tile.overlay());
					} else {
						e.tile.setFloorNet(Blocks.deepwater, e.tile.overlay());
					}
					return;
				}
			}
			if(e.liquid == Liquids.slag) {
				if(e.tile != null) {
					if(GameWork.hasNearFloor(Blocks.water, x, y) 
							|| GameWork.hasNearFloor(Blocks.darksandTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepwater, x, y)
							|| GameWork.hasNearFloor(Blocks.sandWater, x, y)
							|| GameWork.hasNearFloor(Blocks.taintedWater, x, y)) {
						e.tile.setFloorNet(Blocks.stone, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.cryofluid, x, y)) {
						e.tile.setFloorNet(Blocks.basalt, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.tar, x, y)) {
						e.tile.setFloorNet(Blocks.hotrock, e.tile.overlay());
					} else {
						e.tile.setFloorNet(Blocks.slag, e.tile.overlay());
					}
					return;
				}
			}
			if(e.liquid == Liquids.cryofluid) {
				if(e.tile != null) {
					if(GameWork.hasNearFloor(Blocks.water, x, y) 
							|| GameWork.hasNearFloor(Blocks.darksandTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepwater, x, y)
							|| GameWork.hasNearFloor(Blocks.sandWater, x, y)
							|| GameWork.hasNearFloor(Blocks.taintedWater, x, y)) {
						e.tile.setFloorNet(Blocks.ice, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.slag, x, y)) {
						e.tile.setFloorNet(Blocks.basalt, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.tar, x, y)) {
						e.tile.setFloorNet(Blocks.shale, e.tile.overlay());
					} else {
						e.tile.setFloorNet(Blocks.cryofluid, e.tile.overlay());
					}
					return;
				}
			}
			if(e.liquid == Liquids.oil) {
				if(e.tile != null) {
					if(GameWork.hasNearFloor(Blocks.water, x, y) 
							|| GameWork.hasNearFloor(Blocks.darksandTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepTaintedWater, x, y)
							|| GameWork.hasNearFloor(Blocks.deepwater, x, y)
							|| GameWork.hasNearFloor(Blocks.sandWater, x, y)
							|| GameWork.hasNearFloor(Blocks.taintedWater, x, y)) {
						e.tile.setFloorNet(Blocks.tar, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.slag, x, y)) {
						e.tile.setFloorNet(Blocks.slag, e.tile.overlay());
					} else if(GameWork.hasNearFloor(Blocks.cryofluid, x, y)) {
						e.tile.setFloorNet(Blocks.shale, e.tile.overlay());
					} else {
						e.tile.setFloorNet(Blocks.tar, e.tile.overlay());
					}
					return;
				}
			}
		});
	}
    
}
