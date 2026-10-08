public class Complete_Exchanger {
    // configuration of exchanger
    private HeatExchanger_Geometry geometry;
    private Fluid_Properties Inner_Fluid;
    private Fluid_Properties Annular_Fluid;
    // result for this specific exchanger
    private double C_min;
    private double C_max;
    private double C_r;
    private double u_0;
    private double NTU;
    private double eps;
    double v_annular;
    double Re_annular;
    double v_inner;
    double Re_inner;
    double alpha;

    // constructor
    public Complete_Exchanger (HeatExchanger_Geometry geometry, Fluid_Properties Inner_Fluid, Fluid_Properties Annular_Fluid){
        if (geometry == null || Inner_Fluid == null || Annular_Fluid == null)
            System.exit(0);

        this.geometry = new HeatExchanger_Geometry(geometry); // use the copy constructor for each of these
        this.Inner_Fluid = new Fluid_Properties(Inner_Fluid);
        this.Annular_Fluid = new Fluid_Properties(Annular_Fluid);
        this.calculate();       // calling this completes all the calculations to set all the instance variables
    }

    // copy constructor
    public Complete_Exchanger (Complete_Exchanger source) {
        if (source == null) System.exit(0);
        this.geometry = new HeatExchanger_Geometry(source.geometry);
        this.Inner_Fluid = new Fluid_Properties(source.Inner_Fluid);
        this.Annular_Fluid = new Fluid_Properties(source.Annular_Fluid);
        this.calculate();       // calling this completes all the calculations to set all the instance variables
    }

    // getter
    public Fluid_Properties Inner_Fluid()   { return new Fluid_Properties(this.Inner_Fluid); }
    public Fluid_Properties Annular_Fluid() { return new Fluid_Properties(this.Annular_Fluid); }
    public HeatExchanger_Geometry geometry() { return new HeatExchanger_Geometry(this.geometry); }


    public double calculate_u_0 () {
        // independent of flow regime
        double Pr_inner = this.Inner_Fluid.Pr();
        double r_foul_i = (this.geometry.dInner_Out() * this.Inner_Fluid.fouling_resistance()) / this.geometry.dInner_In();
        double r_foul_a = this.Annular_Fluid.fouling_resistance();
        double Pr_annular = this.Annular_Fluid.Pr();
        double r_wall = (this.geometry.dInner_Out() * Math.log((this.geometry.dInner_Out() / this.geometry.dInner_In()))) / (2 * this.geometry.k());

        // calculate Nu according to the flow regime for the Inner pipe
        double Nu_inner;
        if (Re_inner <= 2300) {                 // laminar
            double Gz_inner = HeatTransfer_Calculator.Gz_inner(Re_inner, Pr_inner, this.geometry.dInner_In(), this.geometry.lStraightTotal());
            Nu_inner = HeatTransfer_Calculator.Nu_inner_laminar(Gz_inner);
        } else if (Re_inner >= 10000) {         // turbulent
            double f_0 = HeatTransfer_Calculator.f_0_inner(Re_inner);
            Nu_inner = HeatTransfer_Calculator.Nu_inner_turbulent(Re_inner, Pr_inner, f_0);
        } else {                                // transition
            System.out.println("Error: inner flow is in the transition regime (Re = " + Re_inner + ")");
            System.exit(0);
            return Double.NaN;   // never runs, but Java needs it or it says Nu_inner might not be initialized
        }
        // calculate Nu according to the flow regime for the Annular pipe
        double Nu_annular;
        if (Re_annular <= 2300) {
            Nu_annular = HeatTransfer_Calculator.Nu_annulus_laminar(alpha);
        } else if (Re_annular >= 10000) {
            double Re_a_star = HeatTransfer_Calculator.Re_a_star(Re_annular, alpha);
            double f_a = HeatTransfer_Calculator.f_a_annulus(Re_a_star);
            double k1 = HeatTransfer_Calculator.k1_annulus(Re_annular, Pr_annular);
            double f_length = HeatTransfer_Calculator.f_length(this.geometry.d_Ha(), this.geometry.lStraightTotal());
            double f_geom = HeatTransfer_Calculator.f_geom(alpha);
            Nu_annular = HeatTransfer_Calculator.Nu_annulus_turbulent(f_a, Re_annular, Pr_annular, k1, f_length, f_geom);
        } else {
            System.out.println("Error: annular flow is in the transition regime (Re = " + Re_annular + ")");
            System.exit(0);
            return Double.NaN;
        }
        // complete the calculations using the correct Nu depending on the flow regime
        double h_i = (Nu_inner * this.Inner_Fluid.k()) / this.geometry.dInner_In();
        double r_conv_i = this.geometry.dInner_Out() / (this.geometry.dInner_In() * h_i);
        double h_a = (Nu_annular * this.Annular_Fluid.k()) / this.geometry.d_Ha();
        double r_conv_a = 1 / h_a;

        return 1 / (r_conv_i + r_foul_i + r_wall + r_foul_a + r_conv_a);
    }

    // calculate the number of transfer units
    public double calculateNTU (){
        return ((this.geometry.a_O() * this.u_0) / this.C_min) ;
    }

    // calculate effectiveness - accounting for the specific configuration of the exchanger
    public double calculateEps () {
        if (this.geometry.configuration().equalsIgnoreCase("Parallel")){
            return ( 1 - Math.exp(-this.NTU * (1 + this.C_r)))/(1 + this.C_r);
        }
        if (this.geometry.configuration().equalsIgnoreCase("Counter-Current")){
            if (this.C_r != 1)
                return ( 1 - Math.exp(-this.NTU*(1-this.C_r)))/(1-(this.C_r*Math.exp(-this.NTU*(1-this.C_r))));
            return this.NTU / (1 + this.NTU);
        }
        return Double.NaN;
    }

    // call this method within the constructor, then it will calculate everything you need to solve for q when the object is created
    private void calculate(){
        // void return type because we just want to set the values for the instance variables, we are not returning anything from this method
        this.v_annular = this.Annular_Fluid.v(this.geometry.a_A());
        this.Re_annular = this.Annular_Fluid.Re(this.geometry.d_Ha(), v_annular);
        this.v_inner = this.Inner_Fluid.v(this.geometry.a_I());
        this.Re_inner = this.Inner_Fluid.Re(this.geometry.dInner_In(), v_inner);
        this.alpha = this.geometry.dInner_Out() / this.geometry.dOuter_In();this.C_min = Math.min(this.Inner_Fluid.C(), this.Annular_Fluid.C());
        this.C_max = Math.max(this.Inner_Fluid.C(), this.Annular_Fluid.C());
        this.C_r = this.C_min / this.C_max;
        this.u_0 = this.calculate_u_0();
        this.NTU = this.calculateNTU();
        this.eps = this.calculateEps();
    }

    // final equation to calculate the Heat Transfer Rate
    public double calculate_q (Fluid_Properties Process, Fluid_Properties HotWater){
        return this.eps * this.C_min * (HotWater.T_in() - Process.T_in());
    }

    public double[] calculate_t_out (Fluid_Properties Process, Fluid_Properties HotWater){
        // returns an array of doubles,
        // t_out[0] = t_p,out
        // t_out[1] = t_w,out
        double q = this.calculate_q(Process,HotWater);
        double[] t_out = new double[2];
        t_out[0] = Process.T_in() + q / Process.C() ;
        t_out[1] = HotWater.T_in() - q / HotWater.C() ;
        return t_out;
    }



    //========================================================================================================
    //                                    Pressure Drop Calculation
    //========================================================================================================

    public double[] calculate_P(){
        // returns an array of doubles,
        // del_p[0] = inner pressure drop
        // del_p[1] = annular stream pressure drop
        double[] del_P = new double[2];
        double k_total = (1.5 * (1+this.geometry.n())) + (2 * Math.max(0, this.geometry.n()-1));

        double f_d_inner;                       // check the flow regime
        if (Re_inner <= 2300) {                 // laminar flow, use this equation
            f_d_inner = 64 / Re_inner;
        } else if (Re_inner >= 10000) {         // turbulent flow, use this equation
            f_d_inner = Math.pow((-1.8*Math.log10((Math.pow((this.geometry.eps_r()
                    /(3.7 * this.geometry.dInner_In())),1.11)) + (6.9/Re_inner))),-2);
        } else {                                // transition regime, exit and print error
            System.out.println("Error: inner flow is in the transition regime (Re = " + Re_inner + ")");
            System.exit(0);
        return null;
        }

        double f_d_annular;
        if (Re_annular <= 2300) {                 // laminar flow, use this equation
            f_d_annular = (64/Re_annular) * (Math.pow((1-alpha),2))
                    /(1 + Math.pow(alpha,2) + (1 - Math.pow(alpha,2))/(Math.log(alpha)));
        } else if (Re_annular >= 10000) {         // turbulent flow, use this equation
            f_d_annular = Math.pow((-1.8*Math.log10((Math.pow((this.geometry.eps_r()
                    /(3.7 * this.geometry.d_Ha())),1.11)) + (6.9/Re_annular))),-2);
        } else {                                  // transition regime, exit and print error
            System.out.println("Error: annular flow is in the transition regime (Re = " + Re_annular + ")");
            System.exit(0);
            return null;
        }

        // compute the pressure drop for the inner stream
        del_P[0] = (f_d_inner * (this.geometry.lStraightTotal()
                / this.geometry.dInner_In()) + k_total) * (this.Inner_Fluid.rho()*
                Math.pow(this.Inner_Fluid.v(this.geometry.a_I()),2)) / 2 ;
        // compute the pressure drop for the outer stream
        del_P[1] = (f_d_annular * (this.geometry.lStraightTotal()
                / this.geometry.d_Ha()) + k_total) * (this.Annular_Fluid.rho()*
                Math.pow(this.Annular_Fluid.v(this.geometry.a_A()),2)) / 2 ;
        // return both pressure drops as the array
        return del_P;
    }


}
