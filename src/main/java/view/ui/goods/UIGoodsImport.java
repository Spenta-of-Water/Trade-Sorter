package view.ui.goods;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.trade.TradeManager;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.dic.Dic;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HistoryResource;
import view.main.VIEW;
import world.region.RD;

public class UIGoodsImport extends GuiSection{

	static CharSequence ¤¤name = "Import Settings";
	static CharSequence ¤¤Best = "¤Best";
	static CharSequence ¤¤BestD = "¤Make a custom order of this resource from the trade partner with the most favourable price.";
	static CharSequence ¤¤Closest = "¤Closest";
	static CharSequence ¤¤ClosestD = "¤Make a speedy custom order of this resource from the trade partner that is closest.";
	static CharSequence ¤¤Stockpile = "¤Current warehouse stock:";
	
	private static CharSequence ¤¤Capacity = "¤Import Capacity";
	private static CharSequence ¤¤CapacityDesc = "¤The available space of your import depots for import and tribute. When trading with factions, you can only buy according to your available space.";
	
	private static CharSequence ¤¤priceCapD = "¤The maximum price you are willing to pay for this resource.";
	
	private static CharSequence ¤¤minCreds = "¤Minimum Treasury.";
	private static CharSequence ¤¤minCredsD = "¤The minimum credits needed before a purchase is undertaken.";

	private static CharSequence ¤¤LevelDesc = "¤Your import level dictates how much that will be imported based on your warehouse stock. Wares will be delivered to import depots from selling factions, which might take time. If you are short on money, or lacking depot space, nothing will be imported. If you have no warehouse space, yet still want to import, put this setting on maximum.";
	private static CharSequence ¤¤LevelEverything = "¤100% Imports maximum to fill both warehouses and import depots.";
	private static CharSequence ¤¤LevelNothing = "¤Never import.";
	private static CharSequence ¤¤LevelCurrent = "¤Import to maintain warehouse stock at {0}% of total capacity ({1} items).";
	private static CharSequence ¤¤LevelCurrentE = "¤You will currently import {0} additional items.";

	public static final COLOR color = new ColorImp(80, 80, 100);
	private static final COLOR colorDark = color.shade(0.75); 
	
	static {
		D.ts(UIGoodsImport.class);
	}
	
	public GETTER_IMP<RESOURCE> res = new GETTER_IMP<>(RESOURCES.ALL().get(0));
	
	public UIGoodsImport(){
		
		addDown(12, amount());
		addDown(12, priceH());
		addDown(12, cap());
		addDown(12, price());
		addDown(12, credlim());
		addRelBody(16, DIR.E, new UIGoodsTraders(6, false) {

			@Override
			protected int price(FactionNPC f) {
				return f.seller().priceSellP(res.get());
			}

			@Override
			protected int sortValue(FactionNPC f) {
				return f.seller().priceSellP(res.get());
			}
			
		});

		addRelBody(16, DIR.E, new UIGoodsTraders(6, true) {

			@Override
			protected int price(FactionNPC f) {
				return TradeManager.valueResource(res.get(), f, FACTIONS.player(), 1);
			}

			@Override
			protected int sortValue(FactionNPC f) {
				return TradeManager.valueResource(res.get(), f, FACTIONS.player(), 1);
			}

		});

		addRelBody(8, DIR.S, problem());
		
		
		GuiSection h = new GuiSection();
		
		h.add(new HoverableAbs(Icon.M) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				res.get().icon().render(r, body);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(res.get().name);
			}
		});
		h.addRightC(8, new GHeader(¤¤name));
		addRelBody(8, DIR.N, h);
		
		{
			GuiSection s = new GuiSection();
			
			GButt.ButtPanel b = new GButt.ButtPanel(¤¤Best) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					isActive &= FACTIONS.player().trade.pricesBuy.get(res.get()) > 0;
					super.render(r, ds, isActive, isSelected, isHovered);
				}
				
				@Override
				protected void clickA() {
					FactionNPC f = null;
					int pp = Integer.MAX_VALUE;
					for (FactionNPC ff : FACTIONS.NPCs()) {
						if(!ff.isActive())
							continue;
						int p = TradeManager.valueResource(res.get(), ff, FACTIONS.player(), 1);
						if (p > 0 && ff.seller().forSale(res.get()) > 0  && p < pp) {
							pp = p;
							f = ff;
						}
					}
					if (f != null) {
						VIEW.inters().popup.close();
						VIEW.world().UI.factions.openBuy(f, res.get());
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.NL(8);
					CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res.get(), false);
					if (p != null)
						b.error(p);
				}
			};
			b.hoverInfoSet(¤¤BestD);
			b.icon(UI.icons().s.money);
			b.setDim(180);
			s.addRightC(0, b);
			
			
			
			b = new GButt.ButtPanel(¤¤Closest) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					isActive &= FACTIONS.player().trade.pricesBuy.get(res.get()) > 0;
					super.render(r, ds, isActive, isSelected, isHovered);
				}
				
				@Override
				protected void clickA() {
					FactionNPC f = null;
					int pp = Integer.MAX_VALUE;
					for (Faction fff : DIP.traders()) {
						FactionNPC ff = (FactionNPC) fff;
						int p = RD.DIST().distance(ff);
						if (ff.seller().priceSellP(res.get()) > 0 && ff.seller().forSale(res.get()) > 0 && p < pp) {
							pp = p;
							f = ff;
						}
					}
					if (f != null) {
						VIEW.inters().popup.close();
						VIEW.world().UI.factions.openBuy(f, res.get());
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.NL(8);
					CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res.get(), false);
					if (p != null)
						b.error(p);
				}
				
			};
			b.hoverInfoSet(¤¤ClosestD);
			b.icon(UI.icons().s.wheel);
			b.setDim(180);
			s.addRightC(0, b);
			s.addRelBody(10, DIR.N, new GHeader(UIGoodsExport.¤¤special, UI.FONT().S));
			addRelBody(8, DIR.S, s);
		}
		
		
	
		
		
	}
	
	public static RENDEROBJ miniControl(RESOURCE res, UIGoodsImport setting) {
		
		GuiSection s = new GuiSection();
		
		GETTER_IMP<RESOURCE> get = new GETTER_IMP<RESOURCE>(res);
		
		
		
		
		if (res != null) {
			GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().s.cog) {
				
				@Override
				protected void clickA() {
					setting.res.set(res);
					VIEW.inters().popup.show(setting, this);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(UIGoodsImport.¤¤name);
					GBox b = (GBox) text;
					CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res, true);
					if (p != null)
						b.error(p);
					b.NL(4);
					p = SETT.ROOMS().IMPORT.tally.warning(res, true);
					if (p != null)
						b.add(b.text().warnify().add(p));
					
					super.hoverInfoGet(text);
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					super.render(r, ds, isActive, isSelected, isHovered);
					
					
				}
				
			};	
			b.setDim(48, 48);
			
			s.addRelBody(0, DIR.E, b);
		}else {
			s.body().incrW(s.body().height());
		}
		
		s.addRelBody(0, DIR.S, UIGoodsExport.capBar(get, 48, 14));
		
		if (res != null) {
			RENDEROBJ oo = new RenderImp(s.body().width(), s.body().height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (SETT.ROOMS().IMPORT.tally.capacity.get(res) == 0) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body);
						OPACITY.unbind();
					}else {
						if (SETT.ROOMS().IMPORT.tally.importWhenBelow.get(res) > 0) {
							if (SETT.ROOMS().IMPORT.tally.problem(res, true) != null) {
								GCOLOR.UI().BAD.hovered.bind();
								UI.icons().s.alert.renderC(r, body().x2()-8, body().y1());
							}else if (SETT.ROOMS().IMPORT.tally.warning(res, true) != null) {
								GCOLOR.UI().SOSO.hovered.bind();
								UI.icons().s.alert.renderC(r, body().x2()-8, body().y1());
							}
							COLOR.unbind();
						}
					}
				}
			};
			oo.body().centerIn(s);
			s.add(oo);
		}
		

		
		
		return s;
		
	}
	
	private GuiSection priceH() {
		GuiSection s = new GuiSection();
		s.add(priceChart(FACTIONS.player().trade.pricesBuy, Dic.¤¤buyPrice, res, 8, 64));
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.coins));
		return s;
	}

	
	static HOVERABLE priceChart(HistoryResource hi, CharSequence title, GETTER<RESOURCE> res, int ww, int height) {
		
		final int amount = hi.get(0).historyRecords();
		GStaples s = new GStaples(amount, false) {

			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				super.render(r, ds, hoveredIs());
				Str.TMP.clear();
				Str.TMP.add(hi.get(res.get()));
				int w = UI.FONT().S.getDim(Str.TMP).x();
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body().x1(),  body().x1()+w+4, body.y1(), body.y1()+18);
				OPACITY.unbind();
				UI.FONT().S.render(r, Str.TMP, body().x1()+2, body.y1()+1);
				
			}

			@Override
			protected double getValue(int stapleI) {
				double v = hi.history(res.get()).get(amount-1-stapleI);
				if (res.get() == null)
					v /= RESOURCES.ALL().size();
				return v;
			}

			@Override
			protected void hover(GBox b, int stapleI) {
				int si = amount-stapleI-1;
				b.title(Dic.¤¤buyPrice);
				GText t = b.text();
				t.lablify();
				DicTime.setAgo(t, si*GAME.player().res().time.bitSeconds());
				b.add(t);
				b.NL(4);
				
				b.add(GFORMAT.i(b.text(), (long) getValue(stapleI)));
				
			}

		};
		s.body().setWidth(ww*amount).setHeight(height);
		return s;
	}
	
	private GuiSection price() {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title( Dic.¤¤PriceCap);
				text.text(¤¤priceCapD);
			}
			
		};
		
	
		
		
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				return SETT.ROOMS().IMPORT.tally.priceCapsI.max(res.get());
			}
			
			@Override
			public int get() {
				return SETT.ROOMS().IMPORT.tally.priceCapsI.get(res.get());
			}
			
			@Override
			public void set(int t) {
				SETT.ROOMS().IMPORT.tally.priceCapsI.set(res.get(), t);
			}
		};
		
		GInputInt sl = new GInputInt(in, true, true);
		
		s.addRightC(2, sl);
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.coins.twin(UI.icons().s.cog, DIR.NE, 2)));
		return s;
	}
	
	private GuiSection credlim() {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title( ¤¤minCreds);
				text.text(¤¤minCredsD);
			}
			
		};
		
	
		
		
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				return SETT.ROOMS().IMPORT.tally.minMoney.max(res.get());
			}
			
			@Override
			public int get() {
				return SETT.ROOMS().IMPORT.tally.minMoney.get(res.get());
			}
			
			@Override
			public void set(int t) {
				SETT.ROOMS().IMPORT.tally.minMoney.set(res.get(), t);
			}
		};
		
		GInputInt sl = new GInputInt(in, true, true);
		
		s.addRightC(2, sl);
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.coins.twin(UI.icons().s.arrowUp, DIR.NE, 2)));
		return s;
	}
	
	private GuiSection amount() {
		
		GSliderInt sl = slider(res, 260, 32);
		
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				sl.hoverInfoGet(text);
			}
			
		};
		
		s.add(sl);
		s.addDown(0, new HoverableAbs(260, 24) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double tot = SETT.ROOMS().STOCKPILE.tally().space.total(res.get());
				if (tot > 0)
					GMeter.render(r, GMeter.C_REDGREEN, SETT.ROOMS().STOCKPILE.tally().amountTotal(res.get())/tot, body);
				else
					GMeter.render(r, GMeter.C_GRAY, 0, body);
			}
		});
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.cog_big));
		
		return s;
	}

	
	static GSliderInt slider(GETTER<RESOURCE> res, int width, int height) {
		INTE limit = new INTE() {
			
			@Override
			public int get() {
				return SETT.ROOMS().IMPORT.tally.importWhenBelow.get(res.get());
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public int max() {
				return SETT.ROOMS().IMPORT.tally.importWhenBelow.max(res.get());
			}

			@Override
			public void set(int t) {
				SETT.ROOMS().IMPORT.tally.importWhenBelow.set(res.get(), t);
			}
		};

		return new GSliderInt(limit, width, height, false) {
			
			
			
			@Override
			protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
				
				COLOR col = width != widthFull ? colorDark : color;
				col.render(r, x1, x1+width, y1, y2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤ImportLevel);
				int space = (int) SETT.ROOMS().STOCKPILE.tally().space.total(res.get());
				int amount = (int) SETT.ROOMS().STOCKPILE.tally().amountTotal(res.get());
				if (limit.getD() == 1) {
					b.textL(¤¤LevelEverything);
					b.NL(4);
				}else if (limit.getD() == 0) {
					b.textL(¤¤LevelNothing);
					b.NL(4);
				}else {
					
					double lim = limit.get()/(limit.max()-1.0);
					
					
					int imp = (int)CLAMP.d(lim*space-amount, 0, lim*space);
					
					GText t = b.text();
					t.add(¤¤LevelCurrent);
					
					t.insert(0, (int)(Math.round(100*lim)));
					t.insert(1, (int)(lim*space));
					b.textL(t);
					
					b.NL(4);
					
					t = b.text();
					t.add(¤¤LevelCurrentE);
					t.insert(0, imp);
					
					b.add(t);
					b.NL(4);
				}

				b.NL(8);
				b.textLL(¤¤Stockpile);
				b.tab(7);
				b.add(GFORMAT.iofkNoColor(b.text(), amount, space));
				
				b.NL(8);
				
				b.text(¤¤LevelDesc);
				
			}
		};
	}
	
	static HOVERABLE capBar(GETTER<RESOURCE> res, int width, int height) {
		return new HoverableAbs(width, height) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double cap = SETT.ROOMS().IMPORT.tally.capacity.get(res.get());
				if (cap == 0) {
					GMeter.render(r, GMeter.C_ORANGE, 0, body());
				
				}else {
					double c = SETT.ROOMS().IMPORT.tally.amount.get(res.get());
					double n = c + SETT.ROOMS().IMPORT.tally.amount.get(res.get());
					c /= cap;
					n /= cap;
					GMeter.renderDelta(r, n, c, body, GMeter.C_ORANGE);
					
				}
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤Capacity);
				text.text(¤¤CapacityDesc);
				text.NL(8);
				int in = SETT.ROOMS().IMPORT.tally.incoming.get(res.get());
				int am = SETT.ROOMS().IMPORT.tally.amount.get(res.get());
				int ca = SETT.ROOMS().IMPORT.tally.capacity.get(res.get());
				GBox b = (GBox) text;
				b.textL(Dic.¤¤Storage);
				
				
				b.add(b.text().lablifySub().add(Dic.¤¤Inbound));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), in));
				b.NL();
				b.add(b.text().lablifySub().add(Dic.¤¤Stored));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), am));
				b.NL();
				b.add(b.text().lablifySub().add(Dic.¤¤Capacity));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), ca));
				b.NL(8);
				b.add(b.text().lablifySub().add(Dic.¤¤Importable));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), ca - am - in));

			}
		};
	}
	
	private GuiSection cap() {
		HOVERABLE cc = capBar(res, 200, 32);
		
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				cc.hoverInfoGet(text);
			}
		};
		s.add(cc);
		
		
		s.addRelBody(8, DIR.W, icon(SETT.ROOMS().IMPORT.icon));
		
		return s;
	}
	
	private HOVERABLE problem() {
		GuiSection s = new GuiSection();
		s.add(new HoverableAbs(450, 80) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				GCOLOR.UI().bg().render(r, body);
				GCOLOR.UI().borderH(r, body, 0);
				
				CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res.get(), true);
				
				if (p != null) {
					GCOLOR.UI().BAD.hovered.bind();
					UI.FONT().S.render(r, p, body().x1()+8, body().y1()+8, body().width()-16, 1.0);
					COLOR.unbind();
				}
					
				else {
					p = SETT.ROOMS().IMPORT.tally.warning(res.get(), true);
					GCOLOR.UI().SOSO.hovered.bind();
					if (p != null)
						UI.FONT().S.render(r, p, body().x1()+8, body().y1()+8, body().width()-16, 1.0);
					COLOR.unbind();
				}
				
				
			}
		});
		s.hoverInfoSet(Dic.¤¤Problem);
		return s;
	}

	
	private static HOVERABLE icon(SPRITE icon) {
		
		return new HoverableAbs(32, 32) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				icon.renderC(r, body());
			}
		};
		
	}
	

	
}
