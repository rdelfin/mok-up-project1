import java.util.*;

public class PIDController {
    private double Kp, Ti, Td;
    private double err, prevErr, intErr;
    private double setpoint;
    
    public PIDController() {
        
    }
    
    public PIDController(double Kp, double Ti, double Td, double setpoint) {
        this.Kp = Kp;
        this.Ti = Ti;
        this.Td = Td;
        this.setpoint = setpoint;
        err = 0;
        intErr = 0;
        prevErr = 0;
    }
    
    public double output(double input, double time) {

        err = setpoint - input;
        intErr += err*time;

	double intTerm;
	if(Ti == 0.0)
		intTerm = 0;
	else
		intTerm = 1.0/Ti;

        return Kp * (err + intTerm*intErr + Td*(err - prevErr) / time);
    }

}
