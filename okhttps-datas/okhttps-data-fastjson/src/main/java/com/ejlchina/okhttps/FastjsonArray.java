package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FastjsonArray implements Array, List<Object> {

	private final JSONArray json;
	
	public FastjsonArray(JSONArray json) {
		this.json = json;
	}

	@Override
	public int size() {
		return json.size();
	}

	@Override
	public boolean isEmpty() {
		return json.isEmpty();
	}

	@Override
	public Mapper getMapper(int index) {
		JSONObject subJson = json.getJSONObject(index);
		if (subJson != null) {
			return new FastjsonMapper(subJson);
		}
		return null;
	}

	@Override
	public Array getArray(int index) {
		JSONArray subJson = json.getJSONArray(index);
		if (subJson != null) {
			return new FastjsonArray(subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(int index) {
		return json.getBooleanValue(index);
	}

	@Override
	public int getInt(int index) {
		return json.getIntValue(index);
	}

	@Override
	public long getLong(int index) {
		return json.getLongValue(index);
	}
	
	@Override
	public float getFloat(int index) {
		return json.getFloatValue(index);
	}

	@Override
	public double getDouble(int index) {
		return json.getDoubleValue(index);
	}

	@Override
	public String getString(int index) {
		return json.getString(index);
	}

	@Override
	public boolean contains(Object o) {
		return json.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return json.iterator();
	}

	@Override
	public Object[] toArray() {
		return json.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		//noinspection SuspiciousToArrayCall
		return json.toArray(a);
	}

	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return json.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int index) {
		return json.get(index);
	}

	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return json.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return json.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return json.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return json.listIterator(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return json.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		return json.toJSONString();
	}

}
