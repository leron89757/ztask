package org.nutz.ztask.api;

import org.nutz.mongo.annotation.Co;
import org.nutz.mongo.annotation.CoField;
import org.nutz.mongo.annotation.CoId;
import org.nutz.mongo.annotation.CoIndexes;

@Co("label")
@CoIndexes("!:+name")
public class Label {

	@CoId
	private String _id;

	@CoField("nm")
	private String name;

	@CoField("pnm")
	private String parent;

	@CoField("cnt")
	private int count;

	@CoField("chdn")
	private String[] children;

	public String toString() {
		return String.format("%s%s:%d", null == parent ? "" : parent + "/", name, count);
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isNode() {
		return null != children;
	}

	public String[] getChildren() {
		return children;
	}

	public void setChildren(String[] children) {
		this.children = children;
	}

}
