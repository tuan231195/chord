import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Multimap<K, V>{
	private Map<K, Set<V>> items;
	public Multimap(){
		items = new TreeMap<K, Set<V>>();
	}
	
	public void addKeyItem(K key, V item)
	{
		if(item == null)
		{
			return;
		}
		Set<V> itemSet = items.get(key) ;
		if (itemSet == null)
		{
			 itemSet = new HashSet<V>();
			 items.put(key, itemSet);
		}
		itemSet.add(item);
	}
	
	public void addBulkKeyItems(K key, Set<V> newItems)
	{
		if(newItems == null)
		{
			return;
		}
		Set<V> itemSet = items.get(key) ;
		if (itemSet == null)
		{
			 itemSet = new HashSet<V>();
			 items.put(key, itemSet);
		}
		itemSet.addAll(newItems);
	}
	
	public  boolean removeKeyItem(K key, V item)
	{
		Set<V> itemSet = items.get(key);
		if (itemSet == null)
		{
			return false;
		}
		else
		{
			return itemSet.remove(item);
		}
	}
	
	public Set<V> removeBulkKeyItems(K key)
	{
		return items.remove(key);
	}
	
	public boolean findKeyItem(K key, V item)
	{
		Set<V> itemSet = items.get(key);
		if (itemSet == null)
		{
			return false;
		}
		else
		{
			return itemSet.contains(item);
		}
	}
	
	public Set<V> getItemsWithKey(K key){
		return items.get(key);
	}
	
	public Set<Entry<K, Set<V>>> getItems()
	{
		return items.entrySet();
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Entry<K, Set<V>> itemBucket : items.entrySet()) {
			sb.append(itemBucket.getKey());
			sb.append(":" + "\n");
			sb.append(itemBucket.getValue().toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
