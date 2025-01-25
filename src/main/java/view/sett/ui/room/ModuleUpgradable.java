package view.sett.ui.room;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.*;
import settlement.room.service.module.ROOM_SERVICER;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.Modules.ModuleMaker;

import java.util.Arrays;

import static java.lang.Math.ceil;

final class ModuleUpgradable implements ModuleMaker {

	private final CharSequence ¤¤UPGRADE_ALL = "¤Upgrade all";
	private final CharSequence ¤¤UPGRADE_ALL_DESC = "¤Upgrade all rooms once. Upgrading a room significantly increases performance. It costs a lot of resources initially that will be deducted from warehouses, and also in maintenance over time.";
	private final CharSequence ¤¤UPGRADABLE = "¤Upgradable:";
	private final CharSequence ¤¤UPGRADE = "¤Upgrade";
	private final CharSequence ¤¤UPGRADE_MAX_REACHED = "¤Maximally Upgraded.";
	//#!# Add new variable
	public double upgrade_cost = 0;
	public double maint_cost = 0;

	ModuleUpgradable(Init init){
		D.t(this);
	}

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> pi = (RoomBlueprintIns<?>) p;
			if (pi.upgrades().max() > 0) {
				l.add(new I(pi));
			}
		}
		
	}
	
	private class I extends UIRoomModule {
		
		private final RoomBlueprintIns<?> blueprint;
		
		I(RoomBlueprintIns<?> blue){
			this.blueprint = blue;
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {
			
			
			
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {

			appliers.add(new UIRoomBulkApplier(new Str(¤¤UPGRADE_ALL)) {
				
				private final int[] res = new int[16];
				
				@Override
				protected void apply(RoomInstance ii) {
					
					if (ii.upgrade() >= blueprint.upgrades().max() || !ii.blueprintI().upgrades().requires(ii.upgrade()+1).passes(FACTIONS.player()))
						return;
					boolean okk = true;
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
						okk &= SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)) >= am;
						
					}
					if (okk) {
						for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
							int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
							SETT.ROOMS().STOCKPILE.remove(blueprint.constructor().resource(ri), am, RTYPE.CONSTRUCTION);
						}
						ii.upgradeSet(ii.upgrade()+1);
					}
				}
				
				@Override
				protected void hover(GBox b) {
					b.title(¤¤UPGRADE_ALL);
					b.text(¤¤UPGRADE_ALL_DESC);
					b.NL(8);
					
					Arrays.fill(res, 0);
					
					int ok = 0;
					for (int i = 0; i < blueprint.instancesSize(); i++) {
						RoomInstance ii = blueprint.getInstance(i);


						if (ii.upgrade() < blueprint.upgrades().max() && ii.blueprintI().upgrades().requires(ii.upgrade()+1).passes(FACTIONS.player())) {
							boolean okk = true;
							for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
								int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
								if (SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)) >= res[ri] + am) {
									
								}else {
									okk = false;
								}


							}
							if (okk) {
								ok++;
								for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
									int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
									res[ri] += am;
								}
							}
						}
						
					}
					
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						if (res[ri] > 0) {
							b.add(blueprint.constructor().resource(ri).icon());
							b.add(GFORMAT.iIncr(b.text(), -res[ri]));
							b.NL();
						}
					}
					
					b.NL(8);
					
					b.textL(¤¤UPGRADABLE);
					b.add(GFORMAT.iofkInv(b.text(), ok, blueprint.instancesSize()));

					
				}
				
			});
			
		}

		@Override
		public void appendButt(GuiSection s, GETTER<RoomInstance> get) {

			if (blueprint.upgrades().max() > 0) {
				SPRITE sp = new SPRITE.Imp(Icon.S+Icon.S, Icon.S) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						RoomInstance ins = get.get();
						int up = ins.upgrade();
						ColorImp.TMP.interpolate(GCOLOR.T().H2, GCOLOR.T().H1, (double)up/blueprint.upgrades().max());
						ColorImp.TMP.bind();
						for (int i = 0; i <= up; i++) {
							int x = i / 2;
							int y = i % 2;
							SPRITES.icons().s.plus.render(r, X1+x*Icon.S/2, Y1+y*Icon.S/2);
						}
						COLOR.unbind();
					}
				};
				s.addRightC(16, sp);
				
			}

			
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			box.NL(4);
			box.add(UI.icons().s.arrowUp);
			box.textL(Dic.¤¤Upgrade);
			box.tab(6);
			RoomBlueprintImp b = (RoomBlueprintImp) room.blueprint();
			box.add(GFORMAT.iofk(box.text(), room.upgrade(rx, ry), b.upgrades().max()));
			box.NL();
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			GButt.BSection b = new GButt.BSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					///#!# reset value
					upgrade_cost = 0;
					maint_cost = 0;
					GBox b = (GBox) text;
					text.title(¤¤UPGRADE);
					
					RoomInstance i = get.get();
					if (i.upgrade() == blueprint.upgrades().max()) {
						text.text(¤¤UPGRADE_MAX_REACHED);
						return;
					}
					
					if (blueprint.upgrades().desc(g(get).upgrade()+1) != null) {
						b.text(blueprint.upgrades().desc(g(get).upgrade()+1));
					}else if (i instanceof ROOM_SERVICER) {
						b.textLL(Dic.¤¤Quality);
						double d = 1.0/(i.blueprintI().upgrades().max()+1.0);
						b.add(GFORMAT.percInc(b.text(), d));
					}else {
						b.textLL(Dic.¤¤Boosts);
						b.add(GFORMAT.f0(b.text(), blueprint.upgrades().boost(g(get).upgrade()+1)-blueprint.upgrades().boost(g(get).upgrade())));
					}
					
					b.NL(8);
					
					g(get).blueprintI().upgrades().requires(g(get).upgrade()+1).hover(text, FACTIONS.player());
					
					b.NL(8);
					
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {


						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > 0) {
							b.add(blueprint.constructor().resource(ri).icon());
							GText t = b.text();
							GFORMAT.iIncr(t, -am);
							if (am > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)))
								t.errorify();
							else
								t.normalify();
							b.add(t);
///////////////////////////////////////////////////////////////////////////////////////////////////////#!#
							//b.NL();

							//#!# Add denari cost per item
							RoomInstance ins = get.get();
							ROOM_DEGRADER deg = get.get().degrader(get.get().mX(), get.get().mY());
							double iso = ins.isolation(get.get().mX(), get.get().mY());
							double boost = SETT.MAINTENANCE().speed();
							double daily_res_use =  ROOM_DEGRADER.rateResource(boost, deg.base(), iso, am);
							double resource_price = FACTIONS.player().trade.pricesBuy.get(blueprint.constructor().resource(ri));
							b.text("("+  (int)   ( am * resource_price ) + " denari)");
							b.NL();
							//#!# Add new variable for the sum of costs
							upgrade_cost +=             am * resource_price;
							maint_cost   +=  daily_res_use * resource_price;

						}
					}
					b.text("Estimated total denari cost: " + (int) upgrade_cost);
					b.NL();
					b.text("Estimated daily maintenance in denari: " + (int) maint_cost);
					b.NL();
///////////////////////////////////////////////////////////////////////////////////////////////////////
				}
				
				
				@Override
				protected void clickA() {
					if (g(get).upgrade() >= blueprint.upgrades().max()) {
						return;
					}
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > 0) {
							SETT.ROOMS().STOCKPILE.remove(blueprint.constructor().resource(ri), am, RTYPE.CONSTRUCTION);
						}
						
					}
					g(get).upgradeSet(g(get).upgrade()+1);
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					activeSet(true);
					
					if (g(get).upgrade() >= blueprint.upgrades().max()) {
						activeSet(false);
						return;
					}
					if (S.get().developer)
						return;
					else if (!g(get).blueprintI().upgrades().requires(g(get).upgrade()+1).passes(FACTIONS.player()))
						activeSet(false);
					
					
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri))) {
							activeSet(false);
							break;
						}
					}
				}
			};
			
			b.add(new GText(UI.FONT().S, ¤¤UPGRADE).lablifySub(), 0, 0);
			b.addRightC(16, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, g(get).upgrade(), blueprint.upgrades().max());
					text.lablify();
				}
			});
			
			b.body().incrW(64);
			b.pad(8, 4);
			
			section.addRelBody(8, DIR.S,b);
			
			
			
		}

		private RoomInstance g(GETTER<RoomInstance> g) {
			return (RoomInstance) g.get();
		}


		
	}

	


	
}
