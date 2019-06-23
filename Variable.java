import java.util.Arrays;
import java.util.Vector;

class Variable{
	private String name;
	private Vector<String> values;
	private Vector<Variable> parents;
	private Vector<Variable> suns; 
	private CPT cpt;

	public Variable(String name){
		this.name = name;
		this.values = new Vector<String>();
		this.parents = new Vector<Variable>();
		this.suns = new Vector<Variable>();
		this.cpt = new CPT(this);
	}

	public void setValues(Vector<String> values){
		this.values = new Vector<String>(values);
	}

	public void setParents(Vector<Variable> parents){
		this.parents = new Vector<Variable>(parents);
		for (Variable parent : parents){
			parent.suns.add(this);
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

	public Vector<Variable> getParents() {
		return this.parents;
	}

	public String getName() {
		return this.name;
	}

	public CPT getCPT() {
		return this.cpt;
	}

	public Vector<String> getValues(){
		return this.values;
	}

	public String getParentsName(){
		return Arrays.toString(this.parents.stream().map(a->a.name).toArray()).substring(1, Arrays.toString(this.parents.stream().map(a->a.name).toArray()).length()-1);
	}

	public boolean isAncestorOfOneOf(Vector<Variable> variables) {
		if(suns.size() == 0) return false;
		for (Variable sun : suns){
			if(variables.contains(sun)) return true;
		}
		for (Variable sun : suns) {
			if(sun.isAncestorOfOneOf(variables)) return true;
		}
		return false;
	}

	public Vector<Variable> getSuns() {
		return this.suns;
	}
}