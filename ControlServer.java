/**
 * This program runs as a server and controls the force to be applied to balance the Inverted Pendulum system running on the clients.
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class ControlServer {

    private static ServerSocket serverSocket;
    private static final int port = 25533;
    

    /**
     * Main method that creates new socket and PoleServer instance and runs it.
     */
    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            System.out.println("unable to set up port");
            System.exit(1);
        }
        System.out.println("Waiting for connection");
        do {
            Socket client = serverSocket.accept();
            System.out.println("\nnew client accepted.\n");
            PoleServer_handler handler = new PoleServer_handler(client);
        } while (true);
    }
}

/**
 * This class sends control messages to balance the pendulum on client side.
 */
class PoleServer_handler implements Runnable {
    // Set the number of poles
    private static final int NUM_POLES = 1;

    static ServerSocket providerSocket;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message = "abc";
    static Socket clientSocket;
    Thread t;
    private PIDController controller;
    private final double K_P, K_I, K_D, SET_POINT;
    PrintWriter angleLog, positionLog;
    File angleFile, positionFile;

    /**
     * Class Constructor
     */
    public PoleServer_handler(Socket socket) {
    	double kp = 0, ki = 0, kd = 0;
    	SET_POINT = 0;
        t = new Thread(this);
        clientSocket = socket;
        
        try {
			Scanner config = new Scanner(new File("config.txt"));
			kp = config.nextDouble();
			ki = config.nextDouble();
			kd = config.nextDouble();
			System.out.println();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        K_P = kp;
        K_I = ki;
        K_D = kd;
        
	String filePrefix = UUID.randomUUID().toString();
        angleFile = new File(filePrefix + "-angle.txt");
	positionFile = new File(filePrefix + "-position.txt");
        try {
			angleLog = new PrintWriter(angleFile);
			positionLog = new PrintWriter(positionFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        controller = new PIDController(K_P, K_I, K_D, SET_POINT);

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        t.start();
    }
    double angle, angleDot, angleDDot, pos, posDot, posDDot,  action = 0, i = 0;

    /**
     * This method receives the pole positions and calculates the updated value
     * and sends them across to the client.
     * It also sends the amount of force to be applied to balance the pendulum.
     * @throws ioException
     */
    void control_pendulum(ObjectOutputStream out, ObjectInputStream in) {
        try {
            while(true){
                System.out.println("-----------------");

                // read data from client
                Object obj = in.readObject();

                // Do not process string data unless it is "bye", in which case,
                // we close the server
                if(obj instanceof String){
                    System.out.println("STRING RECEIVED: "+(String) obj);
                    if(obj.equals("bye")){
                        break;
                    }
                    continue;
                }
                
                double[] data= (double[])(obj);
                assert(data.length == NUM_POLES * 6);
                double[] actions = new double[NUM_POLES];
 
                // Get sensor data of each pole and calculate the action to be
                // applied to each inverted pendulum
                // TODO: Current implementation assumes that each pole is
                // controlled independently. This part needs to be changed if
                // the control of one pendulum needs sensing data from other
                // pendulums.
                for (int i = 0; i < NUM_POLES; i++) {
                  angle = data[i*6+0];
                  angleDot = data[i*6+1];
                  angleDDot = data[i*6+2];
                  pos = data[i*6+3];
                  posDot = data[i*6+4];
                  posDDot = data[i*6+5];
                  
                  System.out.println("server < pole["+i+"]: "+angle+"  "
                      +angleDot+"   "+angleDDot+"   "+pos+"  "+posDot+ "  "+posDDot);
                  actions[i] = calculate_action(angle, angleDot, angleDDot, pos, posDot, posDDot);
                }

                sendMessage_doubleArray(actions);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (clientSocket != null) {
                System.out.println("closing down connection ...");                
                out.writeObject("bye");
                out.flush();
                in.close();
                out.close();
                clientSocket.close();
            }
        } catch (IOException ioe) {
            System.out.println("unable to disconnect");
        }

        System.out.println("Session closed. Waiting for new connection...");

    }

    /**
     * This method calls the controller method to balance the pendulum.
     * @throws ioException
     */
    public void run() {

        try {
            control_pendulum(out, in);

        } catch (Exception ioException) {
            ioException.printStackTrace();
        } finally {
        }

    }

    // Calculate the actions to be applied to the inverted pendulum from the
    // sensing data.
    // TODO: Current implementation assumes that each pole is controlled
    // independently. The interface needs to be changed if the control of one
    // pendulum needs sensing data from other pendulums.
    double calculate_action(double angle, double angleDot, double angleDDot, double pos, double posDot, double posDDot) {
    	double action = controller.output(-angle, 0.01);
	System.out.println("Writting angle: " + angle + " and position " + pos);
    	angleLog.println(angle);
	positionLog.println(pos);
	angleLog.flush();
	positionLog.flush();
        return action;
    }

    /**
     * This method sends the Double message on the object output stream.
     * @throws ioException
     */
    void sendMessage_double(double msg) {
        try {
            out.writeDouble(msg);
            out.flush();
            System.out.println("server>" + msg);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * This method sends the Double message on the object output stream.
     */
    void sendMessage_doubleArray(double[] data) {
        try {
            out.writeObject(data);
            out.flush();
            
            System.out.print("server> ");
            for(int i=0; i< data.length; i++){
                System.out.print(data[i] + "  ");
            }
            System.out.println();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
