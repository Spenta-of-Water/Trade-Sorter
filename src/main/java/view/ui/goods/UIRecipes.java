package view.ui.goods;

import game.boosting.Boostable;
import game.faction.FACTIONS;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprint;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;

import static settlement.main.SETT.ROOMS;

public final class UIRecipes extends IFullView {

    private static final int COLS = 1;

    public final Icon icon = SPRITES.icons().s.storage;
    private static CharSequence ¤¤Name = "Recipes";

    static {
        D.ts(UIRecipes.class);
    }

    public UIRecipes() {
        super(¤¤Name, UI.icons().l.crate);
    }

    @Override
    public void init() {
        section.clear();
        section.body().moveY1(IFullView.TOP_HEIGHT);
        section.body().moveX1(16);

        section.body().setWidth(WIDTH).setHeight(1);

        ArrayListGrower<Industry> all = new ArrayListGrower<>();

        for (RoomBlueprint h : ROOMS().all()) {
            if (h instanceof INDUSTRY_HASER) {
                INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
                for (Industry ins : ii.industries()) {
                    all.add(ins);
                }
            }
        }

        ArrayListGrower<RRow> rows = new ArrayListGrower<>();
        for (Industry ind : all) {
            if (ind.outs().isEmpty())
                continue;
            RRow row = new RRow(ind);
            rows.add(row);
        }

        GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-15);
        section.addDown(0, new GText(UI.FONT().H2, "Profit per employee with no labor bonuses, buying inputs and selling outputs"));
        section.addDown(0, scrollRows.view());
    }



    private class RRow extends GuiSection {
        private final int MARGIN = 4;
        private final Industry ind;
        private double tab;

        RRow(Industry i) {
            super();
            ind = i;
            double goods_sell = 0;
            double goods_buy = 0;
            double goods_va = 0;
            double goods_sub = 0;
            Boostable t = ind.bonus();


            for (Industry.IndustryResource oo : ind.outs()) {
                goods_sell += oo.rate * FACTIONS.player().trade.pricesSell.get(oo.resource);
                goods_sub += oo.rate * FACTIONS.player().trade.pricesBuy.get(oo.resource);
            }

            for (Industry.IndustryResource oo : ind.ins()) {
                goods_buy += oo.rate * FACTIONS.player().trade.pricesBuy.get(oo.resource);
                goods_va += oo.rate * FACTIONS.player().trade.pricesSell.get(oo.resource);
            }

            body().setWidth(WIDTH).setHeight(1);
            add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_buy), incTab(2), MARGIN);
            //add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_va), incTab(2), MARGIN);
            //add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sub), incTab(2), MARGIN);

            add(ind.blue.icon, incTab(1), 0);
            //addRight(margin, (GText) GFORMAT.text(new GText(UI.FONT().S, 0), ind.blue.key).adjustWidth());

            for (Industry.IndustryResource oo : ind.outs()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesSell.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }

            for (Industry.IndustryResource oo : ind.ins()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), -oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesBuy.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }
        }

        private int incTab(double n) {
            double t = tab;
            tab += n;
            return (int) (t * MARGIN * 10);
        }

    }
}
