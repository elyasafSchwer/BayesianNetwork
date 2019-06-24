import java.util.Vector;

class CptVariableCondition{
	private Variable variable;
	private Vector<VariableCondition> variable_states;
	int myValue;

	public CptVariableCondition(Variable variable, 	Vector<VariableCondition> variableState , String value){
		this.variable = variable;
		this.variable_states = new Vector<VariableCondition>(variableState);
		this.myValue = this.variable.indexOf(value);
	}

	public boolean equals_state(CptVariableCondition other){
		for (VariableCondition cptVariableEqualValue : this.variable_states) {
			if(!(other.variable_states.contains(cptVariableEqualValue))) return false;
		}
		for (VariableCondition cptVariableEqualValue : other.variable_states) {
			if(!(this.variable_states.contains(cptVariableEqualValue))) return false;
		}
		return true;
	}

	public boolean equals (CptVariableCondition other){
		return (this.variable == other.variable && this.myValue == other.myValue && equals_state(other));
	}

	public Variable getVariable(){
		return this.variable;
	}

	public VariableCondition getMyVariableState(){
		return new VariableCondition(variable, variable.getVlueAt(myValue));
	}

	public Vector<VariableCondition> getVariableState() {
		return this.variable_states;
	}

	public String toString(){
		return this.variable.getName() + " = " + this.variable.getVlueAt(myValue) + " | " + this.variable_states.toString();
	}

}

