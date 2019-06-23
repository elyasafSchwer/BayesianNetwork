class CptStateProbability{
	private CptVariableState state;
	private double value;

	public CptStateProbability(CptVariableState state, double value){
		this.state = state;
		this.value = value;
	}

	public String toString(){
		return this.state.toString() + " : " + this.value;
	}

	public CptVariableState getState() {
		return this.state;
	}

	public double getValue() {
		return this.value;
	}
}