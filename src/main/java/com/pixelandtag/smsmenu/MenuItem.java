package com.pixelandtag.smsmenu;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class MenuItem {

	private static final String NUM_SPACER = ".";
	private static final String NEW_LINE = "\n";
	private int id;
	private String name;
	private int language_id;
	private int parent_level_id;
	private int menu_id;
	private int service_id;
	private boolean visible;
	
	public MenuItem clone(MenuItem menuItem){
		MenuItem mi = new MenuItem();
		mi.setId(menuItem.getId());
		mi.setName(menuItem.getName());
		mi.setLanguage_id(menuItem.getLanguage_id());
		mi.setParent_level_id(menuItem.getParent_level_id());
		mi.setMenu_id(menuItem.getMenu_id());
		mi.setService_id(menuItem.getService_id());
		mi.setVisible(menuItem.isVisible());
		
		return mi;
		
	}
	private LinkedHashMap<Integer,MenuItem> sub_menus;
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getLanguage_id() {
		return language_id;
	}
	public int getParent_level_id() {
		return parent_level_id;
	}
	public int getMenu_id() {
		return menu_id;
	}
	public int getService_id() {
		return service_id;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLanguage_id(int language_id) {
		this.language_id = language_id;
	}
	public void setParent_level_id(int parent_level_id) {
		this.parent_level_id = parent_level_id;
	}
	public void setMenu_id(int menu_id) {
		this.menu_id = menu_id;
	}
	public void setService_id(int service_id) {
		this.service_id = service_id;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public LinkedHashMap<Integer,MenuItem> getSub_menus() {
		return sub_menus;
	}
	public void setSub_menus(LinkedHashMap<Integer,MenuItem> sub_menus) {
		this.sub_menus = sub_menus;
	}
	
	@Override
	public String toString() {
		return "MenuItem [id=" + id + ", name=" + name + ", language_id="
				+ language_id + ", parent_level_id=" + parent_level_id
				+ ", menu_id=" + menu_id + ", service_id=" + service_id
				+ ", visible=" + visible + ", sub_menus=" + sub_menus + "]";
	}
	
	
	
	/**
	 * Returns the menu item as String
	 * @return
	 */
	public MenuItem getMenuByPosition(int chosen){
		return sub_menus.get(chosen);
	}
	
	/**
	 * Returns the menu item as String
	 * @return
	 */
	public String enumerate(){
		StringBuffer sb = new StringBuffer();
		for (Entry<Integer, MenuItem> entry : sub_menus.entrySet())
		 	sb.append(entry.getKey()).append(NUM_SPACER).append(entry.getValue().getName()).append(NEW_LINE);
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
	
		LinkedHashMap<Integer,String> v = new LinkedHashMap<Integer, String>();
		v.put(1,"1. One");
		v.put(2,"2. Two");
		v.put(3,"3. Three");
		v.put(4,"4. Four");
		v.put(5,"5. Five");
		v.put(6,"6. Six");
		v.put(7,"7. Seven");
		v.put(8,"8. Eight");
		v.put(9,"9. Nine");
		v.put(10,"10. Ten");
		
		int chosen = 3;
		 for (Entry<Integer, String> entry : v.entrySet()) {
			 	if(chosen==entry.getKey())
		       System.out.println(entry.getValue());
		 }
		
	}    
	
}
