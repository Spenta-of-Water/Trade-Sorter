package view.ui.tech;

import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.tech.TECH;
import init.tech.TechCost;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.ui.goods.UIMaintenance;

import static init.tech.Knowledge_Costs.*;

public class Node_Extra{
        public double worker_cost ;
        public void  output(TECH tech, GBox b){
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      For All Techs           ///////////////////////////////////////////////
                /////////      Always displayed        ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                // Calculate the total costs and benefits for simplification
                int j = 0;
                worker_cost   = 0;
                for (TechCost c : tech.costs) {
                        if(know_worker[j] != 0)  { worker_cost += c.amount / know_worker[j]; }
                        // For each tech currency: Tech cost / knowledge per worker
                        j += 1;
                }

                // Calculate the benefit of a technology based on the "benefit"
                int i = 0;
                if (tech.Tech_CostBenefit.benefits != 0 ) {
                        b.NL();
                        b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefits / worker_cost , 3));
                        b.tab(1);
                        b.add(GFORMAT.text(b.text(), "Below '1' means you are spending more labor in tech buildings than you'd gain from having the tech."));
                }
                i += 1;

                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////

                if (KEYS.MAIN().UNDO.isPressed()) {
                        b.NL();
                        b.add(GFORMAT.f(b.text(), worker_cost, 1));
                        b.tab(1);
                        b.add(GFORMAT.text(b.text(), "Tech worker cost"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), UIMaintenance.sum_d(cost_total), 1));
                        b.tab(1);
                        b.add(GFORMAT.text(b.text(), "Tech upkeep cost"));
                }
                output1(tech, b); // Production Bonus Tech text


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
                b.tab(1);
                b.add(GFORMAT.text(b.text(), "Production worker Benefit"));

                b.NL();
                b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefit_tot, 1));
                b.tab(1);
                b.add(GFORMAT.text(b.text(), "Cost of production upkeep per worker"));

                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                if (KEYS.MAIN().UNDO.isPressed()) {
                        int i = 0;
                        for (PTech.TechCurr tech_value : FACTIONS.player().tech().currs()) {
                                b.NL();
                                b.NL();
                                b.add(GFORMAT.text(b.text(), tech_value.cu.bo.name));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_tot[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Knowledge total"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_emp[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Employment"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_worker[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Knowledge per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_total[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Total Costs per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_inputs[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Total input Costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_maint[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Total Maintenance costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_tools[i], 1));
                                b.tab(1);
                                b.add(GFORMAT.text(b.text(), "Total Tools costs"));



                                i += 1;
                        }

                }

        }
}
