package io.aniruddh.serialmonitor.main;

import gnu.io.CommPortIdentifier;
import io.aniruddh.serialmonitor.util.RuntimeConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Controller {

    public ComboBox com_port;
    public ChoiceBox baud_rate;
    public ChoiceBox parity;
    public ChoiceBox data_bits;
    public ChoiceBox stop_bits;

    public TextField timeoutField;

    public MenuItem quit;
    public MenuItem refresh;

    public Button startButton;
    public Button exitButton;

    @SuppressWarnings("unchecked")
    public void initialize(){
        // set default values
        com_port.setValue("Select port");
        baud_rate.setValue("56000");
        parity.setValue("None");
        data_bits.setValue("8");
        stop_bits.setValue("1");
        setAvailablePorts();

        // Set options
        baud_rate.getItems().addAll("110", "300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "56000", "460800");
        parity.getItems().addAll("None", "Odd", "Even", "Mark", "Space");
        data_bits.getItems().addAll("5", "6", "7", "8");
        stop_bits.getItems().addAll("1", "2");
    }

    @SuppressWarnings("unchecked")
    private void setAvailablePorts(){
        List<String> list = new ArrayList<>();
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements())
        {
            CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
            switch (com.getPortType())
            {
                case CommPortIdentifier.PORT_SERIAL:
                    list.add(com.getName());
            }
        }

        System.out.println(list);
        ObservableList<String> availablePorts = FXCollections.observableArrayList(list);
        // set choices
        com_port.setItems(availablePorts);
    }
    private void setRuntimeValues(){
        RuntimeConstants.PORT = String.valueOf(com_port.getValue());
        RuntimeConstants.DATA_BITS = String.valueOf(data_bits.getValue());
        RuntimeConstants.DATA_RATE = String.valueOf(baud_rate.getValue());
        RuntimeConstants.PARITY = String.valueOf(parity.getValue());
        RuntimeConstants.STOP_BITS = String.valueOf(stop_bits.getValue());
        RuntimeConstants.TIMEOUT = String.valueOf(timeoutField.getText());
    }

    public void startSim(ActionEvent actionEvent) {
        setRuntimeValues();
        if (com_port.getValue().toString().equals("Select port")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Select Port");
            alert.setHeaderText("Select a suitable port to continue");
            alert.showAndWait();
        } else {
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prompt_view.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Serial Monitor");
                stage.setScene(new Scene(root1));
                stage.show();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                System.out.println("Started monitor");
            }
        }
    }

    public void exitApp(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void refreshPorts(ActionEvent actionEvent) {
        setAvailablePorts();
    }
}
