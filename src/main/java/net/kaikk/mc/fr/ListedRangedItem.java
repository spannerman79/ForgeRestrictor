package net.kaikk.mc.fr;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;

class ListedRangedItem extends ListedItem {
	int range;
	
	ListedRangedItem(Material material, Short data, String world, int range) {
		super(material, data, world);	
		this.range=range;
	}
	
	ListedRangedItem(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			switch(entry.getKey()) {
			case "material":
				this.material=(Material) Material.valueOf((String) entry.getValue());
				break;
			case "data":
				this.data=Short.valueOf((byte) entry.getValue());
				break;
			case "world":
				this.world=(String) entry.getValue();
				break;
			case "range":
				this.range=(int) entry.getValue();
				break;
			}
		}
	}
	
	ListedRangedItem(String serialized) {
		String[] arr=serialized.split(":");
		if (arr.length<2) {
			throw new IllegalArgumentException();
		}
		
		this.material = (Material) Material.valueOf(arr[0]);
		this.range = Integer.valueOf(arr[1]);
		
		if (arr.length>2) {
			this.data = (arr[2].equals("*")?null:Short.valueOf(arr[2]));
			if (arr.length>3) {
				this.world = (arr[3].equals("*")?null:arr[3]);
			}
		}
	}
	
	String serialize() {
		return material+":"+range+(data==null&&world==null ? "" : (data==null ? ":*" : ":"+data)+(world==null ? "" : ":"+world));
	}
	
	@Override
	public String toString() {
		return material+":"+(data==null?"*":data)+" ("+range+")"+(world==null ? "" : " ["+world+"]");
	}
}
