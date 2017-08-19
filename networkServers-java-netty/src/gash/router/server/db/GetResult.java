package gash.router.server.db;

import java.io.Serializable;
import java.util.List;

public class GetResult implements Serializable{
private List<Record> list = null;

public GetResult(List<Record> list) {
	super();
	this.list = list;
}

public List<Record> getList() {
	return list;
}

public void setList(List<Record> list) {
	this.list = list;
}

@Override
public String toString() {
	return "GetResult [list=" + list + "]";
}

}
