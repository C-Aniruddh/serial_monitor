package io.aniruddh.serialmonitor.main;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import io.aniruddh.serialmonitor.util.RuntimeConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

/**
 * Created by Aniruddh on 12-12-2017.
 */


public class PromptController {

    private int TIME_OUT;
    private int DATA_RATE;
    private int DATA_BITS;
    private int STOP_BITS;
    private int PARITY;

    private static String DATA = "";

    public Button sendButton;

    public TextArea logView;

    public TextField dataToSendField;

    public MenuItem start;
    public MenuItem save;
    public MenuItem quit;

    public InputStream in;
    public OutputStream out;

    public void initialize() throws Exception {
        matchSerialParameters();
        connect(RuntimeConstants.PORT);
    }

    private void matchSerialParameters() {

        TIME_OUT = Integer.valueOf(RuntimeConstants.TIMEOUT) * 1000;
        DATA_RATE = Integer.valueOf(RuntimeConstants.DATA_RATE);

        // Set parity
        switch (RuntimeConstants.PARITY) {
            case "Space":
                PARITY = SerialPort.PARITY_SPACE;
                break;
            case "Odd":
                PARITY = SerialPort.PARITY_ODD;
                break;
            case "Even":
                PARITY = SerialPort.PARITY_EVEN;
                break;
            case "Mark":
                PARITY = SerialPort.PARITY_MARK;
                break;
            default:
                PARITY = SerialPort.PARITY_NONE;
                break;
        }

        // Set Data bits
        switch (RuntimeConstants.DATA_BITS) {
            case "5":
                DATA_BITS = SerialPort.DATABITS_5;
                break;
            case "6":
                DATA_BITS = SerialPort.DATABITS_6;
                break;
            case "7":
                DATA_BITS = SerialPort.DATABITS_7;
                break;
            default:
                DATA_BITS = SerialPort.DATABITS_8;
                break;
        }

        // set stop bits
        switch (RuntimeConstants.STOP_BITS) {
            case "2":
                STOP_BITS = SerialPort.STOPBITS_2;
                break;
            default:
                STOP_BITS = SerialPort.STOPBITS_1;
                break;
        }
    }

     private void connect(String portName) throws Exception {

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        }
        else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), TIME_OUT);
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(DATA_RATE,DATA_BITS,STOP_BITS,PARITY);
                in = serialPort.getInputStream();

                out = serialPort.getOutputStream();


                readData();
                Thread t= new Thread(() -> {
                //the following line will keep this app alive for 1000    seconds,
                //waiting for events to occur and responding to them    (printing incoming messages to console).
                try {Thread.sleep(1000000);} catch (InterruptedException ignored) {}
                });
                t.start();
                System.out.println("Started thread");

            } else {
                System.out.println("Only serial data is supported at this moment.");
            }
        }
    }

    private void readData(){
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int len = -1;
            try {
                while ((len = in.read(buffer)) > -1) {
                    String data = new String(buffer,0,len);
                    System.out.print(data);
                    sb.append(data);
                    String buffer_string = sb.toString();
                    javafx.application.Platform.runLater( () -> logView.setText(buffer_string));
                }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
            PromptController.DATA = sb.toString();
        }).start();
    }

    public void sendAction(ActionEvent actionEvent) {
        writeData();
    }

    private void writeData(){
        try {
            out.write(dataToSendField.getText().getBytes());
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        dataToSendField.clear();
    }

    public void startData(ActionEvent actionEvent) {
        try {
            (new PromptController()).connect(RuntimeConstants.PORT);
        } catch ( Exception e ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could not find any device");
            alert.setHeaderText("Connect a suitable device to continue");
            alert.showAndWait();
        }
    }

    public void saveAction(ActionEvent actionEvent) {
        String save_data = logView.getText();
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(new Stage());

        if(file != null){
            save_file(save_data, file);
        }
    }

    private void save_file(String content, File file){
        try {
            FileWriter fileWriter = null;

            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    public void quitApp(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void enterAction(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)  {
            writeData();
        }
    }

}
