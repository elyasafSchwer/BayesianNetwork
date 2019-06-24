import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Variable{
	private String name;
	private List<String> values;
	private List<Variable> parents;
	private List<Variable> childrens; 
	private CPT cpt;

	public Variable(String name){
		this.name = name;
		this.values = new ArrayList<String>();
		this.parents = new ArrayList<Variable>();
		this.childrens = new ArrayList<Variable>();
		this.cpt = new CPT(this);
	}

	public void setValues(List<String> values){
		this.values = new ArrayList<String>(values);
	}

	public void setParents(List<Variable> parents){
		this.parents = new ArrayList<Variable>(parents);
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

	public void addtoCPT(CptConditionProbability cpt_state_probability){
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
		return Arrays.toString(this.parents.stream().map(a->a.name).toArray())
				.substring(1, Arrays.toString(this.parents.stream().map(a->a.name).toArray()).length()-1);
	}

	public boolean isAncestorOfOneOf(List<Variable> variables) {
		if(childrens.size() == 0) return false;
		for (Variable child : childrens){
			if(variables.contains(child)) return true;
		}
		for (Variable child : childrens) {
			if(child.isAncestorOfOneOf(variables)) return true;
		}
		return false;
	}

	public List<Variable> getChildrens() {
		return this.childrens;
	}
}