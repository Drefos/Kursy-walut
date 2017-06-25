package controller;

import app.Currency;
import app.DataReader;
import com.sun.glass.ui.View;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.StyleableDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static DataReader dataReader;
    private StringProperty currency;
    private double value = 0;
    private String code;

    @FXML
    private TableView<Currency> resultTable;

    @FXML
    private Button uploadButton;

    @FXML
    private Label infoLabel;

    @FXML
    private Label chosenCurrency;

    @FXML
    private TextField result;

    @FXML
    private TextField pln;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setTable();
        setButton();
        setCalc();
        setPlnTextField();




        if (dataReader.date == null) {
            infoLabel.setText("Przy pierwszym uruchomieniu zaktualizuj dane.");
        } else {
            infoLabel.setText("Dane z dnia " + dataReader.date);
        }
    }

    public MainController() {
        dataReader = new DataReader();
        dataReader.read();
        chosenCurrency = new Label();
        currency = new SimpleStringProperty();
        pln = new TextField();
    }

    private boolean isInternetConnection() {
        try {

            InetAddress address = InetAddress.getByName("google.com");
            if (address == null) {
                return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }




    private void search() {
        if (isInternetConnection()) {
            try {
                dataReader.search();
            } catch (IOException e) {
                e.getStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Sprawdź swoje połączenie z internetem w celu aktualizacji danych.");
            alert.setHeaderText("Brak połączenia z internetem!");
            alert.showAndWait();
        }
    }

    public static void stop() {
        dataReader.write();
    }


    private void setTable() {
        TableColumn<Currency, String> countryColumn = new TableColumn<>("Nazwa waluty");
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));

        TableColumn<Currency, String> codeColumn = new TableColumn<>("Kod waluty");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Currency, String> valueColumn = new TableColumn<>("Kurs średni");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        resultTable.getColumns().add(countryColumn);
        resultTable.getColumns().add(codeColumn);
        resultTable.getColumns().add(valueColumn);

        resultTable.setItems(dataReader.get());

        resultTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                code = resultTable.getSelectionModel().getSelectedItem().getCode();
                value = new Double(resultTable.getSelectionModel().getSelectedItem().getValue().replace(',', '.'));
                currency.set(code + "        " + value);
                setResult();
            }
        });
    }

    private void setButton() {
        uploadButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                search();
                infoLabel.setText("Dane z dnia " + dataReader.date);
            }
        });
    }

    private void setCalc() {
        chosenCurrency.textProperty().bind(currency);
        pln.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                setResult();
            }
        });


    }

    private void setPlnTextField() {
        pln.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9]*" + ",*" + "[0-9]*")){
                pln.setText(oldValue);
            }
        });

    }

    private void setResult() {
        int position = pln.getCaretPosition();
        pln.setText(pln.getText().replace(",,", ","));
        if(pln.getText().equals(",")) {
            pln.setText("0,");
            pln.positionCaret(position+1);
        } else {
            if(pln.getText().toCharArray().length > 1) {
                if(pln.getText().charAt(0) == '0' && pln.getText().charAt(1) != ',') {
                    pln.setText(pln.getText(1, pln.getText().toCharArray().length));
                }
            }
            pln.positionCaret(position);
        }
        if(!pln.getText().isEmpty() && pln.getText() != "0" && pln.getText() != "0," && code != null) {
            Double res = new Double(pln.getText().replace(',', '.'));
            result.setText(dataReader.countResult(res, value).toString());
        }
    }

}
