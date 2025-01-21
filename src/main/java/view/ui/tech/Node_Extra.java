package view.ui.tech;

import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TechCost;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import java.util.Objects;
import static init.tech.CostBenefit.*;
import static init.tech.Knowledge_Costs.*;
import static view.ui.goods.UIMaintenance.sum_d;

public class Node_Extra{
        public double worker_cost ;
        public void  output(TECH tech, GBox b){
                output0(tech, b); // All techs, cost in workers, extra: breakdown of costs
                output1(tech, b); // Production Bonus Tech text
                output2(tech, b); // Maint/Spoil/Furniture use reduction tech

        }

        public void output0(TECH tech, GBox b)
        {
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      For All Techs           ///////////////////////////////////////////////
                /////////      Always displayed        ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                // Calculate the total costs and benefits for simplification
                int j = 0;
                worker_cost   = 0;

                if (tech.costs == null){return;}
                if (FACTIONS.player() == null){return;}
                if (FACTIONS.player().tech == null){return;}

                for (TechCost c : tech.costs) {
                        PTech t = FACTIONS.player().tech();
                        int cost = t.costLevelNext(c.amount, tech);
                        worker_cost += cost;
                        // For each tech currency: Tech cost / knowledge per worker
                        j += 1;
                }
                worker_cost = (sum_d(know_worker) != 0) ? worker_cost/sum_d(know_worker) : 0;


                // Calculate the benefit of a technology based on the "benefit"
                int i = 0;
                if (tech.Tech_CostBenefit.benefits != 0 ) {
                        b.NL();
                        b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefits / worker_cost , 3));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Below '1' means you are spending more labor in tech buildings than you'd gain from having the tech."));
                }
                i += 1;
                if (!KEYS.MAIN().UNDO.isPressed()) {
                        b.sep();
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Press Undo button for more info"));
                        b.sep();
                }
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////

                if (KEYS.MAIN().UNDO.isPressed()) {
                        b.sep();
                        b.add(GFORMAT.f(b.text(), worker_cost, 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Tech worker cost"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), sum_d(cost_total), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Tech upkeep cost"));
                }

        }
        public void output1(TECH tech, GBox b)
        {
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      Production Bonus Tech     /////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      Always displayed        ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                tech.Tech_CostBenefit.update(tech);

                b.NL();
                b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefits, 1));
                b.tab(2);
                b.add(GFORMAT.text(b.text(), "Production worker Benefit"));

                b.NL();
                b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefit_tot, 1));
                b.tab(2);
                b.add(GFORMAT.text(b.text(), "Cost of production upkeep per worker"));

                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                if (KEYS.MAIN().UNDO.isPressed()) {
                        int i = 0;
                        for (PTech.TechCurr tech_value : FACTIONS.player().tech().currs()) {
                                b.NL();
                                b.add(GFORMAT.text(b.text(), " "));
                                b.NL();

                                b.add(GFORMAT.text(b.text(), tech_value.cu.bo.name));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_tot[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Tech currency total"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_emp[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Employment"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_worker[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Knowledge per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_total[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Costs per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_inputs[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total input Costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_maint[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Maintenance costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_tools[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Tools costs"));

                                b.NL();
                                b.NL();

                                i += 1;
                        }

                }

        }



        // Maintenance, Spoilage, Furniture cost reduction boosts
        public void output2(TECH tech, GBox b) {
                double maint_output=0;
                double spoil_output=0;
                double furniture_output=0;

                for (BoostSpec bb : tech.boosters.all()) {

                        if (Objects.equals(bb.boostable.key(), "CIVIC_MAINTENANCE")) {
                                double costs = resource_use("Maintenance");
                                maint_output = tech_divisor_presentation(tech, "maintenance", costs, bb, b);

                        }
                        if (Objects.equals(bb.boostable.key(), "CIVIC_SPOILAGE")) {
                                double costs = resource_use("Spoilage");
                                spoil_output = tech_divisor_presentation(tech, "spoilage", costs, bb, b);
                                b.sep();
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Note: Spoilage estimate excludes floor spoilage which gets no benefit from spoilage tech"));
                                b.NL();
                        }
                        if (Objects.equals(bb.boostable.key(), "CIVIC_FURNITURE")) {
                                double costs = resource_use("Furniture");
                                furniture_output = tech_divisor_presentation(tech, "furniture", costs, bb, b);
                        }

                        if (KEYS.MAIN().UNDO.isPressed()) {
                                if (!(tech.Tech_CostBenefit.benefit_maint == 0 && tech.Tech_CostBenefit.benefit_tools == 0 && tech.Tech_CostBenefit.benefit_tot == 0)) {
                                        b.sep();
                                }

                                if (tech.Tech_CostBenefit.benefit_maint != 0) {
                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_maint * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per boosted industry worker"));
                                        b.NL();
                                }
                                if (tech.Tech_CostBenefit.benefit_tools != 0) {
                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tools * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per boosted industry worker"));
                                        b.NL();
                                }
                                if (tech.Tech_CostBenefit.benefit_tot != 0) {
                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average boosted industry workers costs"));
                                        b.NL();
                                }
                                if (!(tech.Tech_CostBenefit.benefit_maint == 0 && tech.Tech_CostBenefit.benefit_tools == 0 && tech.Tech_CostBenefit.benefit_tot == 0)) {
                                        b.NL();
                                }
                        }
                }
                if (KEYS.MAIN().UNDO.isPressed()) {



                        if (!(sum_d(cost_maint) == 0 && sum_d(cost_tools) == 0 && sum_d(cost_inputs) == 0 && cost_tot == 0)) {
                                b.sep();
                        }
                        if (sum_d(cost_maint) != 0) {// If sum of employment array isn't 0, maint/emp sums to 1 decimal or display 0
                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) (sum_d(know_emp)!=0 ? Math.ceil(sum_d(cost_maint)/sum_d(know_emp) * 10)  / 10 : 0), 1).color(GCOLOR.T().IBAD));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per knowledge worker"));
                                b.NL();
                        }
                        if (sum_d(cost_tools) != 0) {
                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(sum_d(cost_tools) * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per knowledge worker"));
                                b.NL();
                        }
                        if (sum_d(cost_inputs) != 0) {
                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(sum_d(cost_inputs) * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Input costs per knowledge worker (paper)"));
                                b.NL();
                        }
                        if (cost_tot != 0) {
                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average knowledge worker costs"));
                                b.NL();
                        }
                        if (!(sum_d(cost_maint) == 0 && sum_d(cost_tools) == 0 && sum_d(cost_inputs) == 0 && cost_tot == 0)) {
                                b.NL();
                        }
                }
                if (!(maint_output == 0 && spoil_output == 0 && furniture_output == 0)) {
                        if (!(KEYS.MAIN().UNDO.isPressed())) {
                                b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs)));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Profit per knowledge worker"));
                                b.NL();
                                b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs)));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Profit per knowledge worker"));
                                b.NL();
                        }
                }

        }
        public double tech_divisor_presentation(TECH tech, String what, double denari_costs, BoostSpec bb, GBox b) {
                double tech_benefit = next_tech_benefit(bb); // % of maintenance you'll still have, e.g. 20% reduction from current tech level
                b.sep();
                if (Objects.equals(what, "spoilage")){
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Note: Spoilage estimate excludes floor spoilage which gets no benefit from tech"));
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

                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(-denari_costs * tech_benefit/ tech.Tech_CostBenefit.costs/100)));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Estimated ".concat(what).concat(" benefits per worker")));
                        b.NL();

                        b.sep();
                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long)  ( ( -denari_costs * tech_benefit/100 + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs ) ) );
                        //  ( (-Amount of money saved)  + (costs per worker * # of tech workers) )/ # of tech workers
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost-Benefit per knowledge worker from this tech"));
                        b.NL();
                }
                return -denari_costs * tech_benefit/100;
        }
}
