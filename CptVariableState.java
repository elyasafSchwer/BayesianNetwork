import java.util.Vector;

class CptVariableState{
	private Variable variable;
	private Vector<VariableState> variable_states;
	int myValue;

	public CptVariableState(Variable variable, 	Vector<VariableState> variableState , String value){
		this.variable = variable;
		this.variable_states = new Vector<VariableState>(variableState);
		this.myValue = this.variable.indexOf(value);
	}

	public boolean equals_state(CptVariableState other){
		for (VariableState cptVariableEqualValue : this.variable_states) {
			if(!(other.variable_states.contains(cptVariableEqualValue))) return false;
		}
		for (VariableState cptVariableEqualValue : other.variable_states) {
			if(!(this.variable_states.contains(cptVariableEqualValue))) return false;
		}
		return true;
	}

	public boolean equals (CptVariableState other){
		return (this.variable == other.variable && this.myValue == other.myValue && equals_state(other));
	}

	public Variable getVariable(){
		return this.variable;
	}

	public VariableState getMyVariableState(){
		return new VariableState(variable, variable.getVlueAt(myValue));
	}

	public Vector<VariableState> getVariableState() {
		return this.variable_states;
	}

	public String toString(){
		return this.variable.getName() + " = " + this.variable.getVlueAt(myValue) + " | " + this.variable_states.toString();
	}

}

