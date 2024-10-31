package view.ui.goods;

import game.faction.FACTIONS;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;
import init.resources.RESOURCE;
import init.resources.RESOURCES;

public final class UIMaintenance extends IFullView {

        private static CharSequence ¤¤Name = "Maintenance";
        public static double import_costs = 0;
        public UIMaintenance() {
            super(¤¤Name, SPRITES.icons().l.workshop);
        }

        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);

                        // Display the rows using the list of resources
                        ArrayListGrower<MaintRow> rows = new ArrayListGrower<>();
                        double value_costs = 0;
                        // Sum up the total first
                        for (RESOURCE res : RESOURCES.ALL()) {
                                import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
                                value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
                        }

                // Display top line messages
                section.addDown(0, new GText(UI.FONT().H2, "Overall Maintenance costs"));
                GText tableHeader = new GText(UI.FONT().S, "Resource per day         Costs if imported per day   Average value per day");
                section.addDown(10, tableHeader);

                // Create each row
                RESOURCE last_res = null;
                for (RESOURCE res : RESOURCES.ALL()) {
                        if (SETT.MAINTENANCE().estimateGlobal(res) != 0) {
                                rows.add(new ResourceRow(res, tableHeader.width(), 0, 0));
                        }
                last_res=res;
                }
                rows.add(new ResourceRow(last_res, tableHeader.width() , import_costs, value_costs));


                // Display the rows!
                GScrollRows scrollRows = new GScrollRows(rows, HEIGHT);
                section.addDown(5, scrollRows.view());
        }

//        private static int getHeightForElementToFill(GuiSection section, GuiSection finalElementToBeDisplayed, ArrayListGrower<MaintRow> rows){
//            int bottomPixelOfElementAboveUs = section.getLastY2();
//            int remainingScreenSpace = HEIGHT - bottomPixelOfElementAboveUs;
//            int pixelsWeCanNotUse = finalElementToBeDisplayed.body().height();
//
//            int maximumHeightElementInTheMiddleCouldTakeUp = remainingScreenSpace - pixelsWeCanNotUse;
//
//            // We assume all elements have the same height.
//            int spaceWeActuallyNeed = rows.get(0).body().height() * rows.size();
//
//            return Math.min(maximumHeightElementInTheMiddleCouldTakeUp, spaceWeActuallyNeed);
//        }

        private static class ResourceRow extends MaintRow {
            // Create the row using the resource:
                ResourceRow(RESOURCE res, int width, double import_costs, double value_costs) {
                        if (import_costs ==0 &  value_costs ==0 ){
                                double amount_of_res = SETT.MAINTENANCE().estimateGlobal(res);
                                body().setWidth(width).setHeight(1);
                                // Display resource.icon()
                                add(GFORMAT.f(new GText(UI.FONT().S, 0), amount_of_res).adjustWidth(), incTab(2), MARGIN);
                                // Amount of resource used:
                                add(res.icon(), incTab(3), 0);
                                // Import costs for that resource:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (amount_of_res * FACTIONS.player().trade.pricesBuy.get(res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);
                                // Value of those resources:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (amount_of_res * FACTIONS.PRICE().get(res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);
                        }
                        else{
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total Costs:").adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) -import_costs).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) -value_costs).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);
                        }
                }
        }

        private abstract static class MaintRow extends GuiSection {
                protected static final int MARGIN = 4;
                private double tab;

                protected int incTab(double n) {
                        double t = tab;
                        tab += n;
                        return (int) (t * MARGIN * 10);
                }
        }
}
