This mod is a collection of smaller mods made over time to provide more information to make decisions.

* Creating UI tabs uses a modification to IManager, here are all changes for that file:
* All 3 ui.goods.X java files are created from scratch and wouldn't have any conflict with other mods, other than needing the IManager modifications
view.ui.manage.IManager.java

	1)
	// Import the tabs code
	import view.ui.goods.UIMaintenance;
	import view.ui.goods.UIRecipes;
	import view.ui.goods.UIValues;
	
	2)
	// The tabs being added
    private final UIRecipes recipes;
    private final UIValues values;
    private final UIMaintenance all_maintenance;

	3)
	// The tabs being added
	recipes = new UIRecipes();
	all.add(recipes);

	values = new UIValues();
	all.add(values);

	all_maintenance = new UIMaintenance();
	all.add(all_maintenance);
	
	4)
	// The icons being added to the main UI screen:
	bAdd(s, i++, recipes, UI.icons().s.storage, null);
	bAdd(s, i++, values, UI.icons().s.storage, null);
	bAdd(s, i++, all_maintenance, UI.icons().s.storage, null);
	
	$TODO$ Change icons from "storage" to something more useful
	
* The "Recipes" tab that helps determine if an industry is currently profitable to buy inputs (if any) and sell outputs to your current trade partners
* This is to be used to determine if you should go into an industry, but better information is available if you're already in the industry.
view.ui.goods.UIRecipes.java

	1)
	// Go through all rooms to find the ones that have an "industry." Either using goods or producing goods = INDUSTRY_HASER
	// Then go through each industy to collect each recipe.
	ArrayListGrower<Industry> all = new ArrayListGrower<>();
		for (RoomBlueprint h : ROOMS().all()) {
		if (h instanceof INDUSTRY_HASER) {
			INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
			for (Industry ins : ii.industries()) {
				all.add(ins);
			}
		}
	}
	
	2)
	// Add the rows generated in RRow to "rows"
	ArrayListGrower<RRow> rows = new ArrayListGrower<>();
	for (Industry ind : all) {
		if (ind.outs().isEmpty())
			continue;
		RRow row = new RRow(ind);
		rows.add(row);
	}

	3)
	// Call the rows into the UI, including the top title. This is scrollable if it is very long. The height-15 gives the title 15 space.
	GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-15);
	section.addDown(0, new GText(UI.FONT().H2, "Profit per employee with no labor bonuses, buying inputs and selling outputs"));
	section.addDown(0, scrollRows.view());
	
	4)
	// The `private class RRow extends GuiSection` makes variables for the various prices, but some of these were later dropped because of information overload.
	// Collect all inputs to purchase and all outputs to sell
	double goods_sell = 0;
	double goods_buy = 0;
	for (Industry.IndustryResource oo : ind.outs()) {
		goods_sell += oo.rate * FACTIONS.player().trade.pricesSell.get(oo.resource);
	}
	for (Industry.IndustryResource oo : ind.ins()) {
		goods_buy += oo.rate * FACTIONS.player().trade.pricesBuy.get(oo.resource);
	}
	
	5) 
	// Display them for a given industry's recipe 
	body().setWidth(WIDTH).setHeight(1);
	add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_buy), incTab(2), MARGIN);
	add(ind.blue.icon, incTab(1), 0);


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
	$TODO$   Can add  Boostable t = ind.bonus(); to make a multiplier of the labor speed, but since this is to inform someone to *join* an industry it isn't as important
	
* The "Values" UI panel uses the same code as "Recipes" but instead of using trade prices, it uses "Average Value" the 'middle' of buy or sell prices, averaged from the world. 
view.ui.goods.UIValues.java	
	double goods_sell = 0;
	double goods_buy = 0;
	for (Industry.IndustryResource oo : ind.outs()) {
		goods_sell += oo.rate * FACTIONS.PRICE().get(oo.resource);
	}
	for (Industry.IndustryResource oo : ind.ins()) {
		goods_buy += oo.rate * FACTIONS.PRICE().get(oo.resource);
	}
	
* The "Maintenance" UI panel uses the import price and average value prices to display the cost of maintenance, one resource at a time and the total
	1) 
	// Display the rows using the list of resources
	ArrayListGrower<EmiRow> rows = new ArrayListGrower<>();
	double import_costs = 0;
	double value_costs = 0;
	// Sum up the total first
	for (RESOURCE res : RESOURCES.ALL()) {
			import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
			value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
	}
	
	
* The Node.java file is being updated to allow for more informed decisions. 
* Costs (person cost in terms of lab worker) and Benefits (person benefit in terms of industry speed bonuses) are compared.
* Colors are added to the overlay and text is added to the hover over text. 
* ATM THIS IS A $TODO$ list AND NOT IMPLEMENTED
* 1+2 Color the outline of a tech based on Cost/Benefit analysis
* 3 Calculate costs
* 4 Calculate benefits (of select tech benefits)
ui.tech.Node.java

	1) 
	// Add Cgood and Cbad for their shading colors -- to denote a technology being good or bad, brighten when hovered over as currently done
	// May altar to make it a shade of red / green based on how close it is to being beneficial *in the moment*. 
	public static final COLOR Cgoodhovered  = COLOR.GREEN100.shade(0.8);
	public static final COLOR CgoodDormant  = COLOR.GREEN100.shade(0.3);
	
	public static final COLOR Cbadhovered	= COLOR.RED100.shade(0.8);
	public static final COLOR CbadDormant   = COLOR.RED100.shade(0.3);
	
	// Keep the white in case Benefits can't be calculated
	public static final COLOR Cdormant = COLOR.WHITE100.shade(0.3);
	public static final COLOR Chovered = COLOR.WHITE100.shade(0.8);
	
	2)
	// Convert this function to use (1)'s colors, create a variable for the cost/benefit ratio. 
	private COLOR col(boolean hovered) {
	if (hovered)
		return Chovered;
	return CgoodDormant;
	
	3)
	// Calculate the Costs via workers of labs and libraries, it excludes maintenance costs though.
	double know_tot = 0;        
	double know_emp = 0;
	for (ROOM_LABORATORY lab : ROOMS().LABORATORIES)
	{
		know_tot += lab.knowledge();
		know_emp += lab.employment().employed()
	}
	// can repeat for libraries once I figure that out, probably
