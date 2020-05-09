package com.ejlchina.okhttps;

public class User {

    private int id;
    private String name;

    
    public User() {
	}
    
    public User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
