public class Fluid_Properties {

    private double mu;                 // Viscosity:
    private double rho;                 // Density: kg/m^3
    private double M_flow;              // Mass Flow In: kg/s
    private double T_in;                // Inlet Temperature: ºC
    private double Cp;                  // Heat Capacity:
    private double k;                   // Thermal Conductivity:
    private double fouling_resistance;  // Fouling Resistance: m^2/W*K


    // constructor method
    public Fluid_Properties(double mu, double rho, double M_flow, double T_in, double Cp, double k, double fouling_resistance){
        this.mu = mu;
        this.rho = rho;
        this.M_flow = M_flow;
        this.T_in = T_in;
        this.Cp = Cp;
        this.k = k;
        this.fouling_resistance = fouling_resistance;
    }

    // copy constructor
    public Fluid_Properties(Fluid_Properties source){
        this.mu = source.mu;
        this.rho = source.rho;
        this.M_flow = source.M_flow;
        this.T_in = source.T_in;
        this.Cp = source.Cp;
        this.k = source.k;
        this.fouling_resistance = source.fouling_resistance;
    }

    //getter methods
    public double mu(){ return this.mu; }
    public double rho(){ return this.rho; }
    public double M_flow(){ return this.M_flow; }
    public double T_in(){ return this.T_in; }
    public double Cp(){ return this.Cp; }
    public double k(){ return this.k; }
    public double fouling_resistance() { return this.fouling_resistance; }

    // flow velocity (m/s)
    public double v(double area ){
        return this.M_flow / (this.rho*area);
    }

    // Reynolds number (dimensionless)
    public double Re(double hydraulic_diameter, double flow_velocity){
        return (this.rho * flow_velocity * hydraulic_diameter) / this.mu ;
    }

    // Prandtl number (dimensionless)
    public double Pr (){
        return (this.Cp * this.mu) / this.k ;
    }
}
