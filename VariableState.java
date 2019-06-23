class VariableState{
	private Variable variable;
	private int value;
	public VariableState(Variable variable, String value){
		this.variable = variable;
		this.value = variable.indexOf(value);
	}
	public VariableState(Variable variable){
		this.variable = variable;
		this.value = -1;
	}
	public String toString(){
		return (this.value!=-1) ? this.variable.getName() + "=" + this.variable.getVlueAt(this.value) : this.variable.getName();
	}

	public boolean equals(VariableState other){
		return (this.variable == other.variable && this.value == other.value);
		//yes, I check if the pointers of variable is equals.
	}
	public Variable getVariable() {
		return this.variable;
	}
	public String getValue(){
		return (this.value!=-1) ? this.variable.getVlueAt(value) : "none";
	}
	public boolean haveValue(){
		return value > -1;
	}
}