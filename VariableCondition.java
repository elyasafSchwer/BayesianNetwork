class VariableCondition{
	private Variable variable;
	private int value;
	public VariableCondition(Variable variable, String value){
		this.variable = variable;
		this.value = variable.indexOf(value);
	}
	public VariableCondition(Variable variable){
		this.variable = variable;
		this.value = -1;
	}
	public String toString(){
		return (this.value!=-1) ? this.variable.getName() + "=" + this.variable.getVlueAt(this.value) :
								this.variable.getName();
	}

	public boolean equals(VariableCondition other){
		return (this.variable == other.variable && this.value == other.value);
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