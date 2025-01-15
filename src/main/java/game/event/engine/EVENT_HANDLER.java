package game.event.engine;

import game.GAME;
import game.GAME.GameResource;
import game.Profiler;
import game.event.actions.EventAction;
import game.faction.FACTIONS;
import game.time.TIME;
import game.values.Lockable;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.Errors;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.KeyMap;

import java.io.IOException;
import java.util.Arrays;
////////////////////////////////////////////////
// TEMPORARY ADDITION: only fixing a bug handle until it is updated
///////////////////////////////////////////////
public final class EVENT_HANDLER extends GameResource{

	private Event current = null;
	private Event previous = null;
	private EContext data = new EContext();
	
	private final Data[] datas;
	
	private EventMessage mess;
	
	final KeyMap<Boolean> tags = new KeyMap<Boolean>();
	
	public EVENT_HANDLER() throws IOException {
		super("ENGINE");
		datas = new Data[Event.all.size()];
		for (int i = 0; i < datas.length; i++)
			datas[i] = new Data(Event.all.get(i));
		new UIEventDebug(this);
		
	}
	
	@Override
	protected void save(FilePutter file) {
		if (current != null) {
			file.bool(true);
			file.i(current.allIndex);
			file.chars(current.key);
		}else {
			file.bool(false);
		}
		if (previous != null) {
			
			file.bool(true);
			file.i(previous.allIndex);
			file.chars(previous.key);
		}else {
			file.bool(false);
		}
		
		data.write(file);
		
		file.i(datas.length);
		for (Event k : Event.all) {
			file.i(k.allIndex);
			file.chars(k.key);
			file.i(datas[k.allIndex].fired);
			file.d(datas[k.allIndex].acc);
			file.d(datas[k.allIndex].lastTime);
			file.bsE(datas[k.allIndex].choices);
		}
		
		file.i(tags.all().size());
		for (String s : tags.keys()) {
			file.chars(s);
			file.bool(tags.get(s));
		}
		
		if (mess != null) {
			file.bool(true);
			file.object(mess);
		}else
			file.bool(false);
			
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		clear();
		current = null;
		if (file.bool()) {
			int i = file.i();
			String key = file.chars();
			current = read(i, key); 
		}
		previous = null;
		if (file.bool()) {
			int i = file.i();
			String key = file.chars();
			previous = read(i, key); 
		}
		data = EContext.read(file);
		if (data == null) {
			this.data = new EContext();
		}
		
		int max = file.i();
		
		for (int i = 0; i < datas.length; i++) {
			datas[i] = new Data(Event.all.get(i));
		}
		
		for (int k = 0; k < max; k++) {
			int i = file.i();
			String key = file.chars();
			if (i >= 0 && i < Event.all.size() && Event.all.get(i).key.equals(key)) {
				datas[i].fired = file.i();
				datas[i].acc = file.d();
				datas[i].lastTime = file.d();
				file.bsE(datas[i].choices);
			}else {
				file.i();
				file.d();
				file.d();
				file.bsE(new byte[1]);
			}
		}
		
		tags.clear();
		int am = file.i();
		for (int i = 0; i < am; i++) {
			String k = file.chars();
			if (file.bool())
				tags.put(k, Boolean.TRUE);
		}
		
		if (file.bool())
			mess = (EventMessage) file.object();
		else
			mess = null;
		
	}

	private void clear() {
		current = null;
		previous = null;
		for (Data d : datas) {
			d.acc = 0;
			d.fired = 0;
			d.lastTime = 0;
			d.upI = -1;
			Arrays.fill(d.choices, (byte)0);
		}
	}
	
	@Override
	protected void update(float ds, Profiler prof) {

		if (current == null) {return;}
		if (current.aborters == null) {return;}

		double s = timeElapsed();
			for (EventAction a : current.on_spawn.actions) {
				a.update(current, data, ds, s);
			}
			
			for (ECondition e : current.aborters) {
				if (e.request.passes(FACTIONS.player())) {
					setActions(e.on_fulfill);
					return;
				}
			}
			
			
			if (current.selection.hasFailed(data)) {
				setActions(current.selection.onFail);
			}else if (s >= current.duration.seconds) {
				if (current.condition != null && current.condition.request.passes(FACTIONS.player())) {
					setActions(current.condition.on_fulfill);
				}else {
					setActions(current.duration.on_expire);
				}
			}

	}
	
	private void setActions(EActions actions) {
		Event e = current;
		for (EventAction a : actions.actions)
			a.exe(current, data);
		
		if (e == current)
			current = null;
	}
	
	public double timeElapsed() {
		if (current != null)
			return TIME.currentSecond() - datas[current.allIndex].lastTime;
		return 0;
	}
	
	public void expire() {
		if (current != null)
			datas[current.allIndex].lastTime = TIME.currentSecond()-current.duration.seconds-1;
	}
		
	public boolean can(Event a) {
		if (!a.occurence.plockable.passes(FACTIONS.player()))
			return false;
		int ff = datas[a.allIndex].fired;
		if (ff >= a.occurence.maxSpawns)
			return false;
		
		if (!a.tags.can(tags))
			return false;
		
		return true;
	}
	
	
	public Event read(int index, String key) {
		if (index >= 0 && index < Event.all.size() && Event.all.get(index).key.equals(key))
			return Event.all.get(index);
		return null;
	}
	
	public double acc(Event a) {
		return datas[a.allIndex].acc;
	}
	
	public void accInc(Event a) {
		if (can(a))
			datas[a.allIndex].acc += a.occurence.occurence();
	}
	
	public int occ(Event a) {
		return datas[a.allIndex].fired;
	}
	
	public boolean trySet(Event e) {

		if (can(e)) {
			data.init(e);
			if (!e.selection.hasFailed(data)) {
				set(e, false, false, true, true);
				return true;
			}
		}
		datas[e.allIndex].acc = 0;
		return false;
		
	}
	
	public void set(Event e, boolean keepInfo, boolean keepTime, boolean clearContext, boolean message) {

		if (current != null) {
			for (String s : current.tags.removes)
				tags.putReplace(s, Boolean.FALSE);
		}
		
		if (e == null) {
			current = null;
			return;
		}
		
		if (clearContext || e == null)
			data.init(e);
		
		
		if (!keepTime)
			datas[e.allIndex].lastTime = TIME.currentSecond();
		else
			datas[e.allIndex].lastTime = datas[current.allIndex].lastTime;
		if (!keepInfo) {
			previous = current;
		}
		
		
		current = e;
		for (String s : current.tags.adds)
			tags.putReplace(s, Boolean.TRUE);
		
		datas[e.allIndex].fired ++;
		
		datas[e.allIndex].acc = 0;
		if (datas[e.allIndex].upI == GAME.updateI())
			throw new Errors.DataError("An event is creating an infinate loop! " + e.key);
		
		datas[e.allIndex].upI = GAME.updateI();
		
		if (message && current.info.messages.length > 0) {
			this.mess = new EventMessage(e, data);
			this.mess.send();
		}else {
			this.mess = null;
		}
		
		for (EventAction a : e.on_spawn.actions)
			a.exe(current, data);

		
		
		
		
		
	}
	
	public void setTmp(Event e) {
		
		EContext data = new EContext();
		data.init(e);
		
		datas[e.allIndex].lastTime = TIME.currentSecond();
		datas[e.allIndex].fired ++;
		
		datas[e.allIndex].acc = 0;
		if (datas[e.allIndex].upI == GAME.updateI())
			throw new Errors.DataError("An event is creating an infinate loop! " + e.key);
		
		datas[e.allIndex].upI = GAME.updateI();
		
		if (e.info.messages.length > 0) {
			new EventMessage(e, data).send();
		}
		
		for (EventAction a : e.on_spawn.actions)
			a.exe(current, data);
	}
	
	public Event current() {
		return current;
	}
	
	public EventMessage mess() {
		return mess;
	}

	EContext context() {
		return data;
	}
	
	private class Data {
		double acc;
		double lastTime;
		int fired;
		int upI = -1;
		final byte[] choices;
		
		Data(Event e){
			choices = new byte[e.choices.size()];
		}
	}
	
	public CLICKABLE butt() {
		return new Butt(this);
		
	}
	
	public COLOR color(Induvidual in) {
		if (current != null) {
			if (STATS.EVENT().has(in))
				return data.colorIndu;
			return data.colorinduAll;
		}
		return null;
		
	}
	
	public CharSequence message(Induvidual in) {
		if (current != null && current.info.subject.length() > 0) {
			if (STATS.EVENT().has(in))
				return current.info.subject;
		}
		return null;
	}
	
	public boolean shouldSet(Induvidual i) {
		if (current != null && current.selection.indu.filters.size() > 0 && data.indu.am < data.indu.max) {
			for (Lockable<Induvidual> l : current.selection.indu.filters)
				if (l.passes(i)) {
					data.indu.am++;
					return true;
				}
		}
		return false;
	}

	public boolean choiceHasBeenSelected(Event parent, int choice) {
		Data d = datas[parent.allIndex];
		if (choice >= 0 && choice < d.choices.length)
			return d.choices[choice] > 0;
			return false;
	}
	
	void choiceSelect(Event parent, int choice) {
		Data d = datas[parent.allIndex];
		d.choices[choice] ++;
	}
	

	
}
