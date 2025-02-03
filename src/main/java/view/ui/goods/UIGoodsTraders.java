package view.ui.goods;

import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import init.sprite.UI.UI;
import init.text.D;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.region.RD;

import java.util.Arrays;
import java.util.Comparator;

public abstract class UIGoodsTraders extends GuiSection{

	private GText buttPrice = new GText(UI.FONT().S, 64);
	private final FactionNPC[] facs = new FactionNPC[FACTIONS.MAX];
	private int max = 0;
	private boolean isDiploTrade;

	private static CharSequence ¤¤TradeYes = "¤You have a trade agreement with this faction, and trade is possible.";
	private static CharSequence ¤¤TradeNo = "¤You do not have a trade agreement with this faction. Trade is not possible.";
	private static CharSequence ¤¤Click = "¤Click to go to the diplomacy screen for this faction.";
	private static CharSequence AutoTrade = "Auto Trade";
	private static CharSequence DiploTrade = "Diplo Trade";


	static {
		D.ts(UIGoodsTraders.class);
	}

	public UIGoodsTraders(int hi, boolean isDiploTrade) {
		this.isDiploTrade = isDiploTrade;
		GTableBuilder bu = new GTableBuilder() {

			@Override
			public int nrOFEntries() {
				return max;
			}
		};
		bu.column(null, new Butt(null).body().width(), new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Butt(ier);
			}
		});
		CharSequence label;
		if (isDiploTrade)
			label = DiploTrade;
		else
			label = AutoTrade;
		addRelBody(1, DIR.N,new GHeader(label, UI.FONT().S));
		addRelBody(1, DIR.S, bu.create(hi, false));

	}


	private class Butt extends ClickableAbs {

		private final GETTER<Integer> ier;

		Butt(GETTER<Integer> ier){
			body.setDim(96, 32);
			this.ier = ier;
		}


		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			FactionNPC f = facs[ier.get()];

			isSelected |= DIP.get(f).trades;

			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			GButt.ButtPanel.renderFrame(r, body);

			f.banner().MEDIUM.renderCY(r, body.x1()+4, body.cY());

			buttPrice.clear();
			GFORMAT.i(buttPrice, price(f));
			buttPrice.adjustWidth();

			buttPrice.renderCY(r,  body.x1()+4+24+4, body.cY());
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			FactionNPC f = facs[ier.get()];
			VIEW.world().UI.factions.hover(text, f);
			GBox b = (GBox) text;

			b.sep();

			b.textLL(Dic.¤¤Price);
			b.tab(6).add(GFORMAT.i(b.text(), price(f)));
			b.NL(8);
			if (DIP.get(f).trades)
				b.text(¤¤TradeYes);
			else
				b.text(¤¤TradeNo);
			b.NL(4);
			b.text(¤¤Click);
		}

		@Override
		protected void clickA() {
			FactionNPC f = facs[ier.get()];
			if (RD.DIST().reachable(f) && !DIP.get(f).trades && ROPINIONS.current(f) >= DIP.TRADE().minLoyalty)
				VIEW.world().UI.factions.openTrade(f);
			else
				VIEW.world().UI.factions.openDip(f);
			VIEW.UI().manager.close();
		}


	}

	private final Comparator<FactionNPC> comp = new Comparator<FactionNPC>() {

		@Override
		public int compare(FactionNPC o1, FactionNPC o2) {
			return sortValue(o1) - sortValue(o2);
		}


	};

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		max = 0;
		if (isDiploTrade) {
			for (FactionNPC f : FACTIONS.NPCs()) {
				if (f.isActive() && price(f) > 0) {
					facs[max++] = f;
				}
			}
		}else{
			for (FactionNPC f : RD.DIST().neighs()) {
				if (price(f) > 0) {
					facs[max++] = f;
				}
			}
		}

		Arrays.sort(facs, 0, max, comp);


		super.render(r, ds);
	}

	protected abstract int price(FactionNPC f);

	protected abstract int sortValue(FactionNPC f);


}
