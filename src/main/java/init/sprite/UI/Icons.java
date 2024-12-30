package init.sprite.UI;

import game.GAME;
import init.sprite.UI.Icon.IconSheet;
import init.tool.PathParser;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

import java.io.IOException;

public class Icons {
	
	public final M m = new M();
	public final S s = new S();
	public final L l = new L();
	private KeyMap<IconMaker> makers = new KeyMap<>();
	{
		makers.put("16", s);
		makers.put("24", m);
		makers.put("32", l);
	}
	
	Icons() throws IOException{

		
	}
	
	public Icon get(Json j) throws IOException{
		return get(j, "ICON");
	}
	
	private final TextureCoords tfg = new TextureCoords();
	private final TextureCoords tbg = new TextureCoords();
	
	public Icon get(Json json, String key) throws IOException {
		
		if (json.jsonIs(key)) {
			json = json.json(key);
			Icon bg = get(json.value("BG"), json, "BG");
			Icon fg = get(json.value("FG"), json, "FG");
			COLOR c = json.has("COLOR") ? new ColorImp(json, "COLOR"): COLOR.WHITE100;
			
			
			int offX = json.i("OFFX", -100, 100, 0);
			int offY = json.i("OFFY", -100, 100, 0);
			int shadow = json.i("SHADOW", -100, 100, 0);
			
			Icon biggest = bg.width() >= fg.width() ? bg : fg;
			Icon smallest = bg == biggest ? fg : bg;
			
			SPRITE s = new SPRITE.Imp(biggest.width()) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					double dx = (double)(X2-X1)/width();
					double dy = (double)(Y2-Y1)/height();
					
					int cx = X1 + (X2-X1)/2;
					int cy = Y1 + (Y2-Y1)/2;
					
					int ox = (int) (offX*dx);
					int oy = (int) (offY*dy);
					
					if (bg == biggest) {
						render(r, bg, dx, dy, cx, cy);
						renderShadow(dx, dy, X1, Y1);
						c.bind();
						render(r, fg, dx, dy, cx+ox, cy+oy);
					}
					else {
						render(r, bg, dx, dy, cx+ox, cy+oy);
						renderShadow(dx, dy, X1, Y1);
						c.bind();
						render(r, fg, dx, dy, cx, cy);
					}
					COLOR.unbind();
					
				}
				
				private void render(SPRITE_RENDERER r, Icon icon, double dx, double dy, int CX, int CY) {
					
					int w = (int) (icon.width()*dx);
					int h = (int) (icon.height()*dy);
					
					int x1 = CX - w/2;
					int y1 = CY - h/2;
					int x2 = x1 + w;
					int y2 = y1 + h;
					
					icon.render(r, x1, x2, y1, y2);
					
				}
				
				void renderShadow(double dx, double dy, int X1, int Y1) {
					if (shadow == 0)
						return;
					
					if (!(bg instanceof IconSheet))
						return;
					
					if (!(fg instanceof IconSheet))
						return;
					

					
					tbg.get(biggest.texture());
					tfg.get(smallest.texture());
										
					int sx = shadow + (tbg.width()-tfg.width())/2+offX;
					int sy = shadow + (tbg.height()-tfg.height())/2+offY;
					
					int tx1 = sx;
					int ty1 = sy;
					if (tx1 >= biggest.width() || ty1 >= biggest.height()) {
						return;
					}
					
					
					
					if (tx1 < 0) {
						tx1 = 0;
					}
					
					if (ty1 < 0) {
						ty1 = 0;
					}
					
					int wi = smallest.width();
					int hi = smallest.height();
					
					if (tx1 + wi > biggest.width()) {
						wi = bg.width()-tx1;
					}
					
					if (ty1 + hi > biggest.height()) {
						hi = bg.height()-ty1;
					}
					
					if (wi < 0 || hi < 0)
						return;
					
					
					tbg.x1 += tx1;
					tbg.y1 += ty1;
					tbg.x2 = (short) (tbg.x1 + wi);
					tbg.y2 = (short) (tbg.y1 + hi);
					
					
					tfg.x1 -= Math.max(-sx, 0);
					tfg.x2 = (short) (tfg.x1 +wi); 
					tfg.y1 += Math.max(-sy, 0);
					tfg.y2 = (short) (tfg.y1 +hi); 
					
					int x1 = X1 + (int) (tx1*dx);
					int x2 = X1 + (int) ((tx1+wi)*dx);
					
					int y1 = Y1 + (int) (ty1*dy);
					int y2 = Y1 + (int) ((ty1+wi)*dy);
					
					COLOR.WHITE30.bind();
					
					if (bg == biggest) {
						CORE.renderer().renderTextured(x1, x2, y1, y2, tbg, tfg);
					}else {
						CORE.renderer().renderTextured(x1, x2, y1, y2, tfg, tbg);
					}
					
					

					COLOR.unbind();
				}
			};
			
			return new Icon(s);
			
		}else {
			String relPath = json.value(key);
			return get(relPath, json, key);
		}
		
		
	}
	
	private Icon get(String relPath, Json j, String key) throws IOException{
		

		String[] ss = relPath.split(IconMaker.split);
		
		
		if (ss.length < 2) {
			GAME.Warn(j.errorGet("is badly formatted. Needs to contain a path with separation denoted by -> and the final entry being a number indicating which icon to pick of the sheet", key));
			return m.DUMMY;
		}
		if (!makers.containsKey(ss[0])) {
			String e = "icons must be either in 16, 24 or 32 folders.";
			PathParser.error(e, j, key);
			return m.DUMMY;
		}
		
		IconMaker m = makers.get(ss[0]);
		
		relPath = relPath.substring(relPath.indexOf(PathParser.split) + PathParser.split.length());
		
		return m.get(relPath, j, key);
		
	}

	
	public static class M extends IconMaker{
		
		private M() throws IOException{
			super("24", 24);
		}
		
		int i = 0;

		public final Icon clear_structure = get();
		public final Icon capitol = get();
		public final Icon furniture = get();
		{get();}
		public final Icon agriculture = get();
		public final Icon fertility = get();
		public final Icon cancel = get();
		public final Icon terrain = get();
		{get();}
		public final Icon crossair = get();
		{get();}
		public final Icon wall = get();
		public final Icon anti = get();
		{get();}
		
		public final Icon noble = get();
		{get();}
		public final Icon wildlife = get();
		{get();}
		{get();}
		{get();}
		{get();}
		{get();}
		public final Icon skull = get();
		public final Icon descrimination = get();
		public final Icon admin = get();
		{get();}
		public final Icon ok = get();
		public final Icon questionmark = get();
		public final Icon arrow_up = get();
		public final Icon arrow_right = get();
		public final Icon arrow_down = get();
		public final Icon arrow_left = get();
		{get();}
		public final Icon expand = get();
		public final Icon shrink = get();
		public final Icon citizen = get();
		public final Icon rebellion = get();
		{get();}
		public final Icon urn = get();
		{get();}
		{get();};
		public final Icon stength = get();
		public final Icon plus = get();
		public final Icon minus = get();
		public final Icon rotate = get();
		public final Icon exit = get();
		public final Icon repair = get();
		public final Icon time = get();
		public final Icon menu = get();
		public final Icon wheel = get();
		public final Icon city = get();
		{get();}
		public final Icon flag = get();
		public final Icon cog = get();
		public final Icon openscroll = get();
		public final Icon raw_materials = get();
		{get();}
		{get();}
		{get();}
		public final Icon building = get();
		public final Icon pickaxe = get();
		public final Icon place_fill = get();
		public final Icon shield = get();
		public final Icon horn = get();
		public final Icon clear_food = get();
		public final Icon for_loose = get();
		public final Icon for_tight = get();
		public final Icon fast_forw = get();
		public final Icon for_muster = get();
		public final Icon circle_frame = get();
		public final Icon circle_inner = get();
		{get();}
		public final Icon cog_big = get();
		public final Icon place_brush = get();
		public final Icon place_rec = get();
		public final Icon place_line = get();
		public final Icon place_ellispse = get();
		public final Icon place_rec_hollow = get();
		public final Icon trash = get();
		public final Icon menu2 = get();
		public final Icon law = get();
		public final Icon overwrite = get();
		public final Icon workshop = get();
		public final Icon slave = get();
		{get();}
		public final Icon water = get();
		public final Icon sword = get();
		{get();}
		{get();}
		public final Icon heart = get();
		public final Icon lock = get();
		public final Icon search = get();
		public final Icon bow = get();
		public final Icon fortification = get();
		public final Icon disease = get();
		public final Icon ceiling = get();
		public final Icon wallceiling = get();
		public final Icon chainsFree = get();
		public final Icon coins = get();
		public final Icon factions = get();
		{get();}
		public final Icon place_ellispse_hollow = get();
		public final Icon place_hex = get();
		public final Icon place_hex_hollow = get();
		public final Icon wall_opening = get();
		public final Icon gov = get();
		
		private Icon get() throws IOException {
			int k = i;
			i++;
			return get("_Icons", k);
		}


		
	}
	
	public static class S extends IconMaker{
		
		private static ArrayListGrower<IconS> all = new ArrayListGrower<>();
		
		{
			all.clear();
		}
		
		private S() throws IOException{
			super("16", 16);
		}
		
		int i = 0;
		
		
		public final IconS magnifier = get();
		public final IconS minifier = get();
		public final IconS minimap = get();
		public final IconS arrowUp = get();
		
		public final IconS arrowDown = get();
		public final IconS cancel = get();
		public final IconS camera = get();
		public final IconS crazy = get();
		
		public final IconS menu = get();
		public final IconS cog = get();
		public final IconS question = get();
		public final IconS storage = get();
		
		public final IconS magnifierBig = get();
		public final IconS minifierBig = get();
		public final IconS human = get();
		public final IconS hammer = get();
		
		public final IconS column = get();
		public final IconS vial = get();
		public final IconS gift = get();
		public final IconS plate = get();
		
		public final IconS sword = get();
		public final IconS money = get();
		public final IconS crossheir = get();
		public final IconS standard = get();
		
		public final IconS temperature = get();
		public final IconS eye = get();
		public final IconS law = get();
		public final IconS pickaxe = get();
		
		public final IconS shield = get();
		public final IconS capitol = get();
		public final IconS sprout = get();
		public final IconS trade = get();
		
		public final IconS bow = get();
		public final IconS fish = get();
		public final IconS heart = get();
		public final IconS citizen = get();
		
		public final IconS slave = get();
		public final IconS noble = get();
		public final IconS world = get();
		public final IconS admin = get();
		
		public final IconS muster = get();
		public final IconS time = get();
		public final IconS ice = get();
		public final IconS heat = get();
		
		public final IconS pluses = get();
		public final IconS squatter = get();
		public final IconS fly = get();
		public final IconS honor = get();
		
		public final IconS bed = get();
		public final IconS alert = get();
		public final IconS arrow_right = get();
		public final IconS arrow_left = get();
		
		public final IconS plus = get();
		public final IconS minus = get();
		public final IconS allRight = get();
		public final IconS circle = get();
		
		public final IconS clock = get();
		public final IconS death = get();
		public final IconS dot = get();
		public final IconS house = get();
		
		public final IconS degrade = get();
		public final IconS fist = get();
		public final IconS armour = get();
		public final IconS handOpen = get();
		public final IconS speed = get();
		
		public final IconS boom = get();
		public final IconS drop = get();
		public final IconS star = get();
		public final IconS ship = get();

		public final IconS[] chevrons = new IconS[]{
			get(),
			get(),
			get(),
			get()
		};

		public final IconS happy = get();
		public final IconS soso = get();
		public final IconS angry = get();
		public final SPRITE[] faces = new SPRITE[]{
				angry.createColored(new ColorImp(165, 30, 30)),
				soso.createColored(new ColorImp(165, 165, 10)),
				happy.createColored(new ColorImp(30, 165, 30)),
		};
		
		public final IconS crown = get();
		public final IconS flags = get();
		public final IconS expand = get();
		public final IconS wheel = get();
		public final IconS flag = get();
		public final IconS cameraBig = get();
		public final IconS tolerence = get();
		public final IconS headspike = get();
		public final IconS jug = get();
		public final IconS bars = get();
		public final IconS shrine = get();
		public final IconS temple = get();
		public final IconS book = get();
		public final IconS plus2 = get();

		public final IconS custom1 = get();
		public final IconS custom2 = get();
		public final IconS custom3 = get();
		public final IconS custom4 = get();

		public IconS chevron(DIR d) {
			return chevrons[d.orthoID()];
		}
		
		private IconS get() throws IOException {
			int k = i;
			i++;
			
			return new IconS(super.get("_Icons", k));
		}

		private final static COLOR mask = new ColorImp(142, 134, 107);
		
		public static class IconS extends Icon {

			public final int index;
			
			IconS(Icon i) {
				super(Icon.S, i);
				index = all.add(this);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR c = CORE.renderer().colorGet();
				if (c.red() == 127 && c.green() == 127 && c.blue() == 127) {
					mask.bind();
					super.render(r, X1, X2, Y1, Y2);
					COLOR.unbind();
				}else {
					
					
					super.render(r, X1, X2, Y1, Y2);
				}
				
			}
	
			
		}
		
		public IconS get(int index) {
			if (index > all.size())
				return cancel;
			return all.get(index);
		}
		
	}
	
	public static class L extends IconMaker{
		
		private L() throws IOException{
			super("32", 32);
		}
		
		private int i = 0;
		public final Icon agri = get();
		public final Icon work = get();
		public final Icon service = get();
		public final Icon jobs = get();
		public final Icon gov = get();
		public final Icon mysteryman = get();
		{get();get();};
//		public final Icon pLeft = get();
//		public final Icon pRight = get();
		public final Icon rebel = get();
		public final Icon menu = get();
		public final Icon world = get();
		public final Icon battle = get();
		public final Icon city = get();
		public final Icon coin = get();
		public final Icon flags = get();
		public final Icon vial = get();
		public final Icon tourist = get();
		public final Icon book = get();
		public final Icon up = get();
		public final Icon infra = get();
		public final Icon crate = get();
		
		{
			i = 0;
		}
		public final Icon bannerPole = b();
		public final Icon[] banners = new Icon[] {
			b(),b(),b(),b(),
			b(),b(),b(),b()
		};
		{
			i = 0;
		}
		public final Icon clear_all = get2();
		public final Icon copy = get2();
		public final Icon copyRoom = get2();
		public final Icon repair = get2();
		public final Icon suspend = get2();
		public final Icon dia = get2();
		public final Icon square = get2();
		public final Icon prints = get2();
		public final Icon upgrade = get2();
		public final Icon mine = get2();
		public final Icon pasture = get2();
		public final Icon farm = get2();
		
		public final Icon fish = get2();
		public final Icon refiner = get2();
		public final Icon workshop = get2();
		public final Icon law = get2();
		public final Icon trainig = get2();
		public final Icon admin = get2();
		public final Icon breeding = get2();
		public final Icon decor = get2();
		public final Icon logistics = get2();
		public final Icon water = get2();
		public final Icon religion = get2();
		public final Icon dist = get2();
		public final Icon health = get2();
		public final Icon entertain = get2();
		public final Icon death = get2();
		public final Icon home = get2();
		public final Icon demolish = get2();
		public final Icon new1 = get2();
		public final Icon new2 = get2();
		public final Icon new3 = get2();

		public final Icon plus = get2();
		public final Icon minus = get2();
		public final Icon maint = get2();

		private Icon get() throws IOException {
			int k = i;
			i++;
			return get("_UI", k);
		}
		
		private Icon get2() throws IOException {
			int k = i;
			i++;
			return get("_ICONS", k);
		}
		
		private Icon b() throws IOException {
			int k = i;
			i++;
			return get("_BANNER", k);
		}
		
	}
	
}
