package view.ui.goods;

import game.faction.FACTIONS;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.NEEDS;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.RoomProduction;
import settlement.stats.STATS;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static settlement.main.SETT.ROOMS;

public final class UIProduction extends IFullView {

        static double total_export = 0;
        static double total_value = 0;
        private static CharSequence ¤¤Name = "Production";
        public UIProduction() {
                super(¤¤Name, SPRITES.icons().l.plus);
        }


        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);


                // Display top line messages
                section.addDown(0, new GText(UI.FONT().H2, "Producers"));
                ArrayListGrower<RegRow> rows = new ArrayListGrower<>();
                GText tableHeader = new GText(UI.FONT().S, "                                                                              ");
                section.addDown(10, tableHeader);

                // Order by source
                HashMap<CharSequence, ArrayListGrower<RoomProduction.Source>> data = new HashMap<>();
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source ii : SETT.ROOMS().PROD.producers(res)) {
                                if (!data.containsKey(ii.name())) {
                                        data.put(ii.name(), new ArrayListGrower<>());
                                }
                                data.get(ii.name()).add(ii);
                        }
                }
                // Display by source
                for (Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>> item :       data.entrySet()) {
                        CharSequence category = item.getKey();

                        //Check if any value >0
                        boolean check = false;
                        for (RoomProduction.Source ii : item.getValue()) {
                                if (ii.am() != 0) {
                                        check = true;
                                }
                        }
                        if (!check){continue;}
                        // Reset the totals for the next category
                        total_export = 0;
                        total_value = 0;

                        // Display rows of resources and amounts
                        rows.add(new AddRow(null, tableHeader.width(), (String) category )); // Source title line
                        rows.add(new AddRow(null, tableHeader.width(), "columns" )); // column description line
                        for (RoomProduction.Source ii : item.getValue()) {
                                if (ii.am() != 0) {
                                        rows.add(new AddRow(ii, tableHeader.width(), ""));
                                }
                        }
                        rows.add(new AddRow(null, tableHeader.width(), "total" )); // total line
                        rows.add(new AddRow(null, tableHeader.width(), "space" )); // space line(s)
                        rows.add(new AddRow(null, tableHeader.width(), "space" )); // space line(s)
                }


                // Display the rows!
                GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-20);
                section.addDown(5, scrollRows.view());
        }

        private static class AddRow extends RegRow {
                // Create the row using the resource:
                AddRow(RoomProduction.Source ii, int width, String spec) {

                        if ( ii != null ) {
                                body().setWidth(width).setHeight(1);

                                // Display resource.icon()
                                add(GFORMAT.f(new GText(UI.FONT().S, 0), ii.am()).adjustWidth(), incTab(2), MARGIN);

                                // Amount of resource used:
                                add(ii.res.icon(), incTab(3), 0);

                                // Export values for that resource:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                                // Value of those resources:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (ii.am() * FACTIONS.PRICE().get(ii.res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                                total_export += (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                                total_value += (ii.am() * FACTIONS.PRICE().get(ii.res));
                        }else if ( ii == null &&
                                !Objects.equals(spec, "columns") &&
                                !Objects.equals(spec, "total") &&
                                !Objects.equals(spec, "space") ){ // Then display  what "spec" is
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), spec ).adjustWidth(), incTab(4), MARGIN);

                        }else if ( ii == null && Objects.equals(spec, "columns")){ // New Columnn titles
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Resource per day         Value if exported per day   Average value per day").adjustWidth(), incTab(4), MARGIN);

                        }else if( ii == null && Objects.equals(spec, "total")){ // Total values
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total Values:").adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) +total_export).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) +total_value).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "denari").adjustWidth(), incTab(4), MARGIN);

                        }else if( ii == null && Objects.equals(spec, "space")){ // blank line!
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), " ").adjustWidth(), incTab(5), MARGIN);
                        }
                }
        }

        private abstract static class RegRow extends GuiSection {
                protected static final int MARGIN = 4;
                private double tab;

                protected int incTab(double n) {
                        double t = tab;
                        tab += n;
                        return (int) (t * MARGIN * 10);
                }
        }
}
