This mod adds edits import/export depot and import/export settings Ui to add diplomacy price list,
it also changes functionality of "Best" button. It now uses diplomacy price instead of auto trade price.

* Editing Import Export UI required editing: UIGoodsExport, UIGoodsImport, UIGoodsTraders classes.
* A copy of the diplomacy resource cost calculation method was created under TradeManager class. This was done to avoid creating a Deal.

