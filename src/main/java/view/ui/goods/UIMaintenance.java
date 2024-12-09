package view.ui.goods;

import game.faction.FACTIONS;
import game.time.TIME;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;
import init.resources.RESOURCE;
import init.resources.RESOURCES;

import static settlement.main.SETT.ROOMS;

public final class UIMaintenance extends IFullView {

        private static CharSequence ¤¤Name = "Maintenance";
        public static double import_costs = 0;
        public UIMaintenance() {
            super(¤¤Name, SPRITES.icons().l.workshop);
        }
        static double room_type_maint_import_total = 0;
        static double room_type_maint_value_total = 0;

        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);

                        // Display the rows using the list of resources
                        ArrayListGrower<MaintRow> rows = new ArrayListGrower<>();
                        import_costs = 0;
                        double value_costs = 0;

                        // Sum up the total first
                        for (RESOURCE res : RESOURCES.ALL()) {
                                if (SETT.MAINTENANCE().estimateGlobal(res) != 0) {
                                        import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
                                        value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
                                }
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
                GScrollRows scrollRows = new GScrollRows(rows, HEIGHT/3);
                section.addDown(5, scrollRows.view());


                room_type_maint_import_total = 0;
                room_type_maint_value_total = 0;
                ArrayListGrower<MaintRow> BLDGrows = new ArrayListGrower<>();
                section.addDown(0, new GText(UI.FONT().H2, "Building type Maintenance costs"));
                tableHeader = new GText(UI.FONT().S, "Building type         Costs if imported per day   Average value per day");
                section.addDown(10, tableHeader);
                RoomBlueprint lasth = null;
                for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint
                        if (!(h instanceof RoomBlueprintIns)) {
                                continue;
                        } // Industries only
                        BLDGrows.add(new RoomRow(tableHeader.width(),h,false));
                        lasth = h;
                }
                BLDGrows.add(new RoomRow(tableHeader.width(),lasth,true));
                scrollRows = new GScrollRows(BLDGrows, HEIGHT/3);
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

        private static class RoomRow extends MaintRow {
                // Create the row using the resource:
                RoomRow(int width,RoomBlueprint h, boolean last) {

                        if(!last){
                                double room_type_maint_import = 0;
                                double room_type_maint_value = 0;
                                for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry

                                        try {


                                                ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY()); // rateResource requirement
                                                double iso = r.isolation(r.mX(), r.mY());        // rateResource requirement
                                                double boost = SETT.MAINTENANCE().speed();        // rateResource requirement

                                                for (int i = 0; i < deg.resSize(); i++) {                // For each resource
                                                        if (r.resAmount(i, r.upgrade()) <= 0) {
                                                                continue;
                                                        }        // skip if resource use is 0

                                                        double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i, r.upgrade())) * TIME.years().bitConversion(TIME.days()) / 16.0;


                                                        double sellFor = FACTIONS.player().trade.pricesBuy.get(deg.res(i));        // get the import cost of the resource
                                                        room_type_maint_import -= n * sellFor;

                                                        double valueFor = FACTIONS.PRICE().get(deg.res(i)); // "Value" cost of resource
                                                        room_type_maint_value -= n * valueFor;
                                                }
                                        } catch (Exception e) {
                                                continue;
                                        }
                                }
                                if (room_type_maint_import == 0 || room_type_maint_value == 0) {
                                        return;
                                }
                                body().setWidth(width).setHeight(1);

                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ((RoomBlueprintIns<?>) h).info.name).adjustWidth(), incTab(6), MARGIN);

                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) room_type_maint_import).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) room_type_maint_value).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                                room_type_maint_import_total += room_type_maint_import;
                                room_type_maint_value_total += room_type_maint_value;
                        }
                        else{
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Sum of rooms listed").adjustWidth(), incTab(6), MARGIN);

                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) room_type_maint_import_total).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) room_type_maint_value_total).adjustWidth(), incTab(2), MARGIN);
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
