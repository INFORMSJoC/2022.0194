package test;

import java.io.Serializable;
import java.util.Comparator;

public interface comp extends Serializable, Comparator<String>{
	 public int compare(String o1, String o2) ;
}
