package init.tech;

import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import game.values.Lockers;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.dic.Dic;
import util.info.INFO;
import world.map.regions.Region;
import java.io.IOException;

public final class TECH implements INDEXED{
	public CostBenefit Tech_CostBenefit = new CostBenefit(this);
	private final int index;
	public final int levelMax;
	public final int levelCost;
	public final int levelCostInc;
	public final String order;
	public final double levelCostMulInc;
	
	private LIST<TechRequirement> needs;
	private LIST<TechRequirement> needsPruned;
	public final INFO info;

	public final BoostSpecs boosters;
	public final Lockable<Faction> plockable;
	public final Lockers lockers;
	
	private SPRITE icon = null;
	
	public final String key;
	public final TechTree tree;
	public final double AIAmount;
	Json requires;

	TECH(String key, LISTE<TECH> all, Json data, Json text, TechTree tree) throws IOException{
		this.key = key;
		this.tree = tree;
		info = new INFO(text);
		index = all.add(this);
		
		if (false)
			;//security raid needs to go
		//house tech most lead to something significant that's needed forever
		
		
		order = data.has("TREE_ORDER") ? data.value("TREE_ORDER") : "ZZ";
		levelMax = data.i("LEVEL_MAX", 1, 10000, 1);
		levelCost = data.i("LEVEL_COST", 0, 100000);
		levelCostInc = data.i("LEVEL_COST_INC", 0, 100000, 0);
		levelCostMulInc = data.dTry("LEVEL_COST_INC_MUL", 1, 100000, 1);
		AIAmount = data.dTry("AI_AMOUNT", 0, 1, 1.0);
		plockable = GVALUES.FACTION.LOCK.push();
		plockable.push(data);
		lockers = new Lockers(Dic.¤¤TechnologyShort + ": " + info.name, UI.icons().s.vial);
		
		lockers.add(GVALUES.FACTION, data, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				if (t == FACTIONS.player()) {
					if (FACTIONS.player().tech.penalty().getD() < 0.75)
						return 0;
					return FACTIONS.player().tech.level(TECH.this) > 0 ? 1 : 0;
				}
				return 1;
			}
		
		});
		
		lockers.add(GVALUES.INDU, data, new DOUBLE_O<Induvidual>() {

			@Override
			public double getD(Induvidual t) {
				if (t.faction() == FACTIONS.player()) {
					if (FACTIONS.player().tech.penalty().getD() < 0.75)
						return 0;
					return FACTIONS.player().tech.level(TECH.this) > 0 ? 1 : 0;
				}
				return 1;
			}
		
		});
		
		lockers.add(GVALUES.REGION, data, new DOUBLE_O<Region>() {

			@Override
			public double getD(Region t) {
				if (t.faction() == FACTIONS.player()) {
					if (FACTIONS.player().tech.penalty().getD() < 0.75)
						return 0;
					return FACTIONS.player().tech.level(TECH.this) > 0 ? 1 : 0;
				}
				return 1;
			}
		
		});
		
		boosters = new BoostSpecs(info.name, UI.icons().s.vial, false);
		boosters.read(data, null);
		
		if (data.has("ICON"))
			icon = UI.icons().get(data);
		
		requires = data;
		
		{
			//convert(key, data, text);
			
			//new File(path).createNewFile();
		}
		
	}

	@Override
	public int index() {
		return index;
	}
	
	public LIST<TechRequirement> requires(){
		return needs;
	}
	
	public LIST<TechRequirement> requiresNodes(){
		return needsPruned;
	}
	
	void set(LIST<TechRequirement> needs) {
		this.needs = needs;
	}
	
	void prune(LIST<TechRequirement> needs) {
		this.needsPruned = needs;
	}
	
	public boolean requires(TECH other, int level) {
		if (other == this)
			return false;
		for (int i = 0; i < needs.size(); i++) {
			TECH t = needs.get(i).tech;
			if (t == other || t.requires(other, needs.get(i).level))
				if (needs.get(i).level > level)
					return true;
		}
		return false;
	}
	
	public SPRITE icon() {
		if (icon == null) {
			icon = TechIcon.icon(this);
		}
		return icon;
	}
	
	public static final class TechRequirement {
		
		public final TECH tech;
		public final int level;
		
		TechRequirement(TECH t, int l) {
			this.tech = t;
			this.level = l;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TechRequirement) {
				TechRequirement q = (TechRequirement) obj;
				return q.level == level && q.tech == tech;
			}
			return false;
		}
		
	}
	
}
