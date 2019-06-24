import java.util.Arrays;
import java.util.List;
import java.util.Vector;

class Variable{
	private String name;
	private List<String> values;
	private List<Variable> parents;
	private List<Variable> childrens; 
	private CPT cpt;

	public Variable(String name){
		this.name = name;
		this.values = new Vector<String>();
		this.parents = new Vector<Variable>();
		this.childrens = new Vector<Variable>();
		this.cpt = new CPT(this);
	}

	public void setValues(Vector<String> values){
		this.values = new Vector<String>(values);
	}

	public void setParents(Vector<Variable> parents){
		this.parents = new Vector<Variable>(parents);
		for (Variable parent : parents){
			parent.childrens.add(this);
		}
	}

	public String toString(){
		return name + " - " + this.values.toString();	
	}

	public String getVlueAt(int i){
		return this.values.get(i);
	}

	public int indexOf(String value){
		return this.values.indexOf(value);
	}

	public void addtoCPT(CptStateProbability cpt_state_probability){
		this.cpt.addToCPT(cpt_state_probability);
	}

	public List<Variable> getParents() {
		return this.parents;
	}

	public String getName() {
		return this.name;
	}

	public CPT getCPT() {
		return this.cpt;
	}

	public List<String> getValues(){
		return this.values;
	}

	public String getParentsName(){
		return Arrays.toString(this.parents.stream().map(a->a.name).toArray()).substring(1, Arrays.toString(this.parents.stream().map(a->a.name).toArray()).length()-1);
	}

	public boolean isAncestorOfOneOf(Vector<Variable> variables) {
		if(childrens.size() == 0) return false;
		for (Variable sun : childrens){
			if(variables.contains(sun)) return true;
		}
		for (Variable sun : childrens) {
			if(sun.isAncestorOfOneOf(variables)) return true;
		}
		return false;
	}

	public List<Variable> getChildrens() {
		return this.childrens;
	}
}