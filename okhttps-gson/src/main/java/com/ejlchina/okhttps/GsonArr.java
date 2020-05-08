package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GsonArr implements JsonArr {

	private JsonArray json;
	
	public GsonArr(JsonArray json) {
		this.json = json;
	}

	@Override
	public int size() {
		return json.size();
	}

	@Override
	public boolean isEmpty() {
		return json.size() == 0;
	}

	@Override
	public JsonObj getJsonOjb(int index) {
		JsonElement subJson = json.get(index);
		if (subJson != null) {
			return new GsonObj(subJson.getAsJsonObject());
		}
		return null;
	}

	@Override
	public JsonArr getJsonArr(int index) {
		JsonElement subJson = json.get(index);
		if (subJson != null) {
			return new GsonArr(subJson.getAsJsonArray());
		}
		return null;
	}

	@Override
	public Boolean getBool(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsBoolean();
		}
		return null;
	}

	@Override
	public boolean getBoolVal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsBoolean();
		}
		return false;
	}

	@Override
	public Integer getInt(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsInt();
		}
		return null;
	}

	@Override
	public int getIntVal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsInt();
		}
		return 0;
	}

	@Override
	public Short getShort(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsShort();
		}
		return null;
	}

	@Override
	public short getShortVal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsShort();
		}
		return 0;
	}

	@Override
	public Float getFloat(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsFloat();
		}
		return null;
	}

	@Override
	public float getFloatVal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsFloat();
		}
		return 0;
	}

	@Override
	public Double getDouble(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsDouble();
		}
		return null;
	}

	@Override
	public double getDoubleVal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsDouble();
		}
		return 0;
	}

	@Override
	public String getString(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsString();
		}
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsBigDecimal();
		}
		return null;
	}

	@Override
	public BigInteger getBigInteger(int index) {
		JsonElement val = json.get(index);
		if (val != null) {
			return val.getAsBigInteger();
		}
		return null;
	}

}
