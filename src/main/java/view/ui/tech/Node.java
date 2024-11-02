package view.ui.tech;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.player.PTech;
import game.time.TIME;
import game.values.Lock;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.text.D;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.knowledge.laboratory.ROOM_LABORATORY;
import settlement.room.knowledge.library.ROOM_LIBRARY;
import settlement.room.main.*;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import settlement.stats.STATS;
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

import static settlement.main.SETT.ROOMS;
import static settlement.room.industry.module.IndustryUtil.calcProductionRate;

public final class Node extends GuiSection{

	public static double know_worker;  	// knowledge per worker (average)

	public static double know_lab = 0; 	// knowledge per laboratory worker
	public static double know_lib = 0; 	// knowledge per library worker

	public static double benefit_tot = 0; 	// total benefits cost (tools + maint atm) per person
	public static double cost_tot = 0; 	// total costs cost (tools + maint atm) per person

	public static double benefit_maint = 0;	// Maintenance per worker for the benefitting industries
	public static double cost_maint = 0;	// Maintenance per worker for the knowledge buildings

	public static double benefit_maint_upgrade = 0;
	public static double benefit_maint_before = 0 ;

	public static double benefit_tools = 0; // tool cost per person for benefited industry buildings
	public static double cost_tools = 0;    // tool cost per person for knowledge buildings

	public static double cost_inputs = 0;    // paper cost per knowledge worker
	public static double cost_education = 0; // Cost of all schools and universities, including paper, tools, maintenance
	public boolean contains = false;

	public static int know_emp = 0 ; 	// laboratory employment
	public static int know_emp2 = 0 ;	// library  employment

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



		double costs;
		double benefits;

		void knowledge_costs()
		{
		// Knowledge per worker
			know_emp = 0 ; 	// laboratory employment
			know_emp2 = 0 ;	// library  employment
			// Cost analysis
			long know_tot = 0;  	// Laboratory knowledge
			double know_tot2 = 0; 	// library knowledge
			know_worker = 0; 	// reset "knowledge per worker"
			int tech_cost = FACTIONS.player().tech.costOfNextWithRequired(tech);	// knowledge cost for this tech
			// Add knowledge generated and employment in each room for laboratories and libraries
			for (ROOM_LABORATORY lab : ROOMS().LABORATORIES) {
				know_tot += lab.knowledge();
				know_emp += lab.employment().employed();
			}
			for (ROOM_LIBRARY libraries : ROOMS().LIBRARIES) {
				know_tot2 += libraries.knowledge();
				know_emp2 += libraries.employment().employed();
// How ModuleIndustry calculates input costs
//				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
//				double total = 0;
//
//				for (int ri = 0; ri < p.industry().ins().size(); ri++) {
//					Industry.IndustryResource i = p.industry().ins().get(ri);
//					double n = i.dayPrev.get(p);
//					double sellFor = FACTIONS.player().trade.pricesBuy.get(i.resource);
//					total -= n * sellFor;
//				}
			}
			know_tot2 *= know_tot; // libraries knowledge is a multiplier of laboratory knowledge
			if (know_emp  != 0){ know_lab  = know_tot  / know_emp ;} // knowledge per laboratory worker
			if (know_emp2 != 0){ know_lib  = know_tot2 / know_emp2;} // knowledge per library worker
			if ((know_emp + know_emp2)>0){ know_worker = (know_tot + know_tot2) / (know_emp + know_emp2);} // knowledge per worker (both)
			if (know_worker >=0){costs = tech_cost / know_worker;}else{costs = 0;} // workers needed for this tech's cost


		// MAINTENANCE + TOOLS costs
			cost_maint = 0;
			cost_tools = 0;
			cost_inputs= 0;
			double cost_maint_total = 0;
			double cost_emp_total = 0;
			for (RoomBlueprint h : ROOMS().all()) {
				if (Objects.equals(h.key, "LABORATORY_NORMAL") || Objects.equals(h.key, "LIBRARY_NORMAL")) {
					for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) {
						if (r.employees() == null) { continue; } // That has employees

						// TOOLS
						RoomEmploymentSimple ee = r.blueprint().employment();
						RoomEmploymentIns e = r.employees();
						if ( h.key.equals("LIBRARY_NORMAL")) {
							// How ModuleIndustry calculates input costs
							ROOM_PRODUCER s = ((ROOM_PRODUCER) r);
							double total = 0;

							for (int ri = 0; ri < s.industry().ins().size(); ri++) {
								Industry.IndustryResource i = s.industry().ins().get(ri);
								double n = i.dayPrev.get(s);
								double sellFor = FACTIONS.player().trade.pricesBuy.get(i.resource);
								total -= n * sellFor;
							}
							// Adds the paper costs of libraries
							cost_inputs += total;
						}

						for (RoomEquip w : ee.tools()) {
							double n = w.degradePerDay * e.tools(w);
							double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
							cost_tools -= n * sellFor;
						}



						// MAINTENANCE
						ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
						double iso = r.isolation(r.mX(), r.mY());
						double boost = SETT.MAINTENANCE().speed();
						double total_room_maintenance_import = 0;

						if (deg == null) { continue; }
						for (int i = 0; i < deg.resSize(); i++) {
							if (deg.resAmount(i) <= 0)
								continue;
							RESOURCE res = deg.res(i);

							double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i)) * TIME.years().bitConversion(TIME.days()) / 16.0;
							double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
							total_room_maintenance_import -= n * sellFor;
						}
						cost_maint_total += total_room_maintenance_import; // Add for each room
						cost_emp_total += r.employees().employed();
					}
				}
			}
			if (cost_emp_total >0) { cost_maint = cost_maint_total / cost_emp_total; }
			if (cost_emp_total >0) { cost_tools /=  cost_emp_total; }
			if (cost_emp_total >0) { cost_inputs /= cost_emp_total; }
			cost_tot = cost_maint + cost_tools + cost_inputs;

		}
		void booster_benefits()
		{
			// Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
			benefits = 0; // reset benefit upon new tech
			benefit_maint = 0;
			benefit_tools = 0;

			double benefit_maint_total = 0;
			double benefit_maint_upgrade_total = 0;
			double benefit_emp_total = 0;

			for (BoostSpec b : tech.boosters.all()) { // For all boosts of this tech,

				for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
					if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

					for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry

						if (r.employees() == null || !(r instanceof ROOM_PRODUCER)) { continue; } // That has employees and produces goods

						Industry ind = ((ROOM_PRODUCER) r).industry();  // Industry of the workshop
						Boostable bonus = ind.bonus(); // The boosts of the industry

						if (bonus == null || b.boostable.key() != bonus.key()) { continue; } // Boost exists in industry and matches the tech

						// BOOSTS BENEFITS
						double add = 0;
						int tot = 0;

						for (Humanoid person : r.employees().employees()) { // for each person working
							tot++; //adding up the number of employees
							if (STATS.WORK().EMPLOYED.get(person) == r) { //IDK if this is needed, copied from hoverBoosts function
								for (Booster s : bonus.all()) { // look at all boosts an industry has
									if (!s.isMul) { // add up the non-multiplier bonuses
										add += s.get(person.indu());
									}
								}
							}
						}
						add /= tot; // add is the total additive bonuses.
						benefits += tot * b.booster.to() / (1+add); //Add the technology's benefit of each workshop


						// TOOLS COSTS
						RoomEmploymentSimple ee = r.blueprint().employment();
						RoomEmploymentIns e = r.employees();

						for (RoomEquip w : ee.tools()) {
							double n = w.degradePerDay * e.tools(w);
							double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
							benefit_tools -= n * sellFor;
						}
						benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance


						// MAINTENANCE COSTS
						ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
						double iso = r.isolation(r.mX(), r.mY());
						double boost = SETT.MAINTENANCE().speed();

						double total_room_maintenance_import = 0;
						double total_room_maintenance_import_upgraded = 0;
						if (deg == null){ continue; }
						for (int i = 0; i < deg.resSize(); i++) {
							if (r.resAmount(i,1+r.upgrade()) <= 0)
								continue;
							RESOURCE res = deg.res(i);

							double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
							double m = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,1+r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
							double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
							total_room_maintenance_import -= n * sellFor;
							total_room_maintenance_import_upgraded -= m * sellFor;

						}
						benefit_maint_total += total_room_maintenance_import; // Add for each room
						benefit_maint_upgrade_total += total_room_maintenance_import_upgraded; // Add for each room

					}
				}
			}
			if (benefit_emp_total >0) { benefit_tools = benefit_tools / benefit_emp_total; }
			if (benefit_emp_total >0) { benefit_maint = benefit_maint_total / benefit_emp_total; }
			if (benefit_emp_total >0) { benefit_maint_upgrade = benefit_maint_upgrade_total / benefit_emp_total; }
			benefit_tot = benefit_tools + benefit_maint;

		}
		void unlock_benefits()
		{
			// Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
			double benefit_maint_total = 0;
			double benefit_maint_upgrade_total = 0;
			double benefit_emp_total = 0;

			for (Lock ll : tech.lockers.all()){ // For all unlockables
//				if (benefit_maint_upgrade != 0){
//					b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_maint_upgrade * 10) / 10, 1).color(GCOLOR.T().IBAD));
//					b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per boosted industry worker if all rooms are upgraded"));
//					b.NL();
//				}


				for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
					if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

					for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry

						if (r.employees() == null || !(r instanceof ROOM_PRODUCER)) { continue; } // That has employees and produces goods

						Industry ind = ((ROOM_PRODUCER) r).industry();  // Industry of the workshop
						Boostable bonus = ind.bonus(); // The boosts of the industry

//						if (bonus == null || b.boostable.key() != bonus.key()) { continue; } // Boost exists in industry and matches the tech
						RoomBlueprintImp b = (RoomBlueprintImp) r.blueprint();

						if (b.upgrades() == null){continue;}
						try {
							b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all().isEmpty();
						}
						catch(Exception e){
							continue;
						}

						for (Lock<Faction> s : b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all()) {
                                                        if (s.unlocker.name == ll.unlocker.name) {
                                                                contains = true;
								benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance


								// MAINTENANCE COSTS
								ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
								double iso = r.isolation(r.mX(), r.mY());
								double boost = SETT.MAINTENANCE().speed();

								double total_room_maintenance_import = 0;
								double total_room_maintenance_import_upgraded = 0;
								if (deg == null){ continue; }
								for (int i = 0; i < deg.resSize(); i++) {
									if (r.resAmount(i,1+r.upgrade()) <= 0)
										continue;
									RESOURCE res = deg.res(i);

									double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
									double m = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,1+r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
									double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
									total_room_maintenance_import -= n * sellFor;
									total_room_maintenance_import_upgraded -= m * sellFor;

								}
								benefit_maint_total += total_room_maintenance_import; // Add for each room
								benefit_maint_upgrade_total += total_room_maintenance_import_upgraded; // Add for each room
                                                        }
						}
						// is including more employment than it should, so I need restrict it better (everything in the if statement?)

//						// TOOLS COSTS
//						RoomEmploymentSimple ee = r.blueprint().employment();
//						RoomEmploymentIns e = r.employees();
//
//						for (RoomEquip w : ee.tools()) {
//							double n = w.degradePerDay * e.tools(w);
//							double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
//							benefit_tools -= n * sellFor;
//						}


					}
				}
			}
//			if (benefit_emp_total >0) { benefit_tools = benefit_tools / benefit_emp_total; }
			if (benefit_emp_total >0) { benefit_maint_before = benefit_maint_total / benefit_emp_total; }
			if (benefit_emp_total >0) { benefit_maint_upgrade = benefit_maint_upgrade_total / benefit_emp_total; }
//			benefit_tot = benefit_tools + benefit_maint;

		}

		private double resource_use(String Source) {
			double tot = 0;
			for (RESOURCE res : RESOURCES.ALL()) {
				for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(res)) {
					if (Objects.equals(rr.name(), Source)){
						if (rr.am() == 0) {continue;}
						// import price
						// FACTIONS.player().trade.pricesBuy.get(res)
						// resource "value" (average value to factions across the world)
						// FACTIONS.PRICE().get(res)
						tot -= rr.am() * FACTIONS.PRICE().get(res) ;
					}
				}
			}
			return tot;
		}

		private double next_tech_benefit(BoostSpec bb){

//			bb.boostable.key();     CIVIC_MAINTENANCE
//			BOOSTABLES.CIVICS().MAINTENANCE.KEY = CIVIC_MAINTENANCE
//			bb.booster.info.name;  Maintenance
//			BOOSTABLES.CIVICS().MAINTENANCE.get(POP_CL.clP(null, null)) = value
			double cur = 0;
			for ( Boostable A : BOOSTABLES.CIVICS().all() ) {
				if (Objects.equals(bb.boostable.key(), A.key)) {
					cur = A.get(POP_CL.clP(null, null));
				}
			}
			// New Method, using the current value from all sources
			double v = bb.booster.to(); 	// benefit per level of tech
			double w = cur + v;
			v = cur;


			// Old Method, using the tech level ONLY to determine current benefit:
//			PTech t = FACTIONS.player().tech();
//			double v = bb.booster.to(); 	// benefit per level of tech
//			double w = v * (t.level(tech)+1);// W = the *next* level's benefit
//			v*=t.level(tech);		//  V = this level's benefit
			// Maintenance and Spoilage tech use the following formula:
			// 1 / ( 1 + v)
			// So to find out the decrease *relative to current maintenance* (above formula)
			// New maintenance / old maintenance = e.g. 90%, so 1-.9= .1 is the decrease, * 100 for 10%
//			return 100 * (1 - (  1 / (1 + w) ) / (  1 / (1 + v) )) ;
			// New method already includes the "1+"
			return 100 * (1 - v/w);
		}
		private double tech_divisor_presentation(String what, double denari_costs, BoostSpec bb, GBox b) {
			double tech_benefit = next_tech_benefit(bb); // % of maintenance you'll still have, e.g. 20% reduction from current tech level
			b.sep();
			if (Objects.equals(what, "spoilage")){
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Note: Spoilage estimates assume spoilage was in a hauler or warehouse"));
				b.NL();
			}
			if (KEYS.MAIN().UNDO.isPressed()) {
				b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(denari_costs)));
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost of all ".concat(what)));
				b.NL();
				b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech_benefit * 100) / 100, 2).color(GCOLOR.T().IGOOD));
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "%"));   b.tab(2);
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Percent reduction from this tech"));
				b.NL();

//				b.sep();
//				b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(-denari_costs * tech_benefit/100)));
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Estimated ".concat(what).concat(" benefits from this tech")));
//				b.NL();
				b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(-denari_costs * tech_benefit/ costs/100)));
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Estimated ".concat(what).concat(" benefits per worker")));
				b.NL();

//				b.sep();
//				b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(100 * cost_tot * costs) / 100, 2).color(GCOLOR.T().IBAD));
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total costs for knowledge workers"));
//				b.NL();
//				b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(100 * cost_tot ) / 100, 2).color(GCOLOR.T().IBAD));
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
//				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total costs per knowledge worker"));
//				b.NL();

				b.sep();
				b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long)  ( ( -denari_costs * tech_benefit/100 + cost_tot * costs) / costs ) ) );
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
				b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost-Benefit per knowledge worker from this tech"));
				b.NL();
			}
			return -denari_costs * tech_benefit/100;
		}

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

			knowledge_costs();
			booster_benefits();
			unlock_benefits();
			COLOR col = col(isHovered, costs, benefits); // Color change function
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
		private COLOR col ( boolean hovered, double costs, double benefits){
			double shade_val = hovered ? 1 : .6;
			// If no knowledge workers or no calculated benefits, default to white
			if (costs <= 0 || benefits <= 0) {
				return new ColorImp(127, 127, 127).shade(shade_val);
			}
			// High relative costs are red, high relative benefits are green
			// benefit 200 cost 100 ->   0 127   0
			// benefit 100 cost 100 -> 127 127   0
			// benefit  50 cost 100 -> 127   0   0
			if (benefits > costs) {
				// benefit = cost => 127 red. benefit >= 2 * cost
				double red = 255 - (benefits / costs * 127);
				// Clamp red from 0 to 127
				red = Math.min(Math.max(red, 0), 127);
				return new ColorImp((int) red, 127, 0).shade(shade_val);
			}
			if (costs > benefits) {
				// cost = benefit => 127 green. cost <= 2 * benefit => 0 green
				double green = 255 - (costs / benefits * 127);
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
					knowledge_costs();
					booster_benefits();
					unlock_benefits();

					b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(costs * 10) /10, 1 ).color(GCOLOR.T().IGOOD));
					b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "The number of knowledge workers to get this tech."));
					b.NL();

					if (benefits > 0) {
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), benefits, 1).color(GCOLOR.T().IGOOD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "The number of workers worth of production this tech gives at current employment."));
						b.NL();
					}

					double maint_output=0;
					double spoil_output=0;
					double furniture_output=0;

					if (contains){
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_maint_before * 10) / 10, 1).color(GCOLOR.T().IBAD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Current maintenance cost per worker"));
						b.NL();
						b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_maint_upgrade * 10) / 10, 1).color(GCOLOR.T().IBAD));
						b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per worker if all rooms are upgraded"));
						b.NL();
					}

					for (BoostSpec bb : tech.boosters.all()) {

						if (Objects.equals(bb.boostable.key(), "CIVIC_MAINTENANCE")){
							double costs = resource_use("Maintenance");
							maint_output = tech_divisor_presentation("maintenance", costs, bb, b);
						}
						if (Objects.equals(bb.boostable.key(), "CIVIC_SPOILAGE")){
							double costs = resource_use("Spoilage");

							spoil_output = tech_divisor_presentation("spoilage", costs, bb, b);
						}
						if (Objects.equals(bb.boostable.key(), "CIVIC_FURNITURE")){
							double costs = resource_use("Furniture");
							furniture_output = tech_divisor_presentation("furniture", costs, bb, b);
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

							if ( !( benefit_maint== 0 && benefit_tools ==0 && benefit_tot==0 ) ) { b.sep(); }

							if (benefit_maint != 0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_maint * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per boosted industry worker"));
								b.NL();
							}
							if (benefit_tools != 0) {
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_tools * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per boosted industry worker"));
								b.NL();
							}
							if (benefit_tot !=0){
								b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(benefit_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
								b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average boosted industry workers costs"));
								b.NL();
							}
						}
					}
					if (! (maint_output == 0 && spoil_output == 0 && furniture_output == 0 )){
						if (!(KEYS.MAIN().UNDO.isPressed())){
							b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * costs) / costs)));
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
