package view.ui.tech;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.text.D;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;
import java.util.Objects;
import static init.tech.CostBenefit.*;


public final class Node extends GuiSection{

	public final static int WIDTH = 112;
	public final static int HEIGHT = 112;
	public static final COLOR Cdormant = COLOR.WHITE100.shade(0.3);
	public static final COLOR CUnlockable = COLOR.WHITE100.shade(0.5);
	public static final COLOR Chovered = COLOR.WHITE100.shade(0.8);
	public static final COLOR Callocated = new ColorImp(10, 35, 55);
	public static final COLOR Cfinished = new ColorImp(10, 60, 60);

	private static CharSequence ¤¤Relock = "¤Hold {0} and click to disable this technology. {1} Knowledge will be added to your frozen pool.";

	int level;

	static {
		D.ts(Node.class);
	}

	public final TECH tech;
	Node(TECH tech){
		this.tech = tech;
		body().setDim(WIDTH, HEIGHT);
		addC(new Content(tech), body().cX(), body().cY());

	}

	private class Content extends ClickableAbs{

		Content(TECH tech){
			body.setDim(88, 88);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

			isHovered |= VIEW.UI().tech.tree.hoverededTechs[tech.index()];
			for (BoostSpec b : tech.boosters.all()) {
				if (b.boostable == VIEW.UI().tech.tree.hoveredBoost)
					isHovered = true;
			}

			isSelected |= FACTIONS.player().tech.level(tech) > 0;

			GCOLOR.T().H1.render(r, body);
			GCOLOR.UI().bg(isActive, false, isHovered).render(r, body,-1);


			// ADDED FUNCTION RUNS
			tech.Tech_CostBenefit.update(tech);

			COLOR col = col(isHovered, tech.Tech_CostBenefit.benefits); // MODIFIED Color change function
			col.render(r, body,-4);

			GCOLOR.UI().bg(isActive, false, isHovered).render(r, body,-7);

			{
				double levels = tech.levelMax;
				int level = FACTIONS.player().tech.level(tech);
				double d = level/levels;
				int y2 = body().y2()-8;
				int y1 = (int) (y2 - d*(body().height()-16));
				(d == 1.0 ? Cfinished : Callocated).render(r, body().x1()+8, body().x2()-8, y1, y2);

			}

			tech.icon().renderC(r, body);
			if (!isSelected) {
				if (FACTIONS.player().tech.costOfNextWithRequired(tech) > FACTIONS.player().tech().available().get() || !FACTIONS.player().tech.getLockable(tech).passes(FACTIONS.player())) {
					OPACITY.O50.bind();
					COLOR.BLACK.render(r, body, -1);
					OPACITY.unbind();
				}
			}



			if (VIEW.UI().tech.tree.filteredTechs[tech.index()]) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body);
				OPACITY.unbind();
			}

			if (hoveredIs()) {

			}else {

			}




		}
		// Color change start
		private COLOR col ( boolean hovered, double benefits){
			if (know_worker >=0){ // workers needed for this tech's cost
				tech.Tech_CostBenefit.costs = FACTIONS.player().tech.costOfNextWithRequired(tech) / know_worker;
			}else{  tech.Tech_CostBenefit.costs = 0;}


			double shade_val = hovered ? 1 : .6;
			// If no knowledge workers or no calculated benefits, default to white
			if (tech.Tech_CostBenefit.costs <= 0 || benefits <= 0) {
				return new ColorImp(127, 127, 127).shade(shade_val);
			}
			// High relative costs are red, high relative benefits are green
			// benefit 200 cost 100 ->   0 127   0
			// benefit 100 cost 100 -> 127 127   0
			// benefit  50 cost 100 -> 127   0   0
			if (benefits > tech.Tech_CostBenefit.costs) {
				// benefit = cost => 127 red. benefit >= 2 * cost
				double red = 255 - (benefits / tech.Tech_CostBenefit.costs * 127);
				// Clamp red from 0 to 127
				red = Math.min(Math.max(red, 0), 127);
				return new ColorImp((int) red, 127, 0).shade(shade_val);
			}
			if (tech.Tech_CostBenefit.costs > benefits) {
				// cost = benefit => 127 green. cost <= 2 * benefit => 0 green
				double green = 255 - (tech.Tech_CostBenefit.costs / benefits * 127);
				// Clamp green from 0 to 127
				green = Math.min(Math.max(green, 0), 127);
				return new ColorImp(127, (int) green, 0).shade(shade_val);
			}
			return new ColorImp(127, 127, 127).shade(shade_val);
		}
		// Color change end

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			text.title(tech.info.name);
			PTech t = FACTIONS.player().tech();

			{
				if (tech.levelMax == 1) {
					if (t.level(tech) == 1) {
						b.add(b.text().normalify2().add(Dic.¤¤Activated));
					}
				}else {
					b.textLL(Dic.¤¤Level);
					b.add(GFORMAT.iofkNoColor(b.text(), t.level(tech), tech.levelMax));
				}

				b.tab(5);
				b.textLL(Dic.¤¤Allocated);
				b.add(SPRITES.icons().s.vial);
				b.add(GFORMAT.iBig(b.text(), t.costTotal(tech)));
				b.NL();



				if (t.level(tech) < tech.levelMax) {
					b.NL(4);
					b.textLL(Dic.¤¤Cost);
					b.add(SPRITES.icons().s.vial);
					int c = t.costLevelNext(tech);

					if (t.available().get() < c)
						b.add(GFORMAT.iBig(b.text(), c).errorify());
					else
						b.add(GFORMAT.iBig(b.text(), c));


					b.tab(5);
					b.textLL(Dic.¤¤TotalCost);
					b.add(SPRITES.icons().s.vial);
					int ct = t.costOfNextWithRequired(tech);
					if (t.available().get() < ct)
						b.add(GFORMAT.iBig(b.text(), ct).errorify());
					else
						b.add(GFORMAT.iBig(b.text(), ct));
					b.NL(4);

					// Added UI elements to show info in hover box
					b.sep();
					tech.Tech_CostBenefit.update(tech);

					b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.costs * 10) /10, 1 ).color(GCOLOR.T().IGOOD));
					b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "The number of knowledge workers to get this tech."));
					b.NL();

					if (tech.Tech_CostBenefit.benefits > 0) {
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), tech.Tech_CostBenefit.benefits, 1).color(GCOLOR.T().IGOOD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "The number of workers worth of production this tech gives at current employment."));
						b.NL();
					}

					double maint_output=0;
					double spoil_output=0;
					double furniture_output=0;

					if (tech.Tech_CostBenefit.contains_upgrade){
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_maint_before * 10) / 10, 1).color(GCOLOR.T().IBAD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Current maintenance cost per worker"));
						b.NL();
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_maint_upgrade * 10) / 10, 1).color(GCOLOR.T().IBAD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per worker if all rooms are upgraded"));
						b.NL();
					}

					for (BoostSpec bb : tech.boosters.all()) {

						if (Objects.equals(bb.boostable.key(), "CIVIC_MAINTENANCE")){
							double costs = resource_use("Maintenance");
							maint_output = tech.Tech_CostBenefit.tech_divisor_presentation("maintenance", costs, bb, b);
						}
						if (Objects.equals(bb.boostable.key(), "CIVIC_SPOILAGE")){
							double costs = resource_use("Spoilage");

							spoil_output = tech.Tech_CostBenefit.tech_divisor_presentation("spoilage", costs, bb, b);
						}
						if (Objects.equals(bb.boostable.key(), "CIVIC_FURNITURE")){
							double costs = resource_use("Furniture");
							furniture_output = tech.Tech_CostBenefit.tech_divisor_presentation("furniture", costs, bb, b);
						}
						if (KEYS.MAIN().UNDO.isPressed()) {
							if ( !( cost_maint== 0 && cost_tools ==0 && cost_inputs==0 && cost_tot==0) ) { b.sep(); }
							if (cost_maint !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_maint * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per knowledge worker"));
								b.NL();
							}
							if (cost_tools !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_tools * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per knowledge worker"));
								b.NL();
							}
							if (cost_inputs !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_inputs * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Input costs per knowledge worker (paper)"));
								b.NL();
							}
							if (cost_tot !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average knowledge worker costs"));
								b.NL();
							}
//							if (benefit_maint_upgrade !=0) {
//								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_maint_upgrade * 10) / 10, 1).color(GCOLOR.T().IBAD));
//								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per worker if all rooms are upgraded"));
//								b.NL();
//							}

							if ( !( tech.Tech_CostBenefit.benefit_maint== 0 && tech.Tech_CostBenefit.benefit_tools ==0 && tech.Tech_CostBenefit.benefit_tot==0 ) ) { b.sep(); }

							if (tech.Tech_CostBenefit.benefit_maint != 0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_maint * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per boosted industry worker"));
								b.NL();
							}
							if (tech.Tech_CostBenefit.benefit_tools != 0) {
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tools * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per boosted industry worker"));
								b.NL();
							}
							if (tech.Tech_CostBenefit.benefit_tot !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average boosted industry workers costs"));
								b.NL();
							}
						}
					}
					if (! (maint_output == 0 && spoil_output == 0 && furniture_output == 0 )){
						if (!(KEYS.MAIN().UNDO.isPressed())){
							b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs)));
							b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));
							b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Profit per knowledge worker"));
							b.NL();
						}
					}
					// End UI edits

				}


			}
			b.sep();

			{
				LIST<TechRequirement> rr = tech.requires();

				int am = 0;
				for (TechRequirement r : rr)
					if (r.level > 0)
						am++;

				FACTIONS.player().tech.getLockable(tech).hover(text, FACTIONS.player());

				if (am > 0) {
					if (FACTIONS.player().tech.getLockable(tech).all().size() == 0)
						b.textLL(Dic.¤¤Requires);
					b.NL();
					for (TechRequirement r : rr) {
						if (r.level <= 0)
							continue;
						b.add(UI.icons().s.vial);
						GText te = b.text();
						te.add(r.tech.tree.name);
						te.add(':').s();
						te.add(r.tech.info.name);
						if (r.tech.levelMax > 1) {
							te.s().add(r.level);
						}
						if (t.level(r.tech) >= r.level)
							te.normalify2();
						else
							te.errorify();
						b.add(te);
						b.NL();

					}
				}
			}
			b.NL(8);

			tech.lockers.hover(text);

			b.NL(8);

			if (tech.boosters.all().size() > 0)

				if (tech.boosters.all().size() > 0) {
					b.textLL(Dic.¤¤Effects);
					b.tab(6);
					b.textLL(Dic.¤¤Current);
					b.tab(9);
					b.textLL(Dic.¤¤Next);
					b.NL();

					for (BoostSpec bb : tech.boosters.all()) {
						b.add(bb.boostable.icon);
						b.text(bb.boostable.name);
						b.tab(6);
						double v = bb.booster.to();
						if (bb.booster.isMul)
							v -= 1;
						v*=t.level(tech);
						if (bb.booster.isMul)
							v += 1;
						b.add(bb.booster.format(b.text(), v));

						if (t.level(tech) < tech.levelMax) {
							v = bb.booster.to();
							if (bb.booster.isMul)
								v -= 1;
							v*=t.level(tech)+1;
							if (bb.booster.isMul)
								v += 1;

							b.tab(9);
							b.add(bb.booster.format(b.text(), v));
						}



						b.NL();
					}
					b.NL(8);
				}

			b.sep();

			text.text(tech.info.desc);
			b.NL();

			if (t.level(tech) > 0) {
				GText te = b.text();
				te.add(¤¤Relock);
				te.insert(0, KEYS.MAIN().UNDO.repr());
				te.insert(1, t.costLevel(tech, t.level(tech)));
				b.error(te);
			}


		}

		@Override
		protected void clickA() {
			if (KEYS.MAIN().UNDO.isPressed())
				VIEW.UI().tech.tree.prompt.forget(tech);
			else
				VIEW.UI().tech.tree.prompt.unlock(tech);
			super.clickA();
		}

	}

}