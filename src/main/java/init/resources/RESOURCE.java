package init.resources;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.time.TIME;
import init.C;
import init.paths.PATH;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.text.D;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.room.industry.module.FlatIndustries.IInBoost;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.RoomProduction.Source;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.MAPPED;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

import java.io.IOException;

import static java.lang.Math.ceil;
/////////////////////////////////////////////////////////////////////////////////////////
//#!# Modifying so haulers are included on the Hover over GUI for resources with depots
/////////////////////////////////////////////////////////////////////////////////////////

public final class RESOURCE extends INFO implements MAPPED{
	
	public final String key;
	private final byte index;
	
	private final double degradeSpeed;
	private final Sprite sprite;
	private final Icon icon;
	private final TILE_SHEET debris;
	private final COLOR tint; 
	private final COLOR miniC;
	public final int category;
	LIST<RESOURCE> tradeSameAs = new ArrayList<>(0);
	
	final long bitL1;
	final long bitL2;
	public final RBIT bit;
	public final double priceCapDef;
	public final double priceMulDef;
	
	public CharSequence specialHelpText = null;

	RESOURCE(LISTE<RESOURCE> all, String key, PATH gData, PATH gText, PATH gSprite, PATH gDebris, KeyMap<Sprite> spriteMap, KeyMap<TILE_SHEET> debrisMap) throws IOException{
		super(new Json(gText.get(key)));
		Json data = new Json(gData.get(key));
		this.key = key;
		index = (byte) all.add(this);
		
		if (index < 64) {
			bitL1 = 1l << index;
			bitL2 = 0;
		}
		else {
			bitL1 = 0;
			bitL2 = 1l << (index-64);
		}
		bit = new RBIT(bitL1, bitL2);
		
		degradeSpeed = data.d("DEGRADE_RATE", 0, 1);
		tint = new ColorImp(data);
		miniC = new ColorImp(data, "MINIMAP_COLOR");
		category = data.i("CATEGORY_DEFAULT", 0, 3);
		icon = SPRITES.icons().get(data);
		
		priceCapDef  = data.dTry("PRICE_CAP", 0, 1, 1);
		priceMulDef = data.dTry("PRICE_MUL", 0, 100, 1);
		String vSprite = data.value("SPRITE");
		if (!spriteMap.containsKey(vSprite)) {
			spriteMap.put(vSprite, new Sprite(gSprite.get(vSprite)));
		}
		this.sprite = spriteMap.get(vSprite);
		
		String vDebris = data.value("SPRITE_DEBRIS");
		if (!debrisMap.containsKey(vDebris)) {
			TILE_SHEET s = new ITileSheet(gDebris.get(vDebris), 716, 28) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 16, 1, d.s16);
					s.singles.setVar(0).paste(1, true);
					return d.s16.saveGame();
				}
			}.get();
			debrisMap.put(vDebris, s);
		}
		this.debris = debrisMap.get(vDebris);
		
	}
	
	public final LIST<RESOURCE> tradeSameAs(){
		return tradeSameAs;
	}
	
	public double degradeSpeed() {
		return degradeSpeed;
	}
	
	public final byte bIndex() {
		return index;
	}
	
	@Override
	public int index() {
		return index;
	}

	public Icon icon() {
		return icon;
	}
	
	@Override
	public String key() {
		return key;
	}

	private final static int max = 3 + 4*2 + 9 + 16;
	
	public void renderLaying(SPRITE_RENDERER r, int x, int y, int random, double amount) {
		
		tint.bind();
		
		if (amount > max)
			amount = max;
		
		if (amount >= 16) {
			amount -= 16;
			int ra = random & 0b0011;
			random = random >> 2;
			random &= 0x7FFFFFFF;
			random |= ra << 30;
			sprite.lay.render(r, (ra&0b011) + 12, x, y);
		}
		
		if (amount >= 9) {
			amount -= 9;
			int ra = random & 0b0011;
			random = random >> 2;
			random &= 0x7FFFFFFF;
			random |= ra << 30;
			int d = -1 + ra;
			sprite.lay.render(r, (ra&0b011) + 8, x+d, y+d);
		}
		
		while(amount >= 4) {
			amount -= 4;
			int ra = random & 0b0111;
			random = random >> 3;
			random &= 0x7FFFFFFF;
			random |= ra << 29;
			int d = -4 + ra;
			sprite.lay.render(r,  (ra&0b011) + 4, x+d, y+d);
		}
		
		while(amount > 0) {
			amount --;
			int ra = random & 0b0111;
			random = random >> 3;
			random &= 0x7FFFFFFF;
			random |= ra << 29;
			int d = -4 + ra;
			sprite.lay.render(r, (ra&0b011), x+d, y+d);
		}
		
		COLOR.unbind();
		
	}
	
	public void renderLayingRel(SPRITE_RENDERER r, int x, int y, int random, double amount) {

		amount*= max;
		renderLaying(r, x, y, random, amount);
		
		
	}
	
	public void renderOne(SPRITE_RENDERER r, int x, int y, int random) {
		
		tint.bind();
		sprite.lay.render(r, (random&0b011), x, y);
		COLOR.unbind();
		
	}
	
	public void renderOneC(SPRITE_RENDERER r, int x, int y, int random) {
		
		tint.bind();
		sprite.lay.renderC(r, (random&0b011), x, y);
		COLOR.unbind();
		
	}

	public void renderCarried(SPRITE_RENDERER r, int cx, int cy, DIR d) {
		tint.bind();
		int dd = sprite.carry.size()/2;
		cx -= dd;
		cy -= dd;
		
		int i = d.id();
		cx += 10*d.x();
		cy += 10*d.y();
		sprite.carry.render(r, i, cx, cy);
		COLOR.unbind();
	}
	
	public void renderDebris(SPRITE_RENDERER r, ShadowBatch s, int x, int y, int ran, int amount) {
		int start = 0;
		
		ran = ran & 0x01F;
		
		amount = CLAMP.i(amount, 0, 5);
		
		for (int i = 0; i < amount; i++) {
			x += -C.SCALE*(ran&0b011) + C.SCALE*((ran>>2)&0b011);
			y += -C.SCALE*((ran>>4)&0b011) + C.SCALE*((ran>>6)&0b011);
			
			debris.render(r, start+(ran&0x01F), x, y);
			ran = ran >> 1;
			
		}
	}

	public COLOR miniC() {
		return miniC;
	}
	
	@Override
	public String toString() {
		return "" + key + "[" + index + "]";
	}
	
	public Boostable conBoost() {
		IInBoost bb = SETT.ROOMS().industries.flat.inBoost(this);
		if (bb != null)
			return bb.bo;
		return null;
	}
	
	public double conBoost(BOOSTABLE_O o) {
		IInBoost bb = SETT.ROOMS().industries.flat.inBoost(this);
		if (bb != null)
			return bb.bo.get(o);
		return 1;
	}
	
	@Override
	public void hover(GUI_BOX box) {
		GBox b = (GBox) box;
		
		b.title(name);
		b.text(desc);
		b.NL(8);
		b.add(BOOSTABLES.CIVICS().SPOILAGE.icon);
		b.textLL(Dic.¤¤SpoilRate);
		double d = degradeSpeed();
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), -d, 2));
		b.add(b.text().add('/').s().add(TIME.years().cycleName()));
		b.NL();
		d /= BOOSTABLES.CIVICS().SPOILAGE.get(POP_CL.clP(null, null));
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), -0.5*d, 2));
		b.text(Dic.¤¤Stored);
		
		
		b.NL(8);
		if (RESOURCES.EDI().is(this)) {
			b.textL(Dic.¤¤Edible);
		}
		b.NL();
		if (RESOURCES.DRINKS().is(this)) {
			b.textL(Dic.¤¤Drinkable);
		}
		
		b.NL();
		
	}
	
	private static CharSequence ¤¤Exists = "¤Resources exist scattered on the map that are not yet stored and counted: ";
	private static CharSequence ¤¤Production = "¤Produced per day (estimation):";
	
	static {
		D.ts(RESOURCE.class);
	}
	
	public void hoverDetailed(GUI_BOX text) {
		
		hover(text);
		RESOURCE res = this;
		
		int a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
		int c = (int) SETT.ROOMS().STOCKPILE.tally().space.total(res);
		GBox b = (GBox) text;
		
		b.textLL(Dic.¤¤buyPrice);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesBuy.get(this)));
		b.NL();
		b.textLL(Dic.¤¤sellPrice);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesSell.get(this)));
		
		b.sep();
		b.textLL(¤¤Production);
		b.NL();
		
		double tot = 0;
		{
			for (Source rr : SETT.ROOMS().PROD.producers(res)) {
				if (rr.am() == 0)
					continue;
				tot += rr.am();
				b.add(rr.icon());
				b.textL(rr.name());
				if (rr.thereAreMultipleIns() != null) {
					for (IndustryResource ii : rr.thereAreMultipleIns().ins())
						b.add(ii.resource.icon().small);
				}
				b.tab(7);
				b.add(GFORMAT.f0(b.text(),rr.am()));
				b.NL();
			}
			for (Source rr : SETT.ROOMS().PROD.consumers(res)) {
				if (rr.am() == 0)
					continue;
				tot -= rr.am();
				b.add(rr.icon());
				b.textL(rr.name());
				if (rr.thereAreMultipleIns() != null) {
					for (IndustryResource ii : rr.thereAreMultipleIns().ins())
						b.add(ii.resource.icon().small);
				}
				b.tab(7);
				b.add(GFORMAT.f0(b.text(),-rr.am()));
				b.NL();
			}
			
			
		}
		b.NL(4);
		b.textLL(Dic.¤¤Net);
		b.add(GFORMAT.f0(b.text(), tot));
		
		b.sep();
		b.textLL(Dic.¤¤Stored);
		b.NL();
		b.add(SETT.ROOMS().STOCKPILE.icon.small);
		b.textL(SETT.ROOMS().STOCKPILE.info.names);
		b.tab(7);
		b.add(GFORMAT.iofk(b.text(), a, c));
		b.NL();
/////////////////////////////////////////////////////////////////#!#
		b.add(SETT.ROOMS().HAULER.icon.small);
		b.textL(SETT.ROOMS().HAULER.info.names);
		b.tab(7);
		b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().HAULER.tally.amountTotal(res) ,  (long) ceil( SETT.ROOMS().HAULER.tally.amountTotal(res)/SETT.ROOMS().HAULER.tally.usage.getD(res) )   ));
		b.NL();
/////////////////////////////////////////////////////////////////#!#
		b.add(SETT.ROOMS().IMPORT.icon.small);
		b.textL(SETT.ROOMS().IMPORT.info.names);
		b.tab(7);
		b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().IMPORT.tally.amount.get(res), SETT.ROOMS().IMPORT.tally.capacity.get(res)));
		b.NL();
		
		b.add(SETT.ROOMS().EXPORT.icon.small);
		b.textL(SETT.ROOMS().EXPORT.info.names);
		b.tab(7);
		b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().EXPORT.tally.amount.get(res), SETT.ROOMS().EXPORT.tally.capacity.get(res)));
		b.NL(8);
		
		Str.TMP.clear().add(¤¤Exists);
		Str.TMP.s().add(SETT.PATH().finders.resource.scattered.has(res));
		b.text(Str.TMP);
	}
	
}
