package net.kaikk.mc.fr;

import org.bukkit.Material;

class ListedItem {
	Material material;
	Short data;
	String world;
	
	ListedItem(){}
	
	ListedItem(Material material, Short data, String world) {
		this.material = material;
		this.data = data;
		this.world = world;
	}
	
	ListedItem(String serialized){
		String[] arr=serialized.split(":");
		if (arr.length<1) {
			throw new IllegalArgumentException();
		}
		
		this.material = (Material) Material.valueOf(arr[0]);
		if (arr.length>1) {
			this.data = (arr[1].equals("null")?null:Short.valueOf(arr[1]));
			if (arr.length>2) {
				this.world = (arr[2].equals("null")?null:arr[2]);
			}
		}
	}
	
	String serialize() {
		return material+":"+(data==null&&world==null ? "" : (data==null ? ":*" : ":"+data)+(world==null ? "" : ":"+world));
	}
	
	boolean match(Material material, Short data, String world) {
		boolean test = material==this.material && (this.data==null || this.data.equals(data)) && (this.world==null || this.world.equals(world));
		return test;
	}
	
	boolean equals(Material material, Short data, String world) {
		boolean test = material==this.material && (this.data==data || (this.data!=null && this.data.equals(data))) && (this.world==world || (this.world!=null && this.world.equals(world)));
		return test;
	}

	@Override
	public String toString() {
		return material+":"+(data==null?"*":data)+(world==null ? "" : " ["+world+"]");
	}
}
