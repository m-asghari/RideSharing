package edu.usc.infolab.ridesharing;

public class Pair<X, Y>{
	public final X First;
	public final Y Second;
	
	public Pair(X First, Y Second) {
		this.First = First;
		this.Second = Second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((First == null) ? 0 : First.hashCode());
		result = prime * result + ((Second == null) ? 0 : Second.hashCode());
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
		Pair other = (Pair) obj;
		if (First == null) {
			if (other.First != null)
				return false;
		} else if (!First.equals(other.First))
			return false;
		if (Second == null) {
			if (other.Second != null)
				return false;
		} else if (!Second.equals(other.Second))
			return false;
		return true;
	}
}
