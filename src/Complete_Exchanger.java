public class Complete_Exchanger {
    private HeatExchanger_Geometry geometry;
    private Fluid_Properties Inner_Fluid;
    private Fluid_Properties Annular_Fluid;
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
    public Fluid_Properties getInner_Fluid()   { return new Fluid_Properties(this.Inner_Fluid); }
    public Fluid_Properties getAnnular_Fluid() { return new Fluid_Properties(this.Annular_Fluid); }
    public HeatExchanger_Geometry getGeometry() { return new HeatExchanger_Geometry(this.geometry); }








    //========================================================================================================
    //                                    Heat Transfer Calculation
    //========================================================================================================

    public double calculate_u_0 () {
        // independent of flow regime
        double Pr_inner = this.Inner_Fluid.Pr();
        double r_foul_i = (this.geometry.getdInner_Out() * this.Inner_Fluid.getFouling_resistance()) / this.geometry.getdInner_In();
        double r_foul_a = this.Annular_Fluid.getFouling_resistance();
        double Pr_annular = this.Annular_Fluid.Pr();
        double r_wall = (this.geometry.getdInner_Out() * Math.log((this.geometry.getdInner_Out() / this.geometry.getdInner_In()))) / (2 * this.geometry.getK());

        // calculate Nu according to the flow regime for the Inner pipe
        double Nu_inner;
        if (Re_inner <= 2300) {                 // laminar
            double Gz_inner = Exchanger_Correlations.Gz_inner(Re_inner, Pr_inner, this.geometry.getdInner_In(), this.geometry.lStraightTotal());
            Nu_inner = Exchanger_Correlations.Nu_inner_laminar(Gz_inner);
        } else if (Re_inner >= 10000) {         // turbulent
            double f_0 = Exchanger_Correlations.f_0_inner(Re_inner);
            Nu_inner = Exchanger_Correlations.Nu_inner_turbulent(Re_inner, Pr_inner, f_0);
        } else {                                // transition
            System.out.println("Error: inner flow is in the transition regime (Re = " + Re_inner + ")");
            System.exit(0);
            return Double.NaN;   // never runs, but Java needs it or it says Nu_inner might not be initialized
        }
        // calculate Nu according to the flow regime for the Annular pipe
        double Nu_annular;
        if (Re_annular <= 2300) {
            Nu_annular = Exchanger_Correlations.Nu_annulus_laminar(alpha);
        } else if (Re_annular >= 10000) {
            double Re_a_star = Exchanger_Correlations.Re_a_star(Re_annular, alpha);
            double f_a = Exchanger_Correlations.f_a_annulus(Re_a_star);
            double k1 = Exchanger_Correlations.k1_annulus(Re_annular, Pr_annular);
            double f_length = Exchanger_Correlations.f_length(this.geometry.d_Ha(), this.geometry.lStraightTotal());
            double f_geom = Exchanger_Correlations.f_geom(alpha);
            Nu_annular = Exchanger_Correlations.Nu_annulus_turbulent(f_a, Re_annular, Pr_annular, k1, f_length, f_geom);
        } else {
            System.out.println("Error: annular flow is in the transition regime (Re = " + Re_annular + ")");
            System.exit(0);
            return Double.NaN;
        }
        // complete the calculations using the correct Nu depending on the flow regime
        double h_i = (Nu_inner * this.Inner_Fluid.getK()) / this.geometry.getdInner_In();
        double r_conv_i = this.geometry.getdInner_Out() / (this.geometry.getdInner_In() * h_i);
        double h_a = (Nu_annular * this.Annular_Fluid.getK()) / this.geometry.d_Ha();
        double r_conv_a = 1 / h_a;

        return 1 / (r_conv_i + r_foul_i + r_wall + r_foul_a + r_conv_a);
    }

    // calculate the number of transfer units
    public double calculateNTU (){
        return ((this.geometry.a_O() * this.u_0) / this.C_min) ;
    }

    // calculate effectiveness - accounting for the specific configuration of the exchanger
    public double calculateEps () {
        if (this.geometry.getConfiguration().equalsIgnoreCase("Parallel")){
            return ( 1 - Math.exp(-this.NTU * (1 + this.C_r)))/(1 + this.C_r);
        }
        if (this.geometry.getConfiguration().equalsIgnoreCase("Counter-Current")){
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
        this.Re_inner = this.Inner_Fluid.Re(this.geometry.getdInner_In(), v_inner);
        this.alpha = this.geometry.getdInner_Out() / this.geometry.getdOuter_In();this.C_min = Math.min(this.Inner_Fluid.getC(), this.Annular_Fluid.getC());
        this.C_max = Math.max(this.Inner_Fluid.getC(), this.Annular_Fluid.getC());
        this.C_r = this.C_min / this.C_max;
        this.u_0 = this.calculate_u_0();
        this.NTU = this.calculateNTU();
        this.eps = this.calculateEps();
    }

    // final equation to calculate the Heat Transfer Rate
    public double calculate_q (Fluid_Properties Process, Fluid_Properties HotWater){
        return this.eps * this.C_min * (HotWater.getT_in() - Process.getT_in());
    }

    public double[] calculate_t_out (Fluid_Properties Process, Fluid_Properties HotWater){
        // returns an array of doubles,
        // t_out[0] = t_p,out
        // t_out[1] = t_w,out
        double q = this.calculate_q(Process,HotWater);
        double[] t_out = new double[2];
        t_out[0] = Process.getT_in() + q / Process.getC() ;
        t_out[1] = HotWater.getT_in() - q / HotWater.getC() ;
        return t_out;
    }



    //========================================================================================================
    //                                    Pressure Drop Calculation
    //========================================================================================================

    public double[] calculate_P(){
        double[] del_P = new double[2];
        double k_total = Exchanger_Correlations.k_total(this.geometry.getN());
        double L = this.geometry.lStraightTotal();
        // calculate pressure drop for the inner tube first
        double f_d_inner;
        if (Re_inner <= 2300) {                     // determine flow regime, if laminar
            f_d_inner = Exchanger_Correlations.f_d_inner_laminar(Re_inner);
        } else if (Re_inner >= 10000) {             // determine flow regime, if turbulent
            f_d_inner = Exchanger_Correlations.f_d_turbulent(Re_inner, this.geometry.getEps_r(), this.geometry.getdInner_In());
        } else {                                    // reject any transition flow regime
            System.out.println("Error: inner flow is in the transition regime (Re = " + Re_inner + ")");
            System.exit(0);
            return null;
        }
        // calculate pressure drop for the annular tube next
        double f_d_annular;
        if (Re_annular <= 2300) {                   // determine flow regime, if laminar
            f_d_annular = Exchanger_Correlations.f_d_annulus_laminar(Re_annular, alpha);
        } else if (Re_annular >= 10000) {           // determine flow regime, if turbulent
            f_d_annular = Exchanger_Correlations.f_d_turbulent(Re_annular, this.geometry.getEps_r(), this.geometry.d_Ha());
        } else {                                    // reject any transition flow regime
            System.out.println("Error: annular flow is in the transition regime (Re = " + Re_annular + ")");
            System.exit(0);
            return null;
        }
        // format outputs and return the array of doubles, inner tube pressure drop at [0] and annular tube pressure drop  at [1]
        del_P[0] = Exchanger_Correlations.delta_P(f_d_inner,   L, this.geometry.getdInner_In(), k_total, this.Inner_Fluid.getRho(),   v_inner);
        del_P[1] = Exchanger_Correlations.delta_P(f_d_annular, L, this.geometry.d_Ha(),         k_total, this.Annular_Fluid.getRho(), v_annular);
        return del_P;
    }


}
