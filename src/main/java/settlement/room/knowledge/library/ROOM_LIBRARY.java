package settlement.room.knowledge.library;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BoosterImp;
import game.faction.Faction;
import init.text.D;
import settlement.path.finders.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.infra.admin.AdminData;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_LIBRARY extends RoomBlueprintIns<LibraryInstance> implements INDUSTRY_HASER {

        public final static String type = "LIBRARY";
        final AdminData data;
        final Job job;
        final Industry industry;
        final Constructor constructor;

        final LIST<Industry> indus;

        public ROOM_LIBRARY(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
                super(index, init, key, block);
                job = new Job(this);

                constructor = new Constructor(this, init);
                pushBo(init.data(), type, true);
                data = new AdminData(employmentExtra(), init.data(), bonus());

                industry = new Industry(this, init.data(), new RoomBoost[]{
                        constructor.efficiency
                }, bonus());

                indus = new ArrayList<>(industry);

                new BoosterImp(new BSourceInfo(info.names, iconBig().small), 1, 100, true) {

                        @Override
                        public double get(BOOSTABLE_O o) {
                                return 1 + o.boostableValue(this);
                        }

                        @Override
                        public double vGet(Faction f) {
                                return data.value();
                        }
                }.add(BOOSTABLES.CIVICS().KNOWLEDGE);
        }

        @Override
        protected void update(float ds) {
                data.update();
        }

        @Override
        public SFinderRoomService service(int tx, int ty) {
                return null;
        }

        @Override
        protected void saveP(FilePutter saveFile) {
                data.save(saveFile);
                industry.save(saveFile);
        }

        @Override
        protected void loadP(FileGetter saveFile) throws IOException {
                data.load(saveFile);
                industry.load(saveFile);

        }

        @Override
        protected void clearP() {
                this.data.clear();
                industry.clear();
        }

        @Override
        public Furnisher constructor() {
                return constructor;
        }

        private static CharSequence ¤¤TargetD = "¤Estimation of how much knowledge will be boosted by.";
        private static CharSequence ¤¤name = "¤Knowledge Boost";

        static {
                D.ts(ROOM_LIBRARY.class);
        }


        @Override
        public void appendView(LISTE<UIRoomModule> mm) {
                mm.add(new AdminData.Gui<LibraryInstance, ROOM_LIBRARY>(this, data, industry, ¤¤name, ¤¤TargetD).make());
        }

        @Override
        public LIST<Industry> industries() {
                return indus;
        }

        public void knowledgeAdd(int i) {
                data.inc(i);

        }

        public double boostPerStation() {
                return data.knowledgePerStation;
        }

        // Add the additional data information to be visible outside of the class:
        public double knowledge() {
                return data.value();
        }
}
