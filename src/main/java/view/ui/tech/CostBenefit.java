package view.ui.tech;

import game.boosting.BOOSTABLES;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import game.values.Lock;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.tech.TECH;
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
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;

import java.util.Objects;

import static game.time.TIME.playedGame;
import static settlement.main.SETT.ROOMS;

public class CostBenefit {

        static double CUR_TIME = 0;
        private static double CUR_RUN = 0;
        public static int know_emp = 0 ; 	// laboratory employment
        public static int know_emp2 = 0 ;	// library  employment

        public static double know_lab = 0; 	// knowledge per laboratory worker
        public static double know_lib = 0; 	// knowledge per library worker
        public static double know_worker;  	// knowledge per worker (average)

        public boolean contains_upgrade = false;

        public static double benefit_maint_upgrade = 0;
        public static double benefit_maint_before = 0 ;
        public static double benefit_maint = 0;	// Maintenance per worker for the benefitting industries
        public static double benefit_tools = 0; // tool cost per person for benefited industry buildings
        public static double benefit_tot = 0; 	// total benefits cost (tools + maint atm) per person

        public static double cost_inputs = 0;    // paper cost per knowledge worker
        public static double cost_maint = 0;	// Maintenance per worker for the knowledge buildings
        public static double cost_tools = 0;    // tool cost per person for knowledge buildings
        public static double cost_tot = 0; 	// total costs (tools + maint atm) per person
        public static double cost_education = 0; // Cost of all schools and universities, including paper, tools, maintenance - not included yet

        double costs;      // Overall cost in workers
        double benefits;   // Overall benefit in workers


        // Create the Green-Yellow-Red color for nodes based on the cost/benefit
        public COLOR col (boolean hovered, double benefits, TECH tech){
                if (know_worker >=0){ // workers needed for this tech's cost
                        costs = FACTIONS.player().tech.costOfNextWithRequired(tech) / know_worker;
                }else{  costs = 0;}


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


        static void knowledge_costs()
        {
                // Only run this if you haven't lately.
                if (CUR_TIME == playedGame()){return;}
                CUR_TIME = playedGame();

                // RESET VARIABLES
                know_emp = 0 ; 		// laboratory employment
                know_emp2 = 0 ;		// library  employment
                know_worker = 0; 	// reset "knowledge per worker"
                long know_tot = 0;  	// Laboratory knowledge
                double know_tot2 = 0; 	// library knowledge

                // KNOWLEDGE AND EMPLOYMENT
                for (ROOM_LABORATORY lab : ROOMS().LABORATORIES) {
                        know_tot += lab.knowledge();
                        know_emp += lab.employment().employed();
                }
                for (ROOM_LIBRARY libraries : ROOMS().LIBRARIES) {
                        know_tot2 += libraries.knowledge();
                        know_emp2 += libraries.employment().employed();
                }
                know_tot2 *= know_tot; // libraries knowledge is a multiplier of laboratory knowledge
                if (know_emp  != 0){ know_lab  = know_tot  / know_emp ;} // knowledge per laboratory worker
                if (know_emp2 != 0){ know_lib  = know_tot2 / know_emp2;} // knowledge per library worker
                if ((know_emp + know_emp2)>0){ know_worker = (know_tot + know_tot2) / (know_emp + know_emp2);} // knowledge per worker (both)


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
                                        RoomEmploymentSimple ee = r.blueprint().employment();
                                        RoomEmploymentIns e = r.employees();

                                        // PAPER (INPUTS)
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
                                                cost_inputs += total;
                                        }

                                        // TOOLS
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

        void booster_benefits(TECH tech)
        {

                // Only run this if you haven't lately.
                if (CUR_RUN == playedGame()){return;}
                CUR_RUN = playedGame();


                // Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
                benefits = 0; // reset benefit upon new tech
                benefit_maint = 0;
                benefit_tools = 0;

                double benefit_maint_total = 0;
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
                                        if(  tot * b.booster.to() / (1+add) > 0  ){
                                                benefits += tot * b.booster.to() / (1+add); //Add the technology's benefit of each workshop
                                        }

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
                                        if (deg == null){ continue; }
                                        for (int i = 0; i < deg.resSize(); i++) {
                                                if (r.resAmount(i,1+r.upgrade()) <= 0)
                                                        continue;
                                                RESOURCE res = deg.res(i);

                                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
                                                total_room_maintenance_import -= n * sellFor;


                                        }
                                        benefit_maint_total += total_room_maintenance_import; // Add for each room


                                }
                        }
                }
                if (benefit_emp_total >0) { benefit_tools = benefit_tools / benefit_emp_total; }
                if (benefit_emp_total >0) { benefit_maint = benefit_maint_total / benefit_emp_total; }
                benefit_tot = benefit_tools + benefit_maint;

        }
        void unlock_benefits(TECH tech)
        { // UNLOCKING UPGRADES

                // Only run this if you haven't lately.
                if (CUR_RUN == TIME.days().bitCurrent()){return;}
                CUR_RUN = TIME.days().bitCurrent();

                // Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
                double benefit_maint_total = 0;
                double benefit_maint_upgrade_total = 0;
                double benefit_emp_total = 0;

                for (Lock ll : tech.lockers.all()){ // For all unlockables

                        for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
                                if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

                                for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry
                                        if (r.employees() == null || !(r instanceof ROOM_PRODUCER)) { continue; } // That has employees and produces goods

                                        RoomBlueprintImp b = (RoomBlueprintImp) r.blueprint();  // Get blueprint to find unlocks
                                        try {b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all().isEmpty();}catch(Exception e){continue;} //Skip this room if there are no upgrades
                                        Industry ind = ((ROOM_PRODUCER) r).industry();  // Industry of the workshop
                                        Boostable bonus = ind.bonus(); // The boosts of the industry


                                        if (bonus == null) { continue; } // Boost exists in industry and matches the tech



                                        boolean contains = false;
                                        // MAINTENANCE COSTS
                                        for (Lock<Faction> s : b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all()) { //loop through all buildings
                                                if (s.unlocker.name == ll.unlocker.name) { // If the tech matches the building
                                                        contains_upgrade = true; // Allow it to be shown later
                                                        contains = true;
                                                        benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance
                                                        if (r.employees().employed()==0){continue;} // Skip if no employees
                                                        int up = 1; // +1 upgrade level
                                                        if ( r.upgrade() == r.blueprintI().upgrades().max() ){up=0;} // Unless it's already at the max

                                                        ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY()); // rateResource requirement
                                                        double iso = r.isolation(r.mX(), r.mY()); 	// rateResource requirement
                                                        double boost = SETT.MAINTENANCE().speed();   	// rateResource requirement

                                                        double total_room_maintenance_import = 0;		// reset 'current building' stats
                                                        double total_room_maintenance_import_upgraded = 0;	// reset 'current building' stats
                                                        if (deg == null){ continue; }    			// If no maintenance, skip
                                                        for (int i = 0; i < deg.resSize(); i++) {		// For each resource
                                                                if (r.resAmount(i,up+r.upgrade()) <= 0){continue;}	// skip if resource use is 0 (at upgraded maintenance level)

                                                                // Maintenance amount per day for current upgrade level
                                                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                                // Maintenance amount per day for upgraded level (if not maxed)
                                                                double m = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,up+r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(deg.res(i)); 	// get the import cost of the resource
                                                                total_room_maintenance_import -= n * sellFor;				// multiply cost * amount of resource (not upgraded)
                                                                total_room_maintenance_import_upgraded -= m * sellFor;			// multiply cost * amount of resource (upgraded)

                                                        }
                                                        benefit_maint_total += total_room_maintenance_import; 				// Add cost for each room
                                                        benefit_maint_upgrade_total += total_room_maintenance_import_upgraded; 		// Add cost for each room
                                                }
                                        }
                                        if (contains) { // If this room had an unlock
                                                // UPGRADE BOOSTS BENEFITS
                                                double add = 0;
                                                int tot = 0;
                                                double upgrade = 0;

                                                for (Humanoid person : r.employees().employees()) { // for each person working
                                                        tot++; //adding up the number of employees
                                                        if (STATS.WORK().EMPLOYED.get(person) == r) { //IDK if this is needed, copied from hoverBoosts function
                                                                for (Booster s : bonus.all()) { // look at all boosts an industry has
                                                                        if (!s.isMul) {
                                                                                add += s.get(person.indu());
                                                                        } // add up the non-multiplier bonuses
                                                                        if (Objects.equals(s.info.name, "Upgrade")) {
                                                                                upgrade = b.upgrades().boost(r.upgrade() + 1);
                                                                        } // assign upgrade the booster amount
                                                                }
                                                        }
                                                }
                                                add /= tot; // add is the total additive bonuses.
                                                if ((tot * upgrade / (1 + add)) > 0) {
                                                        benefits += tot * upgrade / (1 + add); //Add the technology's benefit of each workshop
                                                }
                                        }
                                }
                        }
                }
                if (benefit_emp_total >0) { benefit_maint_before = benefit_maint_total / benefit_emp_total; }
                if (benefit_emp_total >0) { benefit_maint_upgrade = benefit_maint_upgrade_total / benefit_emp_total; }
        }

        static private double resource_use(String Source) {
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



        static private double next_tech_benefit(BoostSpec bb){
                double cur = 0;
                for ( Boostable A : BOOSTABLES.CIVICS().all() ) {
                        if (Objects.equals(bb.boostable.key(), A.key)) {
                                cur = A.get(POP_CL.clP(null, null));
                        }
                }
                double v = bb.booster.to(); 	// benefit per level of tech
                double w = cur + v;		// current benefit from all sources +1 tech level
                v = cur;			// current benefit from all sources

                return 100 * (1 - v/w);
        }
        private double tech_divisor_presentation(String what, double denari_costs, BoostSpec bb, TECH tech, GBox b) {
                double tech_benefit = next_tech_benefit(bb); // % of maintenance you'll still have, e.g. 20% reduction from current tech level
                b.sep();
                if (Objects.equals(what, "spoilage")){
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Note: Spoilage estimates assume spoilage was in a hauler or warehouse"));
                        b.NL();
                }
                if (KEYS.MAIN().UNDO.isPressed()) {

                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), 1/( 1+ FACTIONS.player().tech.level(tech) * bb.booster.to() ) ) );
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "%"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Current percent from this tech"));
                        b.NL();

                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(denari_costs)));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost of all ".concat(what)));
                        b.NL();
                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech_benefit * 100) / 100, 2).color(GCOLOR.T().IGOOD));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "%"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Percent reduction from this tech"));
                        b.NL();

                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(-denari_costs * tech_benefit/ costs/100)));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Estimated ".concat(what).concat(" benefits per worker")));
                        b.NL();

                        b.sep();
                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long)  ( ( -denari_costs * tech_benefit/100 + cost_tot * costs) / costs ) ) );
                        //  ( (-Amount of money saved)  + (costs per worker * # of tech workers) )/ # of tech workers
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost-Benefit per knowledge worker from this tech"));
                        b.NL();
                }
                return -denari_costs * tech_benefit/100;
        }

}
